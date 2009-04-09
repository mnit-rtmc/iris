/*
 * Copyright 1997-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package us.mn.state.dot.tms.client.dms;

import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.border.Border;
import javax.swing.ComboBoxEditor;
import javax.swing.JTextField;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.utils.SDMS;

/**
 * The editor for SignText combo boxes.
 * This class is based on javax.swing.plaf.basic.BasicComboBoxEditor
 *
 * @author Arnaud Weber
 * @author Mark Davidson
 * @author Michael Darter
 */
public class MsgComboBoxEditor implements ComboBoxEditor {

	protected final JTextField m_editor;

	private Object m_oldValue;

	/** Create a new MsgComboBoxEditor */
	public MsgComboBoxEditor() {
		m_editor = new JTextField("", 9);
		m_editor.setBorder(null);
	}

	/** Get the editor component */
	public Component getEditorComponent() {
		return m_editor;
	}

	/**
	 * Sets the item that should be edited.
	 *
	 * @param anObject the displayed value of the editor
	 */
	public void setItem(Object anObject) {
		if(anObject == null)
			m_editor.setText("");
		else {
			m_editor.setText(getItemText(anObject));
			m_oldValue = anObject;
		}
	}

	/** return text for all possible types */
	private static String getItemText(Object o) {
		String txt;
		if(o instanceof SignText)
			txt = ((SignText)o).getMessage();
		else
			txt = o.toString();
		return SDMS.getValidText(txt);
	}

	/** return the edited item */
	public Object getItem() {
		String newValue = SDMS.getValidText(m_editor.getText());
		m_editor.setText(newValue);
		if(m_oldValue instanceof SignText) {
			if(getItemText(newValue).equals(
				 getItemText(m_oldValue)))
			{
				return m_oldValue;
			}
		}
		return newValue;
	}

	/** select all text in control */
	public void selectAll() {
		m_editor.selectAll();
		m_editor.requestFocus();
	}

	/** add action listener */
	public void addActionListener(ActionListener l) {
		m_editor.addActionListener(l);
	}

	/** remove action listener */
	public void removeActionListener(ActionListener l) {
		m_editor.removeActionListener(l);
	}
}
