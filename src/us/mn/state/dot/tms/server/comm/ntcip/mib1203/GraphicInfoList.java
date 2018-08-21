/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import java.io.DataOutputStream;
import java.io.IOException;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.server.comm.ntcip.CRCStream;
import us.mn.state.dot.tms.utils.Base64;

/**
 * GraphicInfoList is used to calculate a GraphicID.  It is encoded
 * using OER (NTCIP 1102).
 *
 * @author Douglas Lau
 */
public class GraphicInfoList extends CRCStream {

	/** Create a new GraphicInfoList */
	public GraphicInfoList(Graphic graphic) throws IOException {
		DataOutputStream dos = new DataOutputStream(this);
		dos.writeByte(graphic.getGNumber());
		dos.writeShort(graphic.getHeight());
		dos.writeShort(graphic.getWidth());
		dos.writeByte(graphic.getColorScheme());
		Integer tc = graphic.getTransparentColor();
		dos.writeByte((tc != null) ? 1 : 0);
		int c = (tc != null) ? tc : 0;
		dos.writeByte(c >> 16);	// transparent color red
		dos.writeByte(c >> 8);	// transparent color green
		dos.writeByte(c >> 0);	// transparent color blue
		dos.write(Base64.decode(graphic.getPixels()));
	}
}
