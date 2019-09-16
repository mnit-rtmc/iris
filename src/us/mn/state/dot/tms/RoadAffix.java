/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2019  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.SonarObject;

/**
 * A road affix is a prefix or suffix to a road name which can be replaced or
 * trimmed for traveler information display.
 *
 * @author Douglas Lau
 */
public interface RoadAffix extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "road_affix";

	/** Set flag to indicate prefix (true) or suffix (false) */
	void setPrefix(boolean p);

	/** Get flag to indicate prefix (true) or suffix (false) */
	boolean getPrefix();

	/** Set the traveler information fixup */
	void setFixup(String f);

	/** Get the traveler information fixup */
	String getFixup();

	/** Set flag to allow retaining the affix */
	void setAllowRetain(boolean r);

	/** Get flag to allow retaining the affix */
	boolean getAllowRetain();
}
