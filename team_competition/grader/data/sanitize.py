#! /usr/bin/python
"""
Strips lines
Deletes empty lines
"""
import os
import sys
from itertools import imap, ifilter
os.system("chmod 644 *.in *.sol")
if __name__ == '__main__':
    for filename in os.listdir("."):
        if os.path.isdir(filename) or filename.startswith(sys.argv[0]):
            continue
        input = open(filename)
        newfile = open("%s.new" % filename, "w")
        for line in ifilter(
                        lambda x:x!='',
                        imap(str.rstrip, input.xreadlines())
                    ):
            newfile.write('%s\n'%line)
        newfile.close()
        input.close()
        os.system("mv %s.new %s" % (filename, filename))
