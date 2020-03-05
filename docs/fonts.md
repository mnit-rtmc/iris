# Fonts

Select `View ➔ Message Signs ➔ DMS Fonts` menu item

A font is a set of bitmapped glyphs for displaying text on a [DMS].  Each font
can contain any of the printable characters in the ASCII character set (0x20 to
0x7E).

When selecting a font, a few parameters must be considered, such as pixel pitch,
desired character height, and font weight.  Pixel pitch can vary from 66 mm down
to 20 mm or smaller.

Typically, for freeway signs, characters should be between 350 and 400 mm (about
14 or 15 inches) high.  See the table below for character heights based on these
common sizes.

Font Height | Pitch: 20 mm | Pitch: 33 mm | Pitch: 50 mm | Pitch 66 mm
------------|--------------|--------------|--------------|-------------
7 px        | 120 mm       | 198 mm       | 300 mm       | 396 mm †
8 px        | 140 mm       | 231 mm       | 350 mm †     | 462 mm
10 px       | 180 mm       | 297 mm       | 450 mm       | 594 mm
12 px       | 220 mm       | 363 mm †     | 550 mm       | 726 mm
14 px       | 260 mm       | 429 mm       | 650 mm       | 858 mm
16 px       | 300 mm       | 495 mm       | 750 mm       | 990 mm
18 px       | 340 mm       | 561 mm       | 850 mm       | 1122 mm
20 px       | 380 mm †     | 627 mm       | 950 mm       | 1254 mm

_† Best height_

In the past, DMS have been used with upper-case only fonts, but with smaller
pixel pitch, it is possible to create legible fonts which also include
lower-case characters.  If a message containing lower-case characters is used
with an upper-case only font, IRIS will replace those characters with their
equivalent.

## Predefined Fonts

A number of fonts are included in the `sql/fonts` directory.  These fonts are
designed to have a similar visual style.  To import a font into the IRIS
database, use psql:

```
psql tms -f [font file]
```

Number | Font Name    | Description
-------|--------------|---------------------
1      | 07_char      | 7x5 character matrix
2      | 07_line      | 7 pixel high line-matrix
3      | 08_full      | 8 pixel high full-matrix
4      | 09_full      | 9 pixel high full-matrix
5      | 10_full      | 10 pixel high full-matrix
6      | 11_full      | 11 pixel high full-matrix
7      | 12_full      | 12 pixel high full-matrix
8      | 12_full_bold | 12 pixel high full-matrix bold
9      | 13_full      | 13 pixel high full-matrix
11     | 14_full      | 14 pixel high full-matrix
12     | 14_full_thin | 14 pixel high full-matrix
21     | 15_full      | 15 pixel high full-matrix
13     | 16_full      | 16 pixel high full-matrix
14     | 18_full      | 18 pixel high full-matrix
15     | 20_full      | 20 pixel high full-matrix (numerals only)
16     | 24_full      | 24 pixel high full-matrix (numerals only)
20     | 26_full      | 26 pixel high full-matrix<
17     | _09_full_12  | 9 pixel high (12 with lower case descenders)

The IRIS client also contains a font editor which can be used to design new DMS
fonts.


[DMS]: dms.html
