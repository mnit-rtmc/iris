/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
 * Copyright (C) 2008-2010  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.dmsxml;

import java.io.IOException;
import junit.framework.TestCase;

public class XmlTest extends TestCase {

	public void test1() {
		try {
			Pair[] p = Xml.parseTagAndChildren("tn",
				"<tn><a>a</a><b>b</b></tn>");
			assertTrue(p.length == 2);
			assertTrue(((String)p[0].car()).compareTo("a") == 0);
			assertTrue(((String)p[0].cdr()).compareTo("a") == 0);
			assertTrue(((String)p[1].car()).compareTo("b") == 0);
			assertTrue(((String)p[1].cdr()).compareTo("b") == 0);
		}
		catch (IOException ex) {
			assertTrue(false);
		}
	}

	public void test2() {
		try {
			Pair[] p = Xml.parseTagsAndChildren("top", "tn",
				"<top><tn><a>a</a><b>b</b></tn></top>");
			assertTrue(p.length == 2);
			assertTrue(((String)p[0].car()).compareTo("a") == 0);
			assertTrue(((String)p[0].cdr()).compareTo("a") == 0);
			assertTrue(((String)p[1].car()).compareTo("b") == 0);
			assertTrue(((String)p[1].cdr()).compareTo("b") == 0);
		}
		catch (IOException ex) {
			assertTrue(false);
		}
        }

	public void test3() {
		try {
			Pair[] p = Xml.parseTagAndChildren("tnx",
				"<tn><a>a</a><b>b</b></tn>");
			assertTrue(false);
		}
		catch (IOException ex) {
			// exception expected
		}
	}

	public void test4() {
		try {
			String tag = Xml.readSecondTagName("tag1",
				"<tag1><tag2>value</tag2></tag1>");
			assertTrue(tag.compareTo("tag2") == 0);
		}
		catch (IOException ex) {
			assertTrue(false);
		}
	}

	public void test5() {
		try {
			String tag = Xml.readSecondTagName("tag1",
				"<tag1><tag2>value & value</tag2></tag1>");
			assertTrue(false);
		}
		catch (IOException ex) {
			// exception expected
		}
	}

	public void test6() {
		try {
			Pair[] p = Xml.parseTagsAndChildren("DmsXml",
				"SetInitRespMsg",
				"<DmsXml><SetTimeRespMsg><IsValid>false</IsValid><ErrMsg>SignView response invalid code (0x0D)</ErrMsg></SetTimeRespMsg></DmsXml>");
			assertTrue(false);
		}
		catch (IOException ex) {
			// exception expected
		}
	}

	public void test7() {
		StringBuilder x = new StringBuilder();
		x = Xml.addXmlDocHeader(x);
		x = Xml.addXmlTag(x, "name1", "value1");
		x = Xml.addXmlTag(x, "name2<>&",
			"value2 & value 3 < value 4 > value5");
		assertTrue(x.toString().equals(
			"<?xml version=\"1.0\"?><name1>value1</name1><name2>value2 &amp; value 3 &lt; value 4 &gt; value5</name2>"));
	}
}
