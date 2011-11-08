#!/usr/bin/env python

'''This simple script takes a font in Ledstar .lf1 format and creates a series
of SQL statements to import the font into IRIS.'''

from sys import argv, exit
from base64 import b64encode
from binascii import unhexlify

HEADER = """\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
"""
HFONT = """INSERT INTO iris.font (name, f_number, height, width, line_spacing,
	char_spacing, version_id) VALUES ('%s', %s, %s, %s, %s, %s, %s);
"""
GRAPHIC = """INSERT INTO iris.graphic (name, bpp, height, width, pixels)
	VALUES ('%s_%s', 1, %s, %s, '%s');"""
GLYPH = """INSERT INTO iris.glyph (name, font, code_point, graphic)
	VALUES ('%s_%s', '%s', %s, '%s_%s');"""

def create_font_sql(lines):
	print HEADER
	f_num = lines.next()
	name = lines.next()[:16]
	version_id = lines.next()
	line_height = lines.next()
	char_spacing = lines.next()
	line_spacing = lines.next()
	print HFONT % (name, f_num, line_height, 0, line_spacing, char_spacing,
		version_id)
	while True:
		try:
			c_point = lines.next()
			c_width = lines.next()
			c_data = lines.next()
			print GRAPHIC % (name, c_point, line_height, c_width,
				b64encode(unhexlify(c_data)))
			print GLYPH % (name, c_point, name, c_point, name,
				c_point)
		except StopIteration:
			break

if len(argv) != 2:
	print "Usage:\n./ledstar_import.py [file-name]\n"
	exit(1)
create_font_sql(line.strip() for line in open(argv[1]))
