#!/usr/bin/env python
from __future__ import print_function

from utils import app_path, system
import utils

START_SCRIPT = app_path('target', 'start')

system('{script} {args}', script=START_SCRIPT, args=utils.args())
