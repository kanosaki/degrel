from __future__ import print_function
import sys
import os
from datetime import datetime
import json
import shutil
import itertools

import jinja2

import start
import utils


class Bench(object):
    def __init__(self, config):
        self.config = config
        # Cahce, prevent from getting twice
        self.timestamp = datetime.now()
        self.timestamp_str = self.timestamp.strftime('%Y%m%d-%H%M%S')

    @property
    def bench_list(self):
        return self.config['benchmarks']

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
            print("Runing: %s (Try: %d/%d) (All: %d/%d)" %
                  (entry.name,
                   index_of_entry, len(entry.tasks),
                   index, len(tasks)),
                  file=sys.stderr)
            task.start()

        self.write_param_json(tasks)
        if self.temp_output and os.path.exists(self.temp_output):
            shutil.copytree(self.temp_output, self.real_bench_dir())

    def write_param_json(self, tasks):
        path = os.path.join(self.bench_dir, 'param.json')
        bench_list = list(self.bench_list)
        for bl in bench_list:
            del bl['noise']
        task_entries = []
        for (index, (entry, (index_of_entry, task))) in enumerate(tasks):
            task_entries.append(task.export_dict(self.bench_dir))

        params = dict(
            bench_list=bench_list,
            options=self.config['options'],
            timestamp=self.timestamp.isoformat(),
            tasks=task_entries,
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
        params = self.config['options']
        for config in self.bench_list:
            entry_dir = os.path.join(self.bench_dir, config['name'])
            yield BenchEntry(self, config, params, entry_dir)


class BenchEntry(object):
    def __init__(self, bench, config, params, entry_dir):
        self.bench = bench
        self.params = params
        self.output_dir = os.path.join(entry_dir, 'output')
        self.scripts_dir = os.path.join(entry_dir, 'scripts')
        self.config = config
        self.count = config['try_count']

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
        params = list(self.mk_param_product(self.params))
        params_padsize = len(str(len(params)))
        count_padsize = len(str(self.count))
        for param_index, param in enumerate(params):
            for count_index in range(self.count):
                count_index_str = str(count_index).rjust(count_padsize, '0')
                param_index_str = str(param_index).rjust(params_padsize, '0')
                result_path = os.path.join(
                    self.output_dir,
                    '%s-%s.json' % (param_index_str, count_index_str))
                yield BenchRun(
                    param_index,
                    count_index,
                    self.config,
                    param,
                    result_path,
                    self.scripts_dir)

    @property
    def tasks(self):
        """Return task objects (BenchRun) for multi tasking in future."""
        try:
            return self._tasks
        except AttributeError:
            self._tasks = list(self._mk_tasks())
            return self._tasks

    def mk_param_product(self, params):
        keys = params.keys()

        def zipWithKey(values):
            return zip(keys, values)

        return map(zipWithKey, itertools.product(*params.values()))


class BenchRun(object):
    def __init__(self,
                 param_id,
                 sample_id,
                 config,
                 params,
                 output_path,
                 scripts_dir):
        """
        params :: [('param1': 1), ('param2': 'abc'), ...]
        """
        self.param_id = param_id
        self.sample_id = sample_id
        self.params = params
        self.scripts_dir = scripts_dir
        self.output_path = output_path
        self.template_name = config['template']
        self.name = config['name']

    def export_dict(self, bench_dir):
        return {
            'name': self.name,
            'options': dict(self.params),
            'param_id': self.param_id,
            'sample_id': self.sample_id,
            'output': os.path.relpath(self.output_path, bench_dir),
        }

    def format_params(self):
        def join_pair(kv):
            return '%s=%s' % tuple(kv)
        return ','.join(map(join_pair, self.params))

    def start(self):
        ret = start.run_degrel('bench',
                               '--options', self.format_params(),
                               '--report', self.output_path,
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
        self.config = config
        template_str = open(template_path).read()
        template = jinja2.Template(template_str)
        self.template = template
        self.count = config['noise_range_length']
        self.jvm_warm_count = config['warm_up']
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
