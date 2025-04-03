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
    stack: Vec<&'static str>,
    open_tag: bool,
    error: Option<&'static str>,
}

/// HTML Void elements
const VOID_ELEMENTS: &[&str] = &[
    "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta",
    "param", "source", "track", "wbr",
];

/// Check if an element is a Void element
fn is_void(elem: &str) -> bool {
    VOID_ELEMENTS.contains(&elem)
}

impl Html {
    /// Create a new HTML builder
    pub fn new() -> Self {
        Html {
            html: String::new(),
            stack: Vec::new(),
            open_tag: false,
            error: None,
        }
    }

    /// Build the HTML into a String
    ///
    /// # Returns
    /// HTML as an owned `String`
    pub fn build(mut self) -> String {
        if let Some(err) = self.error {
            return format!("<p>Error: {err}");
        }
        while let Some(elem) = self.stack.pop() {
            self.html.push_str("</");
            self.html.push_str(elem);
            self.html.push('>');
        }
        self.html
    }

    /// Add an element
    pub fn elem(&mut self, elem: &'static str) -> &mut Self {
        self.html.push('<');
        self.html.push_str(elem);
        self.html.push('>');
        if !is_void(elem) {
            self.stack.push(elem);
        }
        self.open_tag = true;
        self
    }

    /// Add a boolean attribute to an open element
    ///
    /// Must be called after `elem`, `attr` or `attr_bool`
    pub fn attr_bool(&mut self, attr: &'static str) -> &mut Self {
        if self.open_tag {
            match self.html.pop() {
                Some(gt) => assert_eq!(gt, '>'),
                None => unreachable!(),
            }
            self.html.push(' ');
            self.html.push_str(attr);
            self.html.push('>');
        } else {
            self.error = Some("attribute after text");
        }
        self
    }

    /// Add an attribute with value to an open element
    ///
    /// Must be called after `elem`, `attr` or `attr_bool`
    pub fn attr(&mut self, attr: &'static str, val: &str) -> &mut Self {
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
            self.error = Some("attribute after text");
        }
        self
    }

    /// Add an `id` attribute to an open element
    pub fn id(&mut self, val: &str) -> &mut Self {
        self.attr("id", val)
    }

    /// Add a `class` attribute to an open element
    #[allow(dead_code)]
    pub fn class(&mut self, val: &str) -> &mut Self {
        self.attr("class", val)
    }

    /// Add a `type` attribute to an open element
    #[allow(dead_code)]
    pub fn type_(&mut self, val: &str) -> &mut Self {
        self.attr("type", val)
    }

    /// Add text content which will be escaped
    pub fn text_len(&mut self, text: &str, len: usize) -> &mut Self {
        if self.stack.is_empty() {
            self.error = Some("text at root");
        } else {
            for c in text.chars().take(len) {
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

    /// Add text content which will be escaped
    pub fn text(&mut self, text: &str) -> &mut Self {
        self.text_len(text, usize::MAX)
    }

    /// Add text content with no escaping
    #[allow(dead_code)]
    pub fn text_no_esc(&mut self, text: &str) -> &mut Self {
        if self.stack.is_empty() {
            self.error = Some("text at root");
        } else {
            self.html.push_str(text);
            self.open_tag = false;
        }
        self
    }

    /// End the current element
    pub fn end(&mut self) -> &mut Self {
        match self.stack.pop() {
            Some(elem) => {
                self.html.push_str("</");
                self.html.push_str(elem);
                self.html.push('>');
            }
            None => self.error = Some("stack underflow"),
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
        assert_eq!(html.build(), String::from("<div></div>"));
        let mut html = Html::new();
        html.elem("div").id("test").attr_bool("spellcheck");
        assert_eq!(
            html.build(),
            String::from("<div id=\"test\" spellcheck></div>")
        );
        let mut html = Html::new();
        html.elem("p").text("This is a paragraph");
        assert_eq!(html.build(), String::from("<p>This is a paragraph</p>"));
        let mut html = Html::new();
        html.elem("em").text("You & I");
        assert_eq!(html.build(), String::from("<em>You &amp; I</em>"));
        let mut html = Html::new();
        html.elem("div")
            .elem("span")
            .text("Test")
            .end()
            .text_no_esc("&quot;");
        assert_eq!(
            html.build(),
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
            html.build(),
            String::from(
                "<ol><li class=\"cat\">nori</li><li class=\"cat\">chashu</li></ol>"
            )
        );
    }

    #[test]
    fn void() {
        let mut html = Html::new();
        html.elem("div").elem("input").type_("text").text("Stuff");
        assert_eq!(
            html.build(),
            String::from("<div><input type=\"text\">Stuff</div>")
        );
    }
}
