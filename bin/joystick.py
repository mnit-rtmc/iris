#!/bin/env python

from sys import stdout
from struct import unpack

joystick = open('/dev/input/js0')
while True:
	v = unpack('ihBB', joystick.read(8))
	if v[2] & 0x01:
		print 'button:%d,value:%d' % (v[3], v[1])
	if v[2] & 0x02:
		value = v[1] / 32767.0
		print 'axis:%d,value:%f' % (v[3], value)
	stdout.flush()
