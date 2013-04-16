/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  University of Minnesota
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

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Date;
import java.util.HashMap;

/**
 * Helper class for Density Metering (DensMPlanState).
 * Used for loading global and ramp based parameters from
 * parameter XML file
 *
 * @author Anupam
 */
public class DensMeterLoadXML {

	/** Path to the XML File used for reading parameters */
	static protected String XML_PATH;

	/** The last modified timestamp for the Parameter Data File readed from */
	static protected Date last_paramfile_load = null;

	/** Boolean representing if the Global Node in XML has been loaded into memory or not */
	static protected boolean isGlobalLoaded = false;

	/** Boolean represeting whether the Ramps/Meters block in XML has been loaded into memory */
	static protected boolean isRampsLoaded = false;

	/** Handle to the main XML document contents */
	static protected Document doc;

	/** Handle to the currently tracked Node List */
	static protected NodeList nodeLst;

	/** Handle to the main Global Root Element in XML */
	static protected Element globalRootElement;

	/** Handle to the main Ramps List of Nodes in XML */
	static protected NodeList rampsRootNodes;

	/** HashMap to store the Global Parameters read from XML */
	static protected HashMap<String, String> globalParams = new HashMap<String, String>();

	/** HashMap to store the Ramp/Meter specific parameters read from XML */
	static protected HashMap<String,HashMap<String, String>> rampParams =
		new HashMap<String, HashMap<String, String>>();

	/** HashMap to store the LWR-MFD based paramters for each location as read from XML
	 * The First index is 'rampId',
	 * Second index is 'LWR Block No',
	 * Third index is 'Parameter Name' */
	static protected HashMap<String, HashMap<String, HashMap<String, String>>> lwrParams =
		new HashMap<String, HashMap<String, HashMap<String, String>>>();

	/** Load the Global Parameters block in XML
	 * @throws Exception */
	public static String loadGlobalXMLParams () throws Exception {
		String file_load_status = loadXMLFiletoDoc();
		nodeLst = doc.getElementsByTagName("Global");
		globalRootElement = (Element)nodeLst.item(0);
		isGlobalLoaded = true;
		//loadglobalParams();
		loadNewGlobalParams();
		return file_load_status;
	}

	/** Load the Ramps/Meters Parameter block in XML
	 * @throws Exception */
	public static String loadRampsXMLParams () throws Exception {
		String file_load_status = loadXMLFiletoDoc();
		nodeLst = doc.getElementsByTagName("Meters");
		Element metersXML = (Element)nodeLst.item(0);
		rampsRootNodes = metersXML.getElementsByTagName("Meter");
		isRampsLoaded = true;
		loadRampsParams();
		return file_load_status;
	}

	/** Load individually the Ramp/Meter based parameters.
	 * Use call to loadRampsXMLParams() function for use,
	 * that acts as wrapper over this function
	 * @throws Exception */
	protected static void loadRampsParams () throws Exception {
		if (!isRampsLoaded)
			loadRampsXMLParams();
		Node paramNode, paramNode2;
		Element paramElement;
		String rampId;
		String block_count;

		for (int rampCount = 0; rampCount < rampsRootNodes.getLength(); rampCount++) {
			HashMap<String, String> ramp = new HashMap<String, String>();
			paramElement = (Element)rampsRootNodes.item(rampCount);
			paramNode = paramElement.getElementsByTagName("MeterID").item(0);
			rampId = paramNode.getChildNodes().item(0).getNodeValue();

			paramNode = paramElement.getElementsByTagName("UncongCap").item(0);
			ramp.put("uncongested_capacity", paramNode.getChildNodes().item(0).getNodeValue());

			paramNode = paramElement.getElementsByTagName("MainlineDens").item(0);
			paramNode2 = paramNode.getAttributes().getNamedItem("TargetDens");
			ramp.put("target_dens", paramNode2.getChildNodes().item(0).getNodeValue());
			paramNode2 = paramNode.getAttributes().getNamedItem("CritDens");
			ramp.put("crit_dens", paramNode2.getChildNodes().item(0).getNodeValue());
			paramNode2 = paramNode.getAttributes().getNamedItem("SafeT2Cong");
			ramp.put("safe_t2k", paramNode2.getChildNodes().item(0).getNodeValue());
			paramNode2 = paramNode.getAttributes().getNamedItem("T2CongMax");
			ramp.put("TIME2_CONGESTION_MAX_MAINLINE", paramNode2.getChildNodes().item(0).getNodeValue());

			paramNode = paramElement.getElementsByTagName("RampWait").item(0);
			paramNode2 = paramNode.getAttributes().getNamedItem("TargetRampWait");
			ramp.put("target_wait", paramNode2.getChildNodes().item(0).getNodeValue());
			paramNode2 = paramNode.getAttributes().getNamedItem("SafeT2Cong");
			ramp.put("safe_t2w", paramNode2.getChildNodes().item(0).getNodeValue());
			paramNode2 = paramNode.getAttributes().getNamedItem("T2CongMax");
			ramp.put("TIME2_CONGESTION_MAX", paramNode2.getChildNodes().item(0).getNodeValue());

			paramNode = paramElement.getElementsByTagName("LWR").item(0);
			paramElement = (Element) paramNode;
			paramNode = paramElement.getElementsByTagName("MAXValues").item(0);
			paramNode2 = paramNode.getAttributes().getNamedItem("MinDen");
			ramp.put("MIN_DENSITY_IN_MODEL", paramNode2.getChildNodes().item(0).getNodeValue());
			paramNode2 = paramNode.getAttributes().getNamedItem("MaxDens");
			ramp.put("MAX_DENSITY_IN_MODEL", paramNode2.getChildNodes().item(0).getNodeValue());
			paramNode2 = paramNode.getAttributes().getNamedItem("MinFlow");
			ramp.put("MIN_FLOW_IN_MODEL", paramNode2.getChildNodes().item(0).getNodeValue());
			paramNode2 = paramNode.getAttributes().getNamedItem("MaxFlowRatio");
			ramp.put("MAX_FLOW_RATIO_IN_MODEL", paramNode2.getChildNodes().item(0).getNodeValue());

			paramNode = paramElement.getElementsByTagName("MFDEstimator").item(0);
			paramElement = (Element)paramNode;
			NodeList meterparamNodeList = paramElement.getElementsByTagName("Block");

			HashMap<String, HashMap<String, String>> mfd_model =
				new HashMap<String, HashMap<String, String>>();
			for (int block = 0; block < meterparamNodeList.getLength(); block++) {
				HashMap<String, String> mfd_block = new HashMap<String, String>();
				Node block_x = meterparamNodeList.item(block);
				paramNode = block_x.getAttributes().getNamedItem("Number");
				block_count = paramNode.getChildNodes().item(0).getNodeValue();
				paramNode = block_x.getAttributes().getNamedItem("CongState");
				mfd_block.put("CongState", paramNode.getChildNodes().item(0).getNodeValue());
				paramNode = block_x.getAttributes().getNamedItem("DensityCheck");
				mfd_block.put("DensThresh", paramNode.getChildNodes().item(0).getNodeValue());
				paramNode = block_x.getAttributes().getNamedItem("Slope");
				mfd_block.put("Slope", paramNode.getChildNodes().item(0).getNodeValue());
				paramNode = block_x.getAttributes().getNamedItem("Intercept");
				mfd_block.put("Intercept", paramNode.getChildNodes().item(0).getNodeValue());
				mfd_model.put(block_count, mfd_block);
			}
			lwrParams.put(rampId, mfd_model);
			rampParams.put(rampId, ramp);
		}
		nodeLst = null;
	}


	/** Get the value for a given global parameter.
	 * If value is not found in the loaded xml parameters file,
	 * then return the default value passed as parameter to function.
	 *
	 * @param paramName : ParameterName whose value is sought.
	 * @param defaultVal : Defaul value to use if no value for ParamName is found in XML
	 * @return : Return either the obtained value from XML or default value.
	 */
	public static String getGlobalParam (String paramName, String defaultVal) {
		String value = getGlobalParam(paramName);
		if (value == null)
			value = defaultVal;
		return value;
	}

	/** Get the value for a given global parameter. */
	protected static String getGlobalParam (String paramName) {
		String value = globalParams.get(paramName);
		return value;
	}

	/** Get the integer parsed value for a given global parameter, or default value passed */
	public static int getGlobalIntP(String paramName, int default_val) {
		String s_def_val = String.valueOf(default_val);
		int value = Integer.parseInt(getGlobalParam(paramName, s_def_val));
		return value;
	}

	/** Get the integer parsed value for a given global parameter */
	public static int getGlobalIntP(String paramName) {
		int value = Integer.parseInt(getGlobalParam(paramName));
		return value;
	}

	/** Get the float parsed value for a given global parameter, or default value passed */
	public static float getGlobalFloatP (String paramName, float def_val) {
		String s_def_val = String.valueOf(def_val);
		float value = Float.parseFloat(getGlobalParam(paramName, s_def_val));
		return value;
	}

	/** Get the float parsed value for a given global parameter */
	public static float getGlobalFloatP(String paramName) {
		float value = Float.parseFloat(getGlobalParam(paramName));
		return value;
	}

	/** Get the boolean parsed value for a given global parameter, or default value passed */
	public static boolean getGlobalBooleanP (String paramName, boolean def_val) {
		String s_def_val = String.valueOf(def_val);
		boolean value = Boolean.parseBoolean(getGlobalParam(paramName, s_def_val));
		return value;
	}

	/** Get the boolean parsed value for a given global parameter */
	public static boolean getGlobalBooleanP(String paramName) {
		boolean value = Boolean.parseBoolean(getGlobalParam(paramName));
		return value;
	}

	/** Get the value for a given ramp parameter for a given ramp ID. If entry is not found
	 * for the combination, then first the DEFAULT ramp parameter value is checked,
	 * and otherwise the default value passed to the function is returned back.
	 * @param rampId : rampId for which the value is sought.
	 * @param paramName : Parameter Name whose value is sought.
	 * @param defaultVal : Defaul value to use if no value for ParamName is found in XML
	 * @return : Return either the obtained value from XML or default value.
	 */
	protected static String getRampParam (String rampId, String paramName, String defaultVal) {
		String value = getRampParam(rampId, paramName);
		if (value == null)
			value = defaultVal;
		return value;
	}

	/** Get the value for a given ramp and parameter combination. */
	protected static String getRampParam (String rampId, String paramName) {
		String value = null;
		if(rampParams.containsKey(rampId))
			value = rampParams.get(rampId).get(paramName);
		if (value == null)
			value = rampParams.get("DEFAULT").get(paramName);
		return value;
	}

	/** Get the integer parsed value for a given ramp id and parameter */
	public static int getRampIntP (String rampId, String paramName) {
		int value = Integer.parseInt(getRampParam(rampId, paramName));
		return value;
	}

	/** Get the integer parsed value for a given ramp id and parameter, or default value passed */
	public static int getRampIntP (String rampId, String paramName, int def_val) {
		String s_def_val = String.valueOf(def_val);
		int value = Integer.parseInt(getRampParam(rampId, paramName, s_def_val));
		return value;
	}

	/** Get the float parsed value for a given ramp id and parameter */
	public static float getRampFloatP (String rampId, String paramName) {
		float value = Float.parseFloat(getRampParam(rampId, paramName));
		return value;
	}

	/** Get the float parsed value for a given ramp id and parameter, or default value passed */
	public static float getRampFloatP (String rampId, String paramName, float def_val) {
		String s_def_val = String.valueOf(def_val);
		float value = Float.parseFloat(getRampParam(rampId, paramName, s_def_val));
		return value;
	}

	/** Returns whether an entry for the given rampId was found in the Param File.
	 * When an entry is not found, the param values are loaded according to DEFAULT ramp
	 */
	public static boolean getIsRampFound (String rampId) {
		if(rampParams.containsKey(rampId))
			return true;
		return false;
	}

	/** Get the LWR MFD parameter value for a given ramp, and given LWR-MFD block value
	 * DEFAULT ramp is used if rampId is not found, and default value passed as parameter
	 * is used if no value is loaded from XML.
	 * @param rampId : Ramp ID for which value is sought
	 * @param blockNo : Block No for LWR-MFD estimation model
	 * @param paramName : Name of MFDEstimator param
	 * @param defaultVal : Default value to use if no value is found in XML
	 * @return : Return either the obtained value from XML or default value.
	 */
	protected static String getMFDParam (String rampId, String blockNo, String paramName, String defaultVal) {
		String value = getMFDParam(rampId, blockNo, paramName);
		if (value == null)
			value = defaultVal;
		return value;
	}

	/** Get the MFD Model value for a given MFD Model param, for a given block and ramp combination */
	protected static String getMFDParam (String rampId, String blockNo, String paramName) {
		String value = null;
		if (lwrParams.containsKey(rampId))
			value = lwrParams.get(rampId).get(blockNo).get(paramName);
		if (value == null)
			value = lwrParams.get("DEFAULT").get(blockNo).get(paramName);
		return value;
	}

	/** Get the integer parsed MFD Model value for a given MFD Model param, block no and rampId combination */
	public static int getMFDIntP (String rampId, int blockNo, String paramName) {
		int value = Integer.parseInt(getMFDParam(rampId, String.valueOf(blockNo), paramName));
		return value;
	}

	/** Get the number of defined MFD Blocks for given ramp */
	protected static int getNumberMFDBlocks (String rampId) {
		int value = 0;
		if (lwrParams.containsKey(rampId))
			value = Math.max(lwrParams.get(rampId).size(), 0);
		if (value <= 0)
			value = Math.max(lwrParams.get("DEFAULT").size(), 0);
		return value;
		//return Math.max(lwrParams.get(rampId).size(), lwrParams.get("DEFAULT").size());
	}

	/** Load the global based parameters.
	 * Use call to loadGlobalXMLParams() instead, which
	 * acts as a wrapper over this function
	 * @throws Exception
	 */
	protected static void loadglobalParams () throws Exception {
		if (!isGlobalLoaded)
			loadGlobalXMLParams();

		Node paramNode, paramNode1;
		Element paramElement;

		paramNode = globalRootElement.getElementsByTagName("DebugLevel").item(0);
		globalParams.put("DEBUG_LEVEL", paramNode.getChildNodes().item(0).getNodeValue());

		paramElement = (Element)globalRootElement.getElementsByTagName("LWRModel").item(0);
		paramNode = paramElement.getElementsByTagName("LWRSwitch").item(0);
		globalParams.put("LWR_SWITCH",paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = paramElement.getElementsByTagName("LWRDistSteps").item(0);
		globalParams.put("LWR_DIST_STEP", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = paramElement.getElementsByTagName("LWRTimeSteps").item(0);
		globalParams.put("LWR_TIME_STEP", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = paramElement.getElementsByTagName("MFDSteps").item(0);
		globalParams.put("NO_STEPS_MFD", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = paramElement.getElementsByTagName("MovingAvgLength").item(0);
		globalParams.put("MOVING_AVERAGE_LENGTH", paramNode.getChildNodes().item(0).getNodeValue());

		paramNode = globalRootElement.getElementsByTagName("MeteringRates").item(0);
		paramNode1 = paramNode.getAttributes().getNamedItem("StepChange");
		globalParams.put("METERING_STEP", paramNode1.getChildNodes().item(0).getNodeValue());
		paramNode1 = paramNode.getAttributes().getNamedItem("StepChangeThresh");
		globalParams.put("STEP_CHANGE_THRESHOLD", paramNode1.getChildNodes().item(0).getNodeValue());
		paramNode1 = paramNode.getAttributes().getNamedItem("StepChangePostThresh");
		globalParams.put("STEP_CHANGE_POST_THRESH", paramNode1.getChildNodes().item(0).getNodeValue());

		paramElement = (Element)globalRootElement.getElementsByTagName("RampGlobal").item(0);
		Node rampGlobalChild = paramElement.getElementsByTagName("CritDens").item(0);
		globalParams.put("CRITICAL_DENSITY", rampGlobalChild.getChildNodes().item(0).getNodeValue());
		rampGlobalChild = paramElement.getElementsByTagName("LowDensMarg").item(0);
		globalParams.put("LOW_DENS_MARGIN", rampGlobalChild.getChildNodes().item(0).getNodeValue());
		rampGlobalChild = paramElement.getElementsByTagName("FlowCap").item(0);
		globalParams.put("FLOW_CAPACITY", rampGlobalChild.getChildNodes().item(0).getNodeValue());
		rampGlobalChild = paramElement.getElementsByTagName("MainlineSafeTime").item(0);
		globalParams.put("SAFE_TIME_MAINLINE", rampGlobalChild.getChildNodes().item(0).getNodeValue());
		rampGlobalChild = paramElement.getElementsByTagName("RampSafeTime").item(0);
		globalParams.put("SAFE_TIME_RAMP", rampGlobalChild.getChildNodes().item(0).getNodeValue());
		rampGlobalChild = paramElement.getElementsByTagName("K1").item(0);
		globalParams.put("K1", rampGlobalChild.getChildNodes().item(0).getNodeValue());
		rampGlobalChild = paramElement.getElementsByTagName("K2").item(0);
		globalParams.put("K2", rampGlobalChild.getChildNodes().item(0).getNodeValue());
		rampGlobalChild = paramElement.getElementsByTagName("Q_THRESH_DENS").item(0);
		globalParams.put("Q_THRESH_DENS", rampGlobalChild.getChildNodes().item(0).getNodeValue());

		paramNode = globalRootElement.getElementsByTagName("MeteringCycleTime").item(0);
		globalParams.put("METERING_CYCLE_TIME", paramNode.getChildNodes().item(0).getNodeValue());

		nodeLst = null;
	}

	public static void loadNewGlobalParams() throws Exception {
		if (!isGlobalLoaded)
			loadGlobalXMLParams();

		Node paramNode;
		Element paramElement;

		paramNode = globalRootElement.getElementsByTagName("DebugLevel").item(0);
		globalParams.put("DEBUG_LEVEL", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = globalRootElement.getElementsByTagName("LWRSwitch").item(0);
		globalParams.put("LWR_SWITCH", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = globalRootElement.getElementsByTagName("MeteringRates").item(0);
		globalParams.put("METERING_STEP", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = globalRootElement.getElementsByTagName("MeteringCycleTime").item(0);
		globalParams.put("METERING_CYCLE_TIME", paramNode.getChildNodes().item(0).getNodeValue());

		paramElement = (Element)globalRootElement.getElementsByTagName("RampGlobal").item(0);
		paramNode = paramElement.getElementsByTagName("CritDens").item(0);
		globalParams.put("CRITICAL_DENSITY", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = paramElement.getElementsByTagName("MainlineSafeTime").item(0);
		globalParams.put("SAFE_TIME_MAINLINE", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = paramElement.getElementsByTagName("RampSafeTime").item(0);
		globalParams.put("SAFE_TIME_RAMP", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = paramElement.getElementsByTagName("TargetRampWait").item(0);
		globalParams.put("TARGET_TIME_RAMP", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = paramElement.getElementsByTagName("K1").item(0);
		globalParams.put("K1", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = paramElement.getElementsByTagName("K2").item(0);
		globalParams.put("K2", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = paramElement.getElementsByTagName("FreeFlowSpeed").item(0);
		globalParams.put("FREEFLOW_SPEED", paramNode.getChildNodes().item(0).getNodeValue());
		paramNode = paramElement.getElementsByTagName("Capacity").item(0);
		globalParams.put("LANE_CAPACITY", paramNode.getChildNodes().item(0).getNodeValue());

		nodeLst = null;
	}

	/** Set the XML parameters file path */
	public static void setXMLFilePath (String xmlfile) {
		XML_PATH = xmlfile;
	}

	/** Load the XML parameter file */
	static protected String loadXMLFiletoDoc() throws Exception {
		try {
			File file = new File (XML_PATH);
			Long last_mod = file.lastModified();
			Date new_date_mod = new Date(last_mod);
			if (last_paramfile_load != null)
				if(!last_paramfile_load.before(new_date_mod))
					return "... File Already Loaded ... Skipping";
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(file);
			doc.getDocumentElement().normalize();
			last_paramfile_load = new_date_mod;
			return "... File Loaded with timestamp: (" + new_date_mod.toString() + ")";
		} catch (Exception e) {
			throw e;
		}
	}
}
