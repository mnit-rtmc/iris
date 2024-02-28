# Fonts

A font is a set of bitmapped glyphs for displaying text on a [DMS].

<details>
<summary>API Resources</summary>

* `iris/api/font` (minimal)
* `iris/api/tfon/{name}.tfon`

| Access  | Minimal           |
|---------|-------------------|
| 👁️  View | name, font_number |

</details>

A number of fonts are included in the `/var/lib/iris/fonts` directory:

Name    | Number | Height | Line Spacing | 3-Lines | Notes
--------|--------|--------|--------------|---------|-----------
`F07-C` | 5      | 7 px   | —            | —       | 5 px width
`F07`   | 7      | 7 px   | 3 px         | 27 px   |
`F08`   | 8      | 8 px   | 2 px         | 28 px   |
`F09`   | 9      | 9 px   | 3 px         | 33 px   |
`F10`   | 10     | 10 px  | 3 px         | 36 px   |
`F11`   | 11     | 11 px  | 3 px         | 39 px   |
`F12`   | 12     | 12 px  | 3 px         | 42 px   |
`F12-A` | 112 †  | 12 px  | _(2 px)_     | —       | full ASCII
`F12-B` | 212 †  | 12 px  | _(4 px)_     | —       | **bold**
`F13`   | 13     | 13 px  | 4 px         | 47 px   |
`F14`   | 14     | 14 px  | 4 px         | 50 px   |
`F14-A` | 114 †  | 14 px  | _(6 px)_     | —       | full ASCII
`F15`   | 15     | 15 px  | 4 px         | 53 px   |
`F16`   | 16     | 16 px  | 4 px         | 56 px   |
`F18`   | 18     | 18 px  | 5 px         | 64 px   |
`F20`   | 20     | 20 px  | 6 px         | 72 px   |
`F22`   | 22     | 22 px  | 7 px         | 80 px   |
`F24`   | 24     | 24 px  | 8 px         | 88 px   |
`F26`   | 26     | 26 px  | 8 px         | 94 px   |

† _Normally font number is the same as pixel height, but variations use
1xx or 2xx._

Numbers 1-4 are reserved for **permanent** fonts used by some signs.

### Non-ASCII Characters

Since NTCIP 1203 does not support Unicode, ASCII characters must be used as
surrogates for arrows, diamonds, etc:

| Character     | Code Point | ASCII | Fonts
|---------------|------------|-------|----------------------
| <sup>ND</sup> | 38         | &     | `F14-A`
| ◊             | 42         | *     | `F07`, `F26`
| ↖             | 94         | ^     | `F14-A`
| █             | 96         | \`    | `F14-A`
| ↙             | 123        | {     | `F07`, `F14-A`
| ↘             | 125        | }     | `F07`, `F14-A`

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

## Importing

Fonts in [tfon] format can be imported into the IRIS database using
`tfon_import.py` (in the `bin` directory):

```
tfon_import.py [font file] | psql tms
```

Also, each font file must be copied to the `/var/www/html/iris/api/tfon/`
directory to make it available in the [REST API].

## Send and Query Fonts

The status tab of the DMS properties dialog contains two settings buttons.
Pressing `Send Settings` will cause all necessary fonts to be sent to the sign.
`Query Settings` will read all fonts currently on the sign, and store them in
the `/var/lib/iris/fonts/{sign_name}` directory.


[BDF]: https://en.wikipedia.org/wiki/Glyph_Bitmap_Distribution_Format
[DMS]: dms.html
[fontu]: https://github.com/DougLau/tfon/tree/main/fontu
[REST API]: rest_api.html
[tfon]: https://github.com/DougLau/tfon/tree/main/tfon
