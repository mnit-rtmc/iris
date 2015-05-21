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
import static us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller.LOG;


/**
 * Convenience class for writing Xml.
 *
 * @author      Michael Darter
 */
final class Xml {

    /** Constructor */
    private Xml() {}

    /** append the Xml doc start tag */
    static StringBuilder addXmlDocHeader(StringBuilder doc) {
        doc.append("<?xml version=\"1.0\"?>");
        return doc;
    }

    /** append the xml tag containing a value */
    static StringBuilder addXmlTag(StringBuilder doc, String argname, String argvalue) {
	String name=Xml.validateElementName(argname);
	String value=Xml.validateElementValue(argvalue);
        return Xml.addXmlTag(doc, name, new StringBuilder(value));
    }

    /** append the xml tag containing a value */
    static StringBuilder addXmlTag(StringBuilder doc, String name, int value) {
        return Xml.addXmlTag(doc, name, new StringBuilder(Integer.toString(value)));
    }

    /** append the xml tag containing a value */
    static StringBuilder addXmlTag(StringBuilder doc, String name, boolean value) {
        return Xml.addXmlTag(doc, name, new StringBuilder(Boolean.toString(value)));
    }

    /** given an xml element name return it validated */
    static String validateElementName(String e) {
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
    static String validateElementValue(String v) {
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
    static StringBuilder addXmlTag(StringBuilder doc, String name, StringBuilder value) {

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
    static StringBuilder addXmlComment(StringBuilder doc, String comment) {
        if (comment.length() > 0) {
            doc.append("<!-- ").append(comment).append(" -->");
        }

        return (doc);
    }

    /** append the Xml tag open */
    static StringBuilder addXmlTagOpen(StringBuilder doc, String tagname) {
        doc.append("<").append(tagname).append(">");

        return (doc);
    }

    /** append the Xml tag close */
    static StringBuilder addXmlTagClose(StringBuilder doc, String tagname) {
        doc.append("</").append(tagname).append(">");

        return (doc);
    }

    /** append an empty Xml tag */
    static StringBuilder addXmlEmptyTag(StringBuilder doc, String tagname) {
        doc.append("<").append(tagname).append("/>");

        return (doc);
    }

    /** return a child with the specified name */
    static Element getChild(Element e, String tagname) {
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
    static Element getChild(Element e) {
        NodeList children = e.getChildNodes();
        Node     c        = children.item(0);

        return ((Element) c);
    }

    /**
     *  Parse an xml string in this form: <top><tag><child1></child1><child2></child2>...</tag></top>
     *  @throws IOException if xml is malformed.
     *  @returns An array of Pair objects containing (String,String) which are the name and value.
     */
    static Pair[] parseTagsAndChildren(String lev1name, String lev2name, String xml) throws IOException {

        // check args
        if ((xml == null) || (lev1name == null) || (lev2name == null)) {
            throw new IllegalArgumentException("invalid args in Xml.parseRes()");
        }

        //LOG.log("lev1name=" + lev1name + ", lev2name=" + lev2name + ", xml=" + xml);

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

                //LOG.log("1st level (" + lev1name + ") num children=" + list.getLength());
                topelem1 = (Element) list.item(0);
            }

            // get 2nd level element
            Element topelem2 = Xml.getChild(topelem1, lev2name);

            if (topelem2 == null) {
                throw new IOException("Didn't find tag:" + lev2name + " in xml:" + xml);
            }

            // cycle through children, adding to container
            NodeList children = topelem2.getChildNodes();

            //LOG.log("2nd level (" + lev2name + ") num children=" + children.getLength());
            ret = new Pair[children.getLength()];

            for (int c = 0; c < children.getLength(); c++) {
                Node   child = children.item(c);
                String n     = child.getNodeName();
                String v     = child.getTextContent();

                // create pair
                //LOG.log("name=" + n + "," + "value=" + v);

                Pair p = new Pair(n, v);

                ret[c] = p;
            }
        } catch (ParserConfigurationException ex) {
            LOG.log("Exception:" + ex);
            throw new IOException(ex.toString());
        } catch (SAXException ex) {
            LOG.log("Exception:" + ex);
            throw new IOException(ex.toString());
        } catch (IOException ex) {
            LOG.log("Exception:" + ex);
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
    static Pair[] parseTagAndChildren(String tagname, String xml) throws IOException {

        // check args
        if ((xml == null) || (tagname == null)) {
            throw new IllegalArgumentException("invalid args in Xml.parseTagAndChildren()");
        }

        //LOG.log("tagname=" + tagname + ", xml=" + xml);

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
                //LOG.log("tagname=" + tagname + ", num children=" + list.getLength());
            }

            // cycle through children, adding to container
            NodeList children = topelem.getChildNodes();

            //LOG.log("2nd level num children=" + children.getLength());
            ret = new Pair[children.getLength()];

            for (int c = 0; c < children.getLength(); c++) {
                Node   child = children.item(c);
                String n     = child.getNodeName();
                String v     = child.getTextContent();

                // create pair
                //LOG.log("name=" + n + "," + "value=" + v);

                Pair p = new Pair(n, v);

                ret[c] = p;
            }
        } catch (ParserConfigurationException ex) {
            LOG.log("Exception:" + ex);
            throw new IOException(ex.toString());
        } catch (SAXException ex) {
            LOG.log("Exception:" + ex);
            throw new IOException(ex.toString());
        } catch (IOException ex) {
            LOG.log("Exception:" + ex);
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
    static String readSecondTagName(String lev1name, String xml) throws IOException {

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
            LOG.log("Exception:" + ex);
            throw new IOException(ex.toString());
        } catch (SAXException ex) {
            LOG.log("Exception:" + ex);
            throw new IOException(ex.toString());
        } catch (IOException ex) {
            LOG.log("Exception:" + ex);
            throw new IOException(ex.toString());
        }

        return (ret);
    }
}
