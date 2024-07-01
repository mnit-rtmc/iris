/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  SRF Consulting Group
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.tms.RwisCondition;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.rwis.RwisProperties;

public class RwisConditionImpl extends BaseObjectImpl implements RwisCondition {
	
	protected RwisConditionImpl(int priority, String n, String formula) {
		super(n);
		this.priority = priority;
		this.formula  = formula;
	}

	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** ArrayList of all conditions */
	private static ArrayList<RwisCondition> conditionList = new ArrayList<RwisCondition>();
	
	/** Load all RwisCondition(s) from the database
	 * 
	 * NOTE: Since all conditions are currently hard-coded, for now:
	 *   Registers the "rwis_condition" type with the namespace
	 *   and loads all conditions from the rwis.properties file.
	 */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, RwisConditionImpl.class);
		// for each condition in rwis.properties...
		try {
			String str = RwisProperties.get("conditions", "0");
			int count = Integer.parseInt(str);
			for (int i = 1; (i <= count); ++i) {
				String prefix = "condition" + i;
				String name    = RwisProperties.get(prefix+".name");
				String formula = RwisProperties.get(prefix+".formula");
				RwisCondition rc = new RwisConditionImpl(i, name, formula);
				conditionList.add(rc);
				namespace.addObject(rc);
			}
		}
		catch (NumberFormatException|NamespaceError ex) {
			throw new TMSException(ex);
		}
	}

	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

//	@Override
//	public void destroy() {
//		super.destroy();
//		// TODO Auto-generated method stub
//	}

	private Integer priority;

	@Override
	public Integer getPriority() {
		return priority;
	}

	private String formula;
	
	@Override
	public String getFormula() {
		return formula;
	}

	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name",     name);
		map.put("priority", priority);
		map.put("formula",  formula);
		return map;
	}

	static public RwisCondition findPriority(int priority) {
		--priority;
		if ((priority < 0) || (priority > conditionList.size()))
			return null;
		return conditionList.get(priority);
	}

	//-------------------------------------------
	// Placeholder until we can get full formula evaluator
	//-------------------------------------------
	
}
