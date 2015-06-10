/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2015  Minnesota Department of Transportation
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
		dos.writeByte(DmsColorScheme.fromBpp(graphic.getBpp())
			.ordinal());
		dos.writeByte(1);	// transparency enabled
		dos.writeByte(0);	// red component of transparent color
		dos.writeByte(0);	// green component of transparent color
		dos.writeByte(0);	// blue component of transparent color
		dos.write(Base64.decode(graphic.getPixels()));
	}
}
