#!/usr/bin/env python3

'''This script takes a font in .ifnt format and creates a series of SQL
statements to import the font into IRIS.'''

from sys import argv, exit
from base64 import b64encode

HEADER = """\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;
"""
HFONT = """INSERT INTO iris.font (
    name, f_number, height, width, line_spacing, char_spacing, version_id
) VALUES ('%s', %s, %s, %s, %s, %s, 0);
"""
COPY = "COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;"
COPY_GLYPH = "%s_%s\t%s\t%s\t%s\t%s"
FOOTER = """\.

COMMIT;"""

def parse_kv(key, kv):
    akey, value = kv.split(': ')
    assert akey == key
    return value

def create_font_sql(lines):
    print (HEADER)
    name = parse_kv('name', next(lines))
    f_number = int(parse_kv('font_number', next(lines)))
    height = int(parse_kv('height', next(lines)))
    width = int(parse_kv('width', next(lines)))
    char_spacing = parse_kv('char_spacing', next(lines))
    line_spacing = parse_kv('line_spacing', next(lines))
    print (HFONT % (name, f_number, height, width, line_spacing, char_spacing))
    print (COPY)
    while True:
        try:
            parse_glyph(name, height, width, lines)
        except StopIteration:
            break
    print (FOOTER)

def parse_glyph(name, height, width, lines):
    assert next(lines) == ''
    code_point = parse_kv('codepoint', next(lines)).split()[0]
    pixels = bytearray()
    bit_mask = 0b10000000
    bits = 0
    for row in range(height):
        row = next(lines)
        if width == 0:
            width = len(row)
        else:
            assert width == len(row)
        for b in row:
            if b == 'X':
                bits |= bit_mask
            bit_mask >>= 1
            if bit_mask == 0:
                pixels.append(bits)
                bit_mask = 0b10000000
                bits = 0
    if bit_mask < 0b10000000:
        pixels.append(bits)
    print (COPY_GLYPH % (name, code_point, name, code_point, width,
        b64encode(pixels).decode('ASCII')))

if len(argv) != 2:
    print ("Usage:\n./ifnt_import.py [file-name]\n")
    exit(1)
create_font_sql(line.strip() for line in open(argv[1]))
