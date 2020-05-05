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
import us.mn.state.dot.tms.utils.wysiwyg.token.WtPageBackground;

/**
 * WYSIWYG DMS Message Editor dialog form for editing page background color
 * tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WPageBackgroundTagDialog extends WColorTagDialog {

	protected WtPageBackground editTok;
	
	public WPageBackgroundTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}
	
	@Override
	protected WtPageBackground makeNewTag() {
		// check the z field first
		z = zField.getValue();
		if (z != null)
			return new WtPageBackground(z);
		
		// if we didn't get a z, we must be able to use the others
		r = rField.getValue();
		g = gField.getValue();
		b = bField.getValue();
		return new WtPageBackground(r, g, b);
	}

}
