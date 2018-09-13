#!/usr/bin/python3

'''This simple script imports a font in BDF format and creates a series
of SQL statements to import the font into IRIS.

BDF fonts can be created by converting from OTF with otf2bdf:

otf2bdf -l "32_95" -p 27 /usr/share/fonts/overpass/overpass-regular.otf
otf2bdf -l "32_95" -p 26 /usr/share/fonts/urw-base35/NimbusSansNarrow-Regular.otf
'''

from sys import argv, exit
from base64 import b64encode
from binascii import unhexlify

HEADER = """\set ON_ERROR_STOP
SET SESSION AUTHORIZATION 'tms';
BEGIN;
"""
HFONT = """INSERT INTO iris.font (name, f_number, height, width, line_spacing,
    char_spacing, version_id) VALUES ('%s', %s, %s, %s, %s, %s, %s);
"""
COPY = "COPY iris.glyph (name, font, code_point, width, pixels) FROM stdin;"
COPY_GLYPH = "%s_%s\t%s\t%s\t%s\t%s"
FOOTER = """\.

COMMIT;"""

def set_pixel(bmap, width, row, col):
    if col < width:
        pos = row * width + col
        b8 = pos // 8
        bit = 7 - pos % 8
        b = bmap[b8] | (1 << bit)
        bmap[b8] = b

def create_glyph_sql(lines, fname, height, char_spacing):
    _, c_point = next(lines).split(' ')
    _, swidth, _ = next(lines).split(' ')
    _, dwidth, _ = next(lines).split(' ')
    _, w, h, xd, yd = next(lines).split(' ')
    if 'BITMAP' != next(lines):
        return
    width = max(int(dwidth), int(w))
    n = (width * height + 7) // 8
    bmap = bytearray(n)
    for i in range(len(bmap)):
        bmap[i] = 0
    row = height - (int(h) + int(yd))
    while True:
        b = next(lines)
        if 'ENDCHAR' == b:
            break
        if row < 0 or row >= height:
            row += 1
            continue
        for x4 in range(len(b)):
            hx = int(b[x4], 16)
            if hx & 0b1000:
                set_pixel(bmap, width, row, x4 * 4)
            if hx & 0b0100:
                set_pixel(bmap, width, row, x4 * 4 + 1)
            if hx & 0b0010:
                set_pixel(bmap, width, row, x4 * 4 + 2)
            if hx & 0b0001:
                set_pixel(bmap, width, row, x4 * 4 + 3)
        row += 1
    print (COPY_GLYPH % (fname, c_point, fname, c_point, width, 
        b64encode(bmap).decode('ASCII')))

def create_font_sql(lines):
    print (HEADER)
    f_num = 21
    name = '26_nimbus'
    version_id = '0'
    line_height = 26
    char_spacing = 5
    line_spacing = '9'
    print (HFONT % (name, f_num, line_height, 0, line_spacing, char_spacing,
        version_id))
    print (COPY)
    while True:
        v = next(lines)
        if v.startswith('CHARS '):
            break
    while True:
        v = next(lines)
        if 'ENDFONT' == v:
            break
        if v.startswith('STARTCHAR'):
            create_glyph_sql(lines, name, line_height, char_spacing)
    print (FOOTER)

if len(argv) != 2:
    print ("Usage:\n./bdf_import.py [file.bdf]\n")
    exit(1)
create_font_sql(line.strip() for line in open(argv[1]))
