# Sign Configuration

Select `View ➔ Message Signs ➔ Sign Configuration` menu item

[DMS] sign configuration is queried from a [controller] when communication is
established.

<details>
<summary>API Resources</summary>

* `iris/sign_config`
* `iris/api/sign_config/{name}`

Attribute [permissions]:

| Access       | Minimal |
|--------------|---------|
| Read Only    | name, face\_width, face\_height, border\_horiz, border\_vert, pitch\_horiz, pitch\_vert, pixel\_width, pixel\_height, char\_width, char\_height, monochrome\_foreground, monochrome\_background, color\_scheme |
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


[controller]: controllers.html
[DMS]: dms.html
[font]: fonts.html
[permissions]: permissions.html
[sign message]: sign_message.html
