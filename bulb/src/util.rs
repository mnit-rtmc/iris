// Copyright (C) 2022  Minnesota Department of Transportation
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
use serde_json::Value;
use std::fmt;

/// An optional value which has impl Display
#[derive(Debug)]
pub struct OptVal<T>(pub Option<T>);

impl<T> fmt::Display for OptVal<T>
where
    T: fmt::Display,
{
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match &self.0 {
            Some(v) => write!(f, "{}", v),
            None => Ok(()),
        }
    }
}

impl From<OptVal<u32>> for Value {
    fn from(val: OptVal<u32>) -> Self {
        match val.0 {
            Some(num) => Value::Number(num.into()),
            None => Value::Null,
        }
    }
}

/// String wrapper which can be written as HTML
#[derive(Debug)]
pub struct HtmlStr<S>(pub S);

impl<S> HtmlStr<S> {
    fn fmt_encode(s: &str, f: &mut fmt::Formatter) -> fmt::Result {
        for c in s.chars() {
            match c {
                '&' => write!(f, "&amp;")?,
                '<' => write!(f, "&lt;")?,
                '>' => write!(f, "&gt;")?,
                '"' => write!(f, "&quot;")?,
                '\'' => write!(f, "&#x27;")?,
                _ => write!(f, "{}", c)?,
            }
        }
        Ok(())
    }
}

impl fmt::Display for HtmlStr<&str> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        Self::fmt_encode(self.0, f)
    }
}

impl fmt::Display for HtmlStr<&String> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        Self::fmt_encode(self.0, f)
    }
}

impl fmt::Display for HtmlStr<Option<&String>> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match &self.0 {
            Some(val) => Self::fmt_encode(val, f),
            None => Ok(()),
        }
    }
}

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn html() {
        assert_eq!(HtmlStr("<").to_string(), "&lt;");
        assert_eq!(HtmlStr(">").to_string(), "&gt;");
        assert_eq!(HtmlStr("&").to_string(), "&amp;");
        assert_eq!(HtmlStr("\"").to_string(), "&quot;");
        assert_eq!(HtmlStr("'").to_string(), "&#x27;");
        assert_eq!(
            HtmlStr("<script>XSS stuff</script>").to_string(),
            "&lt;script&gt;XSS stuff&lt;/script&gt;"
        );
    }
}
