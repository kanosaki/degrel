#!/usr/bin/env python

import sys
import os
import itertools

sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'scripts'))

import benchmark


def noise_gen():
    while True:
        yield "foo(a(b, c(d, e)), bar: baz, hoge: fuga, piyo: hogefuga(x, y, z))"


def noise_fn(size=1):
    return "\n".join(itertools.islice(noise_gen(), size))


BENCH_LIST = [
    dict(name='hello',
         template='hello.dg.template',
         reverse=True,
         try_count=1,
         noise_range_length=40,
         warm_up=10,
         noise=noise_fn),

    dict(name='hello_complex',
         template='hello2.dg.template',
         reverse=True,
         try_count=1,
         noise_range_length=40,
         warm_up=10,
         noise=noise_fn)
]

bench = benchmark.Bench(BENCH_LIST)
bench.start()
