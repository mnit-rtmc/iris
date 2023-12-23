/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2023  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.server.comm.onvifptz;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.Node;
import org.w3c.dom.Document;

import org.xml.sax.InputSource;

/**
 * Class for XML document utilities
 *
 * @author Ethan Beauclaire
 */
public class DOMUtils {
	/** don't allow instantiation */
	private DOMUtils() {}

	/** Converts the Node to a string and returns it */
	public static String getString(Node n) {
		if (n == null) return null;

		try {
			StringWriter w = new StringWriter();

			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.transform(new DOMSource(n), new StreamResult(w));

			return w.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** Gets Document from string */
	public static Document getDocument(String s) {
		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(s));
			doc = db.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return doc;
	}
}
