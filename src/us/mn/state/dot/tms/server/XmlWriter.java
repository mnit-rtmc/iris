/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * A simple class for writing out XML documents
 *
 * @author Douglas Lau
 */
abstract public class XmlWriter {

	/** XML version and encoding declaration */
	static protected final String XML_DECLARATION =
		"<?xml version='1.0' encoding='UTF-8'?>";

	/** Validate an xml element name */
	static public String validateElementName(String e) {
		e = e.replace("&", "");
		e = e.replace("<", "");
		e = e.replace(">", "");
		return e;
	}

	/** Validate an xml element value */
	static public String validateElementValue(String v) {
		v = v.replace("&", "&amp;");
		v = v.replace("<", "&lt;");
		v = v.replace(">", "&gt;");
		return v;
	}

	/** Create an xml attribute */
	static public String createAttribute(String name, Object value) {
		StringBuffer sb = new StringBuffer(" ");
		sb.append(validateElementName(name));
		sb.append("='");
		sb.append(validateElementValue(value.toString()));
		sb.append("'");
		return sb.toString();
	}

	/** File to write final XML data */
	protected final File file;

	/** Temporary file to write XML data */
	protected final File temp;

	/** Should the XML data be compressed? */
	protected final boolean gzip;

	/** Create a new XML writer */
	public XmlWriter(String f, boolean gz) {
		if(gz)
			f = f + ".gz";
		file = new File(
			SystemAttrEnum.XML_OUTPUT_DIRECTORY.getString(), f);
		temp = new File(file.getAbsolutePath() + "~");
		gzip = gz;
	}

	/** Create the underlying output stream */
	protected OutputStream createOutputStream() throws IOException {
		OutputStream os = new FileOutputStream(temp);
		if(gzip)
			return new GZIPOutputStream(os);
		else
			return os;
	}

	/** Write the XML file */
	public void write() throws IOException {
		PrintWriter out = new PrintWriter(createOutputStream());
		try {
			print(out);
		}
		finally {
			out.close();
		}
		if(!temp.renameTo(file))
			throw new IOException("rename failed: " + file);
	}

	/** Print the XML to a print writer */
	abstract public void print(PrintWriter out);
}
