/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
 * Copyright (C) 2020 SRF Consulting Group
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
package us.mn.state.dot.tms.client.camera;

import java.util.ArrayList;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraTemplate;
import us.mn.state.dot.tms.CameraVidSourceOrder;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.IncidentDetail;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for camera templates
 *
 * @author Douglas Lau
 * @author Michael Janson
 */
@SuppressWarnings("serial")
public class CameraTemplateModel extends ProxyTableModel<CameraTemplate> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<CameraTemplate> descriptor(Session s) {
		return new ProxyDescriptor<CameraTemplate>(
			s.getSonarState().getCamTemplates(),
			true,	/* has_properties */
			true,	/* has_create_delete */
			true	/* has_name */
				) {
			@Override
			public CameraTemplateProperties createPropertiesForm(
				CameraTemplate ct)
			{
				return new CameraTemplateProperties(s, ct);
			}
		};
	}
	
	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<CameraTemplate>> createColumns() {
		ArrayList<ProxyColumn<CameraTemplate>> cols =
			new ArrayList<ProxyColumn<CameraTemplate>>(2);
		cols.add(new ProxyColumn<CameraTemplate>("camera.template", 200) {
			public Object getValueAt(CameraTemplate ct) {
				return ct.getLabel();
			}
			public boolean isEditable(CameraTemplate ct) {
				return canWrite(ct);
			}
			public void setValueAt(CameraTemplate ct,
				Object value)
			{
				ct.setLabel(value.toString());
			}
		});
		cols.add(new ProxyColumn<CameraTemplate>("camera.template.notes", 500)
			{
				public Object getValueAt(CameraTemplate ct) {
					return ct.getNotes();
				}
				public boolean isEditable(CameraTemplate ct) {
					return canWrite(ct);
				}
				public void setValueAt(CameraTemplate ct,
					Object value)
				{
					ct.setNotes(value.toString());
				}
			});
		return cols;
	}

	/** Create a new camera template table model */
	public CameraTemplateModel(Session s) {
		super(s, descriptor(s), 12);
	}
}
