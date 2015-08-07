
import sys
import os
import json

from matplotlib import pyplot as plt
import itertools
import numpy as np

JVM_WARM = 10

class ParamJson(object):
    def __init__(self, fpath):
        self.path = fpath
        self.obj = json.load(open(fpath))
        self.basedir = os.path.dirname(self.path)
        self.tasks = [
            BenchTask(t, self.basedir) for t in self.obj['tasks']
        ]
        self.graphdir = os.path.join(self.basedir, 'graphs')
        if not os.path.exists(self.graphdir):
            os.makedirs(self.graphdir)

    def savefig(self, title):
        path = os.path.join(self.graphdir, title + '.eps')
        plt.savefig(path)

    @property
    def names(self):
        return set(t.name for t in self.tasks)

    def bench_config(self, name):
        return [bc for bc in self.obj['bench_list'] if bc['name'] == name][0]

    def create_report(self, columns):
        for (col, name) in itertools.product(columns, self.names):
            tasks = [t for t in self.tasks if t.name == name]
            legends = set(t.legend for t in tasks)
            bench_config = self.bench_config(name)
            title = '%s-%s' % (col, name)
            plt.clf()
            plt.title(title)
            ymax = 0
            for legend in legends:
                ys_all = np.array([
                    y.col(col) for y in tasks if y.legend == legend])
                y = np.average(ys_all, axis=0)  # average each samples
                x = np.arange(len(y))
                plt.plot(x, y, label=legend)
                if ymax < max(y):
                    ymax = max(y)
            plt.vlines([bench_config['warm_up']], 0,
                       ymax, linestyles="dashed")
            plt.legend()
            self.savefig(title)


class BenchTask(object):
    def __init__(self, task, basedir):
        self.task = task
        self.basedir = basedir
        self.path = os.path.join(self.basedir, task['output'])
        self.obj = json.load(open(self.path))
        self.reports = self.obj['reports']

    @property
    def legend(self):
        return self.task['options']['rewriteeset']

    @property
    def name(self):
        return self.task['name']

    def __len__(self):
        return len(self.reports)

    def col(self, colname):
        return [r[colname] for r in self.reports]


class ResultJson(object):
    def __init__(self, fpath):
        self.result = json.load(open(fpath))
        self.reports = self.result['reports']
        self.rps = np.array([i['rps'] for i in self.reports])
        self.initial_sizes = np.array([i['initialMainSize'] for i in self.reports])
        self.x = np.arange(0, len(self.reports), 1)

    def plot_size(self):
        plt.clf()
        y = self.initial_sizes
        plt.title("Graph size")
        plt.vlines([JVM_WARM], 0, max(y), linestyles="dashed")
        plt.plot(self.x, y, label='size')

    def plot_rps(self):
        plt.clf()
        y = self.rps
        plt.title("Rewrite per second")
        plt.vlines([JVM_WARM], 0, max(y), linestyles="dashed")
        plt.plot(self.x, y, label='rps')

def process(param_path):
    param = ParamJson(param_path)
    param.create_report(['rps', 'initialMainSize'])


if __name__ == '__main__':
    process(sys.argv[1])
