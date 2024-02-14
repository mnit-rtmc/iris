// segments.rs
//
// Copyright (C) 2019-2024  Minnesota Department of Transportation
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
use crate::files::AtomicFile;
use crate::Result;
use mvt::{WebMercatorPos, Wgs84Pos};
use pointy::Pt;
use postgis::ewkb::{LineString, Point, Polygon};
use postgres::Row;
use serde::Serializer;
use serde_derive::{Deserialize, Serialize};
use std::cmp::Ordering;
use std::collections::hash_map::DefaultHasher;
use std::collections::HashMap;
use std::fmt;
use std::hash::{Hash, Hasher};
use std::path::Path;
use std::sync::mpsc::Receiver;

/// Base segment scale factor
const BASE_SCALE: f64 = 1.0 / 6.0;

/// Outer segment scale factor
const OUTER_SCALE: f64 = 16.0 / 6.0;

/// Road definition
#[allow(unused)]
pub struct Road {
    name: String,
    abbrev: String,
    r_class: i16,
    direction: i16,
    scale: f32,
}

/// Serialize lat/lon values with f32 precision for smaller JSON
fn serialize_latlon<S>(
    x: &Option<f64>,
    s: S,
) -> std::result::Result<S::Ok, S::Error>
where
    S: Serializer,
{
    match x {
        Some(x) => s.serialize_f32(*x as f32),
        None => s.serialize_none(),
    }
}

/// Roadway node
#[derive(Debug, Deserialize, Serialize)]
pub struct RNode {
    name: String,
    #[serde(skip_serializing)]
    roadway: Option<String>,
    #[serde(skip_serializing)]
    road_dir: Option<String>,
    location: Option<String>,
    #[serde(serialize_with = "serialize_latlon")]
    lat: Option<f64>,
    #[serde(serialize_with = "serialize_latlon")]
    lon: Option<f64>,
    transition: String,
    lanes: i32,
    shift: i32,
    active: bool,
    station_id: Option<String>,
    speed_limit: i32,
}

/// Segment notification message
pub enum SegMsg {
    /// Update (or add) a Road
    UpdateRoad(Road),
    /// Update (or add) an RNode
    UpdateNode(RNode),
    /// Remove an RNode
    RemoveNode(String),
    /// Enable/disable ordering for all RNodes
    Order(bool),
}

/// General direction of travel
#[derive(Clone, Copy, Debug, Eq, Hash, PartialEq)]
enum TravelDir {
    /// Northbound
    Nb,
    /// Southbound
    Sb,
    /// Eastbound
    Eb,
    /// Westbound
    Wb,
}

impl TravelDir {
    fn from_str(dir: &str) -> Option<Self> {
        match dir {
            "NB" => Some(TravelDir::Nb),
            "SB" => Some(TravelDir::Sb),
            "EB" => Some(TravelDir::Eb),
            "WB" => Some(TravelDir::Wb),
            _ => None,
        }
    }
}

/// Corridor ID
#[derive(Clone, Debug, Eq, Hash, PartialEq)]
struct CorridorId {
    /// Name of corridor roadway
    roadway: String,
    /// Corridor road abbreviation
    abbrev: String,
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
    /// Base SID
    base_sid: i64,
    /// Road class ordinal
    r_class: i16,
    /// Road class scale
    scale: f64,
    /// Nodes ordered by corridor direction
    nodes: Vec<RNode>,
    /// Valid node count
    count: usize,
}

/// Segments for a corridor
#[allow(unused)]
struct Segments<'a> {
    /// Corridor ref
    cor: &'a Corridor,
    /// Name of corridor
    cor_name: String,
    /// All points on corridor
    pts: Vec<Pt<f64>>,
    /// Normal vectors for all points
    norms: Vec<Pt<f64>>,
    /// Meter distance for all points
    meters: Vec<f64>,
}

/// State of all segments
struct SegmentState {
    /// Mapping of roads
    roads: HashMap<String, Road>,
    /// Mapping of node names to corridor IDs
    node_cors: HashMap<String, CorridorId>,
    /// Mapping of corridor IDs to Corridors
    corridors: HashMap<CorridorId, Corridor>,
    /// Ordered flag
    ordered: bool,
}

impl fmt::Display for TravelDir {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let dir = match self {
            TravelDir::Nb => "NB",
            TravelDir::Sb => "SB",
            TravelDir::Eb => "EB",
            TravelDir::Wb => "WB",
        };
        write!(f, "{dir}")
    }
}

impl fmt::Display for CorridorId {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}", self.roadway, self.travel_dir)
    }
}

impl RNode {
    /// SQL query for all RNodes
    pub const SQL_ALL: &'static str =
        "SELECT name, roadway, road_dir, location, lat, lon, transition, \
                lanes, shift, active, station_id, speed_limit \
        FROM r_node_view";

    /// SQL query for one RNode
    pub const SQL_ONE: &'static str =
        "SELECT name, roadway, road_dir, location, lat, lon, transition, \
                lanes, shift, active, station_id, speed_limit \
        FROM r_node_view n \
        WHERE n.name = $1";

    /// Create an RNode from a result Row
    pub fn from_row(row: &Row) -> Self {
        RNode {
            name: row.get(0),
            roadway: row.get(1),
            road_dir: row.get(2),
            location: row.get(3),
            lat: row.get(4),
            lon: row.get(5),
            transition: row.get(6),
            lanes: row.get(7),
            shift: row.get(8),
            active: row.get(9),
            station_id: row.get(10),
            speed_limit: row.get(11),
        }
    }

    /// Get the corridor ID
    fn cor_id(&self, roads: &HashMap<String, Road>) -> Option<CorridorId> {
        match (&self.roadway, &self.road_dir) {
            (Some(roadway), Some(road_dir)) => {
                let roadway = roadway.clone();
                let abbrev = roads
                    .get(&roadway)
                    .map(|r| r.abbrev.clone())
                    .unwrap_or_else(|| "".to_owned());
                TravelDir::from_str(road_dir).map(|travel_dir| CorridorId {
                    roadway,
                    abbrev,
                    travel_dir,
                })
            }
            _ => None,
        }
    }

    /// Check if active and the location is valid
    fn is_valid(&self) -> bool {
        self.active && self.latlon().is_some()
    }

    /// Get the lat/lon of the node
    fn latlon(&self) -> Option<(f64, f64)> {
        if self.active {
            match (self.lat, self.lon) {
                (Some(lat), Some(lon)) => Some((lat, lon)),
                _ => None,
            }
        } else {
            None
        }
    }

    /// Get the node position
    fn pos(&self) -> Option<Wgs84Pos> {
        if self.active {
            self.latlon().map(|(lat, lon)| Wgs84Pos::new(lat, lon))
        } else {
            None
        }
    }

    /// Check if node should break segment
    fn is_break(&self) -> bool {
        self.station_id.is_some() || self.is_common()
    }

    /// Check if node is a common section
    fn is_common(&self) -> bool {
        self.transition == "common"
    }
}

impl Road {
    /// SQL query for all Roads
    pub const SQL_ALL: &'static str =
        "SELECT name, abbrev, r_class, direction, scale \
        FROM iris.road \
        JOIN iris.road_class ON r_class = id";

    /// SQL query for one Road
    pub const SQL_ONE: &'static str =
        "SELECT name, abbrev, r_class, direction, scale \
        FROM iris.road \
        JOIN iris.road_class ON r_class = id \
        WHERE name = $1";

    /// Create a Road from a result Row
    pub fn from_row(row: &Row) -> Self {
        Road {
            name: row.get(0),
            abbrev: row.get(1),
            r_class: row.get(2),
            direction: row.get(3),
            scale: row.get(4),
        }
    }
}

impl Corridor {
    /// Create a new corridor
    fn new(
        cor_id: CorridorId,
        base_sid: i64,
        r_class: i16,
        scale: f64,
    ) -> Self {
        log::debug!("Corridor::new {}", &cor_id);
        let nodes = vec![];
        let count = 0;
        Corridor {
            cor_id,
            base_sid,
            r_class,
            scale,
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
            TravelDir::Nb => lt.partial_cmp(&lat),
            TravelDir::Sb => lat.partial_cmp(&lt),
            TravelDir::Eb => ln.partial_cmp(&lon),
            TravelDir::Wb => lon.partial_cmp(&ln),
        }
    }

    /// Get index of the first node in corridor direction
    fn first_node(&mut self) -> Option<usize> {
        let mut idx_lt_ln = None; // (node index, lat, lon)
        for (i, n) in self.nodes.iter().enumerate() {
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
        for (i, n) in self.nodes.iter().enumerate().skip(idx + 1) {
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
        log::debug!(
            "order_nodes: {}, count: {} of {}",
            self.cor_id,
            self.count,
            self.nodes.len(),
        );
    }

    /// Create segments for all zoom levels
    fn create_segments(
        &self,
    ) -> Result<()> {
        let pts = self.create_points();
        log::info!("{}: {} points", self.cor_id, pts.len());
        if !pts.is_empty() {
            let segments = Segments::new(self, pts);
            segments.create_all()?;
        }
        Ok(())
    }

    /// Create points for corridor nodes
    fn create_points(&self) -> Vec<Pt<f64>> {
        self.nodes
            .iter()
            .filter_map(|n| n.pos())
            .map(|p| Pt::from(WebMercatorPos::from(p)))
            .collect()
    }

    /// Create meter-points for corridor nodes
    fn create_meterpoints(&self) -> Vec<f64> {
        let mut meters = vec![];
        let mut meter = 0.0;
        let mut ppos: Option<Wgs84Pos> = None;
        for pos in self.nodes.iter().filter_map(|n| n.pos()) {
            if let Some(ppos) = ppos {
                meter += pos.distance_haversine(&ppos);
            }
            meters.push(meter);
            ppos = Some(pos);
        }
        meters
    }

    /// Add a node to corridor
    fn add_node(&mut self, node: RNode, ordered: bool) {
        log::debug!(
            "add_node {} to {} ({} + 1)",
            &node.name,
            &self.cor_id,
            self.nodes.len()
        );
        let order = ordered && node.is_valid();
        self.nodes.push(node);
        if order {
            self.order_nodes();
        }
    }

    /// Update a node
    fn update_node(&mut self, node: RNode, ordered: bool) {
        log::debug!(
            "update_node {} to {} ({})",
            &node.name,
            &self.cor_id,
            self.nodes.len()
        );
        let valid_after = node.is_valid();
        let mut valid_before = false;
        match self.nodes.iter_mut().find(|n| n.name == node.name) {
            Some(n) => {
                valid_before = n.is_valid();
                *n = node;
            }
            None => log::error!("update_node: {} not found", node.name),
        }
        if ordered && (valid_before || valid_after) {
            self.order_nodes();
        }
    }

    /// Remove a node
    fn remove_node(&mut self, name: &str, ordered: bool) {
        log::debug!(
            "remove_node {} from {} ({} - 1)",
            &name,
            &self.cor_id,
            self.nodes.len()
        );
        match self.nodes.iter().position(|n| n.name == name) {
            Some(idx) => {
                let node = self.nodes.remove(idx);
                if ordered && node.is_valid() {
                    self.order_nodes();
                }
            }
            None => log::error!("remove_node: {} not found", name),
        }
    }

    /// Write corridor nodes to a file
    fn write_file(&self) -> Result<()> {
        let dir = Path::new("corridors");
        let cor_name =
            format!("{}_{}", self.cor_id.abbrev, self.cor_id.travel_dir);
        let file = AtomicFile::new(dir, &cor_name)?;
        let writer = file.writer()?;
        serde_json::to_writer(writer, &self.nodes)?;
        Ok(())
    }
}

impl<'a> Segments<'a> {
    /// Create corridor segments
    fn new(cor: &'a Corridor, pts: Vec<Pt<f64>>) -> Self {
        let cor_name = cor.cor_id.to_string();
        let norms = create_norms(&pts);
        let meters = cor.create_meterpoints();
        Segments {
            cor,
            cor_name,
            pts,
            norms,
            meters,
        }
    }

    /// Create segments for all zoom levels
    fn create_all(
        &self,
    ) -> Result<()> {
        for zoom in 0..=18 {
            if road_class_zoom(self.cor.r_class, zoom) {
                self.create_segments_zoom(zoom)?;
            }
        }
        Ok(())
    }

    /// Create segments for one zoom level
    fn create_segments_zoom(
        &self,
        zoom: i32,
    ) -> crate::Result<()> {
        let o_scale = self.scale_zoom(OUTER_SCALE, zoom);
        let i_scale = self.scale_zoom(BASE_SCALE, zoom);
        let mut poly = Vec::<(Pt<f64>, Pt<f64>)>::with_capacity(16);
        let mut seg_meter = 0.0; // meter point for the current segment
        let mut p_meter = 0.0; // meter point for the previous point
        let mut _sid = self.cor.base_sid;
        let mut _station_id = None;
        let nodes = &self.cor.nodes[..];
        for (node, (pt, (norm, meter))) in nodes.iter().zip(
            self.pts
                .iter()
                .zip(self.norms.iter().zip(self.meters.iter())),
        ) {
            // FIXME: use `map_segment_max_meters` system attribute
            let too_long = *meter >= seg_meter + 2500.0;
            let outer = *pt + *norm * o_scale;
            let inner = *pt + *norm * i_scale;
            if node.is_break() || too_long {
                if *meter < p_meter + 2500.0 {
                    poly.push((outer, inner));
                }
                if poly.len() > 1 {
                    let _way = self.create_way(&poly);
                    // FIXME: write way to loam
                    _sid += 1;
                }
                poly.clear();
                seg_meter = *meter;
                _station_id = node.station_id.clone();
            }
            if !node.is_common() {
                poly.push((outer, inner));
            }
            p_meter = *meter;
        }
        if poly.len() > 1 {
            let _way = self.create_way(&poly);
            // FIXME: write way to loam
        }
        Ok(())
    }

    /// Scale a vector normal with zoom level
    fn scale_zoom(&self, scale: f64, zoom: i32) -> f64 {
        self.cor.scale * scale * f64::from(1 << (16 - 16.min(10.max(zoom))))
    }

    /// Create polygon for way column
    fn create_way(&self, poly: &[(Pt<f64>, Pt<f64>)]) -> Polygon {
        let mut points = vec![];
        for (vtx, _) in poly {
            points.push(Point::new(vtx.x, vtx.y, None));
        }
        for (_, vtx) in poly.iter().rev() {
            points.push(Point::new(vtx.x, vtx.y, None));
        }
        if let Some((vtx, _)) = poly.iter().next() {
            points.push(Point::new(vtx.x, vtx.y, None));
        }
        let mut linestring = LineString::new();
        linestring.points = points;
        let mut way = Polygon::new();
        way.rings.push(linestring);
        way.srid = Some(3857);
        way
    }
}

/// Check if a road class should appear at a given zoom level
fn road_class_zoom(r_class: i16, zoom: i32) -> bool {
    match r_class {
        1 => zoom >= 15, // RESIDENTIAL
        2 => zoom >= 14, // BUSINESS
        3 => zoom >= 13, // COLLECTOR
        4 => zoom >= 12, // ARTERIAL
        5 => zoom >= 11, // EXPRESSWAY
        6 => zoom >= 9,  // FREEWAY
        7 => zoom >= 13, // CD ROAD
        _ => false,
    }
}

/// Create normal vectors for a slice of points
fn create_norms(pts: &[Pt<f64>]) -> Vec<Pt<f64>> {
    let mut norms = vec![];
    for i in 0..pts.len() {
        let upstream = vector_upstream(pts, i);
        let downstream = vector_downstream(pts, i);
        let v0 = match (upstream, downstream) {
            (Some(up), Some(down)) => (up + down).normalize(),
            (Some(up), None) => up,
            (None, Some(down)) => down,
            (None, None) => todo!("use corridor direction"),
        };
        norms.push(v0.left());
    }
    norms
}

/// Get vector from upstream point to current point
fn vector_upstream(pts: &[Pt<f64>], i: usize) -> Option<Pt<f64>> {
    let current = pts[i];
    for up in pts[0..i].iter().rev() {
        if *up != current {
            return Some((*up - current).normalize());
        }
    }
    None
}

/// Get vector from current point to downstream point
fn vector_downstream(pts: &[Pt<f64>], i: usize) -> Option<Pt<f64>> {
    let current = pts[i];
    for down in pts[i + 1..].iter() {
        if *down != current {
            return Some((current - *down).normalize());
        }
    }
    None
}

impl SegmentState {
    /// Create a new segment state
    fn new() -> Self {
        SegmentState {
            roads: HashMap::default(),
            corridors: HashMap::default(),
            node_cors: HashMap::default(),
            ordered: false,
        }
    }

    /// Get road class
    fn r_class(&self, road: &str) -> i16 {
        self.roads.get(road).map(|r| r.r_class).unwrap_or(0)
    }

    /// Get scale for a road
    fn scale(&self, road: &str) -> f64 {
        self.roads
            .get(road)
            .map(|r| f64::from(r.scale))
            .unwrap_or(BASE_SCALE)
    }

    /// Update (or add) a node
    fn update_node(&mut self, node: RNode) {
        match node.cor_id(&self.roads) {
            Some(ref cor_id) => {
                match self.node_cors.insert(node.name.clone(), cor_id.clone()) {
                    None => self.add_corridor_node(cor_id, node),
                    Some(ref cid) if cid != cor_id => {
                        self.remove_corridor_node(cid, &node.name);
                        self.add_corridor_node(cor_id, node);
                    }
                    Some(_) => self.update_corridor_node(cor_id, node),
                }
            }
            _ => log::debug!("ignoring node: {}", &node.name),
        }
    }

    /// Add a node to corridor
    fn add_corridor_node(&mut self, cid: &CorridorId, node: RNode) {
        match self.corridors.get_mut(cid) {
            Some(cor) => cor.add_node(node, self.ordered),
            None => {
                // Leaflet doesn't like it when we use the high bits...
                let base_sid = 0xFFFF_FFFF_FFFF & {
                    let mut hasher = DefaultHasher::new();
                    cid.hash(&mut hasher);
                    hasher.finish()
                } as i64;
                let r_class = self.r_class(&cid.roadway);
                let scale = self.scale(&cid.roadway);
                let mut cor =
                    Corridor::new(cid.clone(), base_sid, r_class, scale);
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
                    log::debug!("removing corridor: {:?}", cid);
                    self.corridors.remove(cid);
                }
            }
            None => log::error!("corridor ID not found {:?}", cid),
        }
    }

    /// Update a node on a corridor
    fn update_corridor_node(&mut self, cid: &CorridorId, node: RNode) {
        match self.corridors.get_mut(cid) {
            Some(cor) => cor.update_node(node, self.ordered),
            None => log::error!("corridor ID not found {:?}", cid),
        }
    }

    /// Remove a node
    fn remove_node(&mut self, name: &str) {
        match self.node_cors.remove(name) {
            Some(ref cid) => self.remove_corridor_node(cid, name),
            None => log::error!("corridor not found for node: {}", name),
        }
    }

    /// Order all corridors
    fn set_ordered(&mut self, ordered: bool) -> Result<()> {
        self.ordered = ordered;
        if ordered {
            for cor in self.corridors.values_mut() {
                cor.order_nodes();
                cor.create_segments()?;
                cor.write_file()?;
            }
            // FIXME: write segment loam layer
        }
        Ok(())
    }

    /// Update a road class or scale
    fn update_road(&mut self, road: Road) -> Result<()> {
        log::debug!("update_road {}", road.name);
        let name = road.name.clone();
        self.roads.insert(name.clone(), road);
        if self.ordered {
            let scale = self.scale(&name);
            for cor in self.corridors.values_mut() {
                if cor.cor_id.roadway == name {
                    cor.scale = scale;
                    cor.order_nodes();
                    cor.create_segments()?;
                    cor.write_file()?;
                }
            }
            // FIXME: write segment loam layer
        }
        Ok(())
    }
}

/// Receive segment messages and update corridor segments
pub fn receive_nodes(receiver: Receiver<SegMsg>) -> Result<()> {
    let mut state = SegmentState::new();
    loop {
        match receiver.recv()? {
            SegMsg::UpdateRoad(road) => state.update_road(road)?,
            SegMsg::UpdateNode(node) => state.update_node(node),
            SegMsg::RemoveNode(name) => state.remove_node(&name),
            SegMsg::Order(ordered) => state.set_ordered(ordered)?,
        }
        log::debug!("total corridors: {}", state.corridors.len());
    }
}
