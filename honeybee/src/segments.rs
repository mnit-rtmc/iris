// segments.rs
//
// Copyright (C) 2019-2025  Minnesota Department of Transportation
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
use crate::error::Result;
use crate::files::AtomicFile;
use mvt::{WebMercatorPos, Wgs84Pos};
use pointy::{Pt, Transform};
use resources::Res;
use rosewood::BulkWriter;
use rosewood::gis::Polygons;
use serde::{Deserialize, Serialize, Serializer};
use std::cmp::Ordering;
use std::collections::HashMap;
use std::collections::hash_map::DefaultHasher;
use std::fmt;
use std::hash::{Hash, Hasher};
use std::ops::RangeInclusive;
use std::path::{Path, PathBuf};
use tokio_postgres::Row;

/// Path to store .loam files
const LOAM_PATH: &str = "/var/local/earthwyrm/loam";

/// Base segment scale factor
const BASE_SCALE: f64 = 1.0 / 6.0;

/// Outer segment scale factor
const OUTER_SCALE: f64 = 16.0 / 6.0;

/// Tag values, in order specified by tag pattern rule
type Values = Vec<Option<String>>;

/// Road definition
#[allow(unused)]
#[derive(Clone)]
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
#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct RNode {
    name: String,
    #[serde(skip_serializing)]
    roadway: Option<String>,
    #[serde(skip_serializing)]
    road_dir: Option<String>,
    location: Option<String>,
    #[serde(
        serialize_with = "serialize_latlon",
        skip_serializing_if = "Option::is_none"
    )]
    lat: Option<f64>,
    #[serde(
        serialize_with = "serialize_latlon",
        skip_serializing_if = "Option::is_none"
    )]
    lon: Option<f64>,
    transition: String,
    lanes: i32,
    shift: i32,
    active: bool,
    #[serde(skip_serializing_if = "Option::is_none")]
    station_id: Option<String>,
    speed_limit: i32,
}

/// Geo location
#[derive(Clone, Debug, Default, PartialEq)]
pub struct GeoLoc {
    name: String,
    roadway: Option<String>,
    road_dir: i16,
    lat: Option<f64>,
    lon: Option<f64>,
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

/// Corridor ID
#[derive(Clone, Debug, Eq, Hash, PartialEq)]
pub struct CorridorId {
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
#[derive(Clone, Debug)]
struct Corridor {
    /// Corridor ID
    cor_id: CorridorId,
    /// Base TMS ID
    base_tms_id: i64,
    /// Road class ordinal
    r_class: i16,
    /// Road class scale
    scale: f64,
    /// All nodes in corridor
    nodes: Vec<RNode>,
    /// All points on corridor (WebMercator)
    pts: Vec<Pt<f64>>,
    /// Normal vectors for all points
    norms: Vec<Pt<f64>>,
    /// Meter distance for all points
    meters: Vec<f64>,
}

/// Segment outline builder
struct Segment {
    /// Traffic management system ID (for leaflet)
    tms_id: i64,
    /// Station ID
    station_id: Option<String>,
    /// Meter point on corridor
    meter: f64,
    /// Outer / inner points
    points: Vec<(Pt<f64>, Pt<f64>)>,
}

/// State of all segments
#[derive(Clone, Default)]
pub struct SegmentState {
    /// Mapping of roads
    roads: HashMap<String, Road>,
    /// Mapping of node names to corridor IDs
    node_cors: HashMap<String, CorridorId>,
    /// Mapping of corridor IDs to Corridors
    corridors: HashMap<CorridorId, Corridor>,
    /// Flag indicating r_nodes are complete
    has_nodes: bool,
    /// Flag indicating roads are complete
    has_roads: bool,
    /// Location markers to write
    markers: HashMap<Res, Vec<GeoLoc>>,
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

impl TravelDir {
    fn from_i16(dir: i16) -> Option<Self> {
        match dir {
            1 => Some(TravelDir::Nb),
            2 => Some(TravelDir::Sb),
            3 => Some(TravelDir::Eb),
            4 => Some(TravelDir::Wb),
            _ => None,
        }
    }

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

impl From<TravelDir> for Pt<f64> {
    fn from(td: TravelDir) -> Self {
        match td {
            TravelDir::Nb => Pt::new(0.0, 1.0),
            TravelDir::Sb => Pt::new(0.0, -1.0),
            TravelDir::Eb => Pt::new(1.0, 0.0),
            TravelDir::Wb => Pt::new(-1.0, 0.0),
        }
    }
}

impl From<(&str, TravelDir)> for CorridorId {
    fn from((roadway, travel_dir): (&str, TravelDir)) -> Self {
        CorridorId {
            roadway: roadway.to_string(),
            travel_dir,
        }
    }
}

impl fmt::Display for CorridorId {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}", self.roadway, self.travel_dir)
    }
}

impl Road {
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

impl RNode {
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

    /// Get the RNode corridor ID
    fn cor_id(&self) -> Option<CorridorId> {
        match (&self.roadway, &self.road_dir) {
            (Some(roadway), Some(rd)) => TravelDir::from_str(rd)
                .map(|td| CorridorId::from((roadway.as_str(), td))),
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

impl GeoLoc {
    /// Create a GeoLoc from a result Row
    pub fn from_row(row: Row) -> Self {
        GeoLoc {
            name: row.get(0),
            roadway: row.get(1),
            road_dir: row.get(2),
            lat: row.get(3),
            lon: row.get(4),
        }
    }

    /// Get the corridor ID
    fn cor_id(&self) -> Option<CorridorId> {
        match (&self.roadway, TravelDir::from_i16(self.road_dir)) {
            (Some(roadway), Some(td)) => {
                Some(CorridorId::from((roadway.as_str(), td)))
            }
            _ => None,
        }
    }

    /// Get the lat/lon of the location
    fn latlon(&self) -> Option<(f64, f64)> {
        match (self.lat, self.lon) {
            (Some(lat), Some(lon)) => Some((lat, lon)),
            _ => None,
        }
    }

    /// Get the location
    fn pos(&self) -> Option<Wgs84Pos> {
        self.latlon().map(|(lat, lon)| Wgs84Pos::new(lat, lon))
    }

    /// Get the location point
    fn point(&self) -> Option<Pt<f64>> {
        self.pos().map(|pos| Pt::from(WebMercatorPos::from(pos)))
    }

    /// Get the location normal
    fn normal(&self) -> f64 {
        let dir = TravelDir::from_i16(self.road_dir).unwrap_or(TravelDir::Nb);
        Pt::from(dir).right().angle()
    }

    /// Get tag values
    fn values(&self) -> Values {
        vec![Some(self.name.clone())]
    }
}

impl Corridor {
    /// Create a new corridor
    fn new(cor_id: CorridorId, r_class: i16, scale: f64) -> Self {
        log::trace!("Corridor::new {cor_id}");
        // Leaflet doesn't like it when we use the high bits...
        let base_tms_id = 0xFFFF_FFFF_FFFF & {
            let mut hasher = DefaultHasher::new();
            cor_id.hash(&mut hasher);
            hasher.finish()
        } as i64;
        Corridor {
            cor_id,
            base_tms_id,
            r_class,
            scale,
            nodes: Vec::new(),
            pts: Vec::new(),
            norms: Vec::new(),
            meters: Vec::new(),
        }
    }

    /// Check if "dirty" (needs arranging)
    fn is_dirty(&self) -> bool {
        self.pts.is_empty()
    }

    /// Make "dirty" (require arranging)
    fn make_dirty(&mut self) {
        self.pts.clear();
        self.norms.clear();
        self.meters.clear();
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

    /// Arrange nodes, points and normals
    fn arrange(&mut self) {
        self.order_nodes();
        self.create_points();
        self.create_norms();
        self.create_meterpoints();
    }

    /// Order all nodes
    fn order_nodes(&mut self) {
        let count = match self.first_node() {
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
        log::trace!(
            "order_nodes: {}, count: {count} of {}",
            self.cor_id,
            self.nodes.len(),
        );
    }

    /// Create points for corridor nodes
    fn create_points(&mut self) {
        self.pts = self
            .nodes
            .iter()
            .filter_map(|n| n.pos())
            .map(|p| Pt::from(WebMercatorPos::from(p)))
            .collect();
        log::info!("{}: {} points", self.cor_id, self.pts.len());
    }

    /// Create normal vectors for corridor points
    fn create_norms(&mut self) {
        let mut norms = Vec::with_capacity(self.pts.len());
        for i in 0..self.pts.len() {
            let upstream = vector_upstream(&self.pts, i);
            let downstream = vector_downstream(&self.pts, i);
            let v0 = match (upstream, downstream) {
                (Some(up), Some(down)) => (up + down).normalize(),
                (Some(up), None) => up,
                (None, Some(down)) => down,
                (None, None) => self.travel_dir(),
            };
            norms.push(v0.left());
        }
        self.norms = norms;
    }

    /// Get corridor travel direction
    fn travel_dir(&self) -> Pt<f64> {
        Pt::from(self.cor_id.travel_dir)
    }

    /// Create meter-points for corridor nodes
    fn create_meterpoints(&mut self) {
        let mut meters = Vec::with_capacity(self.pts.len());
        let mut meter = 0.0;
        let mut ppos: Option<Wgs84Pos> = None;
        for pos in self.nodes.iter().filter_map(|n| n.pos()) {
            if let Some(ppos) = ppos {
                meter += pos.distance_haversine(&ppos);
            }
            meters.push(meter);
            ppos = Some(pos);
        }
        self.meters = meters;
    }

    /// Add a node to corridor
    fn add_node(&mut self, node: RNode) {
        log::trace!(
            "Corridor::add_node {} to {} ({} + 1)",
            &node.name,
            &self.cor_id,
            self.nodes.len()
        );
        if node.is_valid() {
            self.make_dirty();
        }
        self.nodes.push(node);
    }

    /// Update a node
    fn update_node(&mut self, node: RNode) {
        log::trace!(
            "Corridor::update_node {} to {} ({})",
            &node.name,
            &self.cor_id,
            self.nodes.len()
        );
        let Some(n) = self.nodes.iter_mut().find(|n| n.name == node.name)
        else {
            log::error!("update_node: {} not found", node.name);
            return;
        };
        let dirty = n.is_valid() || node.is_valid();
        *n = node;
        if dirty {
            self.make_dirty();
        }
    }

    /// Remove a node
    fn remove_node(&mut self, name: &str) {
        log::trace!(
            "Corridor::remove_node {name} from {} ({} - 1)",
            &self.cor_id,
            self.nodes.len()
        );
        match self.nodes.iter().position(|n| n.name == name) {
            Some(idx) => {
                let node = self.nodes.remove(idx);
                if node.is_valid() {
                    self.make_dirty();
                }
            }
            None => log::error!("remove_node: {name} not found"),
        }
    }

    /// Write corridor nodes to a file
    async fn write_file(&self, roads: &HashMap<String, Road>) -> Result<()> {
        let abbrev = roads
            .get(&self.cor_id.roadway)
            .map(|r| &r.abbrev[..])
            .unwrap_or_else(|| "");
        if abbrev.is_empty() {
            log::warn!("write_file no 'abbrev' for {}", self.cor_id.roadway);
            return Ok(());
        }
        let cor_name = format!("{abbrev}_{}", self.cor_id.travel_dir);
        log::trace!("write_file {cor_name}");
        let json = serde_json::to_vec(&self.nodes)?;
        let dir = Path::new("corridors");
        let file = AtomicFile::new(dir, &cor_name).await?;
        file.write_buf(&json).await
    }

    /// Write segment layer for one zoom level
    fn write_segments(
        &self,
        writer: &mut BulkWriter<Values, f64, Polygons<f64, Values>>,
        zoom: i32,
    ) -> Result<()> {
        let o_scale = self.scale_zoom(OUTER_SCALE, zoom);
        let i_scale = self.scale_zoom(BASE_SCALE, zoom);
        let mut seg = Segment::new(self.base_tms_id);
        let mut p_meter = 0.0; // meter point for the previous point
        let nodes = &self.nodes[..];
        for (node, (pt, (norm, meter))) in nodes.iter().zip(
            self.pts
                .iter()
                .zip(self.norms.iter().zip(self.meters.iter())),
        ) {
            // FIXME: use `map_segment_max_meters` system attribute
            let too_long = *meter >= seg.meter + 2500.0;
            let outer = *pt + *norm * o_scale;
            let inner = *pt + *norm * i_scale;
            if node.is_break() || too_long {
                if *meter < p_meter + 2500.0 {
                    seg.points.push((outer, inner));
                }
                if seg.points.len() > 1 {
                    let polygon = seg.outline();
                    writer.push(&polygon)?;
                }
                seg.advance(node.station_id.clone(), *meter);
            }
            if !node.is_common() {
                seg.points.push((outer, inner));
            }
            p_meter = *meter;
        }
        if seg.points.len() > 1 {
            let polygon = seg.outline();
            writer.push(&polygon)?;
        }
        Ok(())
    }

    /// Scale a vector normal with zoom level
    fn scale_zoom(&self, scale: f64, zoom: i32) -> f64 {
        self.scale * scale * f64::from(1 << (16 - 16.min(10.max(zoom))))
    }

    /// Calculate normal vector for a corridor location
    fn loc_normal(&self, loc: &GeoLoc) -> f64 {
        let mut norm = loc.normal();
        let Some(pos) = loc.pos() else {
            return norm;
        };
        let mut dist_m = 2_000.0; // 2 km
        for (pt, n) in self.pts.iter().zip(&self.norms) {
            let pt = Wgs84Pos::from(WebMercatorPos::new(pt.x, pt.y));
            let dist = pt.distance_haversine(&pos);
            if dist < dist_m {
                dist_m = dist;
                norm = n.angle();
            }
        }
        norm
    }
}

impl Segment {
    /// Create a new segment
    fn new(tms_id: i64) -> Self {
        let station_id = None;
        let meter = 0.0;
        let points = Vec::<(Pt<f64>, Pt<f64>)>::with_capacity(16);
        Segment {
            tms_id,
            station_id,
            meter,
            points,
        }
    }

    /// Advance to next segment
    fn advance(&mut self, station_id: Option<String>, meter: f64) {
        self.tms_id += 1;
        self.station_id = station_id;
        self.meter = meter;
        self.points.clear();
    }

    /// Create outline polygon
    fn outline(&self) -> Polygons<f64, Values> {
        let mut pts = Vec::with_capacity(2 * self.points.len() + 1);
        for (vtx, _) in self.points.iter() {
            pts.push(Pt::from(vtx));
        }
        for (_, vtx) in self.points.iter().rev() {
            pts.push(Pt::from(vtx));
        }
        if let Some((vtx, _)) = self.points.first() {
            pts.push(Pt::from(vtx));
        }
        let values =
            vec![Some(self.tms_id.to_string()), self.station_id.clone()];
        let mut polygon = Polygons::new(values);
        polygon.push_outer(pts);
        polygon
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
    pub fn new() -> Self {
        Self::default()
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

    /// Check if corridors and segments should be written
    fn can_arrange(&self) -> bool {
        self.has_nodes && self.has_roads
    }

    /// Set has_nodes flag
    pub fn set_has_nodes(&mut self, has_nodes: bool) {
        self.has_nodes = has_nodes;
    }

    /// Set has_roads flag
    pub fn set_has_roads(&mut self, has_roads: bool) {
        self.has_roads = has_roads;
    }

    /// Update (or add) a node
    pub fn update_node(&mut self, node: RNode) {
        let Some(ref cor_id) = node.cor_id() else {
            log::debug!("ignoring node: {}", &node.name);
            return;
        };
        match self.node_cors.insert(node.name.clone(), cor_id.clone()) {
            None => self.add_corridor_node(cor_id, node),
            Some(ref cid) if cid != cor_id => {
                self.remove_corridor_node(cid, &node.name);
                self.add_corridor_node(cor_id, node);
            }
            Some(_) => self.update_corridor_node(cor_id, node),
        }
    }

    /// Remove a node
    pub fn remove_node(&mut self, name: &str) {
        match self.node_cors.remove(name) {
            Some(ref cid) => self.remove_corridor_node(cid, name),
            None => log::error!("corridor not found for node: {name}"),
        }
    }

    /// Add a node to corridor
    fn add_corridor_node(&mut self, cid: &CorridorId, node: RNode) {
        if !self.corridors.contains_key(cid) {
            let r_class = self.r_class(&cid.roadway);
            let scale = self.scale(&cid.roadway);
            let cor = Corridor::new(cid.clone(), r_class, scale);
            self.corridors.insert(cid.clone(), cor);
        }
        if let Some(cor) = self.corridors.get_mut(cid) {
            cor.add_node(node);
        }
    }

    /// Remove a node from a corridor
    fn remove_corridor_node(&mut self, cid: &CorridorId, name: &str) {
        match self.corridors.get_mut(cid) {
            Some(cor) => {
                cor.remove_node(name);
                if cor.nodes.is_empty() {
                    log::debug!("removing corridor: {cid:?}");
                    self.corridors.remove(cid);
                }
            }
            None => log::error!("corridor ID not found {cid:?}"),
        }
    }

    /// Update a node on a corridor
    fn update_corridor_node(&mut self, cid: &CorridorId, node: RNode) {
        match self.corridors.get_mut(cid) {
            Some(cor) => cor.update_node(node),
            None => log::error!("corridor ID not found {cid:?}"),
        }
    }

    /// Update a road class or scale
    pub fn update_road(&mut self, road: Road) {
        log::trace!("update_road {}", road.name);
        let name = road.name.clone();
        self.roads.insert(name.clone(), road);
        let r_class = self.r_class(&name);
        let scale = self.scale(&name);
        for cor in self.corridors.values_mut() {
            if cor.cor_id.roadway == name
                && (r_class != cor.r_class || scale != cor.scale)
            {
                cor.r_class = r_class;
                cor.scale = scale;
                cor.make_dirty();
            }
        }
    }

    /// Arrange all corridors
    pub fn arrange_corridors(&mut self) -> Vec<CorridorId> {
        let mut cors = Vec::new();
        if self.can_arrange() {
            for cor in self.corridors.values_mut() {
                if cor.is_dirty() {
                    cors.push(cor.cor_id.clone());
                    cor.arrange();
                }
            }
        }
        cors
    }

    /// Write a corridor file
    pub async fn write_corridor(&self, cid: CorridorId) -> Result<()> {
        if let Some(cor) = self.corridors.get(&cid) {
            cor.write_file(&self.roads).await?;
        }
        Ok(())
    }

    /// Write segments to loam files
    pub fn write_segments(&self) -> Result<()> {
        let dir = Path::new(LOAM_PATH);
        for zoom in 0..=18 {
            let mut loam = PathBuf::from(dir);
            loam.push(format!("segment_{zoom}.loam"));
            let mut empty = true;
            let mut writer = BulkWriter::new(loam)?;
            for cor in self.corridors.values() {
                if road_class_zoom(cor.r_class, zoom) {
                    cor.write_segments(&mut writer, zoom)?;
                    empty = false;
                }
            }
            if empty {
                writer.cancel()?;
            } else {
                writer.finish()?;
            }
        }
        Ok(())
    }

    /// Add location markers to write loam files
    ///
    /// * `res` Resource type.
    /// * `locs` Geo locations.
    pub fn add_loc_markers(&mut self, res: Res, locs: Vec<GeoLoc>) {
        self.markers.insert(res, locs);
    }

    /// Clear location markers
    pub fn clear_markers(&mut self) {
        if self.can_arrange() {
            self.markers.clear();
        }
    }

    /// Write location markers to loam files
    pub fn write_loc_markers(&self) -> Result<()> {
        let dir = Path::new(LOAM_PATH);
        for (res, locs) in self.markers.iter() {
            for zoom in zoom_levels(*res) {
                let sz = 800_000.0 * zoom_scale(zoom);
                let mut loam = PathBuf::from(dir);
                loam.push(format!("{}_{zoom}.loam", res.as_str()));
                let mut writer = BulkWriter::new(loam)?;
                for loc in locs {
                    if let Some(pt) = loc.point() {
                        let norm = self.loc_normal(loc);
                        let values = loc.values();
                        let mut polygon = Polygons::new(values);
                        polygon.push_outer(loc_marker(*res, pt, norm, sz));
                        writer.push(&polygon)?;
                    }
                }
                writer.finish()?;
            }
        }
        Ok(())
    }

    /// Calculate normal vector for a location
    fn loc_normal(&self, loc: &GeoLoc) -> f64 {
        if let Some(cid) = loc.cor_id()
            && let Some(cor) = self.corridors.get(&cid)
        {
            return cor.loc_normal(loc);
        };
        loc.normal()
    }
}

/// Calculate scale at one zoom level
fn zoom_scale(zoom: u32) -> f64 {
    1.0 / f64::from(1 << zoom)
}

/// Get range of zoom levels for a resource
fn zoom_levels(res: Res) -> RangeInclusive<u32> {
    match res {
        Res::Beacon => 10..=18,
        Res::Camera => 10..=18,
        Res::Dms => 11..=18,
        Res::Lcs => 12..=18,
        Res::RampMeter => 11..=18,
        Res::WeatherSensor => 10..=18,
        _ => unimplemented!(),
    }
}

/// Make a resource location marker
fn loc_marker(res: Res, pt: Pt<f64>, norm: f64, sz: f64) -> Vec<Pt<f64>> {
    match res {
        Res::Beacon => beacon_marker(pt, norm, sz),
        Res::Camera => camera_marker(pt, norm, sz),
        Res::Dms => dms_marker(pt, norm, sz),
        Res::Lcs => lcs_marker(pt, norm, sz),
        Res::RampMeter => ramp_meter_marker(pt, norm, sz),
        Res::WeatherSensor => weather_sensor_marker(pt, sz),
        _ => unimplemented!(),
    }
}

/// Make beacon marker
fn beacon_marker(pt: Pt<f64>, norm: f64, sz: f64) -> Vec<Pt<f64>> {
    const S2: f64 = std::f64::consts::SQRT_2;
    const S1: f64 = 2.0 - S2;
    let t = Transform::with_scale(sz, sz)
        .rotate(norm)
        .translate(pt.x, pt.y);
    vec![
        // base
        Pt::from((0.0, -2.0)) * t,
        Pt::from((S1, -S2)) * t,
        Pt::from((S2, -S2)) * t,
        Pt::from((S2, -S1)) * t,
        Pt::from((2.0, 0.0)) * t,
        Pt::from((S2, S1)) * t,
        Pt::from((S2, S2)) * t,
        Pt::from((S1, S2)) * t,
        // point
        Pt::from((0.0, 3.0)) * t,
        Pt::from((-S1, S2)) * t,
        Pt::from((-S2, S2)) * t,
        Pt::from((-S2, S1)) * t,
        Pt::from((-2.0, 0.0)) * t,
        Pt::from((-S2, -S1)) * t,
        Pt::from((-S2, -S2)) * t,
        Pt::from((-S1, -S2)) * t,
        // close
        Pt::from((0.0, -2.0)) * t,
    ]
}

/// Make camera marker
fn camera_marker(pt: Pt<f64>, norm: f64, sz: f64) -> Vec<Pt<f64>> {
    let t = Transform::with_scale(sz, sz)
        .rotate(norm)
        .translate(pt.x, pt.y);
    vec![
        Pt::from((0.0, 1.2)) * t,
        Pt::from((1.5, 0.4)) * t,
        Pt::from((2.0, 0.4)) * t,
        Pt::from((2.0, 1.2)) * t,
        Pt::from((6.0, 1.2)) * t,
        Pt::from((6.0, -1.2)) * t,
        Pt::from((2.0, -1.2)) * t,
        Pt::from((2.0, -0.4)) * t,
        Pt::from((1.5, -0.4)) * t,
        Pt::from((0.0, -1.2)) * t,
        Pt::from((0.0, 1.2)) * t,
    ]
}

/// Make DMS marker
fn dms_marker(pt: Pt<f64>, norm: f64, sz: f64) -> Vec<Pt<f64>> {
    let t = Transform::with_scale(sz, sz)
        .rotate(norm)
        .translate(pt.x, pt.y);
    vec![
        Pt::from((0.0, 0.0)) * t,
        Pt::from((5.0, 0.0)) * t,
        Pt::from((5.0, 1.0)) * t,
        Pt::from((4.0, 1.0)) * t,
        Pt::from((4.0, 3.0)) * t,
        Pt::from((3.0, 3.0)) * t,
        Pt::from((3.0, 1.0)) * t,
        Pt::from((2.0, 1.0)) * t,
        Pt::from((2.0, 3.0)) * t,
        Pt::from((1.0, 3.0)) * t,
        Pt::from((1.0, 1.0)) * t,
        Pt::from((0.0, 1.0)) * t,
        Pt::from((0.0, 0.0)) * t,
    ]
}

/// Make LCS marker
fn lcs_marker(pt: Pt<f64>, norm: f64, sz: f64) -> Vec<Pt<f64>> {
    let t = Transform::with_scale(sz, sz)
        .rotate(norm)
        .translate(pt.x, pt.y);
    vec![
        Pt::from((0.0, 0.0)) * t,
        Pt::from((1.4, 0.0)) * t,
        Pt::from((1.4, 0.4)) * t,
        Pt::from((2.6, 0.4)) * t,
        Pt::from((2.6, 0.0)) * t,
        Pt::from((4.0, 0.0)) * t,
        Pt::from((4.0, 3.0)) * t,
        Pt::from((2.6, 3.0)) * t,
        Pt::from((2.6, 2.6)) * t,
        Pt::from((1.4, 2.6)) * t,
        Pt::from((1.4, 3.0)) * t,
        Pt::from((0.0, 3.0)) * t,
        Pt::from((0.0, 0.0)) * t,
    ]
}

/// Make ramp meter marker
fn ramp_meter_marker(pt: Pt<f64>, norm: f64, sz: f64) -> Vec<Pt<f64>> {
    let t = Transform::with_scale(sz, sz)
        .rotate(norm)
        .translate(pt.x, pt.y);
    vec![
        Pt::from((0.0, 0.0)) * t,
        Pt::from((1.8, 0.0)) * t,
        Pt::from((2.4, -1.0)) * t,
        Pt::from((2.0, -2.0)) * t,
        Pt::from((1.0, -2.4)) * t,
        Pt::from((0.0, -1.8)) * t,
        Pt::from((0.0, 0.0)) * t,
        Pt::from((1.0, -1.0)) * t,
    ]
}

/// Make weather sensor marker
fn weather_sensor_marker(pt: Pt<f64>, sz: f64) -> Vec<Pt<f64>> {
    let t = Transform::with_scale(sz, sz).translate(pt.x, pt.y);
    vec![
        Pt::from((-3.0, -2.0)) * t,
        Pt::from((-3.0, 2.0)) * t,
        Pt::from((3.0, 1.0)) * t,
        Pt::from((2.0, 0.0)) * t,
        Pt::from((3.0, -1.0)) * t,
        Pt::from((-3.0, -2.0)) * t,
    ]
}
