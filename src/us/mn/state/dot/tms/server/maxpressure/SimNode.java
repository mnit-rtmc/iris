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

import java.util.List;
import us.mn.state.dot.tms.server.R_NodeImpl;


/**
 * An abstract representation of the nodes used in the cell transmission model.
 * @author Michael Levin
 */
public abstract class SimNode {
    private R_NodeImpl rnode;
    
    public SimNode(R_NodeImpl rnode){
        this.rnode = rnode;
    }
    
    public abstract void setMainlineOut(CTMLink out);
    
    public R_NodeImpl getRnode(){
        return rnode;
    }
    
    public String getName(){
        return rnode.getName();
    }
    
    public String toString(){
        return rnode.getName();
    }
    public abstract void step();
    

    public abstract void propagateExcessRemovedFlow(double y);
    
    // attempt to find the CTMLink for the mainline
    public abstract CTMLink getMainlineIn(); 
    public abstract CTMLink getMainlineOut(); 
}
