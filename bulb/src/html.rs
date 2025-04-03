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

/// Simple HTML builder
#[derive(Debug)]
pub struct Html {
    html: String,
    parents: Vec<&'static str>,
    open_tag: bool,
    error: bool,
}

impl Html {
    /// Create a new HTML builder
    pub fn new() -> Self {
        Html {
            html: String::new(),
            parents: Vec::new(),
            open_tag: false,
            error: false,
        }
    }

    /// Build the HTML into a String
    ///
    /// # Returns
    /// HTML as an owned `String`, or `None` on error
    pub fn build(mut self) -> Option<String> {
        if self.error {
            return None;
        }
        while let Some(elem) = self.parents.pop() {
            self.html.push_str("</");
            self.html.push_str(elem);
            self.html.push('>');
        }
        Some(self.html)
    }

    /// Add an element
    pub fn elem(&mut self, elem: &'static str) -> &mut Self {
        self.html.push('<');
        self.html.push_str(elem);
        self.html.push('>');
        self.parents.push(elem);
        self.open_tag = true;
        self
    }

    /// Add an attribute to an open element
    ///
    /// Must be called after `elem`, `attr` or `attr_val`
    pub fn attr(&mut self, attr: &str) -> &mut Self {
        if self.open_tag {
            match self.html.pop() {
                Some(gt) => assert_eq!(gt, '>'),
                None => unreachable!(),
            }
            self.html.push(' ');
            self.html.push_str(attr);
            self.html.push('>');
        } else {
            self.error = true;
        }
        self
    }

    /// Add an attribute with value to an open element
    ///
    /// Must be called after `elem`, `attr` or `attr_val`
    pub fn attr_val(&mut self, attr: &str, val: &str) -> &mut Self {
        if self.open_tag {
            match self.html.pop() {
                Some(gt) => assert_eq!(gt, '>'),
                None => unreachable!(),
            }
            self.html.push(' ');
            self.html.push_str(attr);
            self.html.push_str("=\"");
            for c in val.chars() {
                match c {
                    '&' => self.html.push_str("&amp;"),
                    '"' => self.html.push_str("&quot;"),
                    _ => self.html.push(c),
                }
            }
            self.html.push_str("\">");
        } else {
            self.error = true;
        }
        self
    }

    /// Add an `id` attribute to an open element
    pub fn id(&mut self, val: &str) -> &mut Self {
        self.attr_val("id", val)
    }

    /// Add a `class` attribute to an open element
    #[allow(dead_code)]
    pub fn class(&mut self, val: &str) -> &mut Self {
        self.attr_val("class", val)
    }

    /// Add text content which will be escaped
    pub fn text(&mut self, text: &str) -> &mut Self {
        if self.parents.is_empty() {
            self.error = true;
        } else {
            for c in text.chars() {
                match c {
                    '&' => self.html.push_str("&amp;"),
                    '<' => self.html.push_str("&lt;"),
                    '>' => self.html.push_str("&gt;"),
                    _ => self.html.push(c),
                }
            }
            self.open_tag = false;
        }
        self
    }

    /// Add text content with no escaping
    #[allow(dead_code)]
    pub fn text_no_esc(&mut self, text: &str) -> &mut Self {
        if self.parents.is_empty() {
            self.error = true;
        } else {
            self.html.push_str(text);
            self.open_tag = false;
        }
        self
    }

    /// End the current element
    pub fn end(&mut self) -> &mut Self {
        match self.parents.pop() {
            Some(elem) => {
                self.html.push_str("</");
                self.html.push_str(elem);
                self.html.push('>');
            }
            None => self.error = true,
        }
        self.open_tag = false;
        self
    }
}

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn html() {
        let mut html = Html::new();
        html.elem("div");
        assert_eq!(html.build().unwrap(), String::from("<div></div>"));
        let mut html = Html::new();
        html.elem("div").attr_val("id", "test").attr("spellcheck");
        assert_eq!(
            html.build().unwrap(),
            String::from("<div id=\"test\" spellcheck></div>")
        );
        let mut html = Html::new();
        html.elem("p").text("This is a paragraph");
        assert_eq!(
            html.build().unwrap(),
            String::from("<p>This is a paragraph</p>")
        );
        let mut html = Html::new();
        html.elem("em").text("You & I");
        assert_eq!(html.build().unwrap(), String::from("<em>You &amp; I</em>"));
        let mut html = Html::new();
        html.elem("div")
            .elem("span")
            .text("Test")
            .end()
            .text_no_esc("&quot;");
        assert_eq!(
            html.build().unwrap(),
            String::from("<div><span>Test</span>&quot;</div>")
        );
    }

    #[test]
    fn ol() {
        let mut html = Html::new();
        html.elem("ol");
        html.elem("li").class("cat").text("nori").end();
        html.elem("li").class("cat").text("chashu");
        assert_eq!(
            html.build().unwrap(),
            String::from(
                "<ol><li class=\"cat\">nori</li><li class=\"cat\">chashu</li></ol>"
            )
        );
    }
}
