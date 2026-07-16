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


/**
 * A single cell on the link.
 * It tracks its own occupancy and computes sending/receiving -> transition flows.
 * @author Michael Levin
 */
public class Cell {
    private double n, y;
    private CTMLink link;

    public Cell(CTMLink link){
        this.link = link;
        this.n = 0;
        this.y = 0;
    }

    public double getSendingFlow()
    {
        return Math.min(n, link.Q * MaxPressureAlgorithm.CTM_DT/3600.0);
    }

    public double getReceivingFlow()
    {
        double actual_Q = link.Q;
        
        // capacity drop
        if(getDensity() > link.getCriticalDensity()){
            actual_Q = link.Q * 0.85; // estimated value of 15-20%
        }
        
        double term1 = actual_Q * MaxPressureAlgorithm.CTM_DT/3600.0;
        double term2 = link.w / link.v * (getMaxOccupancy() - n);
        return Math.min(term1, term2);
    }
    
    public double getMaxOccupancy(){
        return link.K * link.cell_len;
    }
    
    public double getOccupancy()
    {
        return n;
    }
    
    public void addOccupancy(double add){
        n = Math.max(0, n + add);
    }

    public void addFlow(double y)
    {
        this.y += y;
    }

    public void removeFlow(double y)
    {
        this.y -= y;
    }

    public void update()
    {
        n = Math.max(0, n + y);
        y = 0;
    }

    public double getDensity(){
        return n / link.cell_len;
    }
}
