/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

package us.mn.state.dot.tms.client.reports;

import java.util.List;
import java.util.UUID;

import javax.swing.SwingWorker;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.RptConduit;
import us.mn.state.dot.tms.RptConduitHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.reports.RptRequest;
import us.mn.state.dot.tms.reports.RptResults;
import us.mn.state.dot.tms.utils.I18N;

/** Worker thread that handles the client side of
 *  submitting a report-request to the server.
 * 
 * If the server does not return a report within
 * 0.5 seconds, this displays a progress dialog
 * that provides a button to cancel the request.
 * 
 * Once the server returns a report, this closes
 * the progress dialog (if shown) and displays a
 * RptResultForm containing the report data.
 * 
 * @author John L. Stanley - SRF Consulting
 */
public class RptProcess extends SwingWorker<String,Integer> {

	protected final Session session;
	protected final String ssRequest;

	protected IndProgressMonitor indMon = null;
	protected RptConduit conduit = null;

	private RptProcess(Session s, String ssReq) {
		this.session = s;
		this.ssRequest = ssReq;
	}

	/** Submit a report request and wait for the results. */
	@Override
	protected String doInBackground() throws Exception {
		TypeCache<RptConduit> cache = null;
		String conduitName = null;
		String ssResults = null;

		indMon = new IndProgressMonitor(session.getDesktop(),
				I18N.get("report.generating"), null);

		cache = session.getSonarState().getRptConduits();

		int x = 0;
		while (true) {
			if (indMon.isCanceled())
				break;

			// Request new conduit from SONAR.
			if (conduitName == null) {
				UUID uuid = UUID.randomUUID();
				conduitName = uuid.toString();
				cache.createObject(conduitName);
			}

			// If SONAR has finished generating
			// the conduit, submit a request.
			if (conduit == null) {
				conduit = RptConduitHelper.lookup(conduitName);
				if (conduit != null)
					conduit.setRequest(ssRequest);
			}

			// If server has returned a report, exit.
			if (conduit != null) {
				ssResults = conduit.getResults();
				if ((ssResults != null)
				 && !ssResults.isEmpty()
				 && (ssResults.charAt(0) == '{'))
					break;
			}

			// update progress monitor and take a nap
			if (x < 75)
				x += 2;
			publish(x);
			Thread.sleep(100);
		}
		return ssResults;
	}
	
	/** Update progress monitor */
	@Override
	protected void process(List<Integer> chunk) {
		indMon.update();
	}
	
	/** Display the results (if any) and cleanup. */
	@Override
	protected void done() {
		try {
			if ((indMon != null)
			 && indMon.isCanceled()) {
				if (conduit != null)
					conduit.setCanceled(true);
				return;
			}

			String ssResults = get();
			if (ssResults != null) {
				RptResults res = new RptResults(ssResults);
				RptResultsForm form = new RptResultsForm(res);
				session.getDesktop().show(form);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (conduit != null) {
				conduit.destroy();
				conduit = null;
			}
			if (indMon != null) {
				indMon.close();
				indMon = null;
			}
		}
	}
	
	/** Static client-side method to submit a report-request */
	static public void submitRequest(Session s, RptRequest req) {
		String ssReq = req.toReqString();
		RptProcess proc = new RptProcess(s, ssReq);
		proc.execute();
	}
}
