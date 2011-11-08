#!/usr/bin/env python

'''This simple script connects to an IRIS database and exports a single font in
Imago .ifnt format.'''

from sys import argv, exit
from base64 import b64decode
import pgdb

def read_font(conn, font):
	cursor = conn.cursor()
	cursor.execute("SELECT name, f_number, height, line_spacing, "
		"char_spacing FROM iris.font WHERE name = '%s';" % font)
	name, f_num, height, line_spacing, char_spacing = cursor.fetchone()
	print '[FontInfo]'
	print 'FontName=%s' % name
	print 'FontHeight=%s' % height
	print 'CharSpacing=%s' % char_spacing
	print 'LineSpacing=%s' % line_spacing
	cursor.execute("SELECT max(code_point) FROM iris.glyph WHERE "
		"font = '%s';" % font)
	print 'MaxCharNumber=%s' % cursor.fetchone()[0]
	print
	cursor.execute("SELECT code_point, height, width, pixels "
		"FROM iris.glyph gl JOIN iris.graphic gr "
		"ON gl.graphic = gr.name "
		"WHERE font = '%s' ORDER BY code_point;" % font)
	for row in range(cursor.rowcount):
		cp, h, width, pixels = cursor.fetchone()
		assert h == height
		bmap = b64decode(pixels)
		print '[Char_%s]' % cp
		print "Character='%s'" % chr(cp)
		print_char(height, width, bmap)
		print
	cursor.close()

def print_char(height, width, bmap):
	for row in range(height):
		print_row(row, width, bmap)

def print_row(row, width, bmap):
	print 'row%02d=' % (row + 1),
	for col in range(width):
		if lit_pixel(bmap, width, row, col):
			print 'X',
		else:
			print '.',
	print

def lit_pixel(bmap, width, row, col):
	pos = row * width + col
	b8 = pos // 8
	bit = 7 - pos % 8
	return (ord(bmap[b8]) >> bit) & 1

if len(argv) != 2:
	print "Usage:\n./imago_export.py [font-name]\n"
	exit(1)
connection = pgdb.connect(database='tms')
read_font(connection, argv[1])
connection.commit()
connection.close()
