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
import us.mn.state.dot.tms.utils.wysiwyg.token.WtTextRectangle;

/**
 * WYSIWYG DMS Message Editor dialog form for editing text rectangle tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WTextRectangleTagDialog extends WMultiTagDialog {
	
	protected WtTextRectangle editTok;
	protected WTagParamIntField xField;
	protected WTagParamIntField yField;
	protected WTagParamIntField wField;
	protected WTagParamIntField hField;
	private Integer x, y, w, h;
	
	public WTextRectangleTagDialog(String title, WController c, WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtTextRectangle) tok;
		x = editTok.getParamX();
		y = editTok.getParamY();
		w = editTok.getParamW();
		h = editTok.getParamH();
	}

	@Override
	protected void addTagForm() {
		xField = new WTagParamIntField(x, 10, true);
		addField("wysiwyg.rect_tag_dialog.x", xField);
		yField = new WTagParamIntField(y, 10, true);
		addField("wysiwyg.rect_tag_dialog.y", yField);
		wField = new WTagParamIntField(w, 10, true);
		addField("wysiwyg.rect_tag_dialog.w", wField);
		hField = new WTagParamIntField(h, 10, true);
		addField("wysiwyg.rect_tag_dialog.h", hField);
	}
	
	@Override
	protected boolean validateForm() {
		boolean valid = super.validateForm();
		valid = validateFields(xField, yField, wField, hField) && valid;
		return valid;
	}
	
	@Override
	protected WtTextRectangle makeNewTag() {
		x = xField.getValue();
		y = yField.getValue();
		w = wField.getValue();
		h = hField.getValue();
		return new WtTextRectangle(x, y, w, h);
	}

}
