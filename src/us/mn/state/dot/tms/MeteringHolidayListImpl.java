/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003  Minnesota Department of Transportation
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

import java.util.Calendar;
import java.util.Iterator;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * MeteringHolidayListImpl maintains a list of all metering holidays
 *
 * @author Douglas Lau
 */
class MeteringHolidayListImpl extends SortedListImpl
	implements MeteringHolidayList
{
	/** Create a new metering holiday list */
	public MeteringHolidayListImpl() throws RemoteException {
		super();
	}

	/** Add a metering holiday to the list */
	public synchronized TMSObject add(String key) throws TMSException,
		RemoteException
	{
		MeteringHolidayImpl holiday = (MeteringHolidayImpl)map.get(key);
		if(holiday != null) return holiday;
		holiday = new MeteringHolidayImpl(key);
		try { vault.save(holiday, getUserName()); }
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		map.put(key, holiday);
		Iterator iterator = map.keySet().iterator();
		for(int index = 0; iterator.hasNext(); index++) {
			String search = (String)iterator.next();
			if(key.equals(search)) {
				notifyAdd(index, key);
				break;
			}
		}
		return holiday;
	}

	/** Check if (plan) metering is allowed for the given date/time */
	public synchronized boolean allowMetering(Calendar stamp) {
		Iterator i = map.values().iterator();
		while(i.hasNext()) {
			MeteringHolidayImpl h = (MeteringHolidayImpl)i.next();
			if(h.matches(stamp)) return false;
		}
		return true;
	}

	/** Find a match in the metering holidays for the given date/time */
	public synchronized MeteringHoliday findMatch(Calendar stamp) {
		Iterator i = map.values().iterator();
		while(i.hasNext()) {
			MeteringHolidayImpl h = (MeteringHolidayImpl)i.next();
			if(h.matches(stamp)) return h;
		}
		return null;
	}
}
