#!/usr/bin/env python

import sys
import os
import itertools
import json
import argparse

sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'scripts'))

import benchmark
import utils

DEFAULT_CONFIG_FILE = "benchmark/config.json"


def parse_arguments():
    parser = argparse.ArgumentParser(
        description='degrel benchmark tool'
    )
    parser.add_argument('-P', '--prepare-only',
                        action='store_true',
                        default=False)
    parser.add_argument('-R', '--no-report',
                        action='store_true',
                        default=False)
    parser.add_argument('-A', '--no-archive',
                        action='store_true',
                        default=False)
    parser.add_argument('config_file', nargs='?')
    return parser.parse_args()


def main():
    args = parse_arguments()
    config_path = args.config_file or utils.app_path(DEFAULT_CONFIG_FILE)
    config = json.load(open(config_path))
    bench_configs = config['benchmarks']
    bench = benchmark.Bench(config, args)
    bench.start()


if __name__ == '__main__':
    main()
