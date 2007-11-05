/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.lcs;

import java.rmi.RemoteException;

import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.LCSModule;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.ExceptionDialog;

/**
 * InputModel is a special table model for setting up special function
 * inputs of a 170 for LCSModules.
 *
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @author Douglas Lau
 */
public class InputModel extends SpecialFunctionModel {

	/** Create a new input model */
	public InputModel(LCSModule[] modules) throws RemoteException {
		super(modules);
		for(int row = 0; row < ROWS; row++) {
			for(int col = 0; col < COLS; col++) {
				if(col == 0)
					values[row][col] = new Integer(row + 1);
				else if(col == 1) {
					values[row][col] = new Integer(
						modules[row].getSFI(
						LCSModule.GREEN));
				} else if(col == 2) {
					values[row][col] = new Integer(
						modules[row].getSFI(
						LCSModule.YELLOW));
				} else {
					values[row][col] = new Integer(
						modules[row].getSFI(
						LCSModule.RED));
				}
			}
		}
	}

	/** Set one input in the model */
	protected void setInput(int setting, int row, int col)
		throws TMSException, RemoteException
	{
		// only the first 32 bits of the special function
		// inputs are needed to verify the lane control signals.
		// setting an offset past that is not allowed.
		if(setting < -1 || setting > 31) {
			throw new ChangeVetoException(
				"Offset must be between -1 and 31, inclusive.");
		}
		if(col == 1)
			modules[row].setSFI(LCSModule.GREEN, setting);
		else if(col == 2)
			modules[row].setSFI(LCSModule.YELLOW, setting);
		else if(col == 3)
			modules[row].setSFI(LCSModule.RED, setting);
		values[row][col] = new Integer(setting);
	}

	/** Set the value of the model */
	public void setValueAt(Object value, int row, int col) {
		try {
			int setting = Integer.parseInt(value.toString());
			setInput(setting, row, col);
		} catch(Exception e) {
			new ExceptionDialog(e).setVisible(true);
		}
	}
}
