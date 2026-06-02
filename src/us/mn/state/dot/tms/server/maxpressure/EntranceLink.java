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
import static us.mn.state.dot.tms.server.MaxPressureAlgorithm.CTM_DT;
import static us.mn.state.dot.tms.server.MaxPressureAlgorithm.STEP_SECONDS;
import us.mn.state.dot.tms.server.R_NodeImpl;
import us.mn.state.dot.tms.server.SamplerSet;
import static us.mn.state.dot.tms.server.maxpressure.CTMNetwork.EPSILON;

/**
 * This is a queue of vehicles trying to enter the freeway, modeled as a point queue.
 * It's used for on-ramps.
 * @author Michael Levin
 */
public class EntranceLink extends SimLink {
    // this acts like a point queue 

    protected SamplerSet det; 
    
    private double cc;
    public double entered;
    public double actual_entered;
    
    private double S_base;
    
    protected double queue;

    public EntranceLink(R_NodeImpl det_rnode, SamplerSet det){
        start = new DummyNode(det_rnode);
        this.det = det;
        Q = 1900; // ramp capacity
        S_base = 0;
        queue = 0;
    }
    
    public String toString(){
        return start.getName();
    }
    
    public double getOccupancy(){
        return queue; // should not have occupancy. 
        // After every STEP_SECONDS all entering flow (as recorded by sensor) is moved to CTM link.
        // occupancy happens only when CTM link is full
    }
    
    public double getReceivingFlow(){
        return 0; // should never be used
    }
    
    public double getSendingFlow(){
        queue += S_base;
        return queue;
    }
    
    public void propagateExcessRemovedFlow(double y){
        // ignore y. Propagate remove_carry
        queue -= y;
    }
    
    public void propagateExcessAddedFlow(){
        if(queue > EPSILON){
            queue -= end.getMainlineOut().cleanupAddFlow(queue);
        }
    }
    
    public void prepare(long stamp, int PERIOD_MS){
        
        
        actual_entered = 0;
        
        entered = det.getVehCount(stamp, PERIOD_MS);
        
        if(entered >= 0){
        
            cc += entered;
        }
        else{
            // entrance detector is giving bad data (probably -1)
            entered = 0;
        }
        
        S_base = entered * CTM_DT / STEP_SECONDS;
    }
    
    public String getName(){
        return start.getName();
    }
    
    public double getDetCumulativeCount(){
        return cc;
    }
    
    public double getOccupancyChange(){
        return S_base;
    }
    
    public void update(){
        
    }
    
    public void step(){
        // nothing to do here
    }
    
    public void addFlow(double y){
        // do nothing
    }
    
    public void removeFlow(double y){
        // remove as much as possible, but add_carry should not be negative
        actual_entered += y;
        queue -= y;
    }

}
