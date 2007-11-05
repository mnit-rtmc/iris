/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2002  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * ErrorCounter
 *
 * @author Douglas Lau
 */
public interface ErrorCounter extends Remote {

	/** Good counter index */
	public int TYPE_GOOD = 0;

	/** Fail counter index */
	public int TYPE_FAIL = 1;

	/** Counter types */
	public String[] TYPES = { "Good", "Fail" };

	/** Current state "now" counter index */
	public int PERIOD_NOW = 0;

	/** 5 minute counter index */
	public int PERIOD_5_MIN = 1;

	/** 1 hour counter index */
	public int PERIOD_1_HOUR = 2;

	/** 1 day counter index */
	public int PERIOD_1_DAY = 3;

	/** Counter periods */
	public String[] PERIODS = {
		"Now", "5 Minute", "1 Hour", "1 Day"
	};

	/** Get counters for all types and all time periods */
	public int[][] getCounters() throws RemoteException;
}
