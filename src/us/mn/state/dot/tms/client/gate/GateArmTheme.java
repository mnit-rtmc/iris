/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  SRF Consulting Group
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
package us.mn.state.dot.tms.client.gate;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmError;
import us.mn.state.dot.tms.GateArmFlag;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.ToolTipBuilder;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Gate arm theme provides styles and tooltips for gate arms.
 *
 * During a gate arm override, the closed, change and open styles are
 * drawn in the gate override color.  The style names are unchanged, so style
 * groups, filters and the legend are not affected.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class GateArmTheme extends ProxyTheme<GateArm> {

	/** Color to display gate arms with a gate override in progress */
	static public final Color COLOR_GATE_OVERRIDE = new Color(0, 128, 128);

	/** Check if a gate arm has a gate override in progress */
	static public boolean isGateOverride(GateArm ga) {
		return (ga != null) && GateArmFlag.GATE_FLAG_OVERRIDE.checkBit(
			ga.getStatusFlags());
	}

	/** Get the status lines for a gate arm.  Errors are listed first
	 * (most serious first), then status.  The list is empty unless
	 * something other than a normal arm position is reported. */
	static public List<String> getStatusLines(GateArm ga) {
		ArrayList<String> lines = new ArrayList<String>();
		if (ga == null)
			return lines;
		int errors = ga.getErrorFlags();
		int flags = ga.getStatusFlags();
		if (errors == 0 && (flags & ~GateArmFlag.POSITION_BITS) == 0)
			return lines;
		String prefix = I18N.get("gate.error.prefix");
		for (GateArmError e: GateArmError.values()) {
			if (e.checkBit(errors))
				lines.add(prefix + e.toString());
		}
		for (GateArmFlag f: GateArmFlag.values()) {
			if (f.checkBit(flags))
				lines.add(f.toString());
		}
		return lines;
	}

	/** Get the status lines as Swing tooltip text (HTML, one line
	 * per status), or null if there are none */
	static public String getStatusToolTip(GateArm ga) {
		List<String> lines = getStatusLines(ga);
		if (lines.isEmpty())
			return null;
		StringBuilder sb = new StringBuilder("<html>");
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0)
				sb.append("<br>");
			sb.append(lines.get(i).replace("&", "&amp;")
				.replace("<", "&lt;").replace(">", "&gt;"));
		}
		return sb.append("</html>").toString();
	}

	/** Gate override styles, by style name */
	private final HashMap<String, Style> gate_override_styles =
		new HashMap<String, Style>();

	/** Create a new gate arm theme */
	public GateArmTheme(GateArmManager man) {
		super(man, new GateArmMarker());
		addStyle(ItemStyle.CLOSED, COLOR_AVAILABLE);
		addStyle(ItemStyle.CHANGE, COLOR_CHANGE);
		addStyle(ItemStyle.OPEN, COLOR_DEPLOYED);
		addStyle(ItemStyle.FAULT, COLOR_FAULT);
		addStyle(ItemStyle.OFFLINE, COLOR_OFFLINE);
	}

	/** Get an appropriate style for the given proxy object */
	@Override
	public Style getStyle(GateArm ga) {
		Style sty = super.getStyle(ga);
		return isGateOverride(ga) && isPositionStyle(sty)
		      ? getGateOverrideStyle(sty)
		      : sty;
	}

	/** Check if a style is a closed, change or open style */
	private boolean isPositionStyle(Style sty) {
		ItemStyle is = ItemStyle.lookupStyle(sty.toString());
		return (is == ItemStyle.CLOSED)
		    || (is == ItemStyle.CHANGE)
		    || (is == ItemStyle.OPEN);
	}

	/** Get the gate override version of a style (same name) */
	private Style getGateOverrideStyle(Style sty) {
		String n = sty.toString();
		Style os = gate_override_styles.get(n);
		if (os == null) {
			os = new Style(n, sty.outline, COLOR_GATE_OVERRIDE,
				false);
			gate_override_styles.put(n, os);
		}
		return os;
	}

	/** Get tooltip text for the given map object */
	@Override
	public String getTip(MapObject o) {
		GateArm ga = manager.findProxy(o);
		if (ga == null)
			return null;
		List<String> lines = getStatusLines(ga);
		if (lines.isEmpty())
			return manager.getDescription(ga);
		ToolTipBuilder ttb = new ToolTipBuilder();
		ttb.addLine(manager.getDescription(ga));
		for (int i = 0; i < lines.size(); i++) {
			if (i == lines.size() - 1)
				ttb.setLast();
			ttb.addLine(lines.get(i));
		}
		return ttb.get();
	}
}
