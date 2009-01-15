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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * Operation to query the current message on a DMS
 *
 * @author Douglas Lau
 */
public class DMSQueryMessage extends DMSOperation {

	/** Create a new DMS query status object */
	public DMSQueryMessage(DMSImpl d) {
		super(DEVICE_DATA, d);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		if(dms.hasObserver())
			return new QuerySourceAndBrightness();
		else
			return new QueryMessageSource();
	}

	/** Source table (memory type) or the currently displayed message */
	protected final DmsMsgTableSource source = new DmsMsgTableSource();

	/** Process the message table source from the sign controller */
	protected Phase processMessageSource() {
		DMS_LOG.log(dms.getId() + ": " + source);
		SignMessage m = dms.getMessage();
		if(DmsMessageMemoryType.isBlank(source.getMemory())) {
			/* The sign is blank. If IRIS says there should
			 * be a message on the sign, that's wrong and
			 * needs to be updated */
			if(!m.isBlank())
				dms.setMessageFromController("", 0);
		} else {
			/* The sign is not blank. If IRIS says it
			 * should be blank, then we need to query the
			 * current message on the sign. */
			if(m.isBlank())
				return new QueryCurrentMessage();
		}
		return null;
	}

	/** Phase to query the current message source */
	protected class QueryMessageSource extends Phase {

		/** Query the current message source */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(source);
			mess.getRequest();
			return processMessageSource();
		}
	}

	/** Phase to query the message source and brightness */
	protected class QuerySourceAndBrightness extends Phase {

		/** Photocell level status */
		protected final DmsIllumPhotocellLevelStatus p_level =
			new DmsIllumPhotocellLevelStatus();

		/** Brightness level status */
		protected final DmsIllumBrightLevelStatus b_level =
			new DmsIllumBrightLevelStatus();

		/** Light output status */
		protected final DmsIllumLightOutputStatus light =
			new DmsIllumLightOutputStatus();

		/** Illumination control */
		protected final DmsIllumControl control = new DmsIllumControl();

		/** Process the brightness values read from the sign */
		protected void processBrightness() {
			dms.setPhotocellLevel(p_level.getInteger());
			dms.setBrightnessLevel(b_level.getInteger());
			dms.setLightOutput(light.getPercent());
			if(control.isManual())
				dms.setManualBrightness(true);
			else {
				dms.setManualBrightness(false);
				if(!control.isPhotocell()) {
					DMS_LOG.log(dms.getId() + ": " +
						control);
				}
			}
		}

		/** Query the current message source */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(source);
			mess.add(p_level);
			mess.add(b_level);
			mess.add(light);
			mess.add(control);
			mess.getRequest();
			processBrightness();
			return processMessageSource();
		}
	}

	/** Phase to query the current message */
	protected class QueryCurrentMessage extends Phase {

		/** Query the current message */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageMultiString multi = new DmsMessageMultiString(
				DmsMessageMemoryType.CURRENT_BUFFER, 1);
			mess.add(multi);
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.CURRENT_BUFFER, 1);
			mess.add(status);
			DmsMessageTimeRemaining time =
				new DmsMessageTimeRemaining();
			mess.add(time);
			mess.getRequest();
			DMS_LOG.log(dms.getId() + ": " + multi);
			DMS_LOG.log(dms.getId() + ": " + status);
			DMS_LOG.log(dms.getId() + ": " + time);
			if(status.isValid() && time.getInteger() > 0) {
				dms.setMessageFromController(multi.getValue(),
					time.getInteger());
			}
			return null;
		}
	}
}
