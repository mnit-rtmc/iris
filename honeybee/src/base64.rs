// base64.rs
//
//! Helper module to allow serde base64 fields to be used with
//! `#[serde(with = "super::base64")]`
use serde::{de, Deserialize, Deserializer};

pub fn deserialize<'de, D>(deserializer: D) -> Result<Vec<u8>, D::Error>
where
    D: Deserializer<'de>,
{
    let s = <String>::deserialize(deserializer)?;
    base64::decode(s).map_err(de::Error::custom)
}
