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
import us.mn.state.dot.tms.utils.wysiwyg.token.Wt_ColorToken;

/**
 * WYSIWYG DMS Message Editor abstract dialog form for editing color tags
 * (page background, foreground, or color rectangls).
 *
 * @author Gordon Parikh - SRF Consulting
 */

@SuppressWarnings("serial")
abstract public class WColorTagDialog extends WMultiTagDialog {
	
	protected Wt_ColorToken editTok;
	protected WTagParamIntField zField;
	protected WTagParamIntField rField;
	protected WTagParamIntField gField;
	protected WTagParamIntField bField;
	
	protected Integer z;
	protected Integer r;
	protected Integer g;
	protected Integer b;

	public WColorTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (Wt_ColorToken) tok;
		z = editTok.getZValue();
		r = editTok.getRValue();
		g = editTok.getGValue();
		b = editTok.getBValue();
	}
	
	@Override
	protected void addTagForm() {
		// note that we make all required, but we override validateForm to
		// require either only Z or only R, G, and B
		zField = new WTagParamIntField(z, 10, true);
		addField("wysiwyg.color_tag_dialog.z", zField);
		rField = new WTagParamIntField(r, 10, true);
		addField("wysiwyg.color_tag_dialog.r", rField);
		gField = new WTagParamIntField(g, 10, true);
		addField("wysiwyg.color_tag_dialog.g", gField);
		bField = new WTagParamIntField(b, 10, true);
		addField("wysiwyg.color_tag_dialog.b", bField);
	}
	
	@Override
	protected boolean validateForm() {
		boolean zValid = zField.contentsValid();
		boolean rgbValid = validateFields(rField, gField, bField);
		return zValid || rgbValid;
	}
}