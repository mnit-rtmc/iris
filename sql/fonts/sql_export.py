#!/usr/bin/env python

'''This simple script connects to an IRIS database and exports a single font as
a series of SQL statements.'''

from sys import argv, exit
import pgdb

_HEADER = """\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
"""
_SELECT_FONT = """SELECT name, f_number, height, width, line_spacing,
	char_spacing, version_id FROM iris.font WHERE name = '%s';"""
_INSERT_FONT = """INSERT INTO iris.font
    (name, f_number, height, width, line_spacing, char_spacing, version_id)
    VALUES ('%s', %s, %s, %s, %s, %s, %s);
"""
_SELECT_GLYPH = """SELECT name, font, code_point, graphic
	FROM iris.glyph WHERE font = '%s' ORDER BY code_point;"""
_SELECT_GRAPHIC = """SELECT name, bpp, height, width, pixels
	FROM iris.graphic WHERE name = '%s';"""
_INSERT_GRAPHIC = """INSERT INTO iris.graphic (name, bpp, height, width, pixels)
    VALUES ('%s', %d, %d, %d, '%s');"""
_INSERT_GLYPH = """INSERT INTO iris.glyph (name, font, code_point, graphic)
    VALUES ('%s', '%s', %d, '%s');"""

def read_font(conn, name):
	print _HEADER
	cursor = conn.cursor()
	cursor.execute(_SELECT_FONT % name)
	for row in range(cursor.rowcount):
		print _INSERT_FONT % tuple(cursor.fetchone())
	cursor.close()

def read_glyphs(conn, name):
	glyphs = []
	cursor = conn.cursor()
	cursor.execute(_SELECT_GLYPH % name)
	for row in range(cursor.rowcount):
		glyphs.append(tuple(cursor.fetchone()))
	cursor.close()
	cursor = conn.cursor()
	for glyph in glyphs:
		cursor.execute(_SELECT_GRAPHIC % glyph[3])
		print _INSERT_GRAPHIC % tuple(cursor.fetchone())
		print _INSERT_GLYPH % glyph
	cursor.close()

if len(argv) != 2:
	print "Usage:\n./sql_export.py [font-name]\n"
	exit(1)
name = argv[1]
connection = pgdb.connect(database='tms')
read_font(connection, name)
read_glyphs(connection, name)
connection.commit()
connection.close()
