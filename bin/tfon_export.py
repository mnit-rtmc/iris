#!/usr/bin/env python3

'''This script connects to an IRIS database and exports a single font in .tfon
format.'''

from sys import argv, exit
from base64 import b64decode
import pgdb

# Symbols for all ASCII + Latin 1 characters
SYMBOL = [
	"NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL", "BS", "HT", "LF",
	"VT", "FF", "CR", "SO", "SI", "DLE", "DC1", "DC2", "DC3", "DC4", "NAK",
	"SYN", "ETB", "CAN", "EM", "SUB", "ESC", "FS", "GS", "RS", "US", "SP", "!",
	"\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/", "0",
	"1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?",
	"@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
	"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "[", "\\", "]",
	"^", "_", "`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
	"m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{",
	"|", "}", "~", "DEL", "PAD", "HOP", "BPH", "NBH", "IND", "NEL", "SSA",
	"ESA", "HTS", "HTJ", "LTS", "PLD", "PLU", "RI", "SS2", "SS3", "DCS", "PU1",
	"PU2", "STS", "CCH", "MW", "SPA", "EPA", "SOS", "SGCI", "SCI", "CSI", "ST",
	"OSC", "PM", "APC", "NBSP", "¡", "¢", "£", "¤", "¥", "¦", "§", "¨", "©",
	"ª", "«", "¬", "SHY", "®", "¯", "°", "±", "²", "³", "´", "µ", "¶", "·",
	"¸", "¹", "º", "»", "¼", "½", "¾", "¿", "À", "Á", "Â", "Ã", "Ä", "Å", "Æ",
	"Ç", "È", "É", "Ê", "Ë", "Ì", "Í", "Î", "Ï", "Ð", "Ñ", "Ò", "Ó", "Ô", "Õ",
	"Ö", "×", "Ø", "Ù", "Ú", "Û", "Ü", "Ý", "Þ", "ß", "à", "á", "â", "ã", "ä",
	"å", "æ", "ç", "è", "é", "ê", "ë", "ì", "í", "î", "ï", "ð", "ñ", "ò", "ó",
	"ô", "õ", "ö", "÷", "ø", "ù", "ú", "û", "ü", "ý", "þ", "ÿ",
]

def read_font(conn, font):
	cursor = conn.cursor()
	cursor.execute("SELECT name, f_number, height, line_spacing, "
		"char_spacing FROM iris.font WHERE name = '%s';" % font)
	name, f_number, height, line_spacing, char_spacing = cursor.fetchone()
	print('font_name: %s' % name)
	print('font_number: %s' % f_number)
	print('char_spacing: %s' % char_spacing)
	print('line_spacing: %s' % line_spacing)
	print()
	cursor.execute("SELECT code_point, width, pixels FROM iris.glyph "
		"WHERE font = '%s' ORDER BY code_point;" % font)
	for row in range(cursor.rowcount):
		cp, width, pixels = cursor.fetchone()
		bmap = b64decode(pixels)
		print('ch: %s %s' % (cp, SYMBOL[cp]))
		print_char(height, width, bmap)
		print()
	cursor.close()

def print_char(height, width, bmap):
	for row in range(height):
		print_row(row, width, bmap)

def print_row(row, width, bmap):
	for col in range(width):
		if lit_pixel(bmap, width, row, col):
			print('@', end='')
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
