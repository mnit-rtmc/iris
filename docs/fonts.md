# Fonts

A font is a set of bitmapped glyphs for displaying text on a [DMS].  Fonts can
contain only printable ASCII characters (U+0020 to U+007E).

<details>
<summary>API Resources</summary>

* `iris/api/font`
* `iris/api/tfon/{name}.tfon`

Attribute [permissions]:

| Access       | Minimal           | Full |
|--------------|-------------------|------|
| Read Only    | name, font_number |      |
| 👉 Operate   |                   |      |
| 💡 Manage    |                   |      |
| 🔧 Configure |                   |      |

</details>

## Predefined Fonts

A number of fonts are included in the `/var/lib/iris/fonts` directory.  These
fonts are designed to have a similar visual style.

Name    | Number | Description
--------|--------|-----------------------------------------
`F07`   | 7      | 7 px height
`F07-C` | 5      | 7 px height, 5 px width character-matrix
`F07-L` | 107 †  | 7 px height line-matrix
`F08`   | 8      | 8 px height
`F09`   | 9      | 9 px height
`F09-L` | 109 †  | 9 px height line-matrix
`F10`   | 10     | 10 px height
`F11`   | 11     | 11 px height
`F12`   | 12     | 12 px height
`F12-A` | 112 †  | 12 px height all-ASCII (9 height caps)
`F12-B` | 212 †  | 12 px height bold
`F13`   | 13     | 13 px height
`F14`   | 14     | 14 px height
`F14-A` | 114 †  | 14 px height all-ASCII
`F15`   | 15     | 15 px height
`F16`   | 16     | 16 px height
`F18`   | 18     | 18 px height
`F20`   | 20     | 20 px height
`F24`   | 24     | 24 px height
`F26`   | 26     | 26 px height

_† Normally font number is the same as pixel height, but variations use
1xx or 2xx._

Numbers 1-4 are reserved for **permanent** fonts used by some signs.

To import a font into the IRIS database, use tfon_import.py (in `bin`
directory):

```
tfon_import.py [font file] | psql tms
```

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

## Choosing Fonts

When choosing a font, a few parameters must be considered, such as pixel pitch,
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

## Send and Query Fonts

The status tab of the DMS properties dialog contains two settings buttons.
Pressing `Send Settings` will cause all necessary fonts to be sent to the sign.
`Query Settings` will read all fonts currently on the sign, and store them in
the `/var/lib/iris/fonts/{sign_name}` directory.


[DMS]: dms.html
