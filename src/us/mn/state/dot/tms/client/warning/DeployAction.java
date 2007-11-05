/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2005  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.warning;

import java.rmi.RemoteException;
import javax.swing.Action;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.device.TrafficDeviceAction;

/**
 * Action to deploy a warning sign.
 *
 * @author Douglas Lau
 */
public class DeployAction extends TrafficDeviceAction {

	/** Flag to deploy/clear sign */
	protected final boolean deploy;

	/** Create a new deploy action */
	public DeployAction(WarningSignProxy p, boolean d) {
		super(p);
		deploy = d;
		if(deploy) {
			putValue(Action.NAME, "Deploy");
			putValue(Action.SHORT_DESCRIPTION, "Turn on");
			putValue(Action.LONG_DESCRIPTION,
				"Turn on warning sign flashers");
		} else {
			putValue(Action.NAME, "Clear");
			putValue(Action.SHORT_DESCRIPTION, "Turn off");
			putValue(Action.LONG_DESCRIPTION,
				"Turn off warning sign flashers");
		}
	}

	/** Actually perform the action */
	protected void do_perform() throws RemoteException {
		WarningSign sign = (WarningSign)proxy;
		sign.setDeployed(deploy);
	}
}
