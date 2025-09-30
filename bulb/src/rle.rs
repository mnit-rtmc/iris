// Copyright (C) 2025  Minnesota Department of Transportation
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

/// Decode one Base64 character into a value (0-63)
fn decode_char(c: char) -> Option<u32> {
    match c {
        '0'..='9' => Some(u32::from(c) - 48), // 0-9
        ';' | '.' => Some(10),                // '.' is URL-safe alternate
        '?' | '_' => Some(11),                // '_' is URL-safe alternate
        'A'..='Z' => Some(12 + u32::from(c) - 65), // 12-37
        'a'..='z' => Some(38 + u32::from(c) - 97), // 38-63
        _ => None,
    }
}

/// Encode one value (0-63) into a Base64 character
fn encode_char(v: u8) -> char {
    match v {
        0..=9 => char::from(v + 48),
        10 => '.',
        11 => '_',
        12..=37 => char::from(65 + v - 12),
        38..=63 => char::from(97 + v - 38),
        _ => '?',
    }
}

/// Encoder/decoder for custom Base64 alphabet
#[derive(Default)]
pub struct Base64 {
    /// Base64-encoded (u32) data
    data: String,
}

/// Iterator for decoding base64 data
pub struct Base64Iter<'a> {
    /// Base64 encoded data
    encoded: &'a Base64,
    /// Current offset into data
    offset: usize,
}

/// Run-length encoded table
pub struct Table {
    /// Base64-encoded data
    data: Base64,
    /// Most recent encoded value
    value: u32,
    /// Repeat count of value
    count: u32,
}

/// Iterator for decoding RLE data
pub struct TableIter<'a> {
    /// Base64-encoded data
    iter: Base64Iter<'a>,
    /// Most recent decoded value
    value: u32,
    /// Repeat count of value
    count: u32,
}

/// Invalid RLE value (only 30-bits allowed)
const INVALID_VALUE: u32 = 0x4000_0000;

impl Base64 {
    /// Create a new decoder from base64-encoded data
    pub fn new(data: String) -> Self {
        Base64 { data }
    }

    /// Encode a value to base64
    pub fn encode(&mut self, mut val: u32) {
        assert!(val < INVALID_VALUE);
        // encode lower bits of integer, 5 at a time
        while val > 0x1F {
            let v = 0x20 | (val as u8 & 0x1F);
            self.data.push(encode_char(v));
            val >>= 5;
        }
        // encode 5 high bits
        self.data.push(encode_char(val as u8))
    }

    /// Create an iterator to decode base64 data
    pub fn iter(&self) -> Base64Iter<'_> {
        Base64Iter {
            encoded: self,
            offset: 0,
        }
    }
}

impl Base64Iter<'_> {
    /// Decode the next Base64 character
    fn next_char(&mut self) -> Option<u32> {
        let c = *self.encoded.data.as_bytes().get(self.offset)?;
        self.offset += 1;
        decode_char(char::from(c))
    }

    /// Decode one value
    fn decode_value(&mut self) -> Option<u32> {
        let mut value = 0;
        // decode lower bits of integer, 5 at a time
        for i in [0, 5, 10, 15, 20, 25] {
            let v = self.next_char()?;
            value |= (v & 0x1F) << i;
            // check high "extend" bit
            if (v & 0x20) == 0 {
                return Some(value);
            }
        }
        // invalid value; more than 30 bits
        None
    }
}

impl Iterator for Base64Iter<'_> {
    type Item = u32;

    fn next(&mut self) -> Option<Self::Item> {
        self.decode_value()
    }
}

impl From<Table> for String {
    fn from(mut table: Table) -> Self {
        table.flush();
        table.data.data
    }
}

impl Default for Table {
    fn default() -> Self {
        Table {
            data: Base64::default(),
            value: INVALID_VALUE,
            count: 0,
        }
    }
}

impl Table {
    /// Create a new run-length-encoded table
    pub fn new(data: String) -> Self {
        Table {
            data: Base64::new(data),
            value: INVALID_VALUE,
            count: 0,
        }
    }

    /// Encode one 30-bit value
    pub fn encode(&mut self, value: u32) {
        if value == self.value {
            self.count += 1;
        } else {
            self.flush();
            self.value = value;
            self.count = 0;
        }
    }

    /// Flush encoded data to Base64
    fn flush(&mut self) {
        if self.value != INVALID_VALUE {
            self.data.encode(self.value);
            self.data.encode(self.count);
            self.value = INVALID_VALUE;
            self.count = 0;
        }
    }

    /// Create an iterator to decode table data
    pub fn iter(&self) -> TableIter<'_> {
        TableIter {
            iter: self.data.iter(),
            value: INVALID_VALUE,
            count: 0,
        }
    }
}

impl Iterator for TableIter<'_> {
    type Item = u32;

    fn next(&mut self) -> Option<Self::Item> {
        if self.value == INVALID_VALUE {
            let value = self.iter.next()?;
            self.count = self.iter.next()?;
            self.value = value;
        }
        let value = self.value;
        if self.count > 0 {
            self.count -= 1;
        } else {
            self.value = INVALID_VALUE;
            self.count = 0;
        }
        Some(value)
    }
}

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn empty() {
        let b64 = Base64::default();
        let data: Vec<_> = b64.iter().collect();
        assert!(data.is_empty());
    }

    #[test]
    fn decode() {
        let b64 = Base64::new(String::from("4567"));
        let data: Vec<u32> = b64.iter().collect();
        assert_eq!(data, [4, 5, 6, 7]);
    }

    #[test]
    fn decode2() {
        let b64 = Base64::new(String::from("TU1"));
        let data: Vec<u32> = b64.iter().collect();
        assert_eq!(data, [31, 32]);
    }

    #[test]
    fn encode() {
        let mut b64 = Base64::default();
        b64.encode(0);
        b64.encode(1);
        b64.encode(2);
        b64.encode(3);
        assert_eq!(b64.data, "0123");
    }

    #[test]
    fn encode2() {
        let mut b64 = Base64::default();
        b64.encode(31);
        b64.encode(32);
        assert_eq!(b64.data, "TU1");
    }

    #[test]
    fn codec() {
        let mut b64 = Base64::default();
        for i in 0..5000 {
            b64.encode(i);
        }
        assert_eq!(b64.data.len(), 13_944);
        let mut v = b64.iter();
        for i in 0..5000 {
            assert_eq!(v.next(), Some(i));
        }
    }

    #[test]
    fn enc_table() {
        let mut table = Table::default();
        table.encode(0);
        table.encode(0);
        table.encode(1);
        table.encode(1);
        table.encode(1);
        table.encode(1);
        assert_eq!(String::from(table), "0113");
    }

    #[test]
    fn enc_table2() {
        let mut table = Table::default();
        for _ in 0..37 {
            table.encode(37);
        }
        assert_eq!(String::from(table), "Z1Y1");
    }

    #[test]
    fn dec_table() {
        let table = Table::new("1024".to_string());
        let mut it = table.iter();
        assert_eq!(it.next(), Some(1));
        assert_eq!(it.next(), Some(2));
        assert_eq!(it.next(), Some(2));
        assert_eq!(it.next(), Some(2));
        assert_eq!(it.next(), Some(2));
        assert_eq!(it.next(), Some(2));
    }

    #[test]
    fn dec_table2() {
        let table = Table::new("Z1Y1".to_string());
        let mut it = table.iter();
        for _ in 0..37 {
            assert_eq!(it.next(), Some(37));
        }
    }

    #[test]
    fn codec_table() {
        let mut table = Table::default();
        for i in 0..5000 {
            table.encode(i);
        }
        let data = String::from(table);
        assert_eq!(data.len(), 18_944);
        let table = Table::new(data);
        let mut it = table.iter();
        for i in 0..5000 {
            assert_eq!(it.next(), Some(i));
        }
    }
}
