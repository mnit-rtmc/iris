/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
 * Copyright (C) 2018  SRF Consulting Group
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
package us.mn.state.dot.tms.client.dms;

import java.awt.CardLayout;
import java.awt.Color;
import javax.swing.JPanel;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.SignConfig;

/**
 * A SignFacePanel displays a rendering of the face of a DMS, or a preview of
 * a message selected by the user.
 *
 * @author Douglas Lau
 */
public class SignFacePanel extends JPanel {

	/** Card name for dynamic-only sign panels */
	static private final String CARD_DYNAMIC = "Dynamic";

	/** Card name for hybrid sign panels */
	static private final String CARD_HYBRID = "Hybrid";

	/** Width of sign panel */
	static private final int WIDTH = 450;

	/** Height of sign panel */
	static private final int HEIGHT = 100;

	/** Card layout (dynamic / hybrid) */
	private final CardLayout cards = new CardLayout();

	/** Dynamic sign pixel panel */
	private final SignPixelPanel dynamic_pnl = new SignPixelPanel(HEIGHT,
		WIDTH, true);

	/** Hybrid sign panel */
	private final JPanel hybrid_pnl = new JPanel();

	/** Static sign graphic panel */
	private final StaticGraphicPanel hybrid_static_pnl =
		new StaticGraphicPanel(HEIGHT, WIDTH / 2);

	/** Hybrid sign pixel panel */
	private final SignPixelPanel hybrid_dynamic_pnl = new SignPixelPanel(
		HEIGHT, WIDTH / 2, true);

	/** Create a new sign face panel */
	public SignFacePanel() {
		setLayout(cards);
		hybrid_pnl.add(hybrid_static_pnl);
		hybrid_pnl.add(hybrid_dynamic_pnl);
		add(dynamic_pnl, CARD_DYNAMIC);
		add(hybrid_pnl, CARD_HYBRID);
	}

	/** Set the sign */
	public SignPixelPanel setSign(DMS dms) {
		SignConfig sc = (dms != null) ? dms.getSignConfig() : null;
		Graphic g = (dms != null) ? dms.getStaticGraphic() : null;
		if (g != null) {
			hybrid_static_pnl.setGraphic(g);
			hybrid_dynamic_pnl.setDimensions(sc);
			cards.show(this, CARD_HYBRID);
			dynamic_pnl.setDimensions(null);
			return hybrid_dynamic_pnl;
		} else {
			dynamic_pnl.setDimensions(sc);
			cards.show(this, CARD_DYNAMIC);
			hybrid_static_pnl.setGraphic(null);
			hybrid_dynamic_pnl.setDimensions(null);
			return dynamic_pnl;
		}
	}

	/** Set the filter color */
	public void setFilterColor(Color clr) {
		dynamic_pnl.setFilterColor(clr);
		hybrid_dynamic_pnl.setFilterColor(clr);
	}
}
