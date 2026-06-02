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

import us.mn.state.dot.tms.server.MaxPressureAlgorithm;
import us.mn.state.dot.tms.server.R_NodeImpl;

/**
 * This is the merge point for an on-ramp. It uses the standard dynamic network loading merge model.
 * @author Michael Levin
 */
public class MergeNode extends SimNode {
    protected CTMLink inc_mainline, out;
    protected EntranceLink inc_ramp;
    
    public MergeNode(R_NodeImpl rnode){
        super(rnode);
    }
    public MergeNode(R_NodeImpl rnode, CTMLink inc_mainline, EntranceLink inc_ramp){
        super(rnode);
        this.inc_mainline = inc_mainline;
        this.inc_ramp = inc_ramp;  
        
        inc_mainline.end = this;
        inc_ramp.end = this;
    }
    
    public void setMainlineOut(CTMLink out){
        this.out = out;
        out.start = this;
    }
    
    public String toString(){
        return getName()+" [<-"+inc_ramp.start.getName()+"] L="+inc_mainline.L+" lanes="+inc_mainline.lanes;
    }
    
    public CTMLink getMainlineIn(){
        return inc_mainline;
    }
    
    public CTMLink getMainlineOut(){
        return out;
    }
    
    public void propagateExcessRemovedFlow(double y){
        double ent_remove = Math.min(y, inc_ramp.queue);
        
        inc_ramp.propagateExcessRemovedFlow(ent_remove);
        y -= ent_remove;
        
        inc_mainline.propagateExcessRemovedFlow(y);
    }
    
    public void step(){
        double S_ramp = inc_ramp.getSendingFlow(); // units of veh
        double S_up = inc_mainline.getSendingFlow(); // units of veh
        
        double R_down = out.getReceivingFlow(); // units of veh

        double y_ramp = 0; // ramp veh leaving
        double y_up = 0; // upstream veh leaving

        if(S_ramp + S_up <= R_down){
            y_ramp = S_ramp;
            y_up = S_up;
        }
        else{
           
            // proportional allocation
            double lambda_ramp = R_down * inc_ramp.Q / (inc_mainline.Q + inc_ramp.Q);
            y_ramp = median(R_down - S_up, S_ramp, lambda_ramp);
            
            y_up = R_down - y_ramp;
        }
        
        inc_mainline.removeFlow(y_up);
        inc_ramp.removeFlow(y_ramp);
        out.addFlow(y_up + y_ramp);
    }
    
    public static double median(double a, double b, double c){
        return Math.max(Math.min(a,b), Math.min(Math.max(a,b),c));
    }    
}
