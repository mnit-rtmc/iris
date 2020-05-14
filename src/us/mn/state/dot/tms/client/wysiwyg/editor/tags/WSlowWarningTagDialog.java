/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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


package us.mn.state.dot.tms.client.wysiwyg.editor.tags;

import us.mn.state.dot.tms.client.wysiwyg.editor.WController;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtSlowWarning;

/**
 * WYSIWYG DMS Message Editor dialog form for editing Slow Traffic Warning
 * action tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
class WSlowWarningTagDialog extends WMultiTagDialog {
	protected WtSlowWarning editTok;
	private WTagParamIntField spdField;
	private WTagParamIntField distField;
	private WTagParamEnumField<TagReplMode> modeField;
	private Integer spd;
	private Integer dist;
	private TagReplMode mode;
	private String modeStr = "";
	
	public WSlowWarningTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}
	
	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtSlowWarning) tok;
		spd = editTok.getWarningSpeed();
		dist = editTok.getWarningDist();
		modeStr = editTok.getTextReplMode();
		mode = TagReplMode.getEnumFromMode(modeStr);
	}
	
	@Override
	protected void addTagForm() {
		spdField = new WTagParamIntField(spd, 10, true);
		addField("wysiwyg.slow_warning_dialog.spd", spdField);
		distField = new WTagParamIntField(dist, 10, true);
		addField("wysiwyg.slow_warning_dialog.dist", distField);
		modeField = new WTagParamEnumField<TagReplMode>(
				TagReplMode.values(), mode, false);
		addField("wysiwyg.slow_warning_dialog.mode", modeField);
	}

	@Override
	protected WtSlowWarning makeNewTag() {
		spd = spdField.getValue();
		dist = distField.getValue();
		mode = modeField.getSelectedItem();
		modeStr = (mode != null) ? mode.getMode() : "";
		return new WtSlowWarning(spd, dist, modeStr);
	}

	/** Tag replacement modes */
	private enum TagReplMode {
		none(""),
		distance("dist"),
		speed("speed");
		
		private String mode;
		
		private TagReplMode(String m) {
			mode = m;
		}
		
		public String getMode() {
			return mode;
		}
		
		public static TagReplMode getEnumFromMode(String m) {
			for (TagReplMode e: values()) {
				String em = e.getMode();
				if (em.equals(m))
					return e;
			}
			return null;
		}
	};
}
