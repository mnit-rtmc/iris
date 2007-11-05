/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2006  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.util.EventListener;

/**
 * The listener that's notified when the selected TMS object changes.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public interface TmsSelectionListener extends EventListener {

	/** Called whenever the selected TMS object changes */
	void selectionChanged(TmsSelectionEvent e);

	/** Refresh the status of the selected TMS object */
	void refreshStatus();

	/** Update the configuration of the selected TMS object */
	void refreshUpdate();
}
