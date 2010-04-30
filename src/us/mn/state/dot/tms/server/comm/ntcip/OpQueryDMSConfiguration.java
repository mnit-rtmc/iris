/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ntcip.mib1201.*;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to query the configuration of a DMS.
 *
 * @author Douglas Lau
 */
public class OpQueryDMSConfiguration extends OpDMS {

	/** DMS to query configuration */
	protected final DMSImpl dms;

	/** Create a new DMS query configuration object */
	public OpQueryDMSConfiguration(DMSImpl d) {
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
		protected Phase poll(CommMessage mess) throws IOException {
			GlobalMaxModules modules = new GlobalMaxModules();
			mess.add(modules);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + modules);
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
		protected Phase poll(CommMessage mess) throws IOException {
			ModuleMake make = new ModuleMake(mod);
			ModuleModel model = new ModuleModel(mod);
			ModuleVersion version = new ModuleVersion(mod);
			ModuleType m_type = new ModuleType(mod);
			mess.add(make);
			mess.add(model);
			mess.add(version);
			mess.add(m_type);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + make);
			DMS_LOG.log(dms.getName() + ": " + model);
			DMS_LOG.log(dms.getName() + ": " + version);
			DMS_LOG.log(dms.getName() + ": " + m_type);
			if(m_type.getEnum() == ModuleType.Enum.software) {
				dms.setMake(make.getValue());
				dms.setModel(model.getValue());
				dms.setVersion(version.getValue());
			}
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
		protected Phase poll(CommMessage mess) throws IOException {
			DmsSignAccess access = new DmsSignAccess();
			DmsSignType type = new DmsSignType();
			DmsSignHeight height = new DmsSignHeight();
			DmsSignWidth width = new DmsSignWidth();
			DmsHorizontalBorder h_border =
				new DmsHorizontalBorder();
			DmsVerticalBorder v_border = new DmsVerticalBorder();
			DmsLegend legend = new DmsLegend();
			DmsBeaconType beacon = new DmsBeaconType();
			DmsSignTechnology tech = new DmsSignTechnology();
			mess.add(access);
			mess.add(type);
			mess.add(height);
			mess.add(width);
			mess.add(h_border);
			mess.add(v_border);
			mess.add(legend);
			mess.add(beacon);
			mess.add(tech);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + access);
			DMS_LOG.log(dms.getName() + ": " + type);
			DMS_LOG.log(dms.getName() + ": " + height);
			DMS_LOG.log(dms.getName() + ": " + width);
			DMS_LOG.log(dms.getName() + ": " + h_border);
			DMS_LOG.log(dms.getName() + ": " + v_border);
			DMS_LOG.log(dms.getName() + ": " + legend);
			DMS_LOG.log(dms.getName() + ": " + beacon);
			DMS_LOG.log(dms.getName() + ": " + tech);
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
		protected Phase poll(CommMessage mess) throws IOException {
			VmsSignHeightPixels s_height =
				new VmsSignHeightPixels();
			VmsSignWidthPixels s_width = new VmsSignWidthPixels();
			VmsHorizontalPitch h_pitch = new VmsHorizontalPitch();
			VmsVerticalPitch v_pitch = new VmsVerticalPitch();
			VmsCharacterHeightPixels c_height =
				new VmsCharacterHeightPixels();
			VmsCharacterWidthPixels c_width =
				new VmsCharacterWidthPixels();
			mess.add(s_height);
			mess.add(s_width);
			mess.add(h_pitch);
			mess.add(v_pitch);
			mess.add(c_height);
			mess.add(c_width);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + s_height);
			DMS_LOG.log(dms.getName() + ": " + s_width);
			DMS_LOG.log(dms.getName() + ": " + h_pitch);
			DMS_LOG.log(dms.getName() + ": " + v_pitch);
			DMS_LOG.log(dms.getName() + ": " + c_height);
			DMS_LOG.log(dms.getName() + ": " + c_width);
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
