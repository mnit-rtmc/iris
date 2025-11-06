use argh::FromArgs;
use std::error::Error;

/// Command-line arguments
#[derive(FromArgs)]
struct Args {
    /// verbose output
    #[argh(switch, short = 'v')]
    verbose: bool,

    /// host name or IP address
    #[argh(option, short = 'h', default = "String::from(\"127.0.0.1\")")]
    host: String,

    /// speed limit; default 55 mph
    #[argh(option, short = 'l', default = "55")]
    limit: u16,

    /// detector ID
    #[argh(positional)]
    det: String,

    /// dates to process
    #[argh(positional)]
    dates: Vec<String>,
}

/// Intervals per hour (30-second)
const INTERVALS_PER_HOUR: u16 = 2 * 60;

/// Number of feet per mil
const FEET_PER_MILE: f32 = 5280.0;

/// Conjectured avg. field length (ft)
const FIELD_FT_CNJ: f32 = 20.0;

/// Conjectured avg. field length (mi)
const FIELD_MI_CNJ: f32 = FIELD_FT_CNJ / FEET_PER_MILE;

/// Detector data interval (30-second)
#[derive(Clone)]
struct Interval {
    /// Interval number (0-2879)
    number: usize,
    /// Flow rate (veh/hr)
    flow: f32,
    /// Percent of time occupied
    occupancy: f32,
    /// Speed (mi/h) measured
    speed: Option<u16>,
    /// Density (veh/mi) with conjectured avg field
    density_cnj: f32,
    /// Speed (mi/h) with conjectured avg field
    speed_cnj: f32,
}

impl Interval {
    /// Create new interval data
    fn new(number: usize, count: u16, occ: f32, speed: Option<u16>) -> Self {
        let flow = f32::from(count * INTERVALS_PER_HOUR);
        let occupancy = occ / 100.0;
        let density_cnj = occupancy / FIELD_MI_CNJ;
        let speed_cnj = flow / density_cnj;
        Interval {
            number,
            flow,
            occupancy,
            speed,
            density_cnj,
            speed_cnj,
        }
    }

    /// Create new interval data
    fn new_avg(flow: f32, occupancy: f32) -> Self {
        let density_cnj = occupancy / FIELD_MI_CNJ;
        let speed_cnj = flow / density_cnj;
        Interval {
            number: 9999,
            flow,
            occupancy,
            speed: None,
            density_cnj,
            speed_cnj,
        }
    }

    /// Get interval time stamp (`HH:MM:SS`)
    fn time(&self) -> String {
        if self.number < 2880 {
            format!(
                "{:02}:{:02}:{:02}",
                self.number / 120,
                self.number % 60,
                self.number % 2 * 30
            )
        } else {
            "--:--:--".into()
        }
    }

    /// Calculate adjusted density (using speed limit)
    fn density_adj(&self, limit: u16) -> f32 {
        self.flow / f32::from(limit)
    }

    /// Calculate adjusted average field length (ft/veh)
    fn field_len_adj(&self, limit: u16) -> f32 {
        self.occupancy * FEET_PER_MILE / self.density_adj(limit)
    }

    /// Display interval data to stdout
    fn display(&self, limit: u16) {
        println!("        time: {}", self.time());
        println!("  flow (vph): {:.02}", self.flow);
        println!("   occupancy: {:.04}%", self.occupancy);
        if let Some(speed) = &self.speed {
            println!(" speed (mph): {speed}");
        }
        println!();
        println!("           Conjectured    Adjusted");
        println!(
            " field len (ft): {:5.02} => {:5.02}",
            FIELD_FT_CNJ,
            self.field_len_adj(limit),
        );
        println!("    speed (mph): {:5.02} => {limit}", self.speed_cnj);
        println!(
            "  density (vpm): {:5.02} => {:.02}",
            self.density_cnj,
            self.density_adj(limit)
        );
    }
}

impl Args {
    /// Run the calibration process
    fn run(&self) -> Result<(), Box<dyn Error>> {
        for date in &self.dates {
            self.calibrate_date(date)?;
        }
        Ok(())
    }

    /// Calibrate one date
    fn calibrate_date(&self, date: &str) -> Result<(), Box<dyn Error>> {
        let mut intervals = self.fetch_intervals(date)?;
        intervals
            .sort_by(|a, b| a.speed_cnj.partial_cmp(&b.speed_cnj).unwrap());
        let len = intervals.len();
        let typical = len / 2;
        let mut quar1 = typical;
        let mut quar3 = typical + 1;
        let mut interval = intervals[typical].clone();
        if len >= 4 {
            quar1 = len / 4;
            quar3 = len - quar1;
            let mut flow = 0.0;
            let mut occ = 0.0;
            // from 1st to 3rd quartile
            for i in quar1..quar3 {
                flow += intervals[i].flow;
                occ += intervals[i].occupancy;
            }
            let half = (quar3 - quar1) as f32;
            flow /= half;
            occ /= half;
            interval = Interval::new_avg(flow, occ);
        }
        if self.verbose {
            println!("Date: {date}, detector: {}", &self.det);
            println!("Free-flowing intervals: {len} of 2880");
            println!("Q1-Q3 intervals: {quar1}-{quar3} of {len}");
            println!();
            interval.display(self.limit);
        } else {
            println!("{date},{}", interval.field_len_adj(self.limit));
        }
        Ok(())
    }

    /// Fetch all free-flowing intervals for one date/detector
    fn fetch_intervals(
        &self,
        date: &str,
    ) -> Result<Vec<Interval>, Box<dyn Error>> {
        let url = self.make_counts_url(date);
        let counts = fetch_json_u16(&url)?;
        let url = free_flowing_filter(url);
        let counts_filtered = fetch_json_u16(&url)?;
        let url = self.make_occ_url(date);
        let occupancy = fetch_json_f32(&url)?;
        let url = self.make_speed_url(date);
        let speeds = match fetch_json_u16(&url) {
            Ok(speeds) => speeds,
            _ => vec![None; 2880],
        };
        let mut intervals = Vec::new();
        for (i, (((c0, c1), occ), speed)) in counts
            .into_iter()
            .zip(counts_filtered)
            .zip(occupancy)
            .zip(speeds)
            .enumerate()
        {
            match (c0, c1, occ) {
                (Some(c0), Some(c1), Some(occ)) if c0 == c1 && c0 > 0 => {
                    intervals.push(Interval::new(i, c0, occ, speed));
                }
                _ => (),
            }
        }
        Ok(intervals)
    }

    /// Make URL to request vehicle counts
    fn make_counts_url(&self, date: &str) -> String {
        let mut url = String::from("http://");
        url.push_str(&self.host);
        url.push_str("/mayfly/counts");
        url.push_str("?date=");
        url.push_str(date);
        url.push_str("&detector=");
        url.push_str(&self.det);
        url
    }

    /// Make URL to request occupancies
    fn make_occ_url(&self, date: &str) -> String {
        let mut url = String::from("http://");
        url.push_str(&self.host);
        url.push_str("/mayfly/occupancy");
        url.push_str("?date=");
        url.push_str(date);
        url.push_str("&detector=");
        url.push_str(&self.det);
        url
    }

    /// Make URL to request speeds
    fn make_speed_url(&self, date: &str) -> String {
        let mut url = String::from("http://");
        url.push_str(&self.host);
        url.push_str("/mayfly/speed");
        url.push_str("?date=");
        url.push_str(date);
        url.push_str("&detector=");
        url.push_str(&self.det);
        url
    }
}

/// Main entry point
fn main() -> Result<(), Box<dyn Error>> {
    let args: Args = argh::from_env();
    args.run()
}

/// Add free-flowing headway filter to URL
fn free_flowing_filter(mut url: String) -> String {
    url.push_str("&headway_sec_min=4");
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
