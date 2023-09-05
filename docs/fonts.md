# Fonts

A font is a set of bitmapped glyphs for displaying text on a [DMS].  Fonts can
contain only printable ASCII characters (U+0020 to U+007E).

When selecting a font, a few parameters must be considered, such as pixel pitch,
desired character height, and font weight.  Pixel pitch varies from 70 mm down
to 20 mm or smaller.

For legibility, text on freeway signs should be about 380 mm tall (15 in).  See
the table below for optimal character heights based on pixel pitch.

Pixel Pitch | Font Height | Character Height
------------|-------------|-----------------
70 mm       | 7 px        | 420 mm (16.5 in)
66 mm       | 7 px        | 396 mm (15.6 in)
63 mm       | 7 px        | 378 mm (14.9 in)
50 mm       | 8 px        | 350 mm (13.8 in)
43 mm       | 9 px        | 387 mm (15.2 in)
33 mm       | 12 px       | 363 mm (14.3 in)
20 mm       | 20 px       | 380 mm (15.0 in)

Upper-case only fonts are recommended for larger pixel pitch, but fonts with
lower-case characters can be used if pitch is below 50 mm.  If a message
containing lower-case characters is used with an upper-case only font, it will
be converted to upper-case.

## Predefined Fonts

A number of fonts are included in the `/var/lib/iris/fonts` directory.  These
fonts are designed to have a similar visual style.  To import a font into the
IRIS database, use ifnt_import.py (in `bin` directory):

```
ifnt_import.py [font file] | psql tms
```

Number | Font Name      | Description
-------|----------------|---------------------
1      | `07_char`      | 7x5 character matrix
2      | `07_line`      | 7 pixel high line-matrix
3      | `08_full`      | 8 pixel high full-matrix
4      | `09_full`      | 9 pixel high full-matrix
5      | `10_full`      | 10 pixel high full-matrix
6      | `11_full`      | 11 pixel high full-matrix
7      | `12_full`      | 12 pixel high full-matrix
8      | `12_full_bold` | 12 pixel high full-matrix bold
9      | `13_full`      | 13 pixel high full-matrix
10     | `14_ledstar`   | 14 pixel high full-matrix
11     | `14_full`      | 14 pixel high full-matrix
21     | `15_full`      | 15 pixel high full-matrix
13     | `16_full`      | 16 pixel high full-matrix
14     | `18_full`      | 18 pixel high full-matrix
15     | `20_full`      | 20 pixel high full-matrix
16     | `24_full`      | 24 pixel high full-matrix
20     | `26_full`      | 26 pixel high full-matrix
17     | `_09_full_12`  | 9 pixel high (12 with lower case descenders)
18     | `_7_full`      | 7 pixel high full-matrix
19     | `_9_line`      | 9 pixel high line-matrix

### Non-ASCII Characters

| Character     | Code Point | ASCII | Fonts
|---------------|------------|-------|---------------------------------
| <sup>ND</sup> | 38         | &     | `14_ledstar`
| ◊             | 42         | *     | `07_line`, `_07_full`, `26_full`
| ↖             | 94         | ^     | `14_ledstar`
| █             | 96         | \`    | `14_ledstar`
| ↙             | 123        | {     | `_07_full`, `14_ledstar`
| ↓             | 125        | }     | `_07_full`
| ↘             | 125        | }     | `14_ledstar`
| `wide space`  | 126        | ~     | `_07_full`


[DMS]: dms.html
