## Speed Estimation

1. Choose a "free-flow" speed:
  - Right lane: speed limit + 5 mph
  - Left/center lanes: speed limit + 10 mph
2. Guess traffic conditions for each 30-second interval
  - Calculate free-flow density, assuming free-flow speed
  - `density` (veh/mi) = `flow` (veh/hr) / `speed` (mi/hr)
  - Calculate avg. field length, using assumed density
  - `field_len` (ft/veh) = `occupancy` (%) * 5280 (ft/mi) / `density` (veh/mi)
  - 0-24 ft: Free-flow
  - 24-36 ft: Moderate
  - 36-64 ft: Heavy
  - 64+ ft: Congested
3. Calculate "adjusted field length"
  - Sum `occupancy` and `density` of all free-flow intervals
  - `field_len` (ft/veh) = `occupancy` (%) * 5280 (ft/mi) / `density` (veh/mi)
4. Estimate speed for each 30-second interval
  - Adjust `field_len` based on interval conditions
  - Free-flow: +0
  - Moderate: +4 ft
  - Heavy: +9 ft
  - Congested: +14 ft
  - `density` (veh/mi) = `occupancy` (%) * 5280 (ft/mi) / `field_len` (ft/veh)
  - `speed` (mi/hr) = `flow` (veh/hr) / `density` (veh/mi)
