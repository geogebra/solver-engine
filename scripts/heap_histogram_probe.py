#  Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org

"""
Probe retained JVM heap growth by sending traffic to a running solver API server and comparing
post-GC `jcmd GC.class_histogram` snapshots.

Typical usage:
    python scripts/heap_histogram_probe.py --requests 100 --second-requests 100

The script extracts sample expressions from method tests by default, sends warmup and measured
requests, captures histograms before and after each batch, then prints class and package-group
deltas. Pass `--pid` if more than one JVM is running, and use `--expression-file` to exercise a
specific corpus.
"""

import argparse
import concurrent.futures
import json
import os
import random
import re
import subprocess
import sys
import time
from collections import defaultdict
from datetime import datetime
from pathlib import Path
from urllib import request
from urllib.error import HTTPError, URLError


PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_TEST_ROOT = PROJECT_ROOT / "methods/src/test/kotlin"
DEFAULT_OUTPUT_ROOT = PROJECT_ROOT / "build/heap-histogram-probe"

GROUP_PREFIXES = {
    "engine.": "engine",
    "methods.": "methods",
    "server.": "server",
    "org.antlr.": "antlr",
    "kotlin.reflect.": "kotlin-reflect",
    "kotlin.": "kotlin-other",
    "java.": "java",
    "[": "arrays",
}

EXPRESSION_PATTERNS = [
    re.compile(r"\binputExpr\s*=\s*\"((?:[^\"\\]|\\.)*)\""),
]


def main():
    args = parse_args()
    random.seed(args.seed)

    expressions = load_expressions(args)
    if not expressions:
        fail("No expressions found. Use --expression-file or check --test-root.")

    if args.dry_run:
        info(f"Loaded {len(expressions)} expressions")
        for expr in expressions[: args.print_expressions]:
            print(expr)
        return

    output_dir = make_output_dir(args.output_dir)
    write_lines(output_dir / "expressions.txt", expressions)
    info(f"Loaded {len(expressions)} expressions")
    info(f"Output directory: {output_dir}")

    pid = args.pid or detect_pid(args.jcmd)
    info(f"Using JVM pid: {pid}")

    capture_histogram(args.jcmd, pid, output_dir / "hist_0_baseline.txt")

    if args.warmup_requests > 0:
        info(f"Sending {args.warmup_requests} warmup requests")
        run_requests(args, expressions, args.warmup_requests)
        capture_histogram(args.jcmd, pid, output_dir / "hist_1_after_warmup.txt")
    else:
        copy_file(output_dir / "hist_0_baseline.txt", output_dir / "hist_1_after_warmup.txt")

    info(f"Sending {args.requests} measured requests")
    run_requests(args, expressions, args.requests)
    capture_histogram(args.jcmd, pid, output_dir / "hist_2_after_traffic.txt")

    if args.second_requests > 0:
        info(f"Sending {args.second_requests} second-batch requests")
        run_requests(args, expressions, args.second_requests)
        capture_histogram(args.jcmd, pid, output_dir / "hist_3_after_second_traffic.txt")

    write_report(
        output_dir,
        "warmup_vs_baseline",
        output_dir / "hist_0_baseline.txt",
        output_dir / "hist_1_after_warmup.txt",
        args,
    )
    write_report(
        output_dir,
        "traffic_vs_warmup",
        output_dir / "hist_1_after_warmup.txt",
        output_dir / "hist_2_after_traffic.txt",
        args,
    )
    write_report(
        output_dir,
        "traffic_vs_baseline",
        output_dir / "hist_0_baseline.txt",
        output_dir / "hist_2_after_traffic.txt",
        args,
    )
    if args.second_requests > 0:
        write_report(
            output_dir,
            "second_traffic_vs_first_traffic",
            output_dir / "hist_2_after_traffic.txt",
            output_dir / "hist_3_after_second_traffic.txt",
            args,
        )

    info("Done")


def parse_args():
    parser = argparse.ArgumentParser(
        description="Send solver API traffic and compare JVM heap histograms",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    parser.add_argument("--pid", help="JVM pid. If omitted, the script tries to detect it with jcmd.")
    parser.add_argument("--jcmd", default="jcmd", help="path to jcmd")
    parser.add_argument("--base-url", default="http://localhost:8080/api/v1", help="API base URL")
    parser.add_argument(
        "--endpoint",
        choices=["selectPlans", "graph"],
        default="selectPlans",
        help="API endpoint to exercise",
    )
    parser.add_argument("--format", choices=["solver", "latex", "json2"], default="solver", help="response format")
    parser.add_argument("--test-root", default=str(DEFAULT_TEST_ROOT), help="directory to extract test expressions from")
    parser.add_argument("--expression-file", help="optional newline-separated expression file")
    parser.add_argument("--expression-limit", type=int, default=500, help="maximum unique expressions to use, 0 for all")
    parser.add_argument("--requests", type=int, default=1000, help="measured requests to send")
    parser.add_argument("--second-requests", type=int, default=0, help="optional second measured batch")
    parser.add_argument("--warmup-requests", type=int, default=100, help="warmup requests before measured traffic")
    parser.add_argument("--concurrency", type=int, default=8, help="concurrent request workers")
    parser.add_argument("--timeout", type=float, default=10.0, help="HTTP request timeout in seconds")
    parser.add_argument("--seed", type=int, default=0, help="random seed for expression order")
    parser.add_argument("--output-dir", default=str(DEFAULT_OUTPUT_ROOT), help="directory where run output is created")
    parser.add_argument("--limit", type=int, default=80, help="number of delta rows per report section")
    parser.add_argument(
        "--watch-class",
        action="append",
        default=[],
        help="class to include in a watched-class delta section; can be repeated",
    )
    parser.add_argument("--dry-run", action="store_true", help="only extract and print expressions")
    parser.add_argument("--print-expressions", type=int, default=50, help="expressions to print in --dry-run mode")
    return parser.parse_args()


def load_expressions(args):
    expressions = []
    if args.expression_file:
        expressions.extend(read_expression_file(Path(args.expression_file)))
    else:
        expressions.extend(extract_expressions(Path(args.test_root)))

    expressions = deduplicate(filter_expression(expr) for expr in expressions)
    random.shuffle(expressions)
    if args.expression_limit > 0:
        expressions = expressions[: args.expression_limit]
    return expressions


def read_expression_file(path):
    return [line.strip() for line in path.read_text().splitlines()]


def extract_expressions(root):
    expressions = []
    for path in sorted(root.rglob("*.kt")):
        text = path.read_text(errors="replace")
        for pattern in EXPRESSION_PATTERNS:
            expressions.extend(unescape_kotlin_string(match.group(1)) for match in pattern.finditer(text))
    return expressions


def unescape_kotlin_string(value):
    try:
        return bytes(value, "utf-8").decode("unicode_escape")
    except UnicodeDecodeError:
        return value


def filter_expression(expr):
    expr = expr.strip()
    if not expr:
        return None
    if len(expr) > 500:
        return None
    if "${" in expr or "$" in expr:
        return None
    if expr.startswith("#"):
        return None
    if not any(char.isdigit() or char.isalpha() or char in "[]()+-*/^=<>" for char in expr):
        return None
    return expr


def deduplicate(values):
    seen = set()
    result = []
    for value in values:
        if value is None or value in seen:
            continue
        seen.add(value)
        result.append(value)
    return result


def detect_pid(jcmd):
    completed = subprocess.run([jcmd], text=True, capture_output=True, check=False)
    if completed.returncode != 0:
        fail(f"Could not run {jcmd}: {completed.stderr.strip()}")

    candidates = []
    for line in completed.stdout.splitlines():
        parts = line.split(maxsplit=1)
        if not parts or not parts[0].isdigit():
            continue
        description = parts[1] if len(parts) > 1 else ""
        if "solver-engine" in description or "server.ApplicationKt" in description or "api" in description:
            candidates.append(parts[0])

    if len(candidates) == 1:
        return candidates[0]
    if not candidates:
        fail("Could not detect JVM pid. Pass --pid explicitly.")
    fail(f"Multiple JVM candidates found: {', '.join(candidates)}. Pass --pid explicitly.")


def capture_histogram(jcmd, pid, path):
    info(f"Capturing post-GC histogram: {path.name}")
    subprocess.run([jcmd, str(pid), "GC.run"], check=True)
    time.sleep(0.5)
    completed = subprocess.run([jcmd, str(pid), "GC.class_histogram"], text=True, capture_output=True, check=True)
    path.write_text(completed.stdout)


def run_requests(args, expressions, count):
    stats = defaultdict(int)
    started = time.monotonic()
    payloads = [make_payload(args, random.choice(expressions)) for _ in range(count)]

    with concurrent.futures.ThreadPoolExecutor(max_workers=args.concurrency) as executor:
        futures = [executor.submit(send_request, args, payload) for payload in payloads]
        for index, future in enumerate(concurrent.futures.as_completed(futures), 1):
            status = future.result()
            stats[status] += 1
            if index % max(1, count // 10) == 0:
                info(f"Requests completed: {index}/{count}")

    elapsed = time.monotonic() - started
    summary = ", ".join(f"{status}={total}" for status, total in sorted(stats.items()))
    info(f"Request batch finished in {elapsed:.1f}s ({summary})")


def make_payload(args, expression):
    return {
        "input": expression,
        "format": args.format,
    }


def send_request(args, payload):
    url = f"{args.base_url.rstrip('/')}/{args.endpoint}"
    body = json.dumps(payload).encode("utf-8")
    req = request.Request(url, data=body, headers={"Content-Type": "application/json"}, method="POST")
    try:
        with request.urlopen(req, timeout=args.timeout) as response:
            response.read()
            return str(response.status)
    except HTTPError as error:
        error.read()
        return str(error.code)
    except TimeoutError:
        return "timeout"
    except URLError as error:
        if isinstance(error.reason, TimeoutError):
            return "timeout"
        return "url-error"


def write_report(output_dir, name, base_path, target_path, args):
    base = read_histogram(base_path)
    target = read_histogram(target_path)
    report_path = output_dir / f"delta_{name}.txt"

    sections = [
        ("Top positive deltas", format_top_deltas(base, target, args.limit, positive=True)),
        ("Top negative deltas", format_top_deltas(base, target, args.limit, positive=False)),
        ("Group deltas", format_group_deltas(base, target)),
    ]
    if args.watch_class:
        sections.append(("Watched class deltas", format_watched_class_deltas(base, target, args.watch_class)))

    lines = []
    lines.append(f"{target_path.name} vs {base_path.name}")
    for title, section_lines in sections:
        lines.append("")
        lines.append(f"=== {title} ===")
        lines.extend(section_lines)

    report_path.write_text("\n".join(lines) + "\n")
    print("\n".join(lines))


def read_histogram(path):
    data = {}
    for line in path.read_text(errors="replace").splitlines():
        parts = line.split()
        if len(parts) < 4 or not parts[0].endswith(":"):
            continue
        try:
            instances = int(parts[1])
            bytes_ = int(parts[2])
        except ValueError:
            continue
        data[" ".join(parts[3:])] = (instances, bytes_)
    return data


def format_top_deltas(base, target, limit, positive):
    rows = []
    for cls in set(base) | set(target):
        base_instances, base_bytes = base.get(cls, (0, 0))
        target_instances, target_bytes = target.get(cls, (0, 0))
        delta_instances = target_instances - base_instances
        delta_bytes = target_bytes - base_bytes
        if positive and delta_bytes > 0:
            rows.append((delta_bytes, delta_instances, target_bytes, target_instances, cls))
        elif not positive and delta_bytes < 0:
            rows.append((delta_bytes, delta_instances, target_bytes, target_instances, cls))

    rows = sorted(rows, reverse=positive)[:limit]
    return [format_row(*row) for row in rows] or ["No matching deltas"]


def format_group_deltas(base, target):
    groups = defaultdict(lambda: [0, 0])
    for cls in set(base) | set(target):
        base_instances, base_bytes = base.get(cls, (0, 0))
        target_instances, target_bytes = target.get(cls, (0, 0))
        group = group_for(cls)
        groups[group][0] += target_instances - base_instances
        groups[group][1] += target_bytes - base_bytes

    return [
        f"{delta_bytes:>14} bytes  {delta_instances:>12} instances  {group}"
        for group, (delta_instances, delta_bytes) in sorted(
            groups.items(), key=lambda item: item[1][1], reverse=True
        )
    ]


def format_watched_class_deltas(base, target, watched_classes):
    lines = []
    for cls in watched_classes:
        base_instances, base_bytes = base.get(cls, (0, 0))
        target_instances, target_bytes = target.get(cls, (0, 0))
        lines.append(
            f"{target_bytes - base_bytes:>14} bytes  "
            f"{target_instances - base_instances:>12} instances  "
            f"base={base_bytes:>12}/{base_instances:<8}  "
            f"target={target_bytes:>12}/{target_instances:<8}  "
            f"{cls}"
        )
    return lines


def format_row(delta_bytes, delta_instances, total_bytes, total_instances, cls):
    return (
        f"{delta_bytes:>14} bytes  "
        f"{delta_instances:>12} instances  "
        f"total={total_bytes:>14}  "
        f"count={total_instances:>12}  "
        f"{cls}"
    )


def group_for(cls):
    for prefix, group in GROUP_PREFIXES.items():
        if cls.startswith(prefix):
            return group
    return "other"


def make_output_dir(root):
    output_dir = Path(root) / datetime.now().strftime("%Y%m%d-%H%M%S")
    output_dir.mkdir(parents=True, exist_ok=False)
    return output_dir


def write_lines(path, lines):
    path.write_text("\n".join(lines) + "\n")


def copy_file(source, target):
    target.write_text(source.read_text())


def info(message):
    print(message, file=sys.stderr)


def fail(message):
    print(message, file=sys.stderr)
    sys.exit(1)


if __name__ == "__main__":
    main()
