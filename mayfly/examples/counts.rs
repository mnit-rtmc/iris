use mayfly::binned::{BinIter, CountData, VehicleFilter};
use mayfly::vlog::read_blocking;
use std::fs::File;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let file = File::open("res/test.vlog")?;
    let events = read_blocking("20000101", file)?;
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
