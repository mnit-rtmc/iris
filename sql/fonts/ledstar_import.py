#!/usr/bin/env python

'''This script takes a font in Ledstar .lf1 format and creates a series of SQL
statements to import the font into IRIS.'''

from sys import argv, exit
from base64 import b64encode
from binascii import unhexlify

HEADER = """\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;
"""
HFONT = """INSERT INTO iris.font (
    name, f_number, height, width, line_spacing, char_spacing, version_id
) VALUES ('%s', %s, %s, %s, %s, %s, %s);
"""
COPY = "COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;"
COPY_GLYPH = "%s_%s\t%s\t%s\t%s\t%s"
FOOTER = """\.

COMMIT;"""

def create_font_sql(lines):
    print (HEADER)
    f_num = next(lines)
    name = next(lines)
    version_id = next(lines)
    height = next(lines)
    char_spacing = next(lines)
    line_spacing = next(lines)
    print (HFONT % (name, f_num, height, 0, line_spacing, char_spacing,
        version_id))
    print (COPY)
    while True:
        try:
            c_point = next(lines)
            c_width = next(lines)
            c_data = next(lines)
            print (COPY_GLYPH % (name, c_point, name, c_point, c_width,
                b64encode(unhexlify(c_data)).decode('ASCII')))
        except StopIteration:
            break
    print (FOOTER)

if len(argv) != 2:
    print ("Usage:\n./ledstar_import.py [file-name]\n")
    exit(1)
create_font_sql(line.strip() for line in open(argv[1]))
