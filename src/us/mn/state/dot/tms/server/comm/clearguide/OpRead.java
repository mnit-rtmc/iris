/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.clearguide;

import java.io.IOException;
import us.mn.state.dot.tms.server.CommLinkImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to authenticate with a ClearGuide server and read data.
 * @author Michael Darter
 */
public class OpRead extends OpController<ClearGuideProperty> {

	/** Get null-safe string */
	static private String safe(String str) {
		return (str != null ? str : "");
	}

	/** Clear Guide auth tokens */
	private final Tokens cg_tokens;

	/** Feed name */
	private final String feed;

	/** Create a new operation to read
	 * @param cgtoks ClearGuide authentication tokens
	 * @param ci Controller
	 * @param fid Feed ID */
	protected OpRead(Tokens cgtoks, ControllerImpl ci, String fid) {
		super(PriorityLevel.POLL_HIGH, ci);
		cg_tokens = cgtoks;
		feed = fid;
	}

	/** Write a message to the protocol log */
	private void log(String msg) {
		ClearGuidePoller.slog(controller.getName() + " OpRead." + msg);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<ClearGuideProperty> phaseOne() {
		return new AuthenticatePhase();
	}

	/** Phase to authenticate with ClearGuide */
	protected class AuthenticatePhase extends Phase<ClearGuideProperty> {

		/** Maximum authentication retries */
		private final int MAX_AUTH_RETRY = 3;

		/** Retry count */
		private int auth_retry = 0;

		/** Authenticate with ClearGuide auth server
		 * @param msg Associated comm message.
		 * @return Null if done else next phase. */
		protected Phase<ClearGuideProperty> poll(
			CommMessage<ClearGuideProperty> msg)
			throws IOException
		{
			log("AuthenticatePhase.poll: -----------toks_valid=" +
				cg_tokens.valid());
			if (cg_tokens.valid()) {
				log("AuthenticatePhase.poll: tokens valid (" +
					"age=" + cg_tokens.getAge() +
					"s), skip auth");
				return new DmsMetricsPhase();
			}
			log("AuthenticatePhase.poll: must reauth");
			ClearGuideProperty prop =
				new ClearGuideProperty(true, cg_tokens);
			msg.add(prop);
			msg.queryProps(); // IOException on timeout
			log("AuthenticatePhase.poll: " + cg_tokens);
			return new DmsMetricsPhase();
		}
	}

	/** Phase to read DMS metrics from ClearGuide */
	protected class DmsMetricsPhase extends Phase<ClearGuideProperty>{

		/** Read DMS metrics
		 * @param msg Associated comm message.
		 * @return Null if done else next phase. */
		protected Phase<ClearGuideProperty> poll(
			CommMessage<ClearGuideProperty> msg) throws IOException
		{
			log("DmsMetricsPhase.poll: ------------");
			ClearGuideProperty prop =
				new ClearGuideProperty(false, cg_tokens);
			msg.add(prop);
			msg.queryProps(); // IOException on timeout
			if (!prop.api_json.isEmpty()) {
				ClearGuidePoller.cg_dms.add(prop.api_json);
				return null;
			} else {
				cg_tokens.clear(); // trigger reauth
				log("DmsMetricsPhase.poll: api call failed");
				return new AuthenticatePhase();
			}
		}
	}
}
