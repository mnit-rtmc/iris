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
import us.mn.state.dot.tms.server.MaxPressureAlgorithm;
import us.mn.state.dot.tms.server.R_NodeImpl;

/**
 * This is used to connect 2 links in series, e.g. where the freeway changes the number of lanes available.
 * @author Michael Levin
 */
public class SeriesNode extends SimNode {
    protected SimLink inc, out;
    
    public SeriesNode(R_NodeImpl rnode){
        super(rnode);
    }
    
    public SeriesNode(R_NodeImpl rnode, CTMLink inc, ExitLink out){
        super(rnode);
        this.inc = inc;
        this.out = out;
        inc.end = this;
        out.start = this;
    }
    public SeriesNode(R_NodeImpl rnode, EntranceLink inc){
        super(rnode);
        this.inc = inc;
        this.out = out;
        inc.end = this;
        
    }
    
    public void propagateExcessRemovedFlow(double y){
        inc.propagateExcessRemovedFlow(y);
    }
    
    public String toString(){
        if(inc instanceof EntranceLink){
            return ((EntranceLink)inc).getName()+" [entrance]\n\t"+super.toString()+" [series]";
        }
        else if(out instanceof ExitLink){
            return super.toString()+" [series] L="+inc.L+" lanes="+((CTMLink)inc).lanes+"\n\t"+
                    ((ExitLink)out).getName()+" [exit]";
        }
        else{
            return super.toString();
        }
    }
    
    public void setMainlineOut(CTMLink out){
        this.out = out;
        out.start = this;
    }
    
    public CTMLink getMainlineIn(){
        if(inc instanceof CTMLink){
            return (CTMLink)inc;
        }
        else{
            // this may be null at the start of the network where out is CTMLink and inc is 
            return null;
        }
    }
    
    public CTMLink getMainlineOut(){
        if(out instanceof CTMLink){
            return (CTMLink)out;
        }
        else{
            // this may be null at the start of the network where inc is CTMLink and out is ExitLink
            return null;
        }
    }
    
    public void step(){
        double S = inc.getSendingFlow();
        double R = out.getReceivingFlow();
        
        double y = 0;
        
        if(out instanceof ExitLink){
            y = R;
        }
        else{
            y = Math.min(S, R);
        }
        inc.removeFlow(y);
        out.addFlow(y);
    }
    
    
}
