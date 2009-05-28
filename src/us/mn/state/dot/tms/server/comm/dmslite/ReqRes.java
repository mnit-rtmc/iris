/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dmslite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A message request and response container. The response may contain multiple tags.
 * The request contains a single tag (name and value).
 *
 * @author      Michael Darter
 * @version     1.0, 03/19/08
 * @since       1.0
 * @see
 */
public class ReqRes {

    	// fields
	private String   m_reqname;
	private String   m_reqval;
	private String[] m_resnames;
	private String[] m_resvals;

	/**
	 * Constructor for a request that contains no associated responses.
	 *
	 * @param reqname Name of the request field.
	 * @param reqval Value of the request field.
	 */
	ReqRes(String reqname, String reqval) {

		String[] resnames = new String[0];

		// check args
		if((reqname == null) || (reqval == null)
			|| (resnames == null)) {
			throw new IllegalArgumentException(
			    "invalid args in ReqRes constructor.");
		}

		m_reqname = reqname;
		m_reqval = reqval;
		m_resnames = resnames;
		m_resvals = new String[resnames.length];

		for(int i = 0; i < resnames.length; ++i) {
			m_resvals[i] = "";
		}

	}

	/**
	 * Constructor for a request that contains associated responses.
	 *
	 * @param reqname Name of the request field.
	 * @param reqval Value of the request field.
	 * @param resnames Names of all associated response fields (may be zero length if none).
	 */
	ReqRes(String reqname, String reqval, String[] resnames) {

		// check args
		if((reqname == null) || (reqval == null)
			|| (resnames == null)) {
			throw new IllegalArgumentException(
			    "invalid args in ReqRes constructor.");
		}

		m_reqname = reqname;
		m_reqval = reqval;
		m_resnames = resnames;
		m_resvals = new String[resnames.length];

		for(int i = 0; i < resnames.length; ++i) {
			m_resvals[i] = "";
		}
	}

    /** get request name */
    public String getReqName() {
        return (m_reqname);
    }

    /** get request value */
    public String getReqVal() {
        return (m_reqval);
    }

	/**
	 *  search for a value in the request and response fields.
	 *  @param name Name of request or response to search for.
	 *  @return null if not found else the value found.
	 */
	public String searchReqResVal(String name) {
		if(name == null)
			return null;

		// request
		if(m_reqname!=null)
			if(m_reqname.equals(name))
				return m_reqval;
		// response
		String ret=null;
		try {
			ret=getResVal(name);
		} catch(IllegalArgumentException ex) {
			ret=null;
		}
		return ret;
	}

    /**
     *  Get a response value for the specified name
     * 
     *  @return Cooresponding response value or null if not found.
     */
    public String getResVal(String resname) throws IllegalArgumentException {

        // check arg
        if (resname == null) {
            throw new IllegalArgumentException("invalid args in ReqRes.getResVal()");
        }

        for (int i = 0; i < m_resnames.length; ++i) {
            if (m_resnames[i].compareTo(resname) == 0) {
                return (m_resvals[i]);
            }
        }

        // resname not found
        throw new IllegalArgumentException("Didn't find tag (" + resname + ") in ReqRes.getResVal(), reqname="
                                           + m_reqname + ",reqval=" + m_reqval + ".");
    }

    /**
     *  Set a response value for the specified name
     * 
     *  @return void
     */
    public void setResVal(String resname, String resval) {

        // check args
        if ((resname == null) || (resval == null)) {
            throw new IllegalArgumentException("invalid args in ReqRes.setResVal()");
        }

        for (int i = 0; i < m_resnames.length; ++i) {
            if (m_resnames[i].compareTo(resname) == 0) {
                m_resvals[i] = resval;

                return;
            }
        }
    }

	/** toString */
	public String toString() {
		if(m_resnames==null)
			return "";
		String ret="ReqReq(";
		ret += "Request name="+m_reqname;
		ret += ", Request value="+m_reqval;
		ret += ", Response names=";
		for (int i = 0; i < m_resnames.length; ++i) {
			if (m_resnames[i]!=null)
				ret += ", ["+i+"]="+m_resnames[i];
		}
		ret += ", Response values=";
		for (int i = 0; i < m_resnames.length; ++i) {
			if (m_resvals[i]!=null)
				ret += ", ["+i+"]="+m_resvals[i];
		}
		ret += ")";
		return ret;
	}


    /**
     *  Parse an xml response and update response fields.
     */
    public void parseRes(String lev1name, String lev2name, String xml) throws IOException {

        // parse xml
        Pair[] p;

        try {
            p = Xml.parseTagsAndChildren(lev1name, lev2name, xml);
        } catch (IOException ex) {
            throw ex;
        }

        // add tags to response
        for (Pair i : p) {
            String n = (String) (i.car());
            String v = (String) (i.cdr());

            this.setResVal(n, v);
        }

        return;
    }

    /** test this class FIXME: move these test cases to junit */
    public static boolean test() {
        boolean ok = true;
        {
            System.err.println("Test case 1");

            ReqRes x = new ReqRes("tag", "value", new String[] { "t1", "t2" });

            ok = ok && (x.getReqName().compareTo("tag") == 0);
            ok = ok && (x.getReqVal().compareTo("value") == 0);

            try {
                ok = ok && (x.getResVal("t1").compareTo("") == 0);
                x.setResVal("t1", "1");
                x.setResVal("t2", "2");
                ok = ok && (x.getResVal("t1").compareTo("1") == 0);
                ok = ok && (x.getResVal("t2").compareTo("2") == 0);
            } catch (IllegalArgumentException ex) {
                ok = false;
            }

            // try unknown arg
            try {
                x.getResVal("xxx");
                ok = false;
            } catch (IllegalArgumentException ex) {
                ok = ok && true;
            }
        }
        {
            System.err.println("Test case 2");

            ReqRes x = new ReqRes("tag", "value", new String[] { "a", "b" });

            ok = ok && (x.getReqName().compareTo("tag") == 0);
            ok = ok && (x.getReqVal().compareTo("value") == 0);

            try {
                x.parseRes("top", "msg", "<top><msg><a>1</a><b>2</b></msg></top>");
            } catch (IOException ex) {
                ok = false;
            }

            ok = ok && (x.getResVal("a").compareTo("1") == 0);
            ok = ok && (x.getResVal("b").compareTo("2") == 0);
        }
        System.err.println("test() results: " + ok);

        return (ok);
    }
}
