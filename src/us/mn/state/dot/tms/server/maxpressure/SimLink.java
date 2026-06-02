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

/**
 * An abstract representation of the CTM link. It's used because some links are modeled with cells and others (e.g. on-ramp) with point queues.
 * @author Michael Levin
 */
public abstract class SimLink {
    protected SimNode start, end;

    
    public SimLink(){
        Q = 0;
        L = 0;
    }
    
    public String toString(){
        return start.getName()+"-"+end.getName();
    }
    
    protected double Q; // capacity
    protected double L; // length
    
    public abstract void step();
    public abstract void update();
    
    public abstract double getOccupancy();
    
    public abstract double getReceivingFlow();
    public abstract double getSendingFlow();
    
    public abstract void addFlow(double y);
    public abstract void removeFlow(double y);
    

    public abstract void propagateExcessRemovedFlow(double y);
    
    
    public abstract void prepare(long stamp, int PERIOD_MS); // used to obtain sensor data for next time period (usually multiple CTM time steps)
}
