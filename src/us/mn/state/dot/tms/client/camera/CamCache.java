/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * Cache for camera-related proxy objects.
 *
 * @author Douglas Lau
 */
public class CamCache {

	/** Test if a camera preset is available for devices */
	static private boolean isAvailable(CameraPreset cp) {
		return (Direction.fromOrdinal(cp.getDirection())
			== Direction.UNKNOWN) && !cp.getAssigned();
	}

	/** Cache of encoder types */
	private final TypeCache<EncoderType> encoder_types;

	/** Get the encoder types object cache */
	public TypeCache<EncoderType> getEncoderTypes() {
		return encoder_types;
	}

	/** Encoder type model */
	private final ProxyListModel<EncoderType> enc_type_mdl;

	/** Get the encoder type model */
	public ProxyListModel<EncoderType> getEncoderTypeModel() {
		return enc_type_mdl;
	}

	/** Cache of cameras */
	protected final TypeCache<Camera> cameras;

	/** Get the camera cache */
	public TypeCache<Camera> getCameras() {
		return cameras;
	}

	/** Camera proxy list model */
	protected final ProxyListModel<Camera> camera_model;

	/** Get the camera list model */
	public ProxyListModel<Camera> getCameraModel() {
		return camera_model;
	}

	/** Cache of camera presets */
	private final TypeCache<CameraPreset> presets;

	/** Get the camera preset cache */
	public TypeCache<CameraPreset> getPresets() {
		return presets;
	}

	/** Unassigned camera preset list model */
	private final ProxyListModel<CameraPreset> preset_model;

	/** Get the unassigned camera preset list model */
	public ProxyListModel<CameraPreset> getPresetModel() {
		return preset_model;
	}

	/** Cache of video monitor proxies */
	protected final TypeCache<VideoMonitor> monitors;

	/** Get the video monitor type cache */
	public TypeCache<VideoMonitor> getVideoMonitors() {
		return monitors;
	}

	/** VideoMonitor proxy list model */
	protected final ProxyListModel<VideoMonitor> monitor_model;

	/** Get the VideoMonitor list model */
	public ProxyListModel<VideoMonitor> getMonitorModel() {
		return monitor_model;
	}

	/** Create a new camera cache */
	public CamCache(SonarState client) throws IllegalAccessException,
		NoSuchFieldException
	{
		encoder_types = new TypeCache<EncoderType>(EncoderType.class,
			client);
		enc_type_mdl = new ProxyListModel<EncoderType>(encoder_types);
		enc_type_mdl.initialize();
		cameras = new TypeCache<Camera>(Camera.class, client);
		camera_model = new ProxyListModel<Camera>(cameras);
		camera_model.initialize();
		presets = new TypeCache<CameraPreset>(CameraPreset.class,
			client);
		preset_model = new ProxyListModel<CameraPreset>(presets) {
			@Override
			protected boolean check(CameraPreset cp) {
				return isAvailable(cp);
			}
		};
		preset_model.initialize();
		monitors = new TypeCache<VideoMonitor>(VideoMonitor.class,
			client);
		monitor_model = new ProxyListModel<VideoMonitor>(monitors);
		monitor_model.initialize();
	}

	/** Populate the type caches */
	public void populate(SonarState client) {
		client.populateReadable(encoder_types);
		client.populateReadable(cameras);
		if (client.canRead(Camera.SONAR_TYPE))
			cameras.ignoreAttribute("operation");
		client.populateReadable(presets);
		client.populateReadable(monitors);
	}
}
