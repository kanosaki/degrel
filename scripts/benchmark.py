from __future__ import print_function
import sys
import os
from datetime import datetime
import tempfile
import json
import shutil

import jinja2

import start
import utils


class Bench(object):
    def __init__(self, bench_list):
        self.bench_list = bench_list
        # Cahce, prevent from getting twice
        self.timestamp = datetime.now()
        self.timestamp_str = self.timestamp.strftime('%Y%m%d-%H%M%S')

    def start(self):
        self.temp_output = utils.app_relative('benchmark', 'working')
        if os.path.exists(self.temp_output):
            shutil.rmtree(self.temp_output)
        os.makedirs(self.temp_output)

        self.entries = list(self.mk_entries())

        for entry in self.entries:
            entry.prepare()

        tasks = [(e, t)
                for e in self.entries
                for t in enumerate(e.tasks)]

        for (index, (entry, (index_of_entry, task))) in enumerate(tasks):
            print("Runing: %s (Try: %d/%d) (All: %d/%d)"
                % (entry.name,
                    index_of_entry, len(entry.tasks),
                    index, len(tasks)),
                file=sys.stderr)
            task.start()

        self.write_param_json()
        if self.temp_output and os.path.exists(self.temp_output):
            shutil.copytree(self.temp_output, self.real_bench_dir())


    def write_param_json(self):
        path = os.path.join(self.bench_dir, 'param.json')
        bench_list = list(self.bench_list)
        for bl in bench_list:
            del bl['noise']

        params = dict(
            bench_list=bench_list,
            timestamp=self.timestamp.isoformat(),
            version=utils.version(),
            version_hash=utils.version_hash()
        )
        json.dump(params, open(path, 'w'))

    @property
    def result_dir(self):
        return utils.app_relative('benchmark', 'results')

    @property
    def template_dir(self):
        return utils.app_relative('benchmark', 'templates')

    def real_bench_dir(self):
            return os.path.join(self.result_dir, self.timestamp_str)

    @property
    def bench_dir(self):
        if self.temp_output:
            return self.temp_output
        else:
            return self.real_bench_dir()

    def mk_entries(self):
        for config in self.bench_list:
            entry_dir = os.path.join(self.bench_dir, config['name'])
            yield BenchEntry(self, config, entry_dir)


class BenchEntry(object):
    def __init__(self, bench, config, entry_dir):
        self.bench = bench
        self.output_dir = os.path.join(entry_dir, 'output')
        self.scripts_dir = os.path.join(entry_dir, 'scripts')
        self.config = config
        self.count = config.get('try_count', 1)

    @property
    def name(self):
        return self.config['name']

    def prepare(self):
        self.gen_scripts()

    def gen_scripts(self):
        template_path = os.path.join(
            self.bench.template_dir,
            self.config['template'])
        gen = Generator(self.config, template_path, self.scripts_dir)
        gen.start()

    def gen_report(self):
        pass

    def _mk_tasks(self):
        for i in range(self.count):
            result_path = os.path.join(
                self.output_dir,
                '%d.json' % i)
            yield BenchRun(self.config, result_path, self.scripts_dir)

    @property
    def tasks(self):
        """Return task objects (BenchRun) for multi tasking in future."""
        try:
            return self._tasks
        except AttributeError:
            self._tasks = list(self._mk_tasks())
            return self._tasks


class BenchRun(object):
    def __init__(self, config, output_path, scripts_dir):
        self.scripts_dir = scripts_dir
        self.output_path = output_path
        self.template_name = config['template']
        self.name = config['name']

    def start(self):
        ret = start.run_degrel('bench',
                               '-o', self.output_path,
                               self.scripts_dir)
        if ret != 0:
            raise RuntimeError("degrel aborted")


class Generator(object):
    def __init__(self,
                 config,
                 template_path,
                 output_dir):
        if not os.path.isdir(output_dir):
            os.makedirs(output_dir)
        template_str = open(template_path).read()
        template = jinja2.Template(template_str)
        self.template = template
        self.count = config.get('noise_range_length', 40)
        self.jvm_warm_count = config.get('warm_up', 10)
        self.output_dir = output_dir
        self.noise_fn = config['noise']

    def gen(self, index, noise):
        return self.template.render(
            index=index,
            count=self.count,
            noise=noise)

    def write_out(self, index, content):
        script_name = str(index)\
            .rjust(len(str(self.count)), '0') + '.dg'
        output_path = os.path.join(self.output_dir, script_name)
        with open(output_path, 'w') as f:
            f.write(content)

    def start(self):
        for i in range(self.jvm_warm_count):
            content = self.gen(i, utils.fix(""))
            self.write_out(i, content)

        for i in range(self.count):
            content = self.gen(i, self.noise_fn)
            self.write_out(i + self.jvm_warm_count, content)
