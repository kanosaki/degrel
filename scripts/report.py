
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

    def _create_report_index(self, col, name):
        tasks = [t for t in self.tasks if t.name == name]
        legends = set(t.legend for t in tasks)
        bench_config = self.bench_config(name)
        title = '%s-%s-index' % (name, col)
        plt.clf()
        plt.title(title)
        ymax = 0
        for legend in legends:
            ys_all = np.array([
                y.col(col) for y in tasks if y.legend == legend])
            y = np.average(ys_all, axis=0)  # average each samples
            x = np.arange(len(y))
            plt.xlabel('index')
            plt.ylabel(col)
            plt.plot(x, y, label=legend)
            if ymax < max(y):
                ymax = max(y)
        plt.vlines([bench_config['warm_up']], 0,
                    ymax, linestyles="dashed")
        plt.legend()
        self.savefig(title)

    def _create_report(self, col, name, row):
        tasks = [t for t in self.tasks if t.name == name]
        legends = set(t.legend for t in tasks)
        title = '%s-%s-%s' % (name, col, row)
        plt.clf()
        plt.title(title)
        ymax = 0
        for legend in legends:
            ys_all = np.array([
                y.col(col) for y in tasks if y.legend == legend
            ])
            y = np.average(ys_all, axis=0)  # average each samples
            if row == 'index':
                x = np.arange(len(y))
            else:
                x = [x for x in tasks if x.legend == legend][0].col(row)
            plt.xlabel(row)
            plt.ylabel(col)
            plt.plot(x, y, label=legend)
            if ymax < max(y):
                ymax = max(y)
        if row == 'index':
            bench_config = self.bench_config(name)
            plt.vlines([bench_config['warm_up']], 0,
                        ymax, linestyles="dashed")
        plt.legend()
        self.savefig(title)

    def create_reports(self, y_axises, x_axises=None):
        x_axises = x_axises or ['index']
        for (y_ax, name, x_ax) in itertools.product(
            y_axises,
            self.names,
            x_axises):
            if y_ax != x_ax:
                self._create_report(y_ax, name, x_ax)


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


def process(param_path):
    param = ParamJson(param_path)
    param.create_reports(
        ['rps', 'initialMainSize', 'rewriteTryCount'],
        ['index', 'initialMainSize']
    )


if __name__ == '__main__':
    process(sys.argv[1])
