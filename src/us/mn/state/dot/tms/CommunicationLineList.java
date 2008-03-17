/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.io.PrintStream;
import java.rmi.RemoteException;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * CommunicationLineList
 *
 * @author Douglas Lau
 */
final class CommunicationLineList extends IndexedListImpl {

	/** Create a new communication line list */
	public CommunicationLineList() throws RemoteException {
		super( false );
	}

	/** Perform a download on all communication lines */
	synchronized void download() {
		for(TMSObjectImpl obj: list) {
			CommunicationLineImpl line = (CommunicationLineImpl)obj;
			line.download();
		}
	}

	/** Notify all observers for a status change */
	public synchronized void notifyStatus() {
		for(TMSObjectImpl obj: list) {
			CommunicationLineImpl line = (CommunicationLineImpl)obj;
			line.notifyStatus();
		}
		super.notifyStatus();
	}

	/** Perform a sign poll on all communiction lines */
	synchronized void pollSigns(Completer comp) {
		for(TMSObjectImpl obj: list) {
			CommunicationLineImpl line = (CommunicationLineImpl)obj;
			line.pollSigns(comp);
		}
	}

	/** Perform a 30-second poll on all communiction lines */
	synchronized void poll30Second(Completer comp) {
		for(TMSObjectImpl obj: list) {
			CommunicationLineImpl line = (CommunicationLineImpl)obj;
			line.poll30Second(comp);
		}
	}

	/** Perform a 5-minute poll on all communiction lines */
	synchronized void poll5Minute(Completer comp) {
		for(TMSObjectImpl obj: list) {
			CommunicationLineImpl line = (CommunicationLineImpl)obj;
			line.poll5Minute(comp);
		}
	}

	/** Perform a 1-hour poll on all communiction lines */
	synchronized void poll1Hour() {
		for(TMSObjectImpl obj: list) {
			CommunicationLineImpl line = (CommunicationLineImpl)obj;
			line.poll1Hour();
		}
	}

	/** Perform a 1-day poll on all communiction lines */
	synchronized void poll1Day() {
		for(TMSObjectImpl obj: list) {
			CommunicationLineImpl line = (CommunicationLineImpl)obj;
			line.poll1Day();
		}
	}

	/** Append a communication line to the list */
	public synchronized TMSObject append() throws TMSException,
		RemoteException
	{
		int index = list.size();
		CommunicationLineImpl line =
			new CommunicationLineImpl(index + 1);
		try {
			vault.save(line, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		list.add(line);
		notifyAdd(index, line.toString());
		return line;
	}

	/** Remove a CommunicationLine from the list */
	public synchronized void removeLast() throws TMSException {
		if(list.isEmpty())
			throw new ChangeVetoException("List is empty");
		CommunicationLineImpl line =
			(CommunicationLineImpl)list.get( list.size() - 1 );
		super.removeLast();
		line.close();
	}

	/** Find a communication line in the list */
	public synchronized CommunicationLineImpl findLine(
		CommunicationLine line)
	{
		for(TMSObjectImpl obj: list) {
			CommunicationLineImpl l = (CommunicationLineImpl)obj;
			if(l.equals(line))
				return l;
		}
		return null;
	}

	/** Find a controller in the list */
	public synchronized ControllerImpl findController(
		Controller controller)
	{
		for(TMSObjectImpl obj: list) {
			CommunicationLineImpl l = (CommunicationLineImpl)obj;
			ControllerImpl c = l.findController(controller);
			if(c != null)
				return c;
		}
		return null;
	}

	/** Print the current status of the lines */
	public synchronized void print(PrintStream ps) {
		for(TMSObjectImpl obj: list) {
			CommunicationLineImpl l = (CommunicationLineImpl)obj;
			l.print(ps);
		}
	}
}
