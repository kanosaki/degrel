#!/usr/bin/env python

import sys
import os
import itertools
import json

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

def create_noise_fn(param):
    return NOISE_FUNCTIONS[param['type']]

def main():
    if len(sys.argv) > 1:
        config_path = sys.argv[1]
    else:
        config_path = utils.app_path(DEFAULT_CONFIG_FILE)
    config = json.load(open(config_path))
    bench_configs = config['benchmarks']
    for bc in bench_configs:
        bc['noise'] = create_noise_fn(bc['noise_param'])
    bench = benchmark.Bench(config)
    bench.start()


if __name__ == '__main__':
    main()
