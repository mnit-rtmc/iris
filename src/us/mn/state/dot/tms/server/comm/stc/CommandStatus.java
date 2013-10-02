/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.stc;

/**
 * Command status for STC gate arms.
 *
 * @author Douglas Lau
 */
public enum CommandStatus {
	RESET,				/*  0 controller reset */
	OPEN_IN_PROGRESS,		/*  1 gate opening in progress */
	OPEN_COMPLETE,			/*  2 gate open complete */
	CLOSE_IN_PROGRESS,		/*  3 gate closing in progress */
	CLOSE_COMPLETE,			/*  4 gate close complete */
	STOPPED,			/*  5 open/close stopped by command */
	BLOCK_GATE_EDGE_SENSOR,		/*  6 open/close blocked by GEB */
	BLOCK_INHERENT_ENTRAPMENT,	/*  7 open/close blocked by IES */
	BLOCK_FREE_EXIT_LOOP,		/*  8 close blocked by EVD */
	BLOCK_RESET_SHADOW_LOOP,	/*  9 open/close blocked by SLD/HVD */
	BLOCK_INSIDE_OBSTRUCTION_LOOP,	/* 10 open/close blocked by IOLD */
	BLOCK_OUTSIDE_OBSTRUCTION_LOOP,	/* 11 open/close blocked by OOLD */
	BLOCK_PHOTO_EYE_OPEN,		/* 12 open/close blocked by PEO */
	BLOCK_PHOTO_EYE_CLOSE,		/* 13 open/close blocked by PEC */
	BLOCK_OPEN_INTERLOCK,		/* 14 open blocked by OI */
	BLOCK_LOCK_INTERLOCK,		/* 15 open blocked by LI */
	LOCK_LOW_BATTERY,		/* 16 open/closed locked by low batt */
	STOPPED_OBSTRUCTION,		/* 17 open stopped by IOLD and OOLD */
	BLOCK_STUCK,			/* 18 open/close blocked; stuck gate */
	BLOCK_LOCK_OPEN,		/* 19 close blocked by lock open */
	BLOCK_ENTRAPMENT,		/* 20 open/close blocked; entrapment */
	LIMIT_RELEARN,			/* 21 limit relearn in progress */
	FAULT,				/* 22 gate operator fault */
	ERROR,				/* 23 gate operator error */
	ALERT,				/* 24 gate operator alert */
	EM_OPEN_IN_PROGRESS,		/* 25 emergency open in progress */
	EM_OPEN_COMPLETE,		/* 26 emergency open complete */
	EM_CLOSE_IN_PROGRESS,		/* 27 emergency close in progress */
	EM_CLOSE_COMPLETE;		/* 28 emergency close complete */

	/** Lookup a command status from ordinal */
	static public CommandStatus fromOrdinal(int o) {
		for(CommandStatus cs: CommandStatus.values()) {
			if(cs.ordinal() == o)
				return cs;
		}
		return null;
	}

	/** Test if a command status is "reset" */
	static public boolean isReset(CommandStatus cs) {
		return cs == RESET;
	}

	/** Test if a command status is "normal" */
	static public boolean isNormal(CommandStatus cs) {
		switch(cs) {
		case RESET:
		case OPEN_IN_PROGRESS:
		case OPEN_COMPLETE:
		case CLOSE_IN_PROGRESS:
		case CLOSE_COMPLETE:
			return true;
		default:
			return false;
		}
	}

	/** Test if a command status is "fault" */
	static public boolean isFault(CommandStatus cs) {
		switch(cs) {
		case FAULT:
		case ERROR:
		case ALERT:
			return true;
		default:
			return false;
		}
	}

	/** Test if a command status is "opening" */
	static public boolean isOpening(CommandStatus cs) {
		switch(cs) {
		case OPEN_IN_PROGRESS:
		case EM_OPEN_IN_PROGRESS:
			return true;
		default:
			return false;
		}
	}

	/** Test if a command status is "closing" */
	static public boolean isClosing(CommandStatus cs) {
		switch(cs) {
		case CLOSE_IN_PROGRESS:
		case EM_CLOSE_IN_PROGRESS:
			return true;
		default:
			return false;
		}
	}

	/** Test if a command status is "open" */
	static public boolean isOpen(CommandStatus cs) {
		switch(cs) {
		case OPEN_COMPLETE:
		case EM_OPEN_COMPLETE:
		case BLOCK_FREE_EXIT_LOOP:
		case BLOCK_LOCK_OPEN:
			return true;
		default:
			return false;
		}
	}

	/** Test if a command status is "closed" */
	static public boolean isClosed(CommandStatus cs) {
		switch(cs) {
		case CLOSE_COMPLETE:
		case EM_CLOSE_COMPLETE:
		case BLOCK_OPEN_INTERLOCK:
		case BLOCK_LOCK_INTERLOCK:
			return true;
		default:
			return false;
		}
	}
}
