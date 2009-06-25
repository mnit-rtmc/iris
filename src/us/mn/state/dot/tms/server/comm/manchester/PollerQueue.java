/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.manchester;

import java.util.HashMap;

import us.mn.state.dot.tms.server.CameraImpl;

/**
 * A static thread which is responsible for streaming Manchester PTZ commands.
 *
 * @author Timothy A. Johnson
 */

public class PollerQueue extends Thread {

	private static final HashMap<String, MoveCamera> commands =
		new HashMap<String, MoveCamera>();

	/** The number of milliseconds between commands. */
	private static final int CMD_INTERVAL = 60;
	
	/** Add a PTZ command to the queue.
	 * cmd The MoveCamera command to be added.
	 */
	public static void addCommand(CameraImpl c, MoveCamera cmd){
		if( isStopCmd(cmd) ){
			cmd.start();
			synchronized(commands) { commands.remove(c.getName()); }
		}else{
			synchronized(commands) { commands.put(c.getName(), cmd); }
		}
	}
	
	/**
	 * Test whether or not the command is to stop all PTZ.
	 * @param cmd The command to check
	 * @return boolean True if it is a stop command, false otherwise.
	 */
	protected static boolean isStopCmd(MoveCamera cmd){
		return cmd.pan == 0 && cmd.tilt == 0 && cmd.zoom == 0;
	}
	
	public void run(){
		while(true){
			try{
				synchronized(commands){
					for(MoveCamera cmd : commands.values()) cmd.start();
				}
				Thread.sleep(PollerQueue.CMD_INTERVAL);
			}catch(Exception e){
				//swallow the exception and resume
			}
		}
	}
}
