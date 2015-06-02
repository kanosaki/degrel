from __future__ import print_function
# Utilities for bin

import os
import sys

BIN_DIR = os.path.dirname(__file__)

APP_DIR = os.path.join(BIN_DIR, '..')


def app_path(*args):
    path = os.path.join(APP_DIR, *args)
    return os.path.relpath(path, start=APP_DIR)


def system(fmt, *args, **kwargs):
    os.chdir(APP_DIR)
    os.system(fmt.format(*args, **kwargs))


def args():
    return " ".join(sys.argv[1:])
