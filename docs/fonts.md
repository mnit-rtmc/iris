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

Number | Font Name | Description
-------|-----------|---------------------
5      | `F07-C`   | 7x5 character-matrix
7      | `F07`     | 7 height
8      | `F08`     | 8 height
9      | `F09`     | 9 height
10     | `F10`     | 10 height
11     | `F11`     | 11 height
12     | `F12`     | 12 height
13     | `F13`     | 13 height
14     | `F14`     | 14 height
15     | `F15`     | 15 height
16     | `F16`     | 16 height
18     | `F18`     | 18 height
20     | `F20`     | 20 height
24     | `F24`     | 24 height
26     | `F26`     | 26 height
107    | `F07-L`   | 7 height line-matrix
109    | `F09-L`   | 9 height line-matrix
112    | `F12-A`   | 12 height ASCII (9 height caps)
114    | `F14-A`   | 14 height ASCII
212    | `F12-B`   | 12 height bold
214    | `F14-N`   | 14 height narrow

### Non-ASCII Characters

Since NTCIP 1203 does not support Unicode, ASCII characters must be used as
surrogates for arrows, diamonds, etc:

| Character     | Code Point | ASCII | Fonts
|---------------|------------|-------|----------------------
| <sup>ND</sup> | 38         | &     | `F14-A`
| ◊             | 42         | *     | `F07`, `F07-L`, `F26`
| ↖             | 94         | ^     | `F14-A`
| █             | 96         | \`    | `F14-A`
| ↙             | 123        | {     | `F07`, `F14-A`
| ↓             | 125        | }     | `F07`
| ↘             | 125        | }     | `F14-A`
| `wide space`  | 126        | ~     | `F07`

## Send and Query Fonts

The status tab of the DMS properties dialog contains two settings buttons.
Pressing `Send Settings` will cause all necessary fonts to be sent to the sign.
`Query Settings` will read all fonts currently on the sign, and store them in
the `/var/lib/iris/fonts/{sign_name}` directory.


[DMS]: dms.html
