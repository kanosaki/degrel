from __future__ import print_function
# Utilities for bin

import os
import sys
import subprocess

BIN_DIR = os.path.dirname(__file__)

APP_DIR = os.path.abspath(os.path.join(BIN_DIR, '..'))


def app_path(*args):
    path = os.path.join(APP_DIR, *args)
    return os.path.relpath(path, start=APP_DIR)


def app_relative(*args):
    path = app_path(*args)
    return os.path.relpath(path)



def system(*cmd):
    os.chdir(APP_DIR)
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, shell=True)
    out, err = proc.communicate()
    if proc.returncode != 0:
        raise RuntimeError("Command failed")
    return out


def run(*args):
    os.chdir(APP_DIR)
    return os.system(' '.join(args))


def args():
    return sys.argv[1:]


def args_str():
    return ' '.join(map(quote, sys.argv[1:]))


def quote(s):
    if s:
        return '"%s"' % s
    else:
        return ""


def version_hash():
    return system('git rev-parse HEAD').strip()


def version():
    return system('git describe --tag').strip()


def fix(x):
    def fixed_fn(*args, **kw):
        return x
    return fixed_fn
