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
import static us.mn.state.dot.tms.server.maxpressure.CTMNetwork.EPSILON;

/**
 * A link comprised of multiple cells.
 * Links model segments of the freeway in-between merge or diverge points.
 * @author Michael Levin
 */
public class CTMLink extends SimLink {
    protected double K; // jam density
    protected double w; // congested wave speed
    
    protected double v; // free flow speed
    protected int lanes;

    protected double cell_len;

    protected Cell[] cells;


    public CTMLink(double L, int lanes, double v, double Q, double w, double K){
        this.L = L;
        this.v = v;
        this.Q = Q;
        this.lanes = lanes;
        this.w = w;
        this.K = K;

        // want cell length to be approximately v * dt
        cell_len = v * MaxPressureAlgorithm.CTM_DT / 3600;
        // but the cell length must be at least v*dt to avoid CFL condition
        int ncells = Math.max(1, (int)Math.floor(L / (v * MaxPressureAlgorithm.CTM_DT / 3600.0)));
        // minimum of 1 cell

        cells = new Cell[ncells];

        for(int i = 0; i < cells.length; i++){
            cells[i] = new Cell(this);
        }
    }
    
    public void propagateExcessRemovedFlow(double y){
        for(int i = cells.length-1; i >= 0; i--){
            double removed = Math.min(y, cells[i].getOccupancy());
            cells[i].addOccupancy(-removed);
            
            y -= removed;
            
            if(y < EPSILON){
                break;
            }
        }
        
        // even if y=0, keep going because the start node may be a diverge with an exit link
        start.propagateExcessRemovedFlow(y);
    }
    
    public double getOccupancyChange(){
        return 0;
    }
    
    public double getDensity(){
        return getOccupancy() / (cell_len * cells.length);
    }
    
    public double getAvgDensity(){
        return getDensity()/lanes;
    }
    
    public double cleanupAddFlow(double y){
        double total_added = 0;
        
        for(int i = 0; i < cells.length; i++){
            double added = Math.min(y, cells[i].getMaxOccupancy() - cells[i].getOccupancy());
            cells[i].addOccupancy(added);
            
            total_added += added;
            y -= added;
            
            if(y < EPSILON){
                break;
            }
        }
        return total_added;
    }
    
    public int getNumCells(){
        return cells.length;
    }

    public double getOccupancy(){
        double total_n = 0;

        for(Cell c : cells){
            total_n += c.getOccupancy();
        }

        return total_n;
    }
    
    public void prepare(long stamp, int PERIOD_MS){
        // nothing to do here
    }

    public void addFlow(double y){
        cells[0].addOccupancy(y);
    }

    public void removeFlow(double y){
        int idx = cells.length-1;

        // if y > occupancy, propagate backwards
        while(y > EPSILON && idx >= 0){
            
            Cell cell = cells[idx];
            double remove = Math.min(y, cell.getOccupancy());

            cell.addOccupancy(-remove);
            y -= remove;
            
            idx --;
        }
        
        // if y > 0 still after removing from all cells of this link, attempt to go to upstream link
        // use the existing propagate excess removed flow method
        if(y > EPSILON){
            this.start.propagateExcessRemovedFlow(y);
        }
        
    }
    
    public double getCleanupMaxAdd(){
        double total = 0;
        
        for(Cell c : cells){
            total += c.getMaxOccupancy() - c.getOccupancy();
        }
        return total;
    }
    
    public double getCleanupMaxRemove(){
        double total = 0;
        
        for(Cell c : cells){
            total += c.getOccupancy();
        }
        return total;
    }

    // sending flow for next CTM timestep
    // units of veh
    public double getSendingFlow(){
        return cells[cells.length-1].getSendingFlow();
    }

    // receiving flow for next CTM timestep
    // units of veh
    public double getReceivingFlow(){
        return cells[0].getReceivingFlow();
    }
    
    public double getCriticalDensity(){
        return Q / v;
    }

    // calculate state at next CTM time step
    public void step(){
        for(int i = 1; i < cells.length; i++){
            double S = cells[i-1].getSendingFlow();
            double R = cells[i].getReceivingFlow();
            double y = Math.max(0, Math.min(S, R)); // in case it becomes negative due to sensor fault

            cells[i].addFlow(y);
            cells[i-1].removeFlow(y);
        }
    }

    // set state to state at next time step
    public void update(){
        for(int i = 0; i < cells.length; i++){
            cells[i].update();
        }
    }
}
