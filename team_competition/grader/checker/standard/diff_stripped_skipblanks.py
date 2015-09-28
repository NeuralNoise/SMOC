#! /usr/bin/python
import sys

def normalize(ls):
    return filter(bool, map(str.strip, ls))

f1 = file(sys.argv[2])
f2 = file(sys.argv[3])
l1 = normalize(f1.readlines())
l2 = normalize(f2.readlines())
if len(l1)==len(l2):
    for i in xrange(0,len(l1)):
	if l1[i]!=l2[i]:
	    print "0"
	    print "Wrong answer"
	    print "Wrong line %d" % i
	    sys.exit(0)
else:
    print "0"
    print "Wrong answer"
    print "Wrong number of lines"
    sys.exit(0)

print "10"
print "Accepted"
