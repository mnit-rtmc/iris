/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.DeviceImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1201.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;

/**
 * Operation to query the hardware/software modules.
 *
 * @author Douglas Lau
 */
public class OpQueryModules extends OpNtcip {

	/** Module table */
	private final ModuleTable mod_table = new ModuleTable();

	/** Create a new module query operation */
	public OpQueryModules(DeviceImpl d) {
		super(PriorityLevel.DOWNLOAD, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryModuleCount();
	}

	/** Phase to query the number of modules */
	protected class QueryModuleCount extends Phase {

		/** Query the number of modules */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(mod_table.modules);
			mess.queryProps();
			logQuery(mod_table.modules);
			return !mod_table.isDone() ? new QueryModule() : null;
		}
	}

	/** Phase to query a module */
	protected class QueryModule extends Phase {

		/** Query the module make, model and version */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ModuleTable.Row tr = mod_table.addRow();
			mess.add(tr.make);
			mess.add(tr.model);
			mess.add(tr.version);
			mess.add(tr.m_type);
			mess.queryProps();
			logQuery(tr.make);
			logQuery(tr.model);
			logQuery(tr.version);
			logQuery(tr.m_type);
			return !mod_table.isDone() ? this : null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			controller.setSetupNotify(mod_table.toJson());
		super.cleanup();
	}
}
