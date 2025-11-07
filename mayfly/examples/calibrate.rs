use argh::FromArgs;
use std::error::Error;

/// Command-line arguments
#[derive(FromArgs)]
struct Args {
    /// verbose output
    #[argh(switch, short = 'v')]
    verbose: bool,

    /// display interval data
    #[argh(switch, short = 'i')]
    interval: bool,

    /// host name or IP address
    #[argh(option, short = 'h', default = "String::from(\"127.0.0.1\")")]
    host: String,

    /// free-flow speed; default 55 mph
    #[argh(option, short = 'f', default = "55")]
    free_flow: u16,

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
    /// Free-flowing check
    free_flowing: bool,
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
    fn new(
        number: usize,
        free_flowing: bool,
        count: u16,
        occ: f32,
        speed: Option<u16>,
    ) -> Self {
        let flow = f32::from(count * INTERVALS_PER_HOUR);
        let occupancy = occ / 100.0;
        let density_cnj = occupancy / FIELD_MI_CNJ;
        let speed_cnj = flow / density_cnj;
        Interval {
            number,
            free_flowing,
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
            free_flowing: false,
            flow,
            occupancy,
            speed: None,
            density_cnj,
            speed_cnj,
        }
    }

    /// Calculate speed (mph) using a given average field length
    fn speed_adj(&self, field_len: f32) -> f32 {
        let dens = self.occupancy / (field_len / FEET_PER_MILE);
        self.flow / dens
    }

    /// Calculate adjusted density (using free-flow speed)
    fn density_adj(&self, free_flow: u16) -> f32 {
        self.flow / f32::from(free_flow)
    }

    /// Calculate adjusted average field length (ft/veh)
    fn field_len_adj(&self, free_flow: u16) -> f32 {
        self.occupancy * FEET_PER_MILE / self.density_adj(free_flow)
    }

    /// Display interval data to stdout
    fn display(&self, free_flow: u16) {
        println!("        time: {}", time(self.number));
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
            self.field_len_adj(free_flow),
        );
        println!("    speed (mph): {:5.02} => {free_flow}", self.speed_cnj);
        println!(
            "  density (vpm): {:5.02} => {:.02}",
            self.density_cnj,
            self.density_adj(free_flow)
        );
        println!();
    }
}

/// Get interval time stamp (`HH:MM:SS`)
fn time(number: usize) -> String {
    if number < 2880 {
        format!(
            "{:02}:{:02}:{:02}",
            number / 120,
            number % 120 / 2,
            number % 2 * 30
        )
    } else {
        "--:--:--".into()
    }
}

/// Check if an interval is in "daytime" (5 AM to 8 PM)
fn is_daytime(number: usize) -> bool {
    number >= 5 * 120 && number < 20 * 120
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
    fn calibrate_date(&self, date: &str) -> Result<f32, Box<dyn Error>> {
        let intervals_all = self.fetch_intervals(date)?;
        let mut intervals: Vec<_> =
            intervals_all.iter().filter(|i| i.free_flowing).collect();
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
        let field_len_adj = interval.field_len_adj(self.free_flow);
        if self.interval && self.verbose {
            display_intervals_compare(&intervals_all, field_len_adj);
        } else if self.interval {
            display_intervals(&intervals_all, field_len_adj);
        } else if self.verbose {
            println!("Date: {date}, detector: {}", &self.det);
            println!("Free-flowing intervals: {len} of 2880");
            println!("Q1-Q3 intervals: {quar1}-{quar3} of {len}");
            println!();
            interval.display(self.free_flow);
        }
        let mean = mean_speed(&intervals_all);
        let mean_adj = mean_speed_adj(&intervals_all, field_len_adj);
        println!("{date},{field_len_adj:.2},{mean:.2},{mean_adj:.2}");
        Ok(field_len_adj)
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
                (Some(c0), Some(c1), Some(occ)) => {
                    let free_flowing = is_daytime(i) && c0 == c1 && c0 > 0;
                    intervals.push(Interval::new(
                        i,
                        free_flowing,
                        c0,
                        occ,
                        speed,
                    ));
                }
                _ => {
                    intervals.push(Interval::new(i, false, 0, 0.0, speed));
                }
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

/// Display interval speeds
fn display_intervals(intervals: &[Interval], field_len_adj: f32) {
    if intervals.iter().any(|i| i.speed.is_some()) {
        let mut total = 0.0;
        for i in (0..2880).step_by(16) {
            print!("{}: ", time(i));
            let mut line = 0.0;
            for j in i..i + 16 {
                let ival = &intervals[j];
                let speed_adj = ival.speed_adj(field_len_adj);
                match (ival.speed, speed_adj.is_normal()) {
                    (Some(speed), true) => {
                        let diff = speed_adj - f32::from(speed);
                        line += diff;
                        total += diff;
                        print!(" {diff:3.0}");
                    }
                    _ => print!("    "),
                }
            }
            println!(" :{line:4.0}");
        }
        println!("{:74}{total:6.0}", ' ');
    } else {
        for i in (0..2880).step_by(16) {
            print!("{}: ", time(i));
            for j in i..i + 16 {
                let ival = &intervals[j];
                let speed = ival.speed_adj(field_len_adj);
                if speed.is_normal() {
                    print!(" {speed:3.0}");
                } else {
                    print!("    ");
                }
            }
            println!("");
        }
    }
}

/// Display interval speeds side by side
fn display_intervals_compare(intervals: &[Interval], field_len_adj: f32) {
    for i in (0..2880).step_by(8) {
        print!("{}: ", time(i));
        for j in i..i + 8 {
            let ival = &intervals[j];
            match ival.speed {
                Some(speed) => print!(" {speed:3}"),
                None => print!("    "),
            }
        }
        print!(" : ");
        for j in i..i + 8 {
            let ival = &intervals[j];
            let speed = ival.speed_adj(field_len_adj);
            if speed.is_normal() {
                print!(" {speed:3.0}");
            } else {
                print!("    ");
            }
        }
        println!("");
    }
}

/// Calculate the mean recorded speed
fn mean_speed(intervals: &[Interval]) -> f32 {
    let mut sum = 0.0;
    let mut values = 0;
    for i in intervals {
        if let Some(speed) = i.speed {
            sum += f32::from(speed);
            values += 1;
        }
    }
    if values > 0 { sum / values as f32 } else { 0.0 }
}

/// Calculate the mean adjusted speed
fn mean_speed_adj(intervals: &[Interval], field_len_adj: f32) -> f32 {
    let mut sum = 0.0;
    let mut values = 0;
    for i in intervals {
        let speed = i.speed_adj(field_len_adj);
        if speed.is_normal() {
            sum += f32::from(speed);
            values += 1;
        }
    }
    if values > 0 { sum / values as f32 } else { 0.0 }
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
