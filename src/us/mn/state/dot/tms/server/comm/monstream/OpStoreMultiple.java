/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.monstream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * MonStream operation to store multiple properties.
 *
 * @author Douglas Lau
 */
public class OpStoreMultiple extends OpStep {

	/** List of properties to store */
	private final ArrayList<MonProp> props;

	/** Index of property in list */
	private int n_prop;

	/** Create a new store multiple operation */
	public OpStoreMultiple(ArrayList<MonProp> p) {
		props = p;
		n_prop = 0;
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		if (n_prop < props.size()) {
			MonProp p = props.get(n_prop);
			p.encodeStore(op, tx_buf);
			n_prop++;
		}
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return (n_prop < props.size()) ? this : null;
	}
}
