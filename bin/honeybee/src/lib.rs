extern crate serde;
extern crate serde_json;
#[macro_use] extern crate serde_derive;
extern crate failure;
extern crate fallible_iterator;
extern crate postgres;
extern crate ssh2;

pub mod fetcher;
mod resource;
mod mirror;
pub mod multi;
pub mod raster;
