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


def basic_noise_gen():
    while True:
        yield "foo(a(b, c(d, e)), bar: baz, hoge: fuga, piyo: hogefuga(x, y, z))"


def basic_noise(size=1):
    return "\n".join(itertools.islice(basic_noise_gen(), size))

NOISE_FUNCTIONS = {
    "basic": basic_noise
}

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
    parser.add_argument('config_file', nargs='?')
    return parser.parse_args()


def create_noise_fn(param):
    return NOISE_FUNCTIONS[param['type']]

def main():
    args = parse_arguments()
    config_path = args.config_file or utils.app_path(DEFAULT_CONFIG_FILE)
    config = json.load(open(config_path))
    bench_configs = config['benchmarks']
    for bc in bench_configs:
        bc['noise'] = create_noise_fn(bc['noise_param'])
    bench = benchmark.Bench(config, args)
    bench.start()


if __name__ == '__main__':
    main()
