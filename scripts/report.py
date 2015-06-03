
import os
import json

from matplotlib import pyplot as plt
import numpy as np

RESULT_FILE = "result.json"
result = None
JVM_WARM = 10


class Result(object):
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


if __name__ == '__main__':
    result = Result(RESULT_FILE)
    result.plot_rps()
    plt.savefig("rps.eps")
    result.plot_size()
    plt.savefig("size.eps")
