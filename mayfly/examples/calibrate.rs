use std::error::Error;

/// Intervals per hour (30-second)
const INTERVALS_PER_HOUR: u16 = 2 * 60;

/// Number of feet per mil
const FEET_PER_MILE: f32 = 5280.0;

/// Assumed avg. field length (ft)
const ASSUMED_FIELD_FT: f32 = 20.0;

/// Assumed avg. field length (mi)
const ASSUMED_FIELD_MI: f32 = ASSUMED_FIELD_FT / FEET_PER_MILE;

/// Percentile of intervals sorted by speed
const PERCENTILE: usize = 75;

/// Speed limit
const SPEED_LIMIT_MPH: f32 = 60.0;

/// Detector data interval (30-second)
struct Interval {
    /// Interval number (0-2879)
    number: usize,
    /// Flow rate (veh/hr)
    flow: u16,
    /// Percent of time occupied
    occupancy: f32,
    /// Density (veh/mi) with assumed avg field
    density: f32,
    /// Speed (mi/h) with assumed avg field
    speed: f32,
}

impl Interval {
    /// Create new interval data
    fn new(number: usize, count: u16, occ: f32) -> Self {
        let flow = count * INTERVALS_PER_HOUR;
        let occupancy = occ / 100.0;
        let density = occupancy / ASSUMED_FIELD_MI;
        let speed = f32::from(flow) / density;
        Interval {
            number,
            flow,
            occupancy,
            density,
            speed,
        }
    }

    /// Get interval time stamp (`HH:MM:SS`)
    fn time(&self) -> String {
        format!(
            "{:02}:{:02}:{:02}",
            self.number / 120,
            self.number % 60,
            self.number % 2 * 30
        )
    }

    /// Calculate adjusted density (using speed limit)
    fn density_adj(&self) -> f32 {
        f32::from(self.flow) / SPEED_LIMIT_MPH
    }

    /// Calculate adjusted average field length (ft/veh)
    fn field_len_adj(&self) -> f32 {
        self.occupancy * FEET_PER_MILE / self.density_adj()
    }

    /// Display interval data to stdout
    fn display(&self) {
        println!("        time: {}", self.time());
        println!("  flow (vph): {:3}", self.flow);
        println!("   occupancy: {:.04}%", self.occupancy);
        println!();
        println!("               Assumed    Adjusted");
        println!(
            " field len (ft): {:5.02} => {:5.02}",
            ASSUMED_FIELD_FT,
            self.field_len_adj(),
        );
        println!("    speed (mph): {:5.02} => {SPEED_LIMIT_MPH}", self.speed);
        println!(
            "  density (vpm): {:5.02} => {:.02}",
            self.density,
            self.density_adj()
        );
    }
}

/// Main entry point
fn main() -> Result<(), Box<dyn Error>> {
    let date = "20251015";
    let det = "1188";
    let mut intervals = fetch_intervals(date, det)?;
    intervals.sort_by(|a, b| a.speed.partial_cmp(&b.speed).unwrap());
    let len = intervals.len();
    println!("Date: {date}, detector: {det}");
    println!("Free-flowing intervals: {len} of 2880");
    println!();
    let typical = len * PERCENTILE / 100;
    let interval = &intervals[typical];
    println!("Interval {typical} of {len} ({PERCENTILE}%)");
    println!();
    interval.display();
    Ok(())
}

/// Fetch all free-flowing intervals for one date/detector
fn fetch_intervals(
    date: &str,
    det: &str,
) -> Result<Vec<Interval>, Box<dyn Error>> {
    let url = make_counts_url(date, det);
    let counts = fetch_json_u16(&url)?;
    let url = free_flowing_filter(url);
    let counts_filtered = fetch_json_u16(&url)?;
    let url = make_occ_url(date, det);
    let occupancy = fetch_json_f32(&url)?;
    let mut intervals = Vec::new();
    for (i, ((c0, c1), occ)) in counts
        .into_iter()
        .zip(counts_filtered)
        .zip(occupancy)
        .enumerate()
    {
        match (c0, c1, occ) {
            (Some(c0), Some(c1), Some(occ)) if c0 == c1 && c0 > 0 => {
                intervals.push(Interval::new(i, c0, occ));
            }
            _ => (),
        }
    }
    Ok(intervals)
}

/// Make URL to request vehicle counts
fn make_counts_url(date: &str, det: &str) -> String {
    let mut url = String::from("http://127.0.0.1");
    url.push_str("/mayfly/counts");
    url.push_str("?date=");
    url.push_str(date);
    url.push_str("&detector=");
    url.push_str(det);
    url
}

/// Add free-flowing headway filter to URL
fn free_flowing_filter(mut url: String) -> String {
    url.push_str("&headway_sec_min=4");
    url
}

/// Make URL to request occupancies
fn make_occ_url(date: &str, det: &str) -> String {
    let mut url = String::from("http://127.0.0.1");
    url.push_str("/mayfly/occupancy");
    url.push_str("?date=");
    url.push_str(date);
    url.push_str("&detector=");
    url.push_str(det);
    url
}

/// Fetch JSON data into a `Vec` of `u16`
fn fetch_json_u16(url: &str) -> Result<Vec<Option<u16>>, Box<dyn Error>> {
    let body: String = ureq::get(url).call()?.body_mut().read_to_string()?;
    let values = serde_json::from_str::<Vec<Option<u16>>>(&body)?;
    Ok(values)
}

/// Fetch JSON data into a `Vec` of `f32`
fn fetch_json_f32(url: &str) -> Result<Vec<Option<f32>>, Box<dyn Error>> {
    let body: String = ureq::get(url).call()?.body_mut().read_to_string()?;
    let values = serde_json::from_str::<Vec<Option<f32>>>(&body)?;
    Ok(values)
}
