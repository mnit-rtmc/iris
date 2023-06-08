# Graphic Images

Select `View ➔ Message Signs ➔ Graphics` menu item

Graphic images can be displayed on [DMS] [sign message]s.  These can be warning
pictographs, interstate shields or other signs.

Image editing, resizing, painting, *etc.* is not supported - graphics must be
edited and sized properly before importing into the IRIS image library.  Use an
image editor such as [GIMP] or Microsoft Paint for this purpose.

Requirements:

- 1-bit monochrome or 24-bit color
- PNG or GIF image format
- Width between 1 and 240 pixels
- Height between 1 and 144 pixels

To import a graphic, enable [edit mode] and press the **Create** button.
Select the image in the file picker.

Each image has a unique **Graphic Number**, between 1 and 999.  This number
can be used in a [MULTI] `[g`...`]` tag to display the graphic on a sign
message.

A single **Transparent Color** can be selected to be fully transparent - alpha
transparency is not supported. 

## Static Graphics

Graphic images can also represent a [static sign] with a smaller DMS inset.  For
example, a static sign would contain a travel time message with a destination,
and the DMS inset would only be large enough to display a 2-digit number of
minutes.  The static graphic is displayed when selecting the DMS, to help
operators understand the sign's function.


[DMS]: dms.html
[edit mode]: user_interface.html#edit-mode
[GIMP]: https://www.gimp.org/
[MULTI]: multi.html
[sign message]: sign_message.html
[static sign]: dms.html#setup
