#!/usr/bin/env python

'''This simple script connects to an IRIS database and exports a single font in
ADDCO .ifnt format.'''

from sys import argv, exit
from base64 import b64decode
import pgdb

def read_font(conn, font):
	cursor = conn.cursor()
	cursor.execute("SELECT name, f_number, height, width, line_spacing, "
		"char_spacing FROM iris.font WHERE name = '%s';" % font)
	name, f_num, height, width, line_spacing, char_spacing = cursor.fetchone()
	print('name: %s' % name)
	print('height: %s' % height)
	print('width: %s' % width)
	print('char_spacing: %s' % char_spacing)
	print('line_spacing: %s' % line_spacing)
	print()
	cursor.execute("SELECT code_point, width, pixels FROM iris.glyph "
		"WHERE font = '%s' ORDER BY code_point;" % font)
	for row in range(cursor.rowcount):
		cp, width, pixels = cursor.fetchone()
		bmap = b64decode(pixels)
		print('codepoint: %s %s' % (cp, chr(cp)))
		print_char(height, width, bmap)
		print()
	cursor.close()

def print_char(height, width, bmap):
	for row in range(height):
		print_row(row, width, bmap)

def print_row(row, width, bmap):
	for col in range(width):
		if lit_pixel(bmap, width, row, col):
			print('X', end='')
		else:
			print('.', end='')
	print()

def lit_pixel(bmap, width, row, col):
	pos = row * width + col
	b8 = pos // 8
	bit = 7 - pos % 8
	return (bmap[b8] >> bit) & 1

if len(argv) != 2:
	print("Usage:\n%s [font-name]\n" % argv[0])
	exit(1)
connection = pgdb.connect(database='tms')
read_font(connection, argv[1])
connection.commit()
connection.close()
