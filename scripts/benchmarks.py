import argparse
import json
import os
import re
from collections import defaultdict
from datetime import date
from tabulate import tabulate

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.realpath(__file__)))
BENCHMARKS_DIR = os.path.join(PROJECT_ROOT, "methods/build/reports/benchmarks/main")


def main():
    parser = argparse.ArgumentParser(
        description="Display the benchmarks in a readable way"
    )
    parser.add_argument("--today", action="store_true", help="show only benchmarks from today")
    parser.add_argument("--limit", type=int, default=0, help="limit how many runs to show for each benchmark")
    parser.add_argument("--filter", type=str, default="", help="pattern to filter the benchmarks")
    parser.add_argument("--label-last", type=str, default="", help="associate a label with the last benchmark")
    parser.add_argument("--labelled-only", action="store_true", help="only show labelled benchmarks")
    parser.add_argument("--label", type=str, default="", help="associate a label with a benchmark TIMESTAMP=LABEL")

    args = parser.parse_args()

    benchmark_dirs = sorted(os.listdir(BENCHMARKS_DIR))

    if args.label_last and benchmark_dirs:
        label_benchmark(benchmark_dirs[-1], args.label_last)

    if args.label:
        benchmark_dir, label = args.label.split("=")
        label_benchmark(benchmark_dir, label)

    benchmark_data = load_benchmark_data(benchmark_dirs, args)
    display_benchmark_data(benchmark_data, args)


def label_benchmark(benchmark_dir, label):
    with open(os.path.join(BENCHMARKS_DIR, benchmark_dir, "label"), "w") as f:
        f.write(label)


def load_benchmark_data(benchmark_dirs, args):
    data = defaultdict(list)
    check_prefix = str(date.today()) if args.today else ""

    for benchmark_dir in benchmark_dirs:
        if not benchmark_dir.startswith(check_prefix):
            continue
        with open(os.path.join(BENCHMARKS_DIR, benchmark_dir, "benchmarks.json")) as f:
            benchmark_data = json.load(f)
        label = ""
        try:
            with open(os.path.join(BENCHMARKS_DIR, benchmark_dir, "label")) as f:
                label = f.read()
        except IOError:
            pass
        if args.labelled_only and not label:
            continue
        for item in benchmark_data:
            name = item["benchmark"]
            if not re.search(args.filter, name):
                continue
            score = item["primaryMetric"]["score"]
            error = item["primaryMetric"]["scoreError"]
            data[name].append((benchmark_dir, score, error, label))

    return data


def display_benchmark_data(benchmark_data, args):
    for name, runs in benchmark_data.items():
        print()
        print(name)
        if args.limit > 0:
            runs = runs[-args.limit:]
        print(tabulate(runs, headers=["run", "score", "error", "label"]))


if __name__ == "__main__":
    main()
