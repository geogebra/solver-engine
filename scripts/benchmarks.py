import argparse
import json
import os
import re
from collections import defaultdict
from datetime import date
from tabulate import tabulate

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.realpath(__file__)))

BENCHMARKS_DIR = os.path.join(PROJECT_ROOT, "methods/build/reports/benchmarks/main")

parser = argparse.ArgumentParser(
    description="Show the benchmarks in a readable way"
)
parser.add_argument("--today", action="store_true", help="show only benchmarks from today")
parser.add_argument("--limit", type=int, default=0, help="limit how many runs to show for each benchmark")
parser.add_argument("--filter", type=str, default="", help="pattern to filter the benchmarks")
args = parser.parse_args()

data = defaultdict(list)

benchmark_dirs = sorted(os.listdir(BENCHMARKS_DIR))

check_prefix = str(date.today()) if args.today else ""

for benchmark_dir in benchmark_dirs:
    if not benchmark_dir.startswith(check_prefix):
        continue
    with open(os.path.join(BENCHMARKS_DIR, benchmark_dir, "benchmarks.json")) as f:
        benchmark_data = json.load(f)
    for item in benchmark_data:
        name = item["benchmark"]
        if not re.search(args.filter, name):
            continue
        score = item["primaryMetric"]["score"]
        error = item["primaryMetric"]["scoreError"]
        data[name].append((benchmark_dir, score, error))

for name, runs in data.items():
    print()
    print(name)
    if args.limit > 0:
        runs = runs[-args.limit:]
    print(tabulate(runs, headers=["run", "score", "error"]))
