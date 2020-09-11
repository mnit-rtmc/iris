// segments.rs
//
// Copyright (C) 2019-2020  Minnesota Department of Transportation
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
use crate::geo::Wgs84Pos;
use postgres::rows::Row;
use std::cmp::Ordering;
use std::collections::HashMap;
use std::sync::mpsc::Receiver;

/// Geographic location
#[derive(Debug)]
struct GeoLoc {
    roadway: Option<String>,
    road_dir: Option<i16>,
    cross_mod: Option<i16>,
    cross_street: Option<String>,
    cross_dir: Option<i16>,
    lankmark: Option<String>,
    location: Option<String>,
    lat: Option<f64>,
    lon: Option<f64>,
}

/// Roadway node
#[derive(Debug)]
pub struct RNode {
    name: String,
    loc: GeoLoc,
    node_type: i32,
    pickable: bool,
    above: bool,
    transition: i32,
    lanes: i32,
    attach_side: bool,
    shift: i32,
    active: bool,
    station_id: Option<String>,
    speed_limit: i32,
    notes: String,
}

/// RNode notification message
pub enum RNodeMsg {
    /// Add or update an RNode
    AddUpdate(RNode),
    /// Remove an RNode
    Remove(String),
    /// Enable/disable ordering for all RNodes
    Order(bool),
}

impl From<RNode> for RNodeMsg {
    fn from(node: RNode) -> Self {
        RNodeMsg::AddUpdate(node)
    }
}

/// General direction of travel
#[derive(Clone, Copy, Debug, Eq, Hash, PartialEq)]
enum TravelDir {
    /// Northbound
    NB,
    /// Southbound
    SB,
    /// Eastbound
    EB,
    /// Westbound
    WB,
}

impl TravelDir {
    fn from_i16(dir: i16) -> Option<Self> {
        match dir {
            1 => Some(TravelDir::NB),
            2 => Some(TravelDir::SB),
            3 => Some(TravelDir::EB),
            4 => Some(TravelDir::WB),
            _ => None,
        }
    }
}

/// Corridor ID
#[derive(Clone, Debug, Eq, Hash, PartialEq)]
struct CorridorId {
    /// Name of corridor roadway
    roadway: String,
    /// Travel direction of corridor
    travel_dir: TravelDir,
}

/// A corridor is one direction of a roadway.
///
/// Each corridor contains a Vec of nodes.
///
/// When ordered, the first node depends on the direction of travel.  For
/// example, on a northbound corridor it would be the furthest south node.
/// The other nodes are ordered based on the nearest remaining node to the
/// previous, using haversine distance.
///
/// Invalid nodes (not active or missing lat/lon) are placed at the end.
struct Corridor {
    /// Corridor ID
    cor_id: CorridorId,
    /// Nodes ordered by corridor direction
    nodes: Vec<RNode>,
    /// Valid node count
    count: usize,
}

/// State of all segments
#[derive(Default)]
struct SegmentState {
    /// Mapping of node names to corridor IDs
    node_cors: HashMap<String, CorridorId>,
    /// Mapping of corridor IDs to Corridors
    corridors: HashMap<CorridorId, Corridor>,
    /// Ordered flag
    ordered: bool,
}

impl GeoLoc {
    /// Get the lat/lon of the location
    fn latlon(&self) -> Option<(f64, f64)> {
        match (self.lat, self.lon) {
            (Some(lat), Some(lon)) => Some((lat, lon)),
            _ => None,
        }
    }

    /// Get the position
    fn pos(&self) -> Option<Wgs84Pos> {
        self.latlon().map(|(lat, lon)| Wgs84Pos::new(lat, lon))
    }
}

impl RNode {
    /// SQL query for all RNodes
    pub const SQL_ALL: &'static str =
        "SELECT n.name, roadway, road_dir, cross_mod, cross_street, cross_dir, \
                landmark, lat, lon, node_type, pickable, above, transition, \
                lanes, attach_side, shift, active, station_id, speed_limit, \
                notes \
        FROM iris.r_node n \
        JOIN iris.geo_loc g ON n.geo_loc = g.name";

    /// SQL query for one RNode
    pub const SQL_ONE: &'static str =
        "SELECT n.name, roadway, road_dir, cross_mod, cross_street, cross_dir, \
                landmark, lat, lon, node_type, pickable, above, transition, \
                lanes, attach_side, shift, active, station_id, speed_limit, \
                notes \
        FROM iris.r_node n \
        JOIN iris.geo_loc g ON n.geo_loc = g.name \
        WHERE n.name = $1";

    /// Create an RNode from a result Row
    pub fn from_row(row: &Row) -> Self {
        let loc = GeoLoc {
            roadway: row.get(1),
            road_dir: row.get(2),
            cross_mod: row.get(3),
            cross_street: row.get(4),
            cross_dir: row.get(5),
            lankmark: row.get(6),
            location: None,
            lat: row.get(7),
            lon: row.get(8),
        };
        RNode {
            name: row.get(0),
            loc,
            node_type: row.get(9),
            pickable: row.get(10),
            above: row.get(11),
            transition: row.get(12),
            lanes: row.get(13),
            attach_side: row.get(14),
            shift: row.get(15),
            active: row.get(16),
            station_id: row.get(17),
            speed_limit: row.get(18),
            notes: row.get(19),
        }
    }

    /// Get the corridor ID
    fn cor_id(&self) -> Option<CorridorId> {
        match (&self.loc.roadway, self.loc.road_dir) {
            (Some(roadway), Some(road_dir)) => {
                let roadway = roadway.clone();
                match TravelDir::from_i16(road_dir) {
                    Some(travel_dir) => Some(CorridorId {
                        roadway,
                        travel_dir,
                    }),
                    None => None,
                }
            }
            _ => None,
        }
    }

    /// Get the lat/lon of the node
    fn latlon(&self) -> Option<(f64, f64)> {
        if self.active {
            self.loc.latlon()
        } else {
            None
        }
    }

    /// Get the node position
    fn pos(&self) -> Option<Wgs84Pos> {
        if self.active {
            self.loc.pos()
        } else {
            None
        }
    }
}

impl Corridor {
    /// Create a new corridor
    fn new(cor_id: CorridorId) -> Self {
        debug!("Corridor::new {:?}", &cor_id);
        let nodes = vec![];
        let count = 0;
        Corridor {
            cor_id,
            nodes,
            count,
        }
    }

    /// Compare lat/lon positions in corridor direction
    fn cmp_dir(
        &self,
        lat: f64,
        lt: f64,
        lon: f64,
        ln: f64,
    ) -> Option<Ordering> {
        match self.cor_id.travel_dir {
            TravelDir::NB => lt.partial_cmp(&lat),
            TravelDir::SB => lat.partial_cmp(&lt),
            TravelDir::EB => ln.partial_cmp(&lon),
            TravelDir::WB => lon.partial_cmp(&ln),
        }
    }

    /// Get index of the first node in corridor direction
    fn first_node(&mut self) -> Option<usize> {
        let mut idx_lt_ln = None; // (node index, lat, lon)
        for (i, ref n) in self.nodes.iter().enumerate() {
            if let Some((lat, lon)) = n.latlon() {
                idx_lt_ln = Some(match idx_lt_ln {
                    None => (i, lat, lon),
                    Some((j, lt, ln)) => match self.cmp_dir(lat, lt, lon, ln) {
                        Some(Ordering::Greater) => (i, lat, lon),
                        _ => (j, lt, ln),
                    },
                });
            }
        }
        idx_lt_ln.map(|(i, _lt, _ln)| i)
    }

    /// Get index of the nearest node after idx
    fn nearest_node(&self, idx: usize) -> Option<usize> {
        let pos = self.nodes.get(idx)?.pos()?;
        let mut idx_dist = None; // (node index, distance)
        for (i, ref n) in self.nodes.iter().enumerate().skip(idx + 1) {
            if let Some(ref p) = n.pos() {
                let i_dist = (i, pos.distance_haversine(p));
                idx_dist = Some(match idx_dist {
                    None => i_dist,
                    Some((j, d)) => {
                        if d < i_dist.1 {
                            (j, d)
                        } else {
                            i_dist
                        }
                    }
                });
            }
        }
        idx_dist.map(|(i, _d)| i)
    }

    /// Order all nodes
    fn order_nodes(&mut self) {
        self.count = match self.first_node() {
            Some(i) => {
                if i > 0 {
                    self.nodes.swap(0, i);
                }
                let mut idx = 0;
                while idx < self.nodes.len() {
                    match self.nearest_node(idx) {
                        Some(j) => self.nodes.swap(idx + 1, j),
                        None => break,
                    }
                    idx += 1;
                }
                idx + 1
            }
            None => 0,
        };
        debug!(
            "order_nodes: {:?}, count: {} of {}",
            self.cor_id,
            self.count,
            self.nodes.len()
        );
    }

    /// Add a node
    fn add_node(&mut self, node: RNode, ordered: bool) {
        debug!(
            "add_node {} to {:?} ({} + 1)",
            &node.name,
            &self.cor_id,
            self.nodes.len()
        );
        self.nodes.push(node);
        if ordered {
            self.order_nodes();
        }
    }

    /// Update a node
    fn update_node(&mut self, node: RNode, ordered: bool) {
        debug!(
            "update_node {} to {:?} ({})",
            &node.name,
            &self.cor_id,
            self.nodes.len()
        );
        match self.nodes.iter_mut().find(|n| n.name == node.name) {
            Some(n) => *n = node,
            None => error!("update_node: {} not found", node.name),
        }
        if ordered {
            self.order_nodes();
        }
    }

    /// Remove a node
    fn remove_node(&mut self, name: &str, ordered: bool) {
        debug!(
            "remove_node {} from {:?} ({} - 1)",
            &name,
            &self.cor_id,
            self.nodes.len()
        );
        match self.nodes.iter().position(|n| n.name == name) {
            Some(idx) => {
                self.nodes.remove(idx);
                if ordered {
                    self.order_nodes();
                }
            }
            None => error!("remove_node: {} not found", name),
        }
    }
}

impl SegmentState {
    /// Add or update a node
    fn add_update_node(&mut self, node: RNode) {
        match node.cor_id() {
            Some(ref cor_id) => {
                match self.node_cors.insert(node.name.clone(), cor_id.clone()) {
                    None => self.add_corridor_node(&cor_id, node),
                    Some(ref cid) if cid != cor_id => {
                        self.remove_corridor_node(cid, &node.name);
                        self.add_corridor_node(cor_id, node);
                    }
                    Some(_) => self.update_corridor_node(cor_id, node),
                }
            }
            _ => debug!("ignoring node: {}", &node.name),
        }
    }

    /// Add a node to corridor
    fn add_corridor_node(&mut self, cid: &CorridorId, node: RNode) {
        match self.corridors.get_mut(&cid) {
            Some(cor) => cor.add_node(node, self.ordered),
            None => {
                let mut cor = Corridor::new(cid.clone());
                cor.add_node(node, self.ordered);
                self.corridors.insert(cid.clone(), cor);
            }
        }
    }

    /// Remove a node from a corridor
    fn remove_corridor_node(&mut self, cid: &CorridorId, name: &str) {
        match self.corridors.get_mut(cid) {
            Some(cor) => {
                cor.remove_node(name, self.ordered);
                if cor.nodes.is_empty() {
                    debug!("removing corridor: {:?}", cid);
                    self.corridors.remove(cid);
                }
            }
            None => error!("corridor ID not found {:?}", cid),
        }
    }

    /// Update a node on a corridor
    fn update_corridor_node(&mut self, cid: &CorridorId, node: RNode) {
        match self.corridors.get_mut(cid) {
            Some(cor) => cor.update_node(node, self.ordered),
            None => error!("corridor ID not found {:?}", cid),
        }
    }

    /// Remove a node
    fn remove_node(&mut self, name: &str) {
        match self.node_cors.remove(name) {
            Some(ref cid) => self.remove_corridor_node(cid, name),
            None => error!("corridor not found for node: {}", name),
        }
    }

    /// Order all corridors
    fn set_ordered(&mut self, ordered: bool) {
        self.ordered = ordered;
        if ordered {
            for cor in self.corridors.values_mut() {
                cor.order_nodes();
            }
        }
    }
}

/// Receive roadway nodes and update corridor segments
pub fn receive_nodes(receiver: Receiver<RNodeMsg>) {
    let mut state = SegmentState::default();
    loop {
        match receiver.recv().unwrap() {
            RNodeMsg::AddUpdate(node) => state.add_update_node(node),
            RNodeMsg::Remove(name) => state.remove_node(&name),
            RNodeMsg::Order(ordered) => state.set_ordered(ordered),
        }
        debug!("total corridors: {}", state.corridors.len());
    }
}
