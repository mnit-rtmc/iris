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
// /***************************
// This module was enhanced by the Advanced Highway Maintenance &
// Construction Technology (AHMCT) Research Center at the University of
// California - Davis (UCD), in partnership with the California Department
// of Transportation (Caltrans) by Michael Darter, 06/13/08, and
// is provided as open-source software.
// ***************************/

package us.mn.state.dot.tms.client.rwis;

import java.util.logging.Logger;
import java.util.Properties;
import us.mn.state.dot.tdxml.XmlIncidentClient;
import us.mn.state.dot.tdxml.TdxmlException;
import us.mn.state.dot.tdxml.d10.CHPXmlIncidentClient;
import us.mn.state.dot.tms.client.rwis.TmsRwisLayer;
import us.mn.state.dot.trafmap.RwisLayer;


/**
 * A Caltrans D10 specific map layer for displaying incidents.
 *
 * @author Michael Darter
 */
public class D10RwisLayer extends TmsRwisLayer {

	/** Create a new D10 rwis layer */
	public D10RwisLayer(Properties props, Logger logger)
		throws TdxmlException
	{
		super(props, logger);
	}

	/** create incident client, called by constructor, may be overridden by
	 *  each agency. */
	protected XmlIncidentClient createIncidentClient(Properties props,
		Logger logger) throws TdxmlException
	{
        	return new CHPXmlIncidentClient(props, logger);
	}

}
