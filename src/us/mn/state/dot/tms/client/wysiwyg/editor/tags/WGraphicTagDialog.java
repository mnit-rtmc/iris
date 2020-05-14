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
import us.mn.state.dot.tms.utils.wysiwyg.token.WtGraphic;

/**
 * WYSIWYG DMS Message Editor dialog form for editing graphic tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WGraphicTagDialog extends WMultiTagDialog {
	
	protected WtGraphic editTok;
	
	protected WTagParamIntField g_numField;
	protected WTagParamField g_idField;
	private Integer g_num;
	private String g_id;
	
	protected WTagParamIntField xField;
	protected WTagParamIntField yField;
	private Integer x, y;
	
	public WGraphicTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtGraphic) tok;
		g_num = editTok.getGraphicNum();
		g_id = editTok.getVersionId();
		x = editTok.getParamX();
		y = editTok.getParamY();
	}

	@Override
	protected void addTagForm() {
		g_numField = new WTagParamIntField(g_num, 10, true);
		addField("wysiwyg.graphic_tag_dialog.g_num", g_numField);
		g_idField = new WTagParamField(g_id, 10, false);
		addField("wysiwyg.graphic_tag_dialog.g_num", g_idField);
		xField = new WTagParamIntField(x, 10, true);
		addField("wysiwyg.rect_tag_dialog.x", xField);
		yField = new WTagParamIntField(y, 10, true);
		addField("wysiwyg.rect_tag_dialog.y", yField);
	}
	
	@Override
	protected boolean validateForm() {
		boolean valid = super.validateForm();
		valid = validateFields(xField, yField) && valid;
		return valid;
	}
	
	@Override
	protected WtGraphic makeNewTag() {
		g_num = g_numField.getValue();
		g_id = g_idField.getText();
		x = xField.getValue();
		y = yField.getValue();
		return new WtGraphic(g_num, x, y, g_id);
	}
}
