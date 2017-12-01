/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.GroupChecker;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.User;

/**
 * Play list (camera sequence).
 *
 * @author Douglas Lau
 */
public interface PlayList extends SonarObject {

	/** Minimum play list number */
	int NUM_MIN = 1;

	/** Maximum play list number */
	int NUM_MAX = 9999;

	/** SONAR type name */
	String SONAR_TYPE = "play_list";

	/** Set play list number */
	void setNum(Integer n);

	/** Get play list number */
	Integer getNum();

	/** Set the cameras in the play list */
	void setCameras(Camera[] cams);

	/** Get the cameras in the play list */
	Camera[] getCameras();

	/** Group privilege checker */
	GroupChecker GROUP_CHECKER = new GroupChecker() {
		public boolean checkGroup(Name name, User u, String g) {
			if ("user".equals(g)) {
				String n = "PL_" + u.getName();
				return name.getObjectPart().equals(n);
			} else
				return false;
		}
	};
}
