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

import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.wysiwyg.editor.WController;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtSpeedAdvisory;

/**
 * WYSIWYG DMS Message Editor dialog form for adding Variable Speed Advisory
 * action tags. Note that these tags have no fields, so this dialog just adds
 * tags and provides information to the user.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
class WSpeedAdvisoryTagDialog extends WMultiTagDialog {
	
	WtSpeedAdvisory editTok;
	
	public WSpeedAdvisoryTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtSpeedAdvisory) tok;
	}

	@Override
	protected void addTagForm() {
		// just add a label letting the user know there are no fields.
		add(new ILabel("wysiwyg.speed_advisory_dialog.info"));
	}

	@Override
	protected boolean validateForm() {
		// always valid since no parameters
		return true;
	}

	@Override
	protected WToken makeNewTag() {
		return new WtSpeedAdvisory();
	}
	
}