/*
 * Max-pressure ramp metering implemented in IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025-2026 Michael Levin
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
package us.mn.state.dot.tms.server.maxpressure;

import us.mn.state.dot.tms.server.R_NodeImpl;

/**
 * This is used as the start and end of the CTM network. 
 * It's a dummy node because at the start and end, the detectors will determine inflows/outflows.
 * @author Michael Levin
 */
public class DummyNode extends SimNode {

    public DummyNode(R_NodeImpl rnode) {
        super(rnode);
    }
    
    public void propagateExcessRemovedFlow(double y){
        // do nothing
    }
 
    public void setMainlineOut(CTMLink out){
        // do nothing
    }
    public CTMLink getMainlineOut(){
        return null;
    }
    
    public CTMLink getMainlineIn(){
        return null;
    }
    
    public double removeFlowCleanup(double y){
        return 0;
    }
    
    public double addFlowCleanup(double y){
        return 0;
    }
    
    public void cleanup(){}
    
    public void step(){}
}
