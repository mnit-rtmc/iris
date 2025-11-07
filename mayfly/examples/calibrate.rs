use argh::FromArgs;
use std::error::Error;

/// Command-line arguments
#[derive(FromArgs)]
struct Args {
    /// display mode
    #[argh(option, short = 'd')]
    display: Option<char>,

    /// host name or IP address
    #[argh(option, short = 'h', default = "String::from(\"127.0.0.1\")")]
    host: String,

    /// free-flow speed; default 55 mph
    #[argh(option, short = 'f', default = "55")]
    free_speed: u16,

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

/// Vehicle legnth estimate
#[derive(Clone, Copy, Debug, PartialEq)]
enum VehLength {
    Small,
    Medium,
    Large,
    Huge,
}

impl From<u16> for VehLength {
    fn from(len_ft: u16) -> Self {
        match len_ft {
            0..12 => VehLength::Small,
            12..24 => VehLength::Medium,
            24..36 => VehLength::Large,
            _ => VehLength::Huge,
        }
    }
}

impl VehLength {
    fn code(self) -> char {
        match self {
            VehLength::Small => 'S',
            VehLength::Medium => 'M',
            VehLength::Large => 'L',
            VehLength::Huge => 'H',
        }
    }
}

/// Detector data interval (30-second)
#[derive(Clone, Debug, Default)]
struct Interval {
    /// Vehicle count
    count: u16,
    /// Percent of time occupied
    occupancy: f32,
    /// Speed (mi/h) measured
    speed: Option<u16>,
    /// Length (ft) measured
    length: Option<u16>,
    /// Vehicle length estimate
    length_est: Option<VehLength>,
}

impl Interval {
    /// Create new interval data
    fn new(
        count: u16,
        occ: f32,
        speed: Option<u16>,
        length: Option<u16>,
    ) -> Self {
        let occupancy = occ / 100.0;
        Interval {
            count,
            occupancy,
            speed,
            length,
            ..Default::default()
        }
    }

    /// Get flow rate (veh/hr)
    fn flow(&self) -> f32 {
        f32::from(self.count * INTERVALS_PER_HOUR)
    }

    /// Guess vehicle length, with assumed free-flow speed
    fn length_guess(&self, free_speed: u16) -> Option<VehLength> {
        // density = flow / speed
        let dens_free = self.flow() / f32::from(free_speed);
        if dens_free > 0.0 {
            // length = occupancy / density
            let len_mi_free = self.occupancy / dens_free;
            let len_ft_free = (len_mi_free * FEET_PER_MILE).round();
            if len_ft_free > 0.0 && len_ft_free < 65_535.0 {
                return Some(VehLength::from(len_ft_free as u16));
            }
        }
        None
    }

    /// Calculate speed (mph) using a given average field length
    fn speed_adj(&self, field_len: f32) -> f32 {
        let dens = self.occupancy / (field_len / FEET_PER_MILE);
        self.flow() / dens
    }

    /// Calculate adjusted density (using free-flow speed)
    fn density_adj(&self, free_speed: u16) -> f32 {
        self.flow() / f32::from(free_speed)
    }

    /// Display interval speeds
    fn display_speed(&self, field_len: f32) {
        let speed_adj = self.speed_adj(field_len);
        match (self.speed, speed_adj.is_normal()) {
            (Some(speed), true) => {
                let diff = speed_adj - f32::from(speed);
                print!(" {diff:3.0}");
            }
            _ => print!("    "),
        }
    }

    /// Display interval vehicle length estimate
    fn display_length(&self, _field_len: f32) {
        if let (Some(est), Some(len)) = (self.length_est, self.length) {
            if VehLength::from(len) != est {
                print!(" {}--", est.code());
                return;
            }
        }
        let sz = match self.length_est {
            Some(est) => est.code(),
            _ => ' ',
        };
        match self.length {
            Some(len) => print!(" {sz}{len:02}"),
            None => print!(" {sz}--"),
        }
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
        let free_speed = self.free_speed;
        let mut intervals = self.fetch_intervals(date)?;
        for interval in intervals.iter_mut() {
            interval.length_est = interval.length_guess(free_speed);
        }
        let field_len_adj = field_len_adj(&intervals, self.free_speed);
        if let Some('v') = self.display {
            println!("Date: {date}, detector: {}", &self.det);
        }
        if let Some('l') = self.display {
            display_intervals(
                &intervals,
                field_len_adj,
                Interval::display_length,
            );
        }
        if let Some('s') = self.display {
            display_intervals(
                &intervals,
                field_len_adj,
                Interval::display_speed,
            );
        }
        let mean = mean_speed(&intervals);
        let mean_adj = mean_speed_adj(&intervals, field_len_adj);
        println!("{date},{field_len_adj:.2},{mean:.2},{mean_adj:.2}");
        Ok(field_len_adj)
    }

    /// Fetch all free-flowing intervals for one date/detector
    fn fetch_intervals(
        &self,
        date: &str,
    ) -> Result<Vec<Interval>, Box<dyn Error>> {
        let url = self.make_url("counts", date);
        let counts = fetch_json_u16(&url)?;
        let url = self.make_url("occupancy", date);
        let occupancy = fetch_json_f32(&url)?;
        let url = self.make_url("speed", date);
        let speeds = match fetch_json_u16(&url) {
            Ok(speeds) => speeds,
            _ => vec![None; 2880],
        };
        let url = self.make_url("length", date);
        let lengths = match fetch_json_u16(&url) {
            Ok(lengths) => lengths,
            _ => vec![None; 2880],
        };
        let mut intervals = Vec::new();
        for (i, (count, occ)) in counts.into_iter().zip(occupancy).enumerate() {
            let speed = speeds[i];
            let length = lengths[i];
            let interval = match (count, occ) {
                (Some(count), Some(occ)) => {
                    Interval::new(count, occ, speed, length)
                }
                _ => Interval::new(0, 0.0, speed, length),
            };
            intervals.push(interval);
        }
        Ok(intervals)
    }

    /// Make URL to request detector data
    fn make_url(&self, data: &str, date: &str) -> String {
        let mut url = String::from("http://");
        url.push_str(&self.host);
        url.push_str("/mayfly/");
        url.push_str(data);
        url.push_str("?date=");
        url.push_str(date);
        url.push_str("&detector=");
        url.push_str(&self.det);
        url
    }
}

/// Calculate adjusted average field length (ft)
fn field_len_adj(intervals: &[Interval], free_speed: u16) -> f32 {
    let mut occupancy = 0.0;
    let mut density = 0.0;
    let mut number = 0;
    for interval in intervals {
        if let Some(VehLength::Medium) = interval.length_est {
            occupancy += interval.occupancy;
            density += interval.density_adj(free_speed);
            number += 1;
        }
    }
    println!("Free-flowing intervals: {number} of 2880");
    occupancy * FEET_PER_MILE / density
}

/// Display interval speeds
fn display_intervals(
    intervals: &[Interval],
    field_len_adj: f32,
    disp: fn(&Interval, f32),
) {
    for i in (0..2880).step_by(16) {
        print!("{}: ", time(i));
        for j in i..i + 16 {
            disp(&intervals[j], field_len_adj);
        }
        println!();
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
