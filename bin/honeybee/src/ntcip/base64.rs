// base64.rs
//
// Copyright (C) 2019  Minnesota Department of Transportation
//
//! Helper module to allow serde base64 fields to be used with
//! `#[serde(with = "super::base64")]`

use base64::display::Base64Display;
use serde::{Serializer, de, Deserialize, Deserializer};

pub fn serialize<S>(bytes: &[u8], serializer: S) -> Result<S::Ok, S::Error>
    where S: Serializer
{
    serializer.collect_str(&Base64Display::with_config(bytes,
        base64::STANDARD))
}

pub fn deserialize<'de, D>(deserializer: D) -> Result<Vec<u8>, D::Error>
    where D: Deserializer<'de>
{
    let s = <&str>::deserialize(deserializer)?;
    base64::decode(s).map_err(de::Error::custom)
}
