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
package us.mn.state.dot.tms.server.comm.cux50;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.PlayListHelper;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.VideoMonitorHelper;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.VideoMonitorImpl;

/**
 * Protocol handler for Panasonic CU-x50 camera keyboards.
 *
 * @author Douglas Lau
 */
public class CUx50 implements ProtocolHandler {

	/** Packet start/end transmit */
	static private final byte STX = 0x02;
	static private final byte ETX = 0x03;

	/** Panasonic keyboard has char 0x80 mapped to "play" arrow */
	static private final char SEQ_PLAY = '\u0080';
	static private final char SEQ_PAUSE = '"';

	/** Keycode for monitor */
	static private final byte KEY_MON = (byte) 'A';

	/** Keycode for camera */
	static private final byte KEY_CAM = (byte) 'B';

	/** Keycode for clear */
	static private final byte KEY_CLEAR = (byte) 'N';

	/** Keycode for pause */
	static private final byte KEY_PAUSE = (byte) 'X';

	/** Keycode for sequence */
	static private final byte KEY_SEQ = (byte) 'Y';

	/** Joystick stop code */
	static private final int JOY_STOP = '@';

	/** Joystick left code */
	static private final int JOY_LEFT = 'a';

	/** Joystick left-up code */
	static private final int JOY_LEFT_UP = 'b';

	/** Joystick up code */
	static private final int JOY_UP = 'c';

	/** Joystick right-up code */
	static private final int JOY_RIGHT_UP = 'd';

	/** Joystick right code */
	static private final int JOY_RIGHT = 'e';

	/** Joystick right-down code */
	static private final int JOY_RIGHT_DOWN = 'f';

	/** Joystick down code */
	static private final int JOY_DOWN = 'g';

	/** Joystick left-down code */
	static private final int JOY_LEFT_DOWN = 'h';

	/** Dead zone for joystick slop */
	static private final int DEAD_ZONE = 0;

	/** Maximum pan/tilt value */
	static private final int MAX_PAN_TILT = 64;

	/** Map a pan/tilt value to [0, 1] range */
	static private float pt_range(int p) {
		return (p > DEAD_ZONE)
		     ? (p - DEAD_ZONE) / (float) (MAX_PAN_TILT - DEAD_ZONE)
		     : 0;
	}

	/** Parse pan value from joystick pkt */
	static private float parse_pan(byte[] rcv, int off) {
		int pt = rcv[off + 1];
		switch (pt) {
		case JOY_LEFT:
		case JOY_LEFT_UP:
		case JOY_LEFT_DOWN:
			int left = parseHex2(rcv[off + 2], rcv[off + 3]);
			return -pt_range(left);
		case JOY_RIGHT:
		case JOY_RIGHT_UP:
		case JOY_RIGHT_DOWN:
			int right = parseHex2(rcv[off + 2], rcv[off + 3]);
			return pt_range(right);
		default:
			return 0;
		}
	}

	/** Parse tilt value from joystick pkt */
	static private float parse_tilt(byte[] rcv, int off) {
		int pt = rcv[off + 1];
		switch (pt) {
		case JOY_DOWN:
		case JOY_LEFT_DOWN:
		case JOY_RIGHT_DOWN:
			int down = parseHex2(rcv[off + 4], rcv[off + 5]);
			return -pt_range(down);
		case JOY_UP:
		case JOY_LEFT_UP:
		case JOY_RIGHT_UP:
			int up = parseHex2(rcv[off + 4], rcv[off + 5]);
			return pt_range(up);
		default:
			return 0;
		}
	}

	/** Parse zoom value from joystick pkt */
	static private float parse_zoom(byte[] rcv, int off) {
		switch (rcv[off + 6]) {
		case '0': return 0;
		case '1': return 0.25f;
		case '2': return 0.5f;
		case '3': return 0.75f;
		case '4': return 1.0f;
		case '5': return -0.25f;
		case '6': return -0.5f;
		case '7': return -0.75f;
		case '8': return -1.0f;
		default: return 0;
		}
	}

	/** Parse 2 hex digits */
	static private int parseHex2(byte hi, byte lo) {
		int h0 = parseHex(hi);
		int h1 = parseHex(lo);
		if (h0 >= 0 && h1 >= 0)
			return h0 * 16 + h1;
		else
			return 0;
	}

	/** Parse one hex digit */
	static private int parseHex(byte h) {
		if (h >= (byte) '0' && h <= (byte) '9')
			return h - (byte) '0';
		else if (h >= (byte) 'A' && h <= (byte) 'F')
			return 10 + (h - (byte) 'A');
		else if (h >= (byte) 'a' && h <= (byte) 'f')
			return 10 + (h - (byte) 'a');
		else
			return -1;
	}

	/** Heartbeat message */
	static private final byte[] HEARTBEAT =
		"@CU650".getBytes(StandardCharsets.US_ASCII);

	/** Compare two packets for equality */
	static private boolean pktEquals(byte[] a, byte[] b, int off,
		int len)
	{
		if (a.length != len)
			return false;
		for (int i = 0; i < len; i++) {
			if (a[i] != b[off + i])
				return false;
		}
		return true;
	}

	/** Check if a packet is a heartbeat */
	static private boolean checkHeartbeat(byte[] rcv, int off, int len) {
		return pktEquals(HEARTBEAT, rcv, off, len);
	}

	/** Format one LCD display message */
	static private String formatLCD(char ab, String line, String flicker) {
		return String.format("%c%-20.20s%-6.6s", ab, line, flicker);
	}

	/** Keyboard state structure */
	static private class KeyboardState {
		/* Buffer for keyboard output */
		private final ByteBuffer buf = ByteBuffer.allocate(128);
		/* Host string */
		private final String host;
		/* Selected monitor */
		private VideoMonitor monitor = null;
		/* Current keyboard entry data */
		private StringBuilder entry = new StringBuilder();
		/** Create a new keyboard state */
		private KeyboardState(String h) {
			host = h;
		}
		/** Get the camera number */
		private String getCamNum() {
			VideoMonitor vm = monitor;
			if (vm != null) {
				Camera c = vm.getCamera();
				Integer n = c.getCamNum();
				if (n != null)
					return n.toString();
			}
			return null;
		}
		/** Get the sequence (PlayList) number */
		private String getSeqNum() {
			VideoMonitor vm = monitor;
			if (vm instanceof VideoMonitorImpl) {
				VideoMonitorImpl vmi = (VideoMonitorImpl) vm;
				PlayList pl = vmi.getPlayList();
				if (pl != null) {
					Integer n = pl.getNum();
					if (n != null)
						return n.toString();
				}
			}
			return null;
		}
		/** Is the sequence running */
		private boolean isSeqRunning() {
			VideoMonitor vm = monitor;
			if (vm instanceof VideoMonitorImpl) {
				VideoMonitorImpl vmi = (VideoMonitorImpl) vm;
				return vmi.isPlayListRunning();
			}
			return false;
		}
		/** Write a packet */
		private void writePkt(byte[] pkt) {
			buf.put(STX);
			buf.put(pkt);
			buf.put(ETX);
		}
		/** Write a packet */
		private void writePkt(String pkt) {
			// Must use ISO_8859_1 for 8-bit characters (SEQ_PLAY)
			writePkt(pkt.getBytes(StandardCharsets.ISO_8859_1));
		}
		/** Format LCD for line A */
		private String formatLineA() {
			VideoMonitor vm = monitor;
			String line = (vm != null)
			    ? String.format("Mon %-4s%12s", vm.getMonNum(),
			                    formatSeq())
			    : "  Select monitor #";
			return formatLCD('a', line, "000000");
		}
		/** Format sequence */
		private String formatSeq() {
			String sn = getSeqNum();
			char sr = isSeqRunning() ? SEQ_PLAY : SEQ_PAUSE;
			return (sn != null)
			    ? String.format("%c Seq %-4s", sr, sn)
			    : "";
		}
		/** Format LCD for line B */
		private String formatLineB() {
			String line = String.format("%-12s  ~ %-4.4s",
				formatCam(), entry + "_");
			return formatLCD('b', line, "30000f");
		}
		/** Format camera */
		private String formatCam() {
			String cam = getCamNum();
			return (cam != null)
			    ? String.format("Cam %s", cam)
			    : "";
		}
		/** Update the LCD display */
		private void updateDisplay() {
			writePkt(formatLineA());
			writePkt(formatLineB());
		}
		/** Handle key down message */
		private void handleKeyDown(byte k) {
			if (k >= (byte) '0' && k <= (byte) '9')
				addToEntry(k);
			else if (KEY_MON == k)
				selectMon();
			else if (KEY_CAM == k)
				selectCam();
			else if (KEY_SEQ == k)
				selectSeq();
			else if (KEY_PAUSE == k)
				pausePlay();
			else if (KEY_CLEAR == k) {
				entry.setLength(0);
				updateDisplay();
			} else {
				entry.setLength(0);
				beepInvalid();
				updateDisplay();
			}
		}
		/** Add a character to entry */
		private void addToEntry(byte k) {
			if (entry.length() < 4) {
				entry.append((char) k);
				if (entry.length() > 1 && entry.charAt(0) =='0')
					entry.deleteCharAt(0);
				updateDisplay();
			} else
				beepInvalid();
		}
		/** Select a monitor */
		private void selectMon() {
			String n = entry.toString();
			VideoMonitor vm = VideoMonitorHelper.findUID(n);
			if (vm != null || "0".equals(n))
				monitor = vm;
			else
				beepInvalid();
			entry.setLength(0);
			updateDisplay();
		}

		/** Select a camera */
		private void selectCam() {
			VideoMonitor vm = monitor;
			if (vm != null)
				selectCamera(vm);
			else
				beepInvalid();
			entry.setLength(0);
			updateDisplay();
		}

		/** Select a new camera on a video monitor */
		private void selectCamera(VideoMonitor vm) {
			String n = entry.toString();
			CameraImpl c = findCamera(n);
			if (c != null || "0".equals(n)) {
				int mn = vm.getMonNum();
				stopCamControl(vm);
				VideoMonitorImpl.setCameraNotify(mn, c,
					"SEL " + host);
			} else
				beepInvalid();
		}

		/** Find a camera by number */
		private CameraImpl findCamera(String n) {
			Integer cn = CameraHelper.parseUID(n);
			if (cn != null) {
				Camera c = CameraHelper.findNum(cn);
				if (c instanceof CameraImpl)
					return (CameraImpl) c;
			}
			return null;
		}

		/** Stop camera control on selected camera */
		private void stopCamControl(VideoMonitor vm) {
			Camera c = vm.getCamera();
			if (c instanceof CameraImpl)
				((CameraImpl) c).sendPTZ(0, 0, 0);
		}

		/** Select a sequence (PlayList) */
		private void selectSeq() {
			VideoMonitor vm = monitor;
			if (vm != null) {
				String n = entry.toString();
				PlayList pl = PlayListHelper.findNum(n);
				vm.setPlayList(pl);
			} else
				beepInvalid();
			entry.setLength(0);
			updateDisplay();
		}
		/** Toggle sequence pause/play */
		private void pausePlay() {
			VideoMonitor vm = monitor;
			if (vm instanceof VideoMonitorImpl) {
				VideoMonitorImpl vmi = (VideoMonitorImpl) vm;
				if (vmi.isPlayListRunning())
					vmi.pausePlayList();
				else
					vmi.unpausePlayList();
				updateDisplay();
			} else
				beepInvalid();
		}
		/** Handle a joystick message */
		private void handleJoystick(byte[] rcv, int off) {
			VideoMonitor vm = monitor;
			if (vm != null) {
				Camera c = vm.getCamera();
				if (c instanceof CameraImpl)
					handleJoystick((CameraImpl) c, rcv,off);
			}
		}
		/** Handle a joystick message */
		private void handleJoystick(CameraImpl c, byte[] rcv, int off) {
			float pan = parse_pan(rcv, off);
			float tilt = parse_tilt(rcv, off);
			float zoom = parse_zoom(rcv, off);
			c.sendPTZ(pan, tilt, zoom);
		}
		/** Beep for invalid input */
		private void beepInvalid() {
			writePkt("d8");
		}
	}

	/** Mapping of host strings to state structures for all keyboards */
	private final ConcurrentHashMap<String, KeyboardState> states =
		new ConcurrentHashMap<String, KeyboardState>();

	/** Lookup the keyboard state for a socket address */
	private KeyboardState lookupState(SocketAddress sa) {
		if (sa instanceof InetSocketAddress) {
			InetSocketAddress isa = (InetSocketAddress) sa;
			String host = isa.getHostString();
			KeyboardState ks = states.get(host);
			if (ks != null)
				return ks;
			else {
				ks = new KeyboardState(host);
				states.put(host, ks);
				return ks;
			}
		} else
			return null;
	}

	/** Handle data receive for protocol */
	@Override
	public byte[] handleReceive(SocketAddress sa, byte[] rcv) {
		KeyboardState ks = lookupState(sa);
		return (ks != null) ? handleReceive(ks, rcv) : new byte[0];
	}

	/** Handle receive for a keyboard state */
	private byte[] handleReceive(KeyboardState ks, byte[] rcv) {
		synchronized (ks) {
			int s = 0;
			while (true) {
				int off = pktOffset(rcv, s);
				if (off < 0)
					break;
				int len = pktLength(rcv, off);
				if (len > 0)
					parsePkt(ks, rcv, off, len);
				s = off + len;
			}
			ks.buf.flip();
			byte[] snd = new byte[ks.buf.remaining()];
			ks.buf.get(snd);
			ks.buf.clear();
			return snd;
		}
	}

	/** Get the buffer offset of a packet */
	private int pktOffset(byte[] rcv, int s) {
		for (int i = s; i < rcv.length; i++) {
			if (STX == rcv[i])
				return i + 1;
		}
		return -1;
	}

	/** Get the packet length */
	private int pktLength(byte[] rcv, int off) {
		for (int i = 0; off + i < rcv.length; i++) {
			if (ETX == rcv[off + i])
				return i;
		}
		return -1;
	}

	/** Parse one packet */
	private void parsePkt(KeyboardState ks, byte[] rcv, int off, int len) {
		if (checkHeartbeat(rcv, off, len))
			ks.updateDisplay();
        	else if (3 == len && rcv[off] == (byte) 'A') {
			if (rcv[off + 2] == (byte) '+')
				ks.handleKeyDown(rcv[off + 1]);
		} else if (7 == len && rcv[off] == (byte) 'B')
			ks.handleJoystick(rcv, off);
		else
			ks.beepInvalid();
	}
}
