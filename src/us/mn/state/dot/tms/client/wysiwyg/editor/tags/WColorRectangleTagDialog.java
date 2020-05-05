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
import us.mn.state.dot.tms.utils.wysiwyg.token.WtColorRectangle;

/**
 * WYSIWYG DMS Message Editor dialog form for editing color rectangle tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WColorRectangleTagDialog extends WColorTagDialog {
	
	protected WtColorRectangle editTok;
	
	protected WTagParamIntField xField;
	protected WTagParamIntField yField;
	protected WTagParamIntField wField;
	protected WTagParamIntField hField;
	private Integer x, y, w, h;
	
	public WColorRectangleTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		// load color values with base method
		super.loadFields(tok);
		
		// load dimensions too
		editTok = (WtColorRectangle) tok;
		x = editTok.getParamX();
		y = editTok.getParamY();
		w = editTok.getParamW();
		h = editTok.getParamH();
	}

	@Override
	protected void addTagForm() {
		// add the dimension fields
		xField = new WTagParamIntField(x, 10, true);
		addField("wysiwyg.rect_tag_dialog.x", xField);
		yField = new WTagParamIntField(y, 10, true);
		addField("wysiwyg.rect_tag_dialog.y", yField);
		wField = new WTagParamIntField(w, 10, true);
		addField("wysiwyg.rect_tag_dialog.w", wField);
		hField = new WTagParamIntField(h, 10, true);
		addField("wysiwyg.rect_tag_dialog.h", hField);
		
		// then add the color fields
		super.addTagForm();
	}
	
	@Override
	protected boolean validateForm() {
		boolean valid = super.validateForm();
		valid = validateFields(xField, yField, wField, hField) && valid;
		return valid;
	}
	
	@Override
	protected WtColorRectangle makeNewTag() {
		// get the dimensions first
		x = xField.getValue();
		y = yField.getValue();
		w = wField.getValue();
		h = hField.getValue();
		
		// now get the colors and return a tag
		// check the z field first
		z = zField.getValue();
		if (z != null)
			return new WtColorRectangle(x, y, w, h, z);
		
		// if we didn't get a z, we must be able to use the others
		r = rField.getValue();
		g = gField.getValue();
		b = bField.getValue();
		return new WtColorRectangle(x, y, w, h, r, g, b);
	}
}
