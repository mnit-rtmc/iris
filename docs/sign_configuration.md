# Sign Configuration

Select `View ➔ Message Signs ➔ Sign Configuration` menu item

[DMS] sign configuration is queried from a [controller] when communication is
established.

<details>
<summary>API Resources</summary>

* `iris/api/sign_config`
* `iris/api/sign_config/{name}`

Attribute [permissions]:

| Access       | Minimal |
|--------------|---------|
| 👁️  View      | name, face\_width, face\_height, border\_horiz, border\_vert, pitch\_horiz, pitch\_vert, pixel\_width, pixel\_height, char\_width, char\_height, monochrome\_foreground, monochrome\_background, color\_scheme |
| 🔧 Configure | default\_font, module\_width, module\_height |

</details>

**Face width** and **height** are the dimensions of the full face of the
sign, in milimeters.

**Border** is the area around the pixels on the face of the sign.

| Sign Type          | `char_width` | `char_height` |
|--------------------|--------------|---------------|
| _character-matrix_ | > 0          | > 0           |
| _line-matrix_      | > 0          | 0             |
| _full-matrix_      | 0            | 0             |

The **default [font]** is used for [sign message]s which do not specify a font.

**Module** width and height are the dimensions of physical pixel modules.

## Sign Details

Sign details are additional parameters queried from a [controller] when
communication is established.

<details>
<summary>API Resources</summary>

* `iris/api/sign_detail`

Attribute [permissions]:

| Access  | Minimal |
|---------|---------|
| 👁️  View | name, dms\_type, portable, technology, sign\_access, legend, beacon\_type, hardware\_make, hardware\_model, software\_make, software\_model, supported\_tags, max\_pages, max\_multi\_len, beacon\_activation\_flag, pixel\_service\_flag |

</details>


[controller]: controllers.html
[DMS]: dms.html
[font]: fonts.html
[permissions]: permissions.html
[sign message]: sign_message.html
