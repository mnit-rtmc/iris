/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to query the configuration of a DMS.
 *
 * @author Douglas Lau
 */
public class DMSQueryConfiguration extends DMSOperation {

	/** DMS to query configuration */
	protected final DMSImpl dms;

	/** Create a new DMS query configuration object */
	public DMSQueryConfiguration(DMSImpl d) {
		super(DOWNLOAD, d);
		dms = d;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new QueryModuleCount();
	}

	/** Phase to query the number of modules */
	protected class QueryModuleCount extends Phase {

		/** Query the number of modules */
		protected Phase poll(AddressedMessage mess) throws IOException {
			GlobalMaxModules modules = new GlobalMaxModules();
			mess.add(modules);
			mess.getRequest();
			return new QueryModules(modules.getInteger());
		}
	}

	/** Phase to query the module information */
	protected class QueryModules extends Phase {

		/** Count of rows in the module table */
		protected final int count;

		/** Module number to query */
		protected int mod = 1;

		/** Create a queryModules phase */
		protected QueryModules(int c) {
			count = c;
		}

		/** Query the module make, model and version */
		protected Phase poll(AddressedMessage mess) throws IOException {
			ModuleMake make = new ModuleMake(mod);
			mess.add(make);
			ModuleModel model = new ModuleModel(mod);
			mess.add(model);
			ModuleVersion version = new ModuleVersion(mod);
			mess.add(version);
			ModuleType m_type = new ModuleType(mod);
			mess.add(m_type);
			mess.getRequest();
			if(m_type.getInteger() ==
			   ModuleType.Enum.software.ordinal())
			{
				dms.setMake(make.getValue());
				dms.setModel(model.getValue());
				dms.setVersion(version.getValue());
			}
			DMS_LOG.log(dms.getName() + ": " + make);
			DMS_LOG.log(dms.getName() + ": " + model);
			DMS_LOG.log(dms.getName() + ": " + version);
			DMS_LOG.log(dms.getName() + ": " + m_type);
			mod += 1;
			if(mod < count)
				return this;
			else
				return new QueryDmsInfo();
		}
	}

	/** Phase to query the DMS information */
	protected class QueryDmsInfo extends Phase {

		/** Query the DMS information */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsSignAccess access = new DmsSignAccess();
			mess.add(access);
			DmsSignType type = new DmsSignType();
			mess.add(type);
			DmsSignHeight height = new DmsSignHeight();
			mess.add(height);
			DmsSignWidth width = new DmsSignWidth();
			mess.add(width);
			DmsHorizontalBorder h_border =
				new DmsHorizontalBorder();
			mess.add(h_border);
			DmsVerticalBorder v_border = new DmsVerticalBorder();
			mess.add(v_border);
			DmsLegend legend = new DmsLegend();
			mess.add(legend);
			DmsBeaconType beacon = new DmsBeaconType();
			mess.add(beacon);
			DmsSignTechnology tech = new DmsSignTechnology();
			mess.add(tech);
			mess.getRequest();
			dms.setSignAccess(access.getValue());
			dms.setDmsType(type.getValueEnum());
			dms.setFaceHeight(height.getInteger());
			dms.setFaceWidth(width.getInteger());
			dms.setHorizontalBorder(h_border.getInteger());
			dms.setVerticalBorder(v_border.getInteger());
			dms.setLegend(legend.getValue());
			dms.setBeaconType(beacon.getValue());
			dms.setTechnology(tech.getValue());
			return new QueryVmsInfo();
		}
	}

	/** Phase to query the VMS information */
	protected class QueryVmsInfo extends Phase {

		/** Query the VMS information */
		protected Phase poll(AddressedMessage mess) throws IOException {
			VmsSignHeightPixels s_height =
				new VmsSignHeightPixels();
			mess.add(s_height);
			VmsSignWidthPixels s_width = new VmsSignWidthPixels();
			mess.add(s_width);
			VmsHorizontalPitch h_pitch = new VmsHorizontalPitch();
			mess.add(h_pitch);
			VmsVerticalPitch v_pitch = new VmsVerticalPitch();
			mess.add(v_pitch);
			VmsCharacterHeightPixels c_height =
				new VmsCharacterHeightPixels();
			mess.add(c_height);
			VmsCharacterWidthPixels c_width =
				new VmsCharacterWidthPixels();
			mess.add(c_width);
			mess.getRequest();
			dms.setHeightPixels(s_height.getInteger());
			dms.setWidthPixels(s_width.getInteger());
			dms.setHorizontalPitch(h_pitch.getInteger());
			dms.setVerticalPitch(v_pitch.getInteger());
			// NOTE: these must be set last
			dms.setCharHeightPixels(c_height.getInteger());
			dms.setCharWidthPixels(c_width.getInteger());
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		dms.setConfigure(success);
		super.cleanup();
	}
}
