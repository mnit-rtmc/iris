use mayfly::binned::{BinIter, CountData, VehicleFilter};
use mayfly::vlog::VehLogReader;
use std::fs::File;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let file = File::open("res/test.vlog")?;
    let vlog = VehLogReader::from_reader_blocking(file)?;
    let events = vlog.events();
    print!("[");
    for (i, v) in
        BinIter::<CountData>::new(30, &events, VehicleFilter::default())
            .enumerate()
    {
        if i > 0 {
            print!(",");
        }
        print!("{}", v);
    }
    println!("]");

    Ok(())
}
