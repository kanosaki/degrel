
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

    def _create_report(self, col, name, row, legends=None):
        tasks = [t for t in self.tasks if t.name == name]
        if not legends:
            legends = set(t.legend for t in tasks)
        title = '%s-%s-%s' % (name, col, row)
        plt.clf()
        plt.title(title)
        ymax = 0
        bench_config = self.bench_config(name)
        warm_up = bench_config['warm_up']
        for legend in legends:
            ys_all = np.array([
                y.col(col) for y in tasks if y.legend == legend
            ])
            y = np.average(ys_all, axis=0)  # average each samples
            # build x data
            if row == 'index':
                x = np.arange(len(y))
            else:
                x = [x for x in tasks if x.legend == legend][0].col(row)
            # cut warm_up data
            if row != 'index':
                x = x[warm_up:]
                y = y[warm_up:]
            plt.xlabel(row)
            plt.ylabel(col)
            plt.plot(x, y, label=legend)
            if ymax < max(y):
                ymax = max(y)
        if row == 'index':
            plt.vlines([warm_up], 0,
                        ymax, linestyles="dashed")
        plt.legend()
        self.savefig(title)

    def create_reports(self, x_axises, y_axises, legends):
        x_axises = x_axises or ['index']
        for (y_ax, name, x_ax) in itertools.product(
            y_axises,
            self.names,
            x_axises):
            if y_ax != x_ax:
                self._create_report(y_ax, name, x_ax, legends)


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

    #def col(self, colname):
    #    return [r[colname] for r in self.reports]

    def col(self, query_path):
        def accessor(query, item):
            if len(query) > 1:
                return accessor(query[1:], item[query[0]])
            else:
                return item[query[0]]
        return [accessor(query_path.split("."), r) for r in self.reports]



def process(param_path, xlabels=None, ylabels=None):
    xlabels = xlabels or ['index', 'initialMainSize']
    ylabels = ['rps',
               'initialMainSize',
               'spans.rewrite.accNanoTime',
               'spans.match.accNanoTime',
               'spans.build.accNanoTime',
               'spans.rewrite.callCount']
    #legends = ['plain']
    legends = None # all
    param = ParamJson(param_path)
    param.create_reports(xlabels, ylabels, legends)


if __name__ == '__main__':
    process(sys.argv[1])
