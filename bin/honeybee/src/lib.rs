extern crate gif;
#[macro_use] extern crate log;
extern crate env_logger;
extern crate serde;
extern crate serde_json;
#[macro_use] extern crate serde_derive;
#[macro_use] extern crate failure;
extern crate fallible_iterator;
extern crate postgres;
extern crate ssh2;

pub mod fetcher;
mod resource;
mod mere;
mod render;
pub mod multi;
pub mod raster;
