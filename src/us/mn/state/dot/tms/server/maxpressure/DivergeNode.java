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
 * This is an off-ramp, which affects freeway density.
 * It does not use a dynamic network loading diverge calculation. 
 * The number of exiting vehicles is determined by the off-ramp detector.
 * @author Michael Levin
 */
public class DivergeNode extends SimNode {
    private CTMLink inc;
    private CTMLink out_mainline;
    private ExitLink out_ramp;
    
    public DivergeNode(R_NodeImpl rnode){
        super(rnode);
    }
    public DivergeNode(R_NodeImpl rnode, CTMLink inc, ExitLink out_ramp){
        super(rnode);
        this.inc = inc;
        this.out_ramp = out_ramp;
        
        inc.end = this;
        out_ramp.start = this;
    }
    
    public void setMainlineOut(CTMLink out){
        out_mainline = out;
        out.start = this;
    }
    
    public String toString(){
        return getName()+" [->"+out_ramp.end.getName()+"] L="+inc.L;
    }
    
    public CTMLink getMainlineIn(){
        return inc;
    }
    
    public CTMLink getMainlineOut(){
        return out_mainline;
    }
    
    public void propagateExcessRemovedFlow(double y){
        y += out_ramp.queue;
        out_ramp.queue = 0;
        inc.propagateExcessRemovedFlow(y);
    }
    
    public void step(){
        // no diverge model here: out_ramp tells us how much flow exits
        double y_exit = out_ramp.getReceivingFlow();
        
        // we always remove y_exit. This is additional flow removed.
        double S = Math.max(0, inc.getSendingFlow()-y_exit);
        double R = out_mainline.getReceivingFlow();
        
        double y = Math.min(S, R);
        inc.removeFlow(y + y_exit);
        out_mainline.addFlow(y);
        out_ramp.addFlow(y_exit);
    }
    
}
