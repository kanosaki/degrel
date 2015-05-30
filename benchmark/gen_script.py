from __future__ import print_function
# Benchmark script generator

import jinja2
import itertools

COUNT = 40
JVM_WARM = 10


def noise_gen(*args, **kwargs):
    while True:
        yield "foo(a(b, c(d, e)), bar: baz, hoge: fuga, piyo: hogefuga(x, y, z))"


def main():
    template_str = open('templates/hello.dg.template').read()
    template = jinja2.Template(template_str)
    for i in range(JVM_WARM):
        script = template.render(noise="")
        with open('scripts/%02d.dg' % i, 'w') as f:
            f.write(script)

    for i in range(COUNT):
        noise = '\n'.join(itertools.islice(noise_gen(), i))
        script = template.render(noise=noise)
        with open("scripts/%02d.dg" % (i + JVM_WARM), 'w') as f:
            f.write(script)

if __name__ == '__main__':
    main()
