import argparse
import os
import re
import sqlite3
import subprocess
import sys

from tabulate import tabulate

project_root = os.path.dirname(os.path.dirname(os.path.realpath(__file__)))

prog_description = '''
Utility to put trace logs obtained from running the engine into a sqlite database file, which allows analysis of
the method calls that occurred while running the engine.

In development mode, if the environment variable LOG_LEVEL is set to "TRACE" then a log file with detailed logs
of method calls is created at $PROJECT_ROOT/logs/trace.log.  Running this utility creates a sqlite database file
called trace.log.db in the same directory, which can then be analyzed with SQL queries.

This utility also allows running a set of predefined queries on the resulting database
'''

create_table = '''
-- Each row represents a method call
CREATE TABLE IF NOT EXISTS method_call(
   id INTEGER PRIMARY KEY ASC,                  -- unique identifier for this method call
   trace_id TEXT,                               -- traceId of the log (probably not very useful)
   method TEXT,                                 -- the name of the method
   input TEXT,                                  -- the input expression of the method
   outcome TEXT,                                -- the result of the method, or "FAIL" if it failed
   time  INTEGER,                               -- time spent inside the method (ns)
   parent_id INTEGER REFERENCES method_call(id) -- Id of parent call (i.e. the call inside which this call occurs)
)
'''

create_data_view = '''
CREATE VIEW IF NOT EXISTS augmented_method_call AS
SELECT 
    p.*, -- all the fields from the row table
    p.time - coalesce(SUM(c.time), 0) as own_time -- time spent inside the method but not in a child method
FROM method_call p LEFT OUTER JOIN method_call c ON p.id = c.parent_id GROUP BY p.id, p.time
'''

create_method_summary_view = '''
-- Timing data grouped by method name
CREATE VIEW IF NOT EXISTS method_summary AS
SELECT method, SUM(time) AS total_time, SUM(own_time) AS total_own_time, COUNT(id) AS call_count
FROM augmented_method_call
GROUP by method
'''


def main():
    class Formatter(argparse.RawDescriptionHelpFormatter, argparse.ArgumentDefaultsHelpFormatter):
        pass

    parser = argparse.ArgumentParser(
        description=prog_description,
        formatter_class=Formatter,
    )
    parser.add_argument(
        "--logfile",
        type=str,
        default=os.path.join(project_root, "logs/trace.log"),
        help='path to log file to examine',
    )
    parser.add_argument(
        "--reload",
        action='store_true',
        help='force reloading of log file'
    )
    parser.set_defaults(cmd_func=cmd_default)
    subparsers = parser.add_subparsers(help='sub-command help')

    shell_parser = subparsers.add_parser(
        "shell",
        help="start sqlite3 shell connected to the generated database",
        formatter_class=Formatter,
    )
    shell_parser.set_defaults(cmd_func=cmd_shell)

    method_summary_parser = subparsers.add_parser(
        "method_summary",
        help='show stats by method',
        formatter_class=Formatter,
    )
    method_summary_parser.add_argument(
        "--limit", "-l",
        type=int,
        default=20,
        help='maximum number of rows to display'
    )
    method_summary_parser.add_argument(
        "--order-by", "-o",
        choices=['total_time', 'total_own_time', 'call_count'],
        default='call_count',
        help='column to order by (in descending order)'
    )
    method_summary_parser.set_defaults(cmd_func=cmd_method_summary)

    args = parser.parse_args()

    conn = setup_db(args)
    args.cmd_func(conn, args)


def setup_db(args):
    """Create a database file from the log file if necessary and return a connection to it"""
    logfilename = os.path.join(os.getcwd(), args.logfile)
    info(f"log file: {logfilename}")
    if not os.path.exists(logfilename):
        error("log file not found")

    dbfilename = logfilename + ".db"
    info(f"db file: {dbfilename}")
    removed = remove_old_dbfile(logfilename, dbfilename, force=args.reload)
    conn = create_dbfile(dbfilename)
    if removed:
        with open(logfilename, 'r') as logfile:
            populate_db(logfile, conn)
    return conn


def cmd_default(conn, args):
    """By default, do nothing other than set up the database"""
    pass


def cmd_shell(conn, args):
    """Start a sqlite3 shell connected to the populated database"""
    conn.close()
    try:
        subprocess.run(["sqlite3", "--header", "--column", args.logfile + ".db"])
    except FileNotFoundError:
        error("unable to run sqlite3 - make sure it is installed and on the PATH")


def cmd_method_summary(conn, args):
    execute_view(conn, "method_summary", args.order_by, args.limit)


#
# Utility functions for creating and managing the database
#

def remove_old_dbfile(logfilename, dbfilename, force=False):
    """Remove any existing db file if necessary, or if force is set to True"""
    try:
        db_stat = os.stat(dbfilename)
    except FileNotFoundError:
        return True

    if force:
        info("removing existing db file")
        os.remove(dbfilename)
        return True

    log_stat = os.stat(logfilename)

    if log_stat.st_mtime_ns > db_stat.st_mtime_ns:
        info(f"removing obsolete db file")
        os.remove(dbfilename)
        return True
    return False


def create_dbfile(dbfilename):
    """Create a db file with the required schema and return a connection to the db"""
    conn = sqlite3.connect(dbfilename)
    conn.execute(create_table)
    conn.execute(create_data_view)
    conn.execute(create_method_summary_view)
    return conn


logline_format = re.compile(r'^\{traceId=([^}]*)\}[. ]*(->|<-) (\d+ )?([^:]+): (.*)')


def populate_db(logfile, conn):
    """Populate the database with data from logfile"""
    info(f"populating db file")
    cur = conn.cursor()
    parent_ids = [None]
    record_count = 0
    for logline in logfile:
        match = logline_format.match(logline)
        if match is not None:
            trace_id, dir, duration, method, value = match.groups()
            if dir == '->':
                cur.execute('INSERT INTO method_call (trace_id, method, input, parent_id) VALUES (?, ?, ?, ?)',
                            (trace_id, method, value, parent_ids[-1]))
                parent_ids.append(cur.lastrowid)
                record_count += 1
            else:
                id = parent_ids.pop()
                cur.execute('UPDATE method_call SET time = ?, outcome = ? WHERE id = ?',
                            (duration and int(duration[:-1]), value, id))

    info(f"{record_count} records created")
    conn.commit()


def execute_view(conn, view_name, order_by_column, limit):
    cur = conn.execute(f"SELECT * FROM {view_name} ORDER BY {order_by_column} DESC LIMIT {limit}")
    column_names = [d[0] for d in cur.description]
    rows = cur.fetchall()
    print()
    print(tabulate(rows, headers=column_names))


#
# Printing to the terminal with fancy colors
#

CEND = '\33[0m'
CRED = '\33[31m'
CGREEN = '\33[32m'
CYELLOW = '\33[33m'


def printcolor(f, color, msg):
    if f.isatty():
        print(color + msg + CEND, file=f)
    else:
        print(msg, file=f)


def info(msg):
    printcolor(sys.stderr, CGREEN, "INFO: " + msg)


def error(msg):
    printcolor(sys.stderr, CRED, "ERROR: " + msg)
    sys.exit(1)


if __name__ == '__main__':
    main()
