#!/usr/bin/env python3

'''This script takes a font in .tfon format and creates a series of SQL
statements to import the font into IRIS.'''

from sys import argv, exit
from base64 import b64encode

HEADER = r"""\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;
"""
HFONT = """INSERT INTO iris.font (
    name, f_number, height, width, line_spacing, char_spacing
) VALUES ('%s', %s, %s, %s, %s, %s);
"""
COPY = "COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;"
COPY_GLYPH = "%s_%s\t%s\t%s\t%s\t%s"
FOOTER = r"""\.

COMMIT;"""

def parse_kv(key, kv):
    akey, value = kv.split(': ')
    assert akey == key
    return value

def create_font_sql(lines):
    print (HEADER)
    name = parse_kv('font_name', next(lines))
    f_number = int(parse_kv('font_number', next(lines)))
    char_spacing = parse_kv('char_spacing', next(lines))
    line_spacing = parse_kv('line_spacing', next(lines))
    glyphs = list(lines)
    height, width = glyph_height_width(iter(glyphs))
    print (HFONT % (name, f_number, height, width, line_spacing, char_spacing))
    print (COPY)
    lines = iter(glyphs)
    while True:
        try:
            parse_glyph(name, height, width, lines)
        except StopIteration:
            break
    print (FOOTER)

def glyph_height_width(lines):
    height = 0
    width = 1
    assert next(lines) == ''
    while True:
        try:
            code_point = parse_kv('ch', next(lines)).split()[0]
            if code_point == 0:
                break
            h = 0
            while True:
                row = next(lines)
                if row == '':
                    assert height > 0 or h != height
                    height = h
                    break
                if width == 1:
                    width = len(row)
                elif width != len(row):
                    width = 0
                h += 1
        except StopIteration:
            break
    return height, width

def parse_glyph(name, height, width, lines):
    assert next(lines) == ''
    code_point = parse_kv('ch', next(lines)).split()[0]
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
            if b == '@':
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
    print ("Usage:\n./tfon_import.py [file-name]\n")
    exit(1)
create_font_sql(line.strip() for line in open(argv[1]))
