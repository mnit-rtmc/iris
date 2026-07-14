//
// Copyright (C) 2026  Minnesota Department of Transportation
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
use std::collections::HashMap;

use std::fmt::{Display, Error, Formatter};
use std::io::BufReader;

use xml::attribute::OwnedAttribute;
use xml::name::OwnedName;
use xml::reader::{EventReader, XmlEvent::*};
use xml::writer::{EventWriter, XmlEvent};

const XSD: &str = "http://www.w3.org/2001/XMLSchema";
const XSI: &str = "http://www.w3.org/2001/XMLSchema-instance";

/// Wrapper for EventWriter with some utility methods
pub struct XmlWriter<W> {
    writer: EventWriter<W>,
}

impl<W: std::io::Write> XmlWriter<W> {
    /// Create a new XmlWriter wrapping an EventWriter
    pub fn new(writer: EventWriter<W>) -> Self {
        XmlWriter { writer }
    }

    /// Write an XML event to the writer
    fn write(&mut self, event: XmlEvent) {
        if let Err(e) = self.writer.write(event) {
            panic!("Write error: {e}")
        }
    }

    /// Start a new element without an XML namespace
    /// Takes name and a list of attribute key/value pairs
    pub fn start_element(&mut self, name: &str, attr: &[(&str, &str)]) {
        self.start_element_ns(name, ("", ""), attr);
    }

    /// Start a new element with a default XML namespace (no prefix)
    /// Takes name, namespace, and a list of attribute key/value pairs
    pub fn start_element_default_ns(
        &mut self,
        name: &str,
        ns: &str,
        attr: &[(&str, &str)],
    ) {
        let mut event = XmlEvent::start_element(name);
        event = event.default_ns(ns);
        for (a, v) in attr {
            event = event.attr(*a, v);
        }
        self.write(event.into());
    }

    /// Start a new element with an XML namespace
    /// Takes name, namespace prefix and URI, and a list of attribute key/value pairs
    pub fn start_element_ns(
        &mut self,
        name: &str,
        ns: (&str, &str),
        attr: &[(&str, &str)],
    ) {
        let mut event = XmlEvent::start_element(name);
        event = event.ns(ns.0, ns.1);
        for (a, v) in attr {
            event = event.attr(*a, v);
        }
        self.write(event.into());
    }

    /// Start a SOAP Body element
    pub fn start_body(&mut self) {
        self.write(
            XmlEvent::start_element("s:Body")
                .ns("xsd", XSD)
                .ns("xsi", XSI)
                .into(),
        );
    }

    /// End the most recent XML element started
    /// If name specified, also verifies it's the right name
    pub fn end_element(&mut self, name: Option<&str>) {
        let mut event = XmlEvent::end_element();
        if let Some(name) = name {
            event = event.name(name);
        }
        self.write(event.into());
    }

    /// End all XML elements started
    pub fn finish(&mut self) {
        loop {
            let event = XmlEvent::end_element();
            match self.writer.write(event) {
                Ok(_) => (),
                Err(_) => return,
            }
        }
    }

    /// Add an XML characters/text node with given value
    pub fn characters(&mut self, chars: &str) {
        let event = XmlEvent::Characters(chars);
        self.write(event);
    }

    /// Add an XML element with no child elements
    /// `chars` specifies any inner text
    pub fn single_element(
        &mut self,
        name: &str,
        chars: &str,
        attr: &[(&str, &str)],
    ) {
        self.single_element_ns(name, ("", ""), chars, attr);
    }

    /// Add an element with no children, with a default namespace (no prefix)
    /// Takes name, namespace, text/characters, and a list of attribute pairs
    pub fn single_element_default_ns(
        &mut self,
        name: &str,
        ns: &str,
        chars: &str,
        attr: &[(&str, &str)],
    ) {
        self.start_element_default_ns(name, ns, attr);
        self.characters(chars);
        self.end_element(Some(name));
    }

    /// Add an element with no children, with a namespace prefix
    /// Takes name, namespace prefix and URI, text/characters, and a list of attribute pairs
    pub fn single_element_ns(
        &mut self,
        name: &str,
        ns: (&str, &str),
        chars: &str,
        attr: &[(&str, &str)],
    ) {
        self.start_element_ns(name, ns, attr);
        self.characters(chars);
        self.end_element(Some(name));
    }
}

impl Default for Element {
    fn default() -> Self {
        Self {
            name: OwnedName {
                local_name: Default::default(),
                namespace: Default::default(),
                prefix: Default::default(),
            },
            index: Default::default(),
            attributes: Default::default(),
            parent: Default::default(),
            children: Default::default(),
            text: Default::default(),
            other: Default::default(),
        }
    }
}

/// XML element representation
#[derive(Clone, Debug)]
pub struct Element {
    name: OwnedName,
    index: usize,
    attributes: HashMap<String, String>,
    /// Index of parent element
    parent: usize,
    /// Indices of child elements
    children: Vec<usize>,
    /// Vec of text nodes
    text: Vec<String>,
    /// Vec of unused node types
    other: Vec<String>,
}

impl Element {
    /// Get an attribute of the element
    pub fn get(&self, attr: &str) -> Option<String> {
        self.attributes.get(attr).cloned()
    }

    /// Get the local name of the element
    pub fn get_local_name(&self) -> String {
        self.name.local_name.clone()
    }
}

impl Display for Element {
    fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), Error> {
        let mut out = String::new();

        out.push_str(&format!(
            "Element {} local name: {}\n",
            self.index, self.name.local_name
        ));
        out.push_str(&format!(
            "Parent, children: {}, {:?}\n",
            self.parent, self.children
        ));
        if !self.attributes.is_empty() {
            out.push_str(&format!("Attributes: {:#?}\n", self.attributes));
        }
        if !self.text.is_empty() {
            out.push_str(&format!("Text: {:?}\n", self.text));
        }

        write!(f, "{}", out)
    }
}

/// An XML DOM representation, containing a list of all elements
/// Uses a Vec to avoid pointers between elements
#[derive(Clone, Debug)]
pub struct XmlDocument {
    elements: Vec<Element>,
}

/// Format each element line by line
impl Display for XmlDocument {
    fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), Error> {
        for elem in &self.elements {
            writeln!(f, "{}", elem)?;
        }
        Ok(())
    }
}

impl XmlDocument {
    /// Create a new XmlDocument with root element
    pub fn new(elem: Element) -> Self {
        Self {
            elements: vec![elem],
        }
    }

    /// Add a child element to the document
    /// Updates the parent's list of children
    /// Returns the index of the new child
    pub fn add_child(&mut self, child: Element) -> usize {
        let i = self.elements.len();
        let parent_idx = child.parent;
        self.elements.push(child);
        if i > 0 {
            self.elements.get_mut(parent_idx).unwrap().children.push(i);
        }
        i
    }

    /// Return the parent of specified element
    pub fn get_parent_element(&self, child: &Element) -> &Element {
        let idx = child.parent;
        &self.elements[idx]
    }

    /// Return a copy of the list of child elements of specified element
    pub fn get_child_elements(&self, elem: &Element) -> Vec<&Element> {
        let mut vec = vec![];
        for c in &elem.children {
            vec.push(&self.elements[*c]);
        }
        vec
    }

    /// Get the text of an Element
    pub fn get_text(&self, elem: &Element) -> String {
        let mut text = String::new();
        for s in &elem.text {
            if !text.is_empty() {
                text.push_str(" :: ");
            }
            text.push_str(s);
        }
        text
    }

    /// Iterate through all elements, return first match
    pub fn find(&self, name: &str) -> Option<&Element> {
        self.elements
            .iter()
            .find(|&elem| elem.get_local_name() == name)
    }

    /// Iterate through all elements, return all matches
    pub fn find_all(&self, name: &str) -> Vec<&Element> {
        let mut vec = vec![];
        for elem in &self.elements {
            if elem.get_local_name() == name {
                vec.push(elem);
            }
        }
        vec
    }
}

/// Convert attributes to map of name: value pairs
fn map_attrs(attrs: Vec<OwnedAttribute>) -> HashMap<String, String> {
    attrs
        .into_iter()
        .map(|a| (a.name.local_name, a.value))
        .collect()
}

/// Consumes an event reader and returns the XML DOM representation
impl From<EventReader<BufReader<&[u8]>>> for XmlDocument {
    fn from(reader: EventReader<BufReader<&[u8]>>) -> Self {
        let mut first = true;

        let mut doc = XmlDocument::new(Element::default());
        let mut current = 0;

        for event in reader {
            if let Err(e) = event {
                log::error!("XML EventReader error: {e:?}");
                continue;
            }
            let ev = event.unwrap();
            match ev {
                StartElement {
                    name, attributes, ..
                } => {
                    if first {
                        doc.elements[0].name = name;
                        doc.elements[0].attributes = map_attrs(attributes);
                        first = false;
                    } else {
                        let el = Element {
                            name,
                            index: doc.elements.len(),
                            attributes: map_attrs(attributes),
                            parent: current,
                            ..Element::default()
                        };
                        current = doc.add_child(el);
                    }
                }
                EndElement { name } => {
                    if name.local_name == doc.elements[current].get_local_name()
                    {
                        current = doc.elements[current].parent;
                    } else {
                        log::error!("EndElement name doesn't match!");
                        log::debug!("Ignoring any further events...");
                        break;
                    }
                }
                Characters(text) => doc.elements[current].text.push(text),
                CData(s)
                | Comment(s)
                | Whitespace(s)
                | Doctype { syntax: s } => {
                    doc.elements[current].other.push(s);
                }
                // Ignore StartDocument, EndDocument, ProcessingInstruction
                _ => (),
            }
        }

        doc
    }
}

/// Wraps the bytes in a BufReader, then reads as XML document
impl From<&[u8]> for XmlDocument {
    fn from(source: &[u8]) -> Self {
        let buf = BufReader::new(source);
        let reader = EventReader::new(buf);
        reader.into()
    }
}
