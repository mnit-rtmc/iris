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
import us.mn.state.dot.tms.utils.wysiwyg.token.WtLocator;

/**
 * WYSIWYG DMS Message Editor dialog form for editing Incident Locator action
 * tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
class WIncidentLocatorTagDialog extends WMultiTagDialog {
	
	protected WtLocator editTok;
	
	private WTagParamEnumField<LocatorCode> locatorCodeField;
	private LocatorCode code;
	private String codeStr;
	
	public WIncidentLocatorTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtLocator) tok;
		codeStr = editTok.getCode();
		code = LocatorCode.getEnumFromMode(codeStr);
	}

	@Override
	protected void addTagForm() {
		locatorCodeField = new WTagParamEnumField<LocatorCode>(
				LocatorCode.values(), code, true);
		addField("wysiwyg.inc_locator_dialog.code", locatorCodeField);
	}

	@Override
	protected WToken makeNewTag() {
		code = locatorCodeField.getSelectedItem();
		codeStr = code.getCode();
		return new WtLocator(codeStr);
	}

	/** Locator codes */
	private enum LocatorCode {
		Road_Name("rn"),
		Road_Direction("rd"),
		Location_Modifier("md"),
		Cross_Street_Name("xn"),
		Distance_Miles("mi");
		
		private String code;
		
		private LocatorCode(String m) {
			code = m;
		}
		
		public String getCode() {
			return code;
		}
		
		public static LocatorCode getEnumFromMode(String m) {
			for (LocatorCode e: values()) {
				String em = e.getCode();
				if (em.equals(m))
					return e;
			}
			return null;
		}
	};
}