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

import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.utils.SDMS;
import us.mn.state.dot.tms.utils.SString;

import java.awt.*;
import java.awt.event.*;

import java.lang.reflect.Method;

import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.ComboBoxEditor;
import javax.swing.JTextField;

/**
 * The default editor for editable combo boxes. The editor is implemented as a JTextField.
 *
 * @author Arnaud Weber
 * @author Mark Davidson
 * @author Michael Darter
 */
public class MsgComboBoxEditor implements ComboBoxEditor
{
	protected JTextField m_editor;
	private Object m_oldValue;

	/** constructor */
	public MsgComboBoxEditor() {
		m_editor = createEditorComponent();
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public Component getEditorComponent() {
		return m_editor;
	}

	/**
	 * Creates the internal editor component. Override this to provide
	 * a custom implementation.
	 *
	 * @return a new editor component
	 * @since 1.6
	 */
	private JTextField createEditorComponent() {
		JTextField e = new BorderlessTextField("", 9);
		e.setBorder(null);
		return e;
	}

	/**
	 * Sets the item that should be edited.
	 *
	 * @param anObject the displayed value of the editor
	 */
	public void setItem(Object anObject) {
		if(anObject == null) {
			m_editor.setText("");
		} else {

			m_editor.setText(getItemText(anObject));
			m_oldValue = anObject;
		}
	}

	/** return text for all possible types */
	private static String getItemText(Object o) {
		String txt = "";
		if(o instanceof String)
			txt = ((String) o).toString();
		else if(o instanceof SignText)
			txt = ((SignText) o).getMessage();
		else {
			String msg="MsgComboBoxEditor.getItemText(o): WARNING: unexpected type encountered.";
			assert false : msg;
			txt="";
		}

		// FIXME: this shouldn't go here
		if(txt.toLowerCase().equals("soccs message"))
			txt = "";

		// validate text
		return SDMS.getValidText(txt);
	}

	/** return the edited item */
	public Object getItem() {
		String newValue = SDMS.getValidText(m_editor.getText());
		m_editor.setText(newValue);

		// we have an oldValue that isn't a string
		if((m_oldValue != null) &&!(m_oldValue instanceof String)) {

			// The original value is not a string, return the object
			if(getItemText(newValue).equals(
				getItemText(m_oldValue))) {
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

	static class BorderlessTextField extends JTextField
	{
		public BorderlessTextField(String value, int n) {
			super(value, n);
		}

		/** workaround for 4530952 */
		public void setText(String s) {
			if(getText().equals(s)) {
				return;
			}

			super.setText(s);
		}

		public void setBorder(Border b) {
			if(!(b instanceof UIResource)) {
				super.setBorder(b);
			}
		}
	}

	/**
	 * A subclass of MsgComboBoxEditor that implements UIResource.
	 * MsgComboBoxEditor doesn't implement UIResource
	 * directly so that applications can safely override the
	 * cellRenderer property with BasicListCellRenderer subclasses.
	 * <p>
	 * <strong>Warning:</strong>
	 * Serialized objects of this class will not be compatible with
	 * future Swing releases. The current serialization support is
	 * appropriate for short term storage or RMI between applications running
	 * the same version of Swing.  As of 1.4, support for long term storage
	 * of all JavaBeans<sup><font size="-2">TM</font></sup>
	 * has been added to the <code>java.beans</code> package.
	 * Please see {@link java.beans.XMLEncoder}.
	 */
	private static class UIResource extends MsgComboBoxEditor
		implements javax.swing.plaf.UIResource
	{
	}
}
