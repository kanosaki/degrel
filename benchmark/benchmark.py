
import os
import json

from matplotlib import pyplot as plt
import numpy as np

RESULT_FILE = "result.json"
result = None
JVM_WARM = 5

def run_benchmark():
    os.system("../run bench -o result.json scripts")


def load():
    return Result(RESULT_FILE)


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
        plt.plot(self.x, y)

    def plot_rps(self):
        plt.clf()
        y = self.rps
        plt.title("Rewrite per second")
        plt.vlines([JVM_WARM], 0, max(y), linestyles="dashed")
        plt.plot(self.x, y)

if __name__ == '__main__':
    result = load()
    result.plot_rps()
    plt.savefig("rps.eps")
    result.plot_size()
    plt.savefig("size.eps")
