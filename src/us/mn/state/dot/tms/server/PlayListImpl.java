/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.TMSException;

/**
 * Play list (camera sequence).
 *
 * @author Douglas lau
 */
public class PlayListImpl extends BaseObjectImpl implements PlayList {

	/** PlayList / Camera table mapping */
	static private TableMappingList mapping;

	/** Load all the play lists */
	static public void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, PlayListImpl.class);
		mapping = new TableMappingList(store, "iris", SONAR_TYPE,
			Camera.SONAR_TYPE);
		store.query("SELECT name, seq_num, description FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new PlayListImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("seq_num", seq_num);
		map.put("description", description);
		return map;
	}

	/** Create a new play list */
	public PlayListImpl(String n) {
		super(n);
	}

	/** Create a play list from database lookup */
	private PlayListImpl(ResultSet row) throws SQLException, TMSException {
		this(row.getString(1),              // name
		     (Integer) row.getObject(2),    // seq_num
		     row.getString(3)               // description
		);
	}

	/** Create a play list from database lookup */
	private PlayListImpl(String n, Integer sn, String d)
		throws TMSException
	{
		this(n);
		seq_num = sn;
		description = d;
		ArrayList<CameraImpl> cam_ls = new ArrayList<CameraImpl>();
		for (String o: mapping.lookup(this)) {
			cam_ls.add(lookupCamera(o));
		}
		cameras = cam_ls.toArray(new CameraImpl[0]);
	}

	/** Sequence number */
	private Integer seq_num;

	/** Set sequence number */
	@Override
	public void setSeqNum(Integer n) {
		seq_num = n;
	}

	/** Set sequence number */
	public void doSetSeqNum(Integer n) throws TMSException {
		if (n != seq_num) {
			if (n != null && (n < NUM_MIN || n > NUM_MAX))
				throw new ChangeVetoException("Invalid seq #");
			store.update(this, "seq_num", n);
			setSeqNum(n);
		}
	}

	/** Get sequence number */
	@Override
	public Integer getSeqNum() {
		return seq_num;
	}

	/** Description of the play list */
	private String description;

	/** Set the description */
	@Override
	public void setDescription(String d) {
		description = d;
	}

	/** Set the description */
	public void doSetDescription(String d) throws TMSException {
		if (!objectEquals(d, description)) {
			store.update(this, "description", d);
			setDescription(d);
		}
	}

	/** Get the description */
	@Override
	public String getDescription() {
		return description;
	}

	/** Cameras in the play list */
	private CameraImpl[] cameras = new CameraImpl[0];

	/** Set the cameras in the play list */
	@Override
	public void setCameras(Camera[] cams) {
		ArrayList<CameraImpl> cam_ls = new ArrayList<CameraImpl>();
		for (Camera c: cams) {
			if (c instanceof CameraImpl)
				cam_ls.add((CameraImpl) c);
		}
		cameras = cam_ls.toArray(new CameraImpl[0]);
	}

	/** Set the cameras in the play list */
	public void doSetCameras(Camera[] cams) throws TMSException {
		ArrayList<Storable> cam_ls = new ArrayList<Storable>();
		for (Camera c: cams) {
			if (c instanceof CameraImpl)
				cam_ls.add((CameraImpl) c);
			else
				throw new ChangeVetoException("Invalid camera");
		}
		mapping.update(this, cam_ls);
		setCameras(cams);
	}

	/** Get the cameras in the play list */
	@Override
	public Camera[] getCameras() {
		return cameras;
	}
}
