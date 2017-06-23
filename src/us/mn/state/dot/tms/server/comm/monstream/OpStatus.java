/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.VideoMonitorImpl;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * MonStream operation to receive monitor status.
 *
 * @author Douglas Lau
 */
public class OpStatus extends OpStep {

	/** ASCII record separator */
	static private final String RECORD_SEP =
		String.valueOf(MonProp.RECORD_SEP);

	/** ASCII unit separator */
	static private final String UNIT_SEP =
		String.valueOf(MonProp.UNIT_SEP);

	/** Buffer to parse received data */
	private final byte[] buf = new byte[1024];

	/** Create a new status op */
	public OpStatus() {
		setPolling(false);
	}

	/** Set polling */
	@Override
	public void setPolling(boolean p) {
		// No polling -- just wait for status messages
		super.setPolling(false);
	}

	/** Parse data received from controller */
	@Override
	public void recv(Operation op, ByteBuffer rx_buf) throws IOException {
		doRecv(op, rx_buf);
	}

	/** Parse received data */
	private void doRecv(Operation op, ByteBuffer rx_buf) throws IOException{
		ControllerImpl ctrl = op.getController();
		int len = rx_buf.remaining();
		rx_buf.get(buf, 0, len);
		doRecv(ctrl, new String(buf, 0, len, "UTF8"));
	}

	/** Parse received messages */
	private void doRecv(ControllerImpl ctrl, String msgs)throws IOException{
		for (String msg : msgs.split(RECORD_SEP)) {
			String[] par = msg.split(UNIT_SEP);
			String cod = (par.length > 0) ? par[0] : "";
			String mon = (par.length > 1) ? par[1] : "";
			String cam = (par.length > 2) ? par[2] : "";
			String stat = (par.length > 3) ? par[3] : "";
			if ("status".equals(cod))
				parseStatus(ctrl, mon, cam, stat);
			else if (cod.length() > 0)
				throw new ParsingException("INVALID MSG");
		}
	}

	/** Parse status message */
	private void parseStatus(ControllerImpl ctrl, String mon, String cam,
		String stat) throws IOException
	{
		try {
			int pin = Integer.parseInt(mon) + 1;
			ControllerIO cio = ctrl.getIO(pin);
			if (cio instanceof VideoMonitorImpl)
				parseStatus((VideoMonitorImpl) cio, cam, stat);
			else
				throw new ParsingException("INVALID PIN: "+pin);
		}
		catch (NumberFormatException e) {
			throw new ParsingException("INVALID MON NUM: " + mon);
		}
	}

	/** Parse video monitor status */
	private void parseStatus(VideoMonitorImpl vm, String cam, String stat)
		throws IOException
	{
		Camera c = CameraHelper.find(cam);
		if (c instanceof CameraImpl)
			parseStatus(vm, (CameraImpl) c, stat);
		else
			throw new ParsingException("INVALID CAM: " + cam);
	}

	/** Parse video monitor status */
	private void parseStatus(VideoMonitorImpl vm, CameraImpl c, String stat)
		throws IOException
	{
		vm.setCameraNotify(c, "MONSTREAM", false);
		c.setVideoLossNotify(stat.length() > 0);
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return this;
	}
}
