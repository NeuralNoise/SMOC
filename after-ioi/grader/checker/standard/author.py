#! /usr/bin/python
import sys
import re

try:
    out = file(sys.argv[2]).readlines()
    float(out[-2])
    assert re.match('\D+', out[-1])
except:
    print("0")
    print("0 - Author judgement empty")
else:
    sys.stdout.writelines('\n'.join(out[-2:]+['']))
