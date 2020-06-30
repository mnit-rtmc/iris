/*
 * IRIS -- Intelligent Roadway Information System
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
import java.util.LinkedList;

import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.EncoderTypeHelper;
import us.mn.state.dot.tms.VidSourceTemplate;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for video source templates
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class VidSrcTemplateModel extends ProxyTableModel<VidSourceTemplate> {

	/** Create a new camera table model */
	public VidSrcTemplateModel(Session s) {
		super(s, descriptor(s), 12);
	}
	
	/** Create a proxy descriptor */
	static public ProxyDescriptor<VidSourceTemplate> descriptor(Session s) {
		return new ProxyDescriptor<VidSourceTemplate>(
			s.getSonarState().getVidSrcTemplates(), false, false, false);
	}
	
	/** Get a table row sorter */
	@Override
	public RowSorter<ProxyTableModel<VidSourceTemplate>> createSorter() {
		TableRowSorter<ProxyTableModel<VidSourceTemplate>> sorter =
			new TableRowSorter<ProxyTableModel<VidSourceTemplate>>(this);
		sorter.setSortsOnUpdates(true);
		LinkedList<RowSorter.SortKey> keys =
			new LinkedList<RowSorter.SortKey>();
		keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(keys);
		return sorter;
	}
	
	/** Create the columns in the model. Note that these columns are view-
	 *  only (editing is done in another part of the VidSourceTemplateEditor).
     */
	@Override
	protected ArrayList<ProxyColumn<VidSourceTemplate>> createColumns() {
		ArrayList<ProxyColumn<VidSourceTemplate>> cols =
			new ArrayList<ProxyColumn<VidSourceTemplate>>(11);
		cols.add(new ProxyColumn<VidSourceTemplate>(
				"camera.video_source.template.name", 100) {
			public Object getValueAt(VidSourceTemplate vst) {
				return vst.getLabel();
			}
		});
		cols.add(new ProxyColumn<VidSourceTemplate>(
				"camera.video_source.template.config", 300) {
			public Object getValueAt(VidSourceTemplate vst) {
				return vst.getConfig();
			}
		});
		cols.add(new ProxyColumn<VidSourceTemplate>(
				"camera.video_source.template.codec", 50) {
			public Object getValueAt(VidSourceTemplate vst) {
				return vst.getCodec();
			}
		});
		cols.add(new ProxyColumn<VidSourceTemplate>(
				"camera.video_source.template.encoder", 100) {
			public Object getValueAt(VidSourceTemplate vst) {
				EncoderType et = EncoderTypeHelper.lookup(vst.getEncoder());
				if (et != null) {
					return (et.getMake() + " " + et.getModel() + " " +
					        et.getConfig()).trim();
				} return null;
			}
		});
		cols.add(new ProxyColumn<VidSourceTemplate>(
				"camera.video_source.template.notes", 200) {
			public Object getValueAt(VidSourceTemplate vst) {
				return vst.getNotes();
			}
		});
		cols.add(new ProxyColumn<VidSourceTemplate>(
				"camera.video_source.template.scheme", 50) {
			public Object getValueAt(VidSourceTemplate vst) {
				return vst.getScheme();
			}
		});
		cols.add(new ProxyColumn<VidSourceTemplate>(
				"camera.video_source.template.subnets", 100) {
			public Object getValueAt(VidSourceTemplate vst) {
				return vst.getSubnets();
			}
		});
		cols.add(new ProxyColumn<VidSourceTemplate>(
				"camera.video_source.template.default_port",
				50, Integer.class) {
			public Object getValueAt(VidSourceTemplate vst) {
				return vst.getDefaultPort();
			}
		});
		cols.add(new ProxyColumn<VidSourceTemplate>(
				"camera.video_source.template.latency", 50, Integer.class) {
			public Object getValueAt(VidSourceTemplate vst) {
				return vst.getLatency();
			}
		});
		cols.add(new ProxyColumn<VidSourceTemplate>(
				"camera.video_source.template.rez_height", 50, Integer.class) {
			public Object getValueAt(VidSourceTemplate vst) {
				return vst.getRezHeight();
			}
		});
		cols.add(new ProxyColumn<VidSourceTemplate>(
				"camera.video_source.template.rez_width", 50, Integer.class) {
			public Object getValueAt(VidSourceTemplate vst) {
				return vst.getRezWidth();
			}
		});
		return cols;
	}
}
