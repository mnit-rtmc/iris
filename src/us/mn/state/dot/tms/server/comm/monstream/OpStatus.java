/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DeviceRequest;
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
		String mon, String mode) throws ParsingException
	{
		try {
			int pin = Integer.parseInt(mon) + 1;
			boolean full = "full".equals(mode);
			VideoMonitorImpl vm = MonStreamPoller
				.getMonitor(ctrl, pin, full);
			if (vm != null)
				return vm;
			else
				throw new ParsingException("INVALID PIN: "+pin);
		}
		catch (NumberFormatException e) {
			throw new ParsingException("INVALID MON NUM: " + mon);
		}
	}

	/** Parse camera number */
	static private CameraImpl parseCam(String cam)
		throws InvalidReqException
	{
		Camera c = CameraHelper.findNum(cam);
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

	/** Parse a preset number */
	static private int parsePresetNum(String num) throws ParsingException {
		try {
			return Integer.parseInt(num);
		}
		catch (NumberFormatException e) {
			throw new ParsingException("INVALID PRESET: " + num);
		}
	}

	/** Parse a sequence number */
	static private int parseSeqNum(String num) throws ParsingException {
		try {
			return Integer.parseInt(num);
		}
		catch (NumberFormatException e) {
			throw new ParsingException("INVALID SEQ: " + num);
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

	/** Parse a lens device request */
	static private DeviceRequest parseLensReq(String cmd)
		throws ParsingException
	{
		if ("iris_stop".equals(cmd))
			return DeviceRequest.CAMERA_IRIS_STOP;
		else if ("iris_open".equals(cmd))
			return DeviceRequest.CAMERA_IRIS_OPEN;
		else if ("iris_close".equals(cmd))
			return DeviceRequest.CAMERA_IRIS_CLOSE;
		else if ("focus_stop".equals(cmd))
			return DeviceRequest.CAMERA_FOCUS_STOP;
		else if ("focus_near".equals(cmd))
			return DeviceRequest.CAMERA_FOCUS_NEAR;
		else if ("focus_far".equals(cmd))
			return DeviceRequest.CAMERA_FOCUS_FAR;
		else if ("wiper".equals(cmd))
			return DeviceRequest.CAMERA_WIPER_ONESHOT;
		else
			throw new ParsingException("INVALID LENS CMD: " + cmd);
	}

	/** Parse a menu device request */
	static private DeviceRequest parseMenuReq(String cmd)
		throws ParsingException
	{
		if ("open".equals(cmd))
			return DeviceRequest.CAMERA_MENU_OPEN;
		else if ("enter".equals(cmd))
			return DeviceRequest.CAMERA_MENU_ENTER;
		else if ("cancel".equals(cmd))
			return DeviceRequest.CAMERA_MENU_CANCEL;
		else
			throw new ParsingException("INVALID MENU CMD: " + cmd);
	}

	/** Buffer to parse received data */
	private final byte[] buf = new byte[2048];

	/** Display property */
	private DisplayProp display;

	/** Create a new status op */
	public OpStatus(VideoMonitorImpl vm) {
		display = new DisplayProp(vm);
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
			case "preset":
				parsePreset(par);
				break;
			case "lens":
				parseLens(par);
				break;
			case "menu":
				parseMenu(par);
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
		String mode = (par.length > 4) ? par[4] : "";
		parseStatus(ctrl, mon, cam, stat, mode);
	}

	/** Parse status message */
	private void parseStatus(ControllerImpl ctrl, String mon, String cam,
		String stat, String mode) throws InvalidReqException
	{
		try {
			VideoMonitorImpl vm = parseMonNum(ctrl, mon, mode);
			CameraImpl c = parseCam(cam);
			vm.setCamNoSelect(c, "STATUS " + ctrl);
			c.setVideoLossNotify(stat.length() > 0, vm.getName());
		}
		catch (ParsingException e) {
			// FIXME: log this
		}
	}

	/** Parse query message */
	private void parseQuery(String[] par) {
		String mon = (par.length > 1) ? par[1] : "";
		display = createDisplayProp(mon);
		setPolling(true);
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
		VideoMonitorImpl.setCamMirrored(mn, c, "SEL " + ctrl);
	}

	/** Parse next message */
	private void parseNext(ControllerImpl ctrl, String[] par)
		throws IOException
	{
		String mon = (par.length > 1) ? par[1] : "";
		VideoMonitorImpl vm = parseMon(mon);
		vm.selectNextCam(ctrl.toString());
	}

	/** Parse previous message */
	private void parsePrevious(ControllerImpl ctrl, String[] par)
		throws IOException
	{
		String mon = (par.length > 1) ? par[1] : "";
		VideoMonitorImpl vm = parseMon(mon);
		vm.selectPrevCam(ctrl.toString());
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
		String seq) throws IOException
	{
		if ("pause".equals(seq)) {
			if (vm.isSequenceRunning())
				vm.pauseSequence();
			else
				vm.unpauseSequence();
		} else {
			int sn = parseSeqNum(seq);
			if (!vm.setSeqNum(sn))
				throw new InvalidReqException();
		}
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

	/** Parse preset message */
	private void parsePreset(String[] par) throws IOException {
		String mon = (par.length > 1) ? par[1] : "";
		String cam = (par.length > 2) ? par[2] : "";
		String cmd = (par.length > 3) ? par[3] : "";
		String num = (par.length > 4) ? par[4] : "";
		parsePreset(mon, cam, cmd, num);
	}

	/** Parse preset message */
	private void parsePreset(String mon, String cam, String cmd,
		String num) throws IOException
	{
		VideoMonitorImpl vm = parseMon(mon);
		CameraImpl c = parseCam(cam);
		if (vm.getCamera() == c) {
			if ("recall".equals(cmd))
				c.setRecallPreset(parsePresetNum(num));
			else if ("store".equals(cmd))
				c.setStorePreset(parsePresetNum(num));
			else
				throw new ParsingException("INVALID CMD: "+cmd);
		}
	}

	/** Parse lens message */
	private void parseLens(String[] par) throws IOException {
		String mon = (par.length > 1) ? par[1] : "";
		String cam = (par.length > 2) ? par[2] : "";
		String cmd = (par.length > 3) ? par[3] : "";
		parseLens(mon, cam, cmd);
	}

	/** Parse lens message */
	private void parseLens(String mon, String cam, String cmd)
		throws IOException
	{
		VideoMonitorImpl vm = parseMon(mon);
		CameraImpl c = parseCam(cam);
		if (vm.getCamera() == c)
			c.setDeviceReq(parseLensReq(cmd));
	}

	/** Parse menu message */
	private void parseMenu(String[] par) throws IOException {
		String mon = (par.length > 1) ? par[1] : "";
		String cam = (par.length > 2) ? par[2] : "";
		String cmd = (par.length > 3) ? par[3] : "";
		parseMenu(mon, cam, cmd);
	}

	/** Parse menu message */
	private void parseMenu(String mon, String cam, String cmd)
		throws IOException
	{
		VideoMonitorImpl vm = parseMon(mon);
		CameraImpl c = parseCam(cam);
		if (vm.getCamera() == c)
			c.setDeviceReq(parseMenuReq(cmd));
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return this;
	}
}
