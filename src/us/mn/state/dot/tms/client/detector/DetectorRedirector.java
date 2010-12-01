/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.detector;

import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.R_Node;

/**
 * An abstraction to redirect detector methods.
 *
 * @author Douglas Lau
 */
public class DetectorRedirector implements Detector {

	/** Detector to redirect */
	protected Detector detector;

	/** Set the detector to redirect */
	public void setDetector(Detector det) {
		detector = det;
	}

	/** Get the type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Get the name */
	public String getName() {
		Detector det = detector;
		if(det != null)
			return det.getName();
		else
			return "";
	}

	/** Destroy the detector */
	public void destroy() {
		Detector det = detector;
		if(det != null)
			det.destroy();
	}

	/** Set the controller for the I/O */
	public void setController(Controller c) {
		Detector det = detector;
		if(det != null)
			det.setController(c);
	}

	/** Get the controller for the I/O */
	public Controller getController() {
		Detector det = detector;
		if(det != null)
			return det.getController();
		else
			return null;
	}

	/** Set the controller I/O pin number */
	public void setPin(int p) {
		Detector det = detector;
		if(det != null)
			det.setPin(p);
	}

	/** Get the controller I/O pin number */
	public int getPin() {
		Detector det = detector;
		if(det != null)
			return det.getPin();
		else
			return 0;
	}

	/** Set the administrator notes */
	public void setNotes(String n) {
		Detector det = detector;
		if(det != null)
			det.setNotes(n);
	}

	/** Get the administrator notes */
	public String getNotes() {
		Detector det = detector;
		if(det != null)
			return det.getNotes();
		else
			return "";
	}

	/** Request a device operation (query message, test pixels, etc.) */
	public void setDeviceRequest(int r) {
		Detector det = detector;
		if(det != null)
			det.setDeviceRequest(r);
	}

	/** Get the operation description */
	public String getOperation() {
		Detector det = detector;
		if(det != null)
			return det.getOperation();
		else
			return "";
	}

	/** Get the operation status */
	public String getOpStatus() {
		Detector det = detector;
		if(det != null)
			return det.getOpStatus();
		else
			return "";
	}

	/** Set the r_node (roadway network node) */
	public void setR_Node(R_Node n) {
		Detector det = detector;
		if(det != null)
			det.setR_Node(n);
	}

	/** Get the r_node (roadway network node) */
	public R_Node getR_Node() {
		Detector det = detector;
		if(det != null)
			return det.getR_Node();
		else
			return null;
	}

	/** Set the lane type */
	public void setLaneType(short t) {
		Detector det = detector;
		if(det != null)
			det.setLaneType(t);
	}

	/** Get the lane type */
	public short getLaneType() {
		Detector det = detector;
		if(det != null)
			return det.getLaneType();
		else
			return 0;
	}

	/** Set the lane number */
	public void setLaneNumber(short n) {
		Detector det = detector;
		if(det != null)
			det.setLaneNumber(n);
	}

	/** Get the lane number */
	public short getLaneNumber() {
		Detector det = detector;
		if(det != null)
			return det.getLaneNumber();
		else
			return 0;
	}

	/** Set the abandoned status */
	public void setAbandoned(boolean a) {
		Detector det = detector;
		if(det != null)
			det.setAbandoned(a);
	}

	/** Get the abandoned status */
	public boolean getAbandoned() {
		Detector det = detector;
		if(det != null)
			return det.getAbandoned();
		else
			return false;
	}

	/** Set the Force Fail status */
	public void setForceFail(boolean f) {
		Detector det = detector;
		if(det != null)
			det.setForceFail(f);
	}

	/** Get the Force Fail status */
	public boolean getForceFail() {
		Detector det = detector;
		if(det != null)
			return det.getForceFail();
		else
			return false;
	}

	/** Set the average field length (feet) */
	public void setFieldLength(float f) {
		Detector det = detector;
		if(det != null)
			det.setFieldLength(f);
	}

	/** Get the average field length (feet) */
	public float getFieldLength() {
		Detector det = detector;
		if(det != null)
			return det.getFieldLength();
		else
			return 22f;
	}

	/** Set the fake expression */
	public void setFake(String f) {
		Detector det = detector;
		if(det != null)
			det.setFake(f);
	}

	/** Get the fake expression */
	public String getFake() {
		Detector det = detector;
		if(det != null)
			return det.getFake();
		else
			return "";
	}
}
