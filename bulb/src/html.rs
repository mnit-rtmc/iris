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
use std::fmt::Display;

/// Get optional `str` reference
pub fn opt_ref(val: &Option<impl AsRef<str>>) -> &str {
    match val {
        Some(v) => v.as_ref(),
        None => "",
    }
}

/// Get optional String
pub fn opt_str(val: Option<impl Display>) -> String {
    match val {
        Some(v) => v.to_string(),
        None => String::new(),
    }
}

/// Simple HTML builder
pub struct Html {
    html: String,
    stack: Vec<&'static str>,
}

/// Borrowed HTML element
pub struct Elem<'h> {
    html: &'h mut Html,
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

#[allow(dead_code)]
impl Html {
    /// Create a new HTML builder
    pub fn new() -> Self {
        Html {
            html: String::new(),
            stack: Vec::new(),
        }
    }

    /// Build the HTML into a String
    ///
    /// # Returns
    /// HTML as an owned `String`
    pub fn build(mut self) -> String {
        while let Some(elem) = self.stack.pop() {
            self.html.push_str("</");
            self.html.push_str(elem);
            self.html.push('>');
        }
        self.html
    }

    /// Add an element
    fn elem(&mut self, elem: &'static str) -> Elem {
        self.html.push('<');
        self.html.push_str(elem);
        self.html.push('>');
        if !is_void(elem) {
            self.stack.push(elem);
        }
        Elem { html: self }
    }

    pub fn a(&mut self) -> Elem {
        self.elem("a")
    }

    pub fn button(&mut self) -> Elem {
        self.elem("button")
    }

    pub fn div(&mut self) -> Elem {
        self.elem("div")
    }

    pub fn em(&mut self) -> Elem {
        self.elem("em")
    }

    pub fn input(&mut self) -> Elem {
        self.elem("input")
    }

    pub fn img(&mut self) -> Elem {
        self.elem("img")
    }

    pub fn label(&mut self) -> Elem {
        self.elem("label")
    }

    pub fn li(&mut self) -> Elem {
        self.elem("li")
    }

    pub fn meter(&mut self) -> Elem {
        self.elem("meter")
    }

    pub fn p(&mut self) -> Elem {
        self.elem("p")
    }

    pub fn ol(&mut self) -> Elem {
        self.elem("ol")
    }

    pub fn option(&mut self) -> Elem {
        self.elem("option")
    }

    pub fn select(&mut self) -> Elem {
        self.elem("select")
    }

    pub fn span(&mut self) -> Elem {
        self.elem("span")
    }

    pub fn textarea(&mut self) -> Elem {
        self.elem("textarea")
    }

    /// Add an attribute with value to an open element
    fn attr(&mut self, attr: &'static str, val: impl AsRef<str>) {
        match self.html.pop() {
            Some(gt) => assert_eq!(gt, '>'),
            None => unreachable!(),
        }
        self.html.push(' ');
        self.html.push_str(attr);
        self.html.push_str("=\"");
        for c in val.as_ref().chars() {
            match c {
                '&' => self.html.push_str("&amp;"),
                '"' => self.html.push_str("&quot;"),
                _ => self.html.push(c),
            }
        }
        self.html.push_str("\">");
    }

    /// Add a boolean attribute to an open element
    fn attr_bool(&mut self, attr: &'static str) {
        match self.html.pop() {
            Some(gt) => assert_eq!(gt, '>'),
            None => unreachable!(),
        }
        self.html.push(' ');
        self.html.push_str(attr);
        self.html.push('>');
    }

    /// Add text content which will be escaped
    pub fn text(&mut self, text: impl AsRef<str>) -> &mut Self {
        self.text_len(text, usize::MAX)
    }

    /// Add text content which will be escaped
    pub fn text_len(&mut self, text: impl AsRef<str>, len: usize) -> &mut Self {
        for c in text.as_ref().chars().take(len) {
            match c {
                '&' => self.html.push_str("&amp;"),
                '<' => self.html.push_str("&lt;"),
                '>' => self.html.push_str("&gt;"),
                _ => self.html.push(c),
            }
        }
        self
    }

    /// Add raw content
    pub fn raw(&mut self, text: impl AsRef<str>) -> &mut Self {
        self.html.push_str(text.as_ref());
        self
    }

    /// End the current element
    pub fn end(&mut self) -> &mut Self {
        if let Some(elem) = self.stack.pop() {
            self.html.push_str("</");
            self.html.push_str(elem);
            self.html.push('>');
        }
        self
    }
}

#[allow(dead_code)]
impl<'h> Elem<'h> {
    /// Add an attribute with value to an open element
    pub fn attr(self, attr: &'static str, val: impl AsRef<str>) -> Self {
        self.html.attr(attr, val);
        self
    }

    /// Add a boolean attribute to an open element
    pub fn attr_bool(self, attr: &'static str) -> Self {
        self.html.attr_bool(attr);
        self
    }

    /// Add a `class` attribute to an open element
    pub fn class(self, val: impl AsRef<str>) -> Self {
        self.html.attr("class", val);
        self
    }

    /// Add an `id` attribute to an open element
    pub fn id(self, val: impl AsRef<str>) -> Self {
        self.html.attr("id", val);
        self
    }

    /// Add a `type` attribute to an open element
    pub fn type_(self, val: impl AsRef<str>) -> Self {
        self.html.attr("type", val);
        self
    }

    /// Add text content which will be escaped
    pub fn text(self, text: impl AsRef<str>) -> &'h mut Html {
        self.html.text_len(text, usize::MAX)
    }

    /// Add text content which will be escaped
    pub fn text_len(self, text: impl AsRef<str>, len: usize) -> &'h mut Html {
        self.html.text_len(text, len)
    }

    /// End the current element
    pub fn end(self) -> &'h mut Html {
        self.html.end()
    }

    pub fn a(self) -> Self {
        self.html.a()
    }

    pub fn button(self) -> Self {
        self.html.button()
    }

    pub fn div(self) -> Self {
        self.html.div()
    }

    pub fn em(self) -> Self {
        self.html.em()
    }

    pub fn input(self) -> Self {
        self.html.input()
    }

    pub fn img(self) -> Self {
        self.html.img()
    }

    pub fn label(self) -> Self {
        self.html.label()
    }

    pub fn li(self) -> Self {
        self.html.li()
    }

    pub fn meter(self) -> Self {
        self.html.meter()
    }

    pub fn p(self) -> Self {
        self.html.p()
    }

    pub fn ol(self) -> Self {
        self.html.ol()
    }

    pub fn option(self) -> Self {
        self.html.option()
    }

    pub fn select(self) -> Self {
        self.html.select()
    }

    pub fn span(self) -> Self {
        self.html.span()
    }

    pub fn textarea(self) -> Self {
        self.html.textarea()
    }
}

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn html() {
        let mut html = Html::new();
        html.div();
        assert_eq!(html.build(), String::from("<div></div>"));
        let mut html = Html::new();
        html.div().id("test").attr_bool("spellcheck");
        assert_eq!(
            html.build(),
            String::from("<div id=\"test\" spellcheck></div>")
        );
        let mut html = Html::new();
        html.p().text("This is a paragraph");
        assert_eq!(html.build(), String::from("<p>This is a paragraph</p>"));
        let mut html = Html::new();
        html.em().text("You & I");
        assert_eq!(html.build(), String::from("<em>You &amp; I</em>"));
        let mut html = Html::new();
        html.div().span().text("Test").end().raw("&quot;");
        assert_eq!(
            html.build(),
            String::from("<div><span>Test</span>&quot;</div>")
        );
    }

    #[test]
    fn ol() {
        let mut html = Html::new();
        html.ol();
        html.li().class("cat").text("nori").end();
        html.li().class("cat").text("chashu");
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
        html.div().input().type_("text").text("Stuff");
        assert_eq!(
            html.build(),
            String::from("<div><input type=\"text\">Stuff</div>")
        );
    }
}
