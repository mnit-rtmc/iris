/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.PlayListHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.VideoMonitorHelper;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.VideoMonitorImpl;
import us.mn.state.dot.tms.server.comm.InvalidReqException;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * MonStream operation to receive monitor status.
 *
 * @author Douglas Lau
 */
public class OpStatus extends OpStep {

	/** Get the "blank" camera number */
	static private int cameraNumBlank() {
		return SystemAttrEnum.CAMERA_NUM_BLANK.getInt();
	}

	/** ASCII record separator */
	static private final String RECORD_SEP =
		String.valueOf(MonProp.RECORD_SEP);

	/** ASCII unit separator */
	static private final String UNIT_SEP =
		String.valueOf(MonProp.UNIT_SEP);

	/** Parse video monitor */
	static private VideoMonitorImpl parseMon(String mon)
		throws InvalidReqException
	{
		VideoMonitor vm = VideoMonitorHelper.findUID(mon);
		if (vm instanceof VideoMonitorImpl)
			return (VideoMonitorImpl) vm;
		else
			throw new InvalidReqException();
	}

	/** Parse video monitor number (pin) */
	static private VideoMonitorImpl parseMonNum(ControllerImpl ctrl,
		String mon) throws IOException
	{
		try {
			int pin = Integer.parseInt(mon) + 1;
			ControllerIO cio = ctrl.getIO(pin);
			if (cio instanceof VideoMonitorImpl)
				return (VideoMonitorImpl) cio;
			else
				throw new ParsingException("INVALID PIN: "+pin);
		}
		catch (NumberFormatException e) {
			throw new ParsingException("INVALID MON NUM: " + mon);
		}
	}

	/** Parse camera number / ID */
	static private CameraImpl parseCam(String cam)
		throws InvalidReqException
	{
		Camera c = CameraHelper.find(cam);
		if (c instanceof CameraImpl)
			return (CameraImpl) c;
		else
			throw new InvalidReqException();
	}

	/** Parse camera number (replacing 0 with blank camera num) */
	static private CameraImpl parseCamOrBlank(String cam)
		throws InvalidReqException
	{
		if ("".equals(cam) || "0".equals(cam))
			cam = Integer.toString(cameraNumBlank());
		return parseCam(cam);
	}

	/** Parse a float value */
	static private float parseFloat(String f) throws IOException {
		try {
			return Float.parseFloat(f);
		}
		catch (NumberFormatException e) {
			throw new ParsingException("INVALID FLOAT: " + f);
		}
	}

	/** Create a new display property */
	static private DisplayProp createDisplayProp(String mon) {
		try {
			VideoMonitorImpl vm = parseMon(mon);
			return new DisplayProp(vm);
		}
		catch (InvalidReqException e) {
			return new DisplayProp();
		}
	}

	/** Buffer to parse received data */
	private final byte[] buf = new byte[2048];

	/** Display property */
	private DisplayProp display;

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

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		DisplayProp dp = display;
		if (dp != null) {
			dp.encodeStore(op, tx_buf);
			display = null;
		}
		setPolling(false);
	}

	/** Parse data received from controller */
	@Override
	public void recv(Operation op, ByteBuffer rx_buf) throws IOException {
		doRecv(op, rx_buf);
	}

	/** Parse received data */
	private void doRecv(Operation op, ByteBuffer rx_buf) throws IOException{
		ControllerImpl ctrl = op.getController();
		int len = Math.min(rx_buf.remaining(), buf.length);
		rx_buf.get(buf, 0, len);
		doRecv(ctrl, new String(buf, 0, len, "UTF8"));
	}

	/** Parse received messages */
	private void doRecv(ControllerImpl ctrl, String msgs)throws IOException{
		for (String msg : msgs.split(RECORD_SEP)) {
			try {
				parseMsg(ctrl, msg);
			}
			catch (InvalidReqException e) {
				// Invalid mon, cam or seq.  Just ignore.
			}
		}
	}

	/** Parse one received message */
	private void parseMsg(ControllerImpl ctrl, String msg)
		throws IOException
	{
		String[] par = msg.split(UNIT_SEP);
		String cod = (par.length > 0) ? par[0] : "";
		if (cod.length() > 0) {
			switch (cod) {
			case "status":
				parseStatus(ctrl, par);
				break;
			case "query":
				parseQuery(par);
				break;
			case "switch":
				parseSwitch(ctrl, par);
				break;
			case "next":
				parseNext(ctrl, par);
				break;
			case "previous":
				parsePrevious(ctrl, par);
				break;
			case "sequence":
				parseSequence(ctrl, par);
				break;
			case "ptz":
				parsePtz(ctrl, par);
				break;
			default:
				throw new ParsingException("INVALID MSG");
			}
		}
	}

	/** Parse status message */
	private void parseStatus(ControllerImpl ctrl, String[] par)
		throws IOException
	{
		String mon = (par.length > 1) ? par[1] : "";
		String cam = (par.length > 2) ? par[2] : "";
		String stat = (par.length > 3) ? par[3] : "";
		parseStatus(ctrl, mon, cam, stat);
	}

	/** Parse status message */
	private void parseStatus(ControllerImpl ctrl, String mon, String cam,
		String stat) throws IOException
	{
		VideoMonitorImpl vm = parseMonNum(ctrl, mon);
		CameraImpl c = parseCam(cam);
		vm.setCameraNotify(c, "MONSTREAM", false);
		c.setVideoLossNotify(stat.length() > 0);
	}

	/** Parse query message */
	private void parseQuery(String[] par) {
		String mon = (par.length > 1) ? par[1] : "";
		display = createDisplayProp(mon);
		super.setPolling(true);
	}

	/** Parse switch message */
	private void parseSwitch(ControllerImpl ctrl, String[] par)
		throws IOException
	{
		String mon = (par.length > 1) ? par[1] : "";
		String cam = (par.length > 2) ? par[2] : "";
		selectCamera(ctrl, parseMon(mon), parseCamOrBlank(cam));
	}

	/** Select a camera on the selected video monitor */
	private void selectCamera(ControllerImpl ctrl, VideoMonitorImpl vm,
		CameraImpl c)
	{
		int mn = vm.getMonNum();
		// FIXME: only needed if we're controlling camera
		c.sendPTZ(0, 0, 0);
		VideoMonitorImpl.setCameraNotify(mn, c, "SEL " + ctrl);
	}

	/** Parse next message */
	private void parseNext(ControllerImpl ctrl, String[] par)
		throws IOException
	{
		String mon = (par.length > 1) ? par[1] : "";
		selectNext(ctrl, parseMon(mon));
	}

	/** Select next camera on the selected video monitor */
	private void selectNext(ControllerImpl ctrl, VideoMonitorImpl vm) {
		Camera c = vm.getCamera();
		if (c != null) {
			Integer cn = c.getCamNum();
			if (cn != null)
				selectNext(ctrl, vm, cn);
		}
	}

	/** Select next camera on the selected video monitor */
	private void selectNext(ControllerImpl ctrl, VideoMonitorImpl vm,
		int cn)
	{
		int mn = vm.getMonNum();
		Camera c = CameraHelper.findNextOrFirst(cn);
		if (c instanceof CameraImpl) {
			VideoMonitorImpl.setCameraNotify(mn, (CameraImpl) c,
				"NEXT " + ctrl);
		}
	}

	/** Parse previous message */
	private void parsePrevious(ControllerImpl ctrl, String[] par)
		throws IOException
	{
		String mon = (par.length > 1) ? par[1] : "";
		selectPrevious(ctrl, parseMon(mon));
	}

	/** Select previous camera on the selected video monitor */
	private void selectPrevious(ControllerImpl ctrl, VideoMonitorImpl vm) {
		Camera c = vm.getCamera();
		if (c != null) {
			Integer cn = c.getCamNum();
			if (cn != null)
				selectPrevious(ctrl, vm, cn);
		}
	}

	/** Select previous camera on the selected video monitor */
	private void selectPrevious(ControllerImpl ctrl, VideoMonitorImpl vm,
		int cn)
	{
		int mn = vm.getMonNum();
		Camera c = CameraHelper.findPrevOrLast(cn);
		if (c instanceof CameraImpl) {
			VideoMonitorImpl.setCameraNotify(mn, (CameraImpl) c,
				"PREV " + ctrl);
		}
	}

	/** Parse sequence message */
	private void parseSequence(ControllerImpl ctrl, String[] par)
		throws IOException
	{
		String mon = (par.length > 1) ? par[1] : "";
		String seq = (par.length > 2) ? par[2] : "";
		parseSequence(ctrl, parseMon(mon), seq);
	}

	/** Parse sequence message */
	private void parseSequence(ControllerImpl ctrl, VideoMonitorImpl vm,
		String seq) throws InvalidReqException
	{
		PlayList pl = PlayListHelper.findNum(seq);
		if (pl != null)
			vm.setPlayList(pl);
		else
			throw new InvalidReqException();
	}

	/** Parse ptz message */
	private void parsePtz(ControllerImpl ctrl, String[] par)
		throws IOException
	{
		String mon = (par.length > 1) ? par[1] : "";
		String cam = (par.length > 2) ? par[2] : "";
		String pan = (par.length > 3) ? par[3] : "";
		String tilt = (par.length > 4) ? par[4] : "";
		String zoom = (par.length > 5) ? par[5] : "";
		parsePtz(mon, cam, pan, tilt, zoom);
	}

	/** Parse ptz message */
	private void parsePtz(String mon, String cam, String pan, String tilt,
		String zoom) throws IOException
	{
		VideoMonitorImpl vm = parseMon(mon);
		CameraImpl c = parseCam(cam);
		if (vm.getCamera() == c) {
			float p = parseFloat(pan);
			float t = parseFloat(tilt);
			float z = parseFloat(zoom);
			c.sendPTZ(p, t, z);
		}
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return this;
	}
}
