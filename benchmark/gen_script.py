from __future__ import print_function
# Benchmark script generator

import os
import sys
import jinja2
import itertools

OUTPUT_DIR = 'scripts'

def noise_gen():
    while True:
        yield "foo(a(b, c(d, e)), bar: baz, hoge: fuga, piyo: hogefuga(x, y, z))"


def noise_fn(size=1):
    return "\n".join(itertools.islice(noise_gen(), size))


def noise_empty(*args, **kw):
    return ""


class Generator(object):
    def __init__(self, template_path, count, jvm_warm_count, output_dir):
        if not os.path.isdir(output_dir):
            os.makedirs(output_dir)
        template_str = open(template_path).read()
        template = jinja2.Template(template_str)
        self.template = template
        self.count = count
        self.jvm_warm_count = jvm_warm_count
        self.output_dir = output_dir

    def gen(self, index, noise):
        return self.template.render(
            index=index,
            count=self.count,
            noise=noise)

    def write_out(self, index, content):
        script_name = str(index).rjust(len(str(self.count)), '0') + '.dg'
        output_path = os.path.join(self.output_dir, script_name)
        with open(output_path, 'w') as f:
            f.write(content)

    def start(self):
        for i in range(self.jvm_warm_count):
            content = self.gen(i, noise_empty)
            self.write_out(i, content)

        for i in range(self.jvm_warm_count + self.count):
            content = self.gen(i, noise_fn)
            self.write_out(i, content)


def start(template_file, count, jvm_warmup, output_dir=OUTPUT_DIR):
    generator = Generator(template_file, count, jvm_warmup, output_dir)
    generator.start()


if __name__ == '__main__':
    start('templates/hello2.dg.template', 40, 10)
