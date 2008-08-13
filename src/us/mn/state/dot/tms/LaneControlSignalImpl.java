/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2008  Minnesota Department of Transportation
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

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Map;
import us.mn.state.dot.tms.event.EventType;
import us.mn.state.dot.tms.event.SignStatusEvent;
import us.mn.state.dot.tms.comm.mndot.LCSCommandMessage;
import us.mn.state.dot.vault.FieldMap;

/**
 * Class representing a set of LCSModules.
 *
 * @author    Douglas Lau
 * @author    Erik Engstrom
 * @author    Timothy A. Johnson
 */
public class LaneControlSignalImpl extends TrafficDeviceImpl implements
	LaneControlSignal, Storable
{
	/** ObjectVault table name */
	public final static String tableName = "lcs";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Camera from which this can be seen */
	protected CameraImpl camera;

	/** Lane modules */
	protected final LCSModuleImpl[] modules;

	/**
	 * Flag to turn off the LCS If communication to the LCS has been lost,
	 * the flag is set.
	 */
	protected transient boolean turnOff;

	/**
	 * Create a new LaneControlSignalImpl object
	 *
	 * @param id                   Id for the new LaneControlSignal
	 * @param lanes                Number of lanes
	 */
	public LaneControlSignalImpl(String id, int lanes)
		throws TMSException, RemoteException
	{
		super( id );
		modules = new LCSModuleImpl[lanes];
		for(int i = 0; i < lanes; i++)
			modules[i] = new LCSModuleImpl();
		turnOff = true;
	}

	/**
	 * Create a LaneControlSignal from an ObjectVault field map.
	 *
	 * @param fields               The fields from the database table
	 */
	public LaneControlSignalImpl( FieldMap fields ) throws RemoteException {
		super( fields );
		modules = ( LCSModuleImpl[] ) fields.get( "modules" );
		turnOff = true;
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		// FIXME: implement this for SONAR
		return null;
	}

	/** Set the verification camera */
	public void setCamera(String id) throws TMSException {
		setCamera((CameraImpl)cameraList.getElement(id));
	}

	/** Set the verification camera */
	protected synchronized void setCamera(CameraImpl c)
		throws TMSException
	{
		if(c == camera)
			return;
		// FIXME: use toString() instead of getOID()
		if(c == null)
			store.update(this, "camera", "0");
		else
			store.update(this, "camera", c.getOID());
		camera = c;
	}

	/** Notify all observers for an update */
	public void notifyUpdate() {
		super.notifyUpdate();
		lcsList.update(id);
	}

	/**
	 * Set the states of the modules. State index zero represents lane one
	 * (the right-most lane).
	 */
	public void setSignals(int[] states, String user)
		throws ChangeVetoException
	{
		if(isActive()) {
			validateStates(states);
			for(int i = 0; i < modules.length; i++)
				modules[i].setState(states[i]);
			new LCSCommandMessage(this, states, user).start();
		}
	}

	/**
	 * Get the LCSModules
	 *
	 * @return   An array of LCSModules
	 */
	public LCSModule[] getModules() {
		LCSModule[] result = new LCSModule[modules.length];
		for(int i = 0; i < result.length; i++)
			result[i] = modules[i];
		return result;
	}

	/**
	 * Get the special function output settings for the given states
	 *
	 * @param states  The states to get the special function outputs for
	 * @return        An integer representation of the special funtion outputs
	 */
	public int getSFOSettings( int[] states ) {
		int settings = 0;
		for(int i = 0; i < states.length; i++)
			settings |= (1 << modules[i].getSFO(states[i]));
		return settings;
	}

	/** Get verification camera */
	public TrafficDevice getCamera() {
		return camera;
	}

	/**
	 * Get the state of the signal.
	 *
	 * @return                     The state of each LCSModule
	 */
	public int[] getSignals() throws RemoteException {
		int[] states = new int[modules.length];
		for(int i = 0; i < modules.length; i++)
			states[i] = modules[i].getState();
		return states;
	}

	/** Get the number of lanes for this LCS */
	public int getLanes() {
		return modules.length;
	}

	/** Get the current status code */
	public int getStatusCode() {
		if(!isActive())
			return STATUS_INACTIVE;
		int code = STATUS_OFF;
		for(int i = 0; i < modules.length; i++) {
			if(modules[i].getState() == LCSModule.ERROR)
				return STATUS_ERROR;
			else if(modules[i].getState() == LCSModule.RED ||
				modules[i].getState() == LCSModule.GREEN ||
				modules[i].getState() == LCSModule.YELLOW)
			{
				code = STATUS_ON;
			}
		}
		return code;
	}

	/**
	 * Process the field verification data.
	 *
	 * @param verifyData  The int representing the state of the verify bits.
	 * @param user        The user that is requesting the processing
	 */
	public void processVerifyData( int verifyData, String user ) {
		// If comm is lost, turn off when restored
		if(verifyData == -1)
			turnOff = true;
		boolean stateChanged = false;
		for ( int i = 0; i < modules.length; i++ ) {
			int initialState = modules[i].getState();
			modules[i].processVerifyData( verifyData );
			if(modules[i].getState() != initialState)
				stateChanged = true;
		}
		if(stateChanged) {
			logStatusChange(user);
			lcsList.update(id);
		}
	}

	/** Process the field verification data */
	public void processVerifyData(int verifyData) {
		processVerifyData(verifyData, null);
	}

	/** Send a command packet with the desired state of this device */
	public void commandSign() {
		int[] desiredStates = new int[modules.length];
		for(int i = 0; i < modules.length; i++) {
			if(modules[i].getDesiredState() == LCSModule.DARK)
				turnOff = true;
			desiredStates[i] = modules[i].getDesiredState();
		}
		if(turnOff) {
			for(int i = 0; i < modules.length; i++)
				desiredStates[i] = LCSModule.DARK;
		}
		try {
			setSignals(desiredStates, null);
			turnOff = false;
		} catch(ChangeVetoException e) {
			// Should never happen
			e.printStackTrace();
		}
	}

	/**
	 * Check to see if the modules are in the proper state.
	 *
	 * @return   True if the module states are verify to what they should be
	 */
	public boolean properStatus() {
		if(turnOff)
			return false;
		for(int i = 0; i < modules.length; i++) {
			if(!modules[i].stateOk())
				return false;
		}
		return true;
	}

	/** Set the controller to which this LCS is assigned */
	public void setController(String c) throws TMSException {
		super.setController(c);
		notifyUpdate();
	}

	/**
	 * Check to see if the state of each module is valid and the
	 * combination of module states is allowed.
	 */
	private void validateStates(int[] state) throws ChangeVetoException {
		if(state.length != modules.length) {
			throw new ChangeVetoException(
				"Number of state values must match lane count");
		}
		int n_dark = 0;
		for(int i = 0; i < state.length; i++) {
			switch(state[i]) {
				case LCSModule.DARK:
					n_dark++;
					break;
				case LCSModule.RED:
				case LCSModule.GREEN:
				case LCSModule.YELLOW:
					break;
				default:
					throw new ChangeVetoException(state[i] +
						" is not a valid state");
			}
		}
		if(n_dark > 0 && n_dark < modules.length) {
			throw new ChangeVetoException("LCS modules must be " +
				"either ALL ON or ALL OFF.");
		}
		return;
	}

	/**
	 * Log a status change to this LCS
	 *
	 * @param user  The person who changed the state of the LCS
	 */
	private void logStatusChange( String user ) {
		String message = "";
		String moduleState;
		for ( int i = 0; i < modules.length; i++ ) {
			if(modules[i].getState() == LCSModule.GREEN)
				moduleState = "GREEN";
			else if(modules[i].getState() == LCSModule.YELLOW)
				moduleState = "YELLOW";
			else if(modules[i].getState() == LCSModule.RED)
				moduleState = "RED";
			else if(modules[i].getState() == LCSModule.DARK)
				moduleState = "DARK";
			else if(modules[i].getState() == LCSModule.ERROR)
				moduleState = "ERROR";
			else
				moduleState = "UNDEFINED";
			message = message + moduleState + " ";
		}
		SignStatusEvent sse = new SignStatusEvent(getEventType(),
			getId(), message, user);
		try {
			sse.doStore();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/** Get the event type */
	protected EventType getEventType() {
		if(modules[0].getState() == LCSModule.DARK)
			return EventType.DMS_CLEARED;
		else
			return EventType.DMS_DEPLOYED;
	}
}
