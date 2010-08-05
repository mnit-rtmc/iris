/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.Pair;

/**
 * Convenience class for writing Xml.
 *
 * @author      Michael Darter
 * @version     1.0, 03/12/08
 * @since       1.0
 * @see
 */
final public class Xml {

    /** Constructor */
    private Xml() {}

    /** test */
    public static boolean test() {
        boolean ok = true;

	//FIXME: move test cases to junit
        System.err.println("Test case 1");
        {
            try {
                Pair[] p = Xml.parseTagAndChildren("tn", "<tn><a>a</a><b>b</b></tn>");

                ok = ok && (p.length == 2);
                ok = ok && ((String) (p[0].car())).compareTo("a") == 0;
                ok = ok && ((String) (p[0].cdr())).compareTo("a") == 0;
                ok = ok && ((String) (p[1].car())).compareTo("b") == 0;
                ok = ok && ((String) (p[1].cdr())).compareTo("b") == 0;
            } catch (IOException ex) {
                ok = false;
                //Log.fine("Exeption=" + ex);
            }
        }

        System.err.println("Test case 2");
        {
            try {
                Pair[] p = Xml.parseTagsAndChildren("top", "tn", "<top><tn><a>a</a><b>b</b></tn></top>");

                ok = ok && (p.length == 2);
                ok = ok && ((String) (p[0].car())).compareTo("a") == 0;
                ok = ok && ((String) (p[0].cdr())).compareTo("a") == 0;
                ok = ok && ((String) (p[1].car())).compareTo("b") == 0;
                ok = ok && ((String) (p[1].cdr())).compareTo("b") == 0;
            } catch (IOException ex) {
                ok = false;
                //Log.fine("Exeption=" + ex);
            }
        }

        System.err.println("Test case 3: bogus expected tag name");
        {
            try {
                Pair[] p = Xml.parseTagAndChildren("tnx", "<tn><a>a</a><b>b</b></tn>");
                ok = false;
            } catch (IOException ex) {
                ok = ok && true;
                //Log.fine("Expected exeption=" + ex);
            }
        }

        System.err.println("Test case 4");
        {
            try {
                String tag = Xml.readSecondTagName("tag1", "<tag1><tag2>value</tag2></tag1>");
                ok = ok && (tag.compareTo("tag2") == 0);
            } catch (IOException ex) {
                ok = false;
            }
        }

        System.err.println("Test case 4A: bogus xml code that contains non-escaped & chars.");
        {
            try {
                String tag = Xml.readSecondTagName("tag1", "<tag1><tag2>value & value</tag2></tag1>");
                ok = false;
            } catch (IOException ex) {
                ok = true;
            }
        }

        System.err.println("Test case 5: bogus level 2 name");
        {
            try {
                Pair[] p = Xml.parseTagsAndChildren("DmsXml", "SetInitRespMsg","<DmsXml><SetTimeRespMsg><IsValid>false</IsValid><ErrMsg>SignView response invalid code (0x0D)</ErrMsg></SetTimeRespMsg></DmsXml>");
                ok = false;
            } catch (IOException ex) {
                ok = ok && true;
                //Log.fine("Expected exeption=" + ex);
            }
        }

        System.err.println("Test case 6: creating xml");
        {
		StringBuilder x=new StringBuilder();
		x=addXmlDocHeader(x);
		x=addXmlTag(x,"name1","value1");
		x=addXmlTag(x,"name2<>&","value2 & value 3 < value 4 > value5");
		String expected="<?xml version=\"1.0\"?><name1>value1</name1><name2>value2 &amp; value 3 &lt; value 4 &gt; value5</name2>";
		ok=ok && x.toString().equals(expected);
		if (!ok) {
			System.err.println(x);
			System.err.println(expected);
		}
        }

        System.err.println("test() results: " + ok);
        return (ok);
    }

    /** append the Xml doc start tag */
    public static StringBuilder addXmlDocHeader(StringBuilder doc) {
        doc.append("<?xml version=\"1.0\"?>");
        return doc;
    }

    /** append the xml tag containing a value */
    public static StringBuilder addXmlTag(StringBuilder doc, String argname, String argvalue) {
	String name=Xml.validateElementName(argname);
	String value=Xml.validateElementValue(argvalue);
        return Xml.addXmlTag(doc, name, new StringBuilder(value));
    }

    /** append the xml tag containing a value */
    public static StringBuilder addXmlTag(StringBuilder doc, String name, int value) {
        return Xml.addXmlTag(doc, name, new StringBuilder(Integer.toString(value)));
    }

    /** append the xml tag containing a value */
    public static StringBuilder addXmlTag(StringBuilder doc, String name, boolean value) {
        return Xml.addXmlTag(doc, name, new StringBuilder(Boolean.toString(value)));
    }

    /** given an xml element name return it validated */
    public static String validateElementName(String e) {
	if (e==null || e.length()<=0)
		return "";
	e=e.replace("&","");
	e=e.replace("<","");
	e=e.replace(">","");
        return e;
    }

    /** 
     *  given an xml element value, return it validated.
     */
    public static String validateElementValue(String v) {
	if (v==null || v.length()<=0)
		return "";
	v=v.replace("&","&amp;");
	v=v.replace("<","&lt;");
	v=v.replace(">","&gt;");
        return v;
    }


    /**
     * The root addXmlTag method, which appends the xml tag containing a value
     * with the specified name.
     *
     * @returns String in the form: <name>value</name>
     */
    public static StringBuilder addXmlTag(StringBuilder doc, String name, StringBuilder value) {

        // sanity check
        assert name != null : "Argument name is null in Xml.addXmlTag()";
        assert value != null : "Argument vlaue is null in Xml.addXmlTag()";

	// note: addXmlEmptyTag() is not called below to facilitate
	// testing and valdation via python scripts. mtod 05/16/08.

        //if (value.length() == 0) {
        //    Xml.addXmlEmptyTag(doc, name);
        //} else {
            Xml.addXmlTagOpen(doc, name);
            doc.append(value);
            Xml.addXmlTagClose(doc, name);
        //}

        return (doc);
    }

    /** append an xml comment tag */
    public static StringBuilder addXmlComment(StringBuilder doc, String comment) {
        if (comment.length() > 0) {
            doc.append("<!-- ").append(comment).append(" -->");
        }

        return (doc);
    }

    /** append the Xml tag open */
    public static StringBuilder addXmlTagOpen(StringBuilder doc, String tagname) {
        doc.append("<").append(tagname).append(">");

        return (doc);
    }

    /** append the Xml tag close */
    public static StringBuilder addXmlTagClose(StringBuilder doc, String tagname) {
        doc.append("</").append(tagname).append(">");

        return (doc);
    }

    /** append an empty Xml tag */
    public static StringBuilder addXmlEmptyTag(StringBuilder doc, String tagname) {
        doc.append("<").append(tagname).append("/>");

        return (doc);
    }

    /** return a child with the specified name */
    public static Element getChild(Element e, String tagname) {
        NodeList children = e.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node c = children.item(i);

            if (c.getNodeName().compareTo(tagname) == 0) {
                return (Element) c;
            }
        }

        return (null);
    }

    /** return the first child */
    public static Element getChild(Element e) {
        NodeList children = e.getChildNodes();
        Node     c        = children.item(0);

        return ((Element) c);
    }

    /**
     *  Parse an xml string in this form: <top><tag><child1></child1><child2></child2>...</tag></top>
     *  @throws IOException if xml is malformed.
     *  @returns An array of Pair objects containing (String,String) which are the name and value.
     */
    static public Pair[] parseTagsAndChildren(String lev1name, String lev2name, String xml) throws IOException {

        // check args
        if ((xml == null) || (lev1name == null) || (lev2name == null)) {
            throw new IllegalArgumentException("invalid args in Xml.parseRes()");
        }

        //Log.finest("lev1name=" + lev1name + ", lev2name=" + lev2name + ", xml=" + xml);

        Pair[] ret = null;

        try {

            // open
            DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document        d = b.parse(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));

            // get top level element
            Element topelem1;

            {
                NodeList list = d.getElementsByTagName(lev1name);

                if (list.getLength() != 1) {
                    throw new IOException("Didn't find tag:" + lev1name + " in xml:" + xml);
                }

                //Log.finest("1st level (" + lev1name + ") num children=" + list.getLength());
                topelem1 = (Element) list.item(0);
            }

            // get 2nd level element
            Element topelem2 = Xml.getChild(topelem1, lev2name);

            if (topelem2 == null) {
                throw new IOException("Didn't find tag:" + lev2name + " in xml:" + xml);
            }

            // cycle through children, adding to container
            NodeList children = topelem2.getChildNodes();

            //Log.finest("2nd level (" + lev2name + ") num children=" + children.getLength());
            ret = new Pair[children.getLength()];

            for (int c = 0; c < children.getLength(); c++) {
                Node   child = children.item(c);
                String n     = child.getNodeName();
                String v     = child.getTextContent();

                // create pair
                //Log.finest("name=" + n + "," + "value=" + v);

                Pair p = new Pair(n, v);

                ret[c] = p;
            }
        } catch (ParserConfigurationException ex) {
            Log.finest("Exception:" + ex);
            throw new IOException(ex.toString());
        } catch (SAXException ex) {
            Log.finest("Exception:" + ex);
            throw new IOException(ex.toString());
        } catch (IOException ex) {
            Log.finest("Exception:" + ex);
            throw ex;
        }

	// postcond
        assert ret != null : "postcond: should not return null in "+
		"parseTagsAndChildren(), lev1name="+lev1name+
		",lev2name="+lev2name+",xml="+xml;

        return (ret);
    }

    /**
     *  Parse an xml string in this form: <tag><child1></child1><child2></child2>...</tag>
     *  @throws IOException if xml is malformed.
     *  @returns An array of Pair objects containing (String,String) which are the name and value.
     */
    static public Pair[] parseTagAndChildren(String tagname, String xml) throws IOException {

        // check args
        if ((xml == null) || (tagname == null)) {
            throw new IllegalArgumentException("invalid args in Xml.parseTagAndChildren()");
        }

        //Log.finest("tagname=" + tagname + ", xml=" + xml);

        Pair[] ret = null;

        try {

            // open
            DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document        d = b.parse(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));

            // get top level element
            Element topelem;

            {
                NodeList list = d.getElementsByTagName(tagname);

                if (list.getLength() != 1) {
                    throw new IOException("Wrong number of tags (" + tagname + "). Should be 1, was "
                                          + list.getLength());
                }

                topelem = (Element) list.item(0);
                //Log.finest("tagname=" + tagname + ", num children=" + list.getLength());
            }

            // cycle through children, adding to container
            NodeList children = topelem.getChildNodes();

            //Log.finest("2nd level num children=" + children.getLength());
            ret = new Pair[children.getLength()];

            for (int c = 0; c < children.getLength(); c++) {
                Node   child = children.item(c);
                String n     = child.getNodeName();
                String v     = child.getTextContent();

                // create pair
                //Log.finest("name=" + n + "," + "value=" + v);

                Pair p = new Pair(n, v);

                ret[c] = p;
            }
        } catch (ParserConfigurationException ex) {
            Log.finest("Exception:" + ex);
            throw new IOException(ex.toString());
        } catch (SAXException ex) {
            Log.finest("Exception:" + ex);
            throw new IOException(ex.toString());
        } catch (IOException ex) {
            Log.finest("Exception:" + ex);
            throw ex;
        }

	// postcond
        assert ret != null : "postcond: should not return null in "+
		"parseTagAndChildren(), tagname="+tagname+
		",xml="+xml;


        return (ret);
    }

    /**
     *  Return the name of the 2nd tag, e.g. for "<taga><tagb>...</tagb></taga>"
     *  the String "tagb" is returned.
     * 
     *  @returns the second tag name otherwise null if a syntax error occures.
     */
    static public String readSecondTagName(String lev1name, String xml) throws IOException {

        // check args
        if (xml == null) {
            throw new IllegalArgumentException("invalid args in Xml.readSecondTagName()");
        }

        String ret = null;

        try {

            // open
            DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document        d = b.parse(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));

            // get top level element
            Element topelem1;

            {
                NodeList list = d.getElementsByTagName(lev1name);

                if (list.getLength() != 1) {
                    return (null);
                }

                topelem1 = (Element) list.item(0);
            }

            // get 2nd level element
            Element topelem2 = Xml.getChild(topelem1);

            if (topelem2 == null) {
                return (null);
            }

            ret = topelem2.getNodeName();
        } catch (ParserConfigurationException ex) {
            Log.finest("Exception:" + ex);
            throw new IOException(ex.toString());
        } catch (SAXException ex) {
            Log.finest("Exception:" + ex);
            throw new IOException(ex.toString());
        } catch (IOException ex) {
            Log.finest("Exception:" + ex);
            throw new IOException(ex.toString());
        }

        return (ret);
    }
}
