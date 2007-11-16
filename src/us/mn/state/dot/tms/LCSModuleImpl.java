/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
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

/**
 * The LCSModuleImpl is the implementation of the LCSModule remote interface.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @author Douglas Lau
 */
public class LCSModuleImpl extends TMSObjectImpl implements LCSModule,
	Storable
{
	/** ObjectVault table name */
	static public final String tableName = "lcs_module";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Constant for number of special function outputs */
	static protected final int SFO_COUNT = 40;

	/** Constant for number of special function inputs */
	static protected final int SFI_COUNT = 40;

	/** Special Function Output setting for RED */
	protected int sfoRed;

	/** Special Function Output setting for YELLOW */
	protected int sfoYellow;

	/** Special Function Output setting for GREEN */
	protected int sfoGreen;

	/** Special Function Input setting for RED */
	protected int sfiRed;

	/** Special Function Input setting for YELLOW */
	protected int sfiYellow;

	/** Special Function Input setting for GREEN */
	protected int sfiGreen;

	/** Actual state of the module */
	protected transient int actualState;

	/** Desired state of the module */
	protected transient int desiredState;

	/** Create a new LCSModule */
	public LCSModuleImpl() throws RemoteException {
		actualState = ERROR;
		desiredState = DARK;
	}

	/** Set the special function output for the GREEN state */
	protected void setGreenOutput(int value) throws TMSException {
		store.update(this, "sfoGreen", value);
		sfoGreen = value;
	}

	/** Set the special function output for the YELLOW state */
	protected void setYellowOutput(int value) throws TMSException {
		store.update(this, "sfoYellow", value);
		sfoYellow = value;
	}

	/** Set the special function output for the RED state */
	protected void setRedOutput(int value) throws TMSException {
		store.update(this, "sfoRed", value);
		sfoRed = value;
	}

	/** Set the special function output for a module state */
	public synchronized void setSFO(int state, int value)
		throws TMSException
	{
		if(value > SFO_COUNT) {
			throw new ChangeVetoException(
				"Invalid output number: " + value);
		}
		switch(state) {
			case GREEN:
				setGreenOutput(value);
				return;
			case YELLOW:
				setYellowOutput(value);
				return;
			case RED:
				setRedOutput(value);
				return;
		}
		throw new ChangeVetoException("Module state " + state +
			" is invalid");
	}

	/** Set the special function input for the GREEN verify */
	protected void setGreenInput(int value) throws TMSException {
		store.update(this, "sfiGreen", value);
		sfiGreen = value;
	}

	/** Set the special function input for the YELLOW verify */
	protected void setYellowInput(int value) throws TMSException {
		store.update(this, "sfiYellow", value);
		sfiYellow = value;
	}

	/** Set the special function input for the RED verify */
	protected void setRedInput(int value) throws TMSException {
		store.update(this, "sfiRed", value);
		sfiRed = value;
	}

	/** Set the special function input for a module state */
	public synchronized void setSFI(int state, int value)
		throws TMSException
	{
		if(value > SFI_COUNT) {
			throw new ChangeVetoException(
				"Invalid input number: " + value);
		}
		switch(state) {
			case GREEN:
				setGreenInput(value);
				return;
			case YELLOW:
				setYellowInput(value);
				return;
			case RED:
				setRedInput(value);
				return;
		}
		throw new ChangeVetoException("Module state " + state +
			" is invalid");
	}

	/**
	 * Get the special function output setting a given state.
	 *
	 * @param state       The state to get the SFO setting of.
	 * @return            The SFO setting for the state given. <p>
	 *
	 *      Returns -1 if the state does not have a sfo setting.
	 */
	public int getSFO(int state) {
		switch(state) {
			case GREEN:
				return sfoGreen;
			case YELLOW:
				return sfoYellow;
			case RED:
				return sfoRed;
			default:
				return -1;
		}
	}

	/**
	 * Get an integer representing the special function input setting for
	 * the given state.
	 *
	 * @param state       The state to get the SFI setting of.
	 * @return            The SFI setting for the state given. <p>
	 *
	 *      Returns -1 if the state does not have a sfi setting.
	 */
	public int getSFI(int state) {
		switch(state) {
			case GREEN:
				return sfiGreen;
			case YELLOW:
				return sfiYellow;
			case RED:
				return sfiRed;
			default:
				return -1;
		}
	}

	/** Return true if the state is what it should be */
	public boolean stateOk() {
		return actualState == desiredState;
	}

	/** Set the state */
	protected void setState(int state) {
		desiredState = state;
	}

	/** Get the state */
	protected int getState() {
		return actualState;
	}

	/** Get the state that this module should be in */
	protected int getDesiredState() {
		return desiredState;
	}

	/**
	 * Set the state of this module according to data received from the
	 * controller.
	 */
	protected void processVerifyData(int verifyData) {
		if(verifyData == -1) {
			actualState = ERROR;
			return;
		}
		int fieldState = getFieldState(verifyData);
		int stateToSet = ERROR;
		if(isVerifiable(desiredState))
			stateToSet = fieldState;
		else if(fieldState == DARK)
			stateToSet = desiredState;
		if(actualState != stateToSet)
			actualState = stateToSet;
		if(actualState == ERROR)
			desiredState = DARK;
	}

	/** Return true if the state is verifyable */
	private boolean isVerifiable(int state) {
		if(state == DARK)
			return true;
		return getSFI(state) >= 0;
	}

	/** Check if the given state if verified */
	private boolean isStateVerified(int state, int data) {
		int input = getSFI(state);
		if(input >= 0) {
			int mask = 1 << input;
			if((data & mask) == mask)
				return true;
		}
		return false;
	}

	/** Get the field state represented by the fieldData */
	private int getFieldState(int data) {
		int statesFound = 0;
		int fieldState = DARK;
		if(isStateVerified(YELLOW, data)) {
			statesFound++;
			fieldState = YELLOW;
		}
		if(isStateVerified(RED, data)) {
			statesFound++;
			fieldState = RED;
		}
		if(isStateVerified(GREEN, data)) {
			statesFound++;
			fieldState = GREEN;
		}
		if(statesFound <= 1)
			return fieldState;
		else
			return ERROR;
	}
}
