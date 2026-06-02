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

import java.io.PrintStream;
import java.util.TreeMap;
import us.mn.state.dot.tms.server.MaxPressureAlgorithm;
import us.mn.state.dot.tms.server.SamplerSet;
import us.mn.state.dot.tms.server.VehicleSampler;

/**
 * I need to track cumulative counts. This is a wrapper around a VehicleSampler to avoid changing that.
 * @author Michael Levin
 */
public class SimpleCCSamplerSet implements VehicleSampler {
    
    private SamplerSet sampler;


    
    private int count;
    private long last_update;
    
    /**
     * I need to reset this periodically otherwise the counts will go to infinity.
     * Ideally we want to reset it at a time where no one is around (e.g. 3am)
     * reset interval is in ms
     * offset is to determine time-of-day
     * timestamp is in GMT, we are GMT-5 or GMT-6, so 3am is 8 or 9 hours after start-of-day
     */
    private static final int RESET_INTERVAL = 1000*3600*24; // every 24 hours
    private static final int RESET_OFFSET = 1000*3600*8; 
    private static final boolean RESET = false;
    
    public SimpleCCSamplerSet(SamplerSet s, long stamp){
        sampler = s;
        
        count = 0;
        last_update = stamp;
    }
    
    
    public void log(long stamp, int per_ms){
        if(MaxPressureAlgorithm.ALG_LOG.isOpen()){
            MaxPressureAlgorithm.ALG_LOG.log(stamp+" "+count);
        }
    }
    
    // this may be asked for historical counts, and those counts need to be stored and retrieved
    // this may be non-integer due to interpolation
    public double getCumulativeCount(long stamp, int per_ms){
        
        // if we passed the reset interval, then reset
        if(RESET && stamp % RESET_INTERVAL >= RESET_OFFSET && last_update % RESET_INTERVAL < RESET_OFFSET)
        {
            count = 0;
            last_update = stamp;
            
            // now continue with remainder of cc (the first case, stamp > last_update, should happen)
        }
        
        
        if(stamp > last_update){
            

            while(stamp - last_update > 0){
                last_update += per_ms;
                int passed = sampler.getVehCount(last_update, per_ms);
                
                if(passed < 0){
                    passed = 0;
                }
                
                count += passed;

            }
        }
        
                        
            
        return count;
        
    }
    
    public int getVehCount(long stamp, int per_ms) {
        return sampler.getVehCount(stamp, per_ms);
    }
    
    public float getSpeed(long stamp, int per_ms) {
        return sampler.getSpeed(stamp, per_ms);
    }
    
    public float getMaxOccupancy(long stamp, int per_ms) {
        return sampler.getMaxOccupancy(stamp, per_ms);
    }
    
    public float getDensity(long stamp, int per_ms) {
        return sampler.getDensity(stamp, per_ms);
    }
    
    public int getFlow(long stamp, int per_ms) {
        return sampler.getFlow(stamp, per_ms);
    }
    
    public boolean isPerfect() {
        return sampler.isPerfect();
    }
    
    public String toString(){
        return sampler.toString();
    }
}
