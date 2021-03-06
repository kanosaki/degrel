#!/usr/bin/env python
from __future__ import print_function

import sys
import glob
import os
from utils import app_path, run, system, quote
import utils
import platform

CLASSPATH_CACHE = app_path('.sbt-classpath')

MAIN_CLASS = 'degrel.Main'

BUILD_SBT = app_path('build.sbt')

if platform.system() == 'Windows':
    PATH_ITEM_SEP = ';'
    SBT_EXPORT_DEPS_COMMAND = ['sbt', 'export compile:dependency-classpath']
else:
    PATH_ITEM_SEP = ':'
    SBT_EXPORT_DEPS_COMMAND = ['sbt "export compile:dependency-classpath"']


def dependency_classpath():
    """
    Fetch project classpath form sbt, and cache in ".sbt-classpath" file

    if mtime of "build.sbt" is newer than
    """
    if os.path.exists(CLASSPATH_CACHE) and\
            os.path.getmtime(CLASSPATH_CACHE) > os.path.getmtime(BUILD_SBT):
        return open(CLASSPATH_CACHE).read()
    else:
        print('Regenerating classpath....', file=sys.stderr)
        try:
            sbt_result = system(*SBT_EXPORT_DEPS_COMMAND)
            classpath = sbt_result.strip().split('\n')[-1].strip()
        except:
            print('Cannot parse CLASSPATH from sbt', file=sys.stderr)
            sys.exit(-1)
        open(CLASSPATH_CACHE, 'w').write(classpath)
        return classpath


def run_degrel(*args):
    if not args:
        args = utils.args()
    os.chdir(utils.APP_DIR)
    try:
        project_classes = glob.glob(
            app_path('target', 'scala-*', 'classes'))[-1]
    except:
        print('Project classes not found in targets/scala-*/classes',
              file=sys.stderr)
        sys.exit(-1)
    classpath = project_classes + PATH_ITEM_SEP + dependency_classpath()
    return run('java',
        '-cp',
        quote(classpath),
        MAIN_CLASS,
        *args)
