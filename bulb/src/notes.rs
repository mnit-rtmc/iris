// Copyright (C) 2024  Minnesota Department of Transportation
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//

/// Check if a character is valid for a hashtag
fn is_tag_char(c: char) -> bool {
    c.is_ascii_digit() || c.is_ascii_uppercase() || c.is_ascii_lowercase()
}

/// Check if a notes field contains a hashtag
pub fn contains_hashtag(notes: &str, tag: &str) -> bool {
    let mut start = None;
    for (i, c) in notes.char_indices() {
        if let Some(s) = start
            && !is_tag_char(c)
        {
            if tag.eq_ignore_ascii_case(&notes[s..i]) {
                return true;
            }
            start = None;
        }
        if c == '#' {
            start = Some(i);
        }
    }
    match start {
        Some(s) => tag.eq_ignore_ascii_case(&notes[s..]),
        None => false,
    }
}
