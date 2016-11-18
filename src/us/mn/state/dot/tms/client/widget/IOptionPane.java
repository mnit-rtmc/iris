/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import javax.swing.JOptionPane;
import us.mn.state.dot.tms.utils.I18N;

/**
 * An internationalized option pane.
 *
 * @author Douglas Lau
 */
public class IOptionPane {

	/** Don't allow instantiation */
	private IOptionPane() { }

	/** Show a hint option pane */
	static public void showHint(String msg) {
		JOptionPane.showMessageDialog(null, I18N.get(msg),
			I18N.get("form.hint"), JOptionPane.INFORMATION_MESSAGE);
	}

	/** Show an error option pane.
	 * @param title I18n key of form title.
	 * @param imsg Already I18n'd message. */
	static public void showError(String title, String imsg) {
		JOptionPane.showMessageDialog(null, imsg,
			I18N.get(title), JOptionPane.ERROR_MESSAGE);
	}

	/** Show an OK/CANCEL option dialog.
	 * @param title I18n Key of form title.
	 * @param imsg Already I18n'd message.
	 * @param options Array of I18n'd options.
	 * @return true if OK selected. */
	static public boolean showOption(String title, String imsg,
		Object[] options)
	{
		return JOptionPane.showOptionDialog(null, imsg,
			I18N.get(title), JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE, null, options,
			options[1]) == JOptionPane.OK_OPTION;
	}
}
