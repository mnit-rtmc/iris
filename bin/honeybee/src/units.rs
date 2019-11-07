// units.rs
//
// Copyright (C) 2019  Minnesota Department of Transportation
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//! Simple units of measure module.
//!
//! There are dozens of Rust crates for units of measure.  I couldn't find one
//! with a simple API which made sense, so I made another one!
//!
//! ```rust
//! use honeybee::units::{Ft, Length, M};
//!
//! let m: Length<M> = Length::<Ft>::new(3.0).to();
//! println!("length: {}", m);
//! ```

use std::fmt;
use std::marker::PhantomData;
use std::ops::{Add, Mul, Sub};

/// Unit definition for Length
pub trait LengthUnit {
    /// Multiplication factor to convert to meters
    fn m_factor() -> f64;
    /// Multiplication factor to convert to another unit
    fn factor<T: LengthUnit>() -> f64 {
        // Use 14 digits precision for conversion constants.
        // The significand of f64 is 52 bits, which is about 15 digits.
        const PRECISION: f64 = 100_000_000_000_000.0;
        // This gets compiled down to a constant value
        ((Self::m_factor() / T::m_factor()) * PRECISION).round() / PRECISION
    }
    /// Unit abbreviation
    const ABBREVIATION: &'static str;
}

/// A length measurement is a value with a specific length unit.
///
/// ## Adding and Subtracting
///
/// Length measurements can be added and subtracted if the units are the same.
/// If not, units should be converted using [to](struct.Length.html#method.to).
///
/// ## Multiplication
///
/// Multiplying Length by f64 returns a Length.
/// Multiplying Length by Length returns Area.
///
#[derive(Debug, Copy, Clone, PartialEq)]
pub struct Length<U> where U: LengthUnit {
    /// Measurement value
    value: f64,
    /// Measurement unit
    unit: PhantomData<U>,
}

/// Area measurement (length squared)
#[derive(Debug, Copy, Clone, PartialEq)]
pub struct Area<U> where U: LengthUnit {
    /// Measurement value
    value: f64,
    /// Measurement unit
    unit: PhantomData<U>,
}

macro_rules! length_unit {
    ($(#[$meta:meta])* $unit:ident, $m_factor:expr, $abbreviation:expr) => {

        $(#[$meta])*
        #[derive(Debug, Copy, Clone, PartialEq)]
        pub struct $unit;

        impl LengthUnit for $unit {
            fn m_factor() -> f64 { $m_factor }
            const ABBREVIATION: &'static str = { $abbreviation };
        }
    };
}

length_unit!(
    /// Kilometer (Kilometre) [Length](struct.Length.html) unit
    Km, 1000.0, "km"
);
length_unit!(
    /// Meter (Metre) [Length](struct.Length.html) unit
    M, 1.0, "m"
);
length_unit!(
    /// Decimeter (Decimetre) [Length](struct.Length.html) unit
    Dm, 0.1, "dm"
);
length_unit!(
    /// Centimeter (Centimetre) [Length](struct.Length.html) unit
    Cm, 0.01, "cm"
);
length_unit!(
    /// Millimeter (Millimetre) [Length](struct.Length.html) unit
    Mm, 0.001, "mm"
);
length_unit!(
    /// Micrometer (Micrometre) [Length](struct.Length.html) unit
    Um, 0.000_001, "um"
);
length_unit!(
    /// Nanometer (Nanometre) [Length](struct.Length.html) unit
    Nm, 0.000_000_001, "nm"
);
length_unit!(
    /// Mile [Length](struct.Length.html) unit
    Mi, 1609.344, "mi"
);
length_unit!(
    /// Foot [Length](struct.Length.html) unit
    Ft, 0.3048, "ft"
);
length_unit!(
    /// Inch [Length](struct.Length.html) unit
    In, 0.0254, "in"
);
length_unit!(
    /// Yard [Length](struct.Length.html) unit
    Yd, 0.9144, "yd"
);

impl<U> Length<U> where U: LengthUnit {
    /// Create a new length measurement
    pub fn new(value: f64) -> Self {
        Length::<U> { value, unit: PhantomData }
    }

    /// Convert to specified units
    pub fn to<T: LengthUnit>(self) -> Length<T> {
        let value = self.value * U::factor::<T>();
        Length::<T> { value, unit: PhantomData }
    }
}

impl<U> fmt::Display for Length<U> where U: LengthUnit {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}", self.value, U::ABBREVIATION)
    }
}

impl<U> Add for Length<U> where U: LengthUnit {
    type Output = Self;

    fn add(self, other: Self) -> Self::Output {
        let value = self.value + other.value;
        Self { value, unit: PhantomData }
    }
}

impl<U> Sub for Length<U> where U: LengthUnit {
    type Output = Self;

    fn sub(self, other: Self) -> Self::Output {
        let value = self.value - other.value;
        Self { value, unit: PhantomData }
    }
}

impl<U> Mul<f64> for Length<U> where U: LengthUnit {
    type Output = Self;

    fn mul(self, other: f64) -> Self::Output {
        let value = self.value * other;
        Self::Output { value, unit: PhantomData }
    }
}

impl<U> Mul<Length<U>> for f64 where U: LengthUnit {
    type Output = Length<U>;

    fn mul(self, other: Length<U>) -> Self::Output {
        let value = self * other.value;
        Self::Output { value, unit: PhantomData }
    }
}

impl<U> Mul for Length<U> where U: LengthUnit {
    type Output = Area<U>;

    fn mul(self, other: Self) -> Self::Output {
        let value = self.value * other.value;
        Self::Output { value, unit: PhantomData }
    }
}

impl<U> Area<U> where U: LengthUnit {
    /// Create a new area measurement
    pub fn new(value: f64) -> Self {
        Area::<U> { value, unit: PhantomData }
    }

    /// Convert to specified units
    pub fn to<T: LengthUnit>(self) -> Area<T> {
        let value = self.value * U::factor::<T>();
        Area::<T> { value, unit: PhantomData }
    }
}

impl<U> fmt::Display for Area<U> where U: LengthUnit {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}²", self.value, U::ABBREVIATION)
    }
}

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn display() {
        assert_eq!(Length::<Km>::new(2.5).to_string(), "2.5 km");
        assert_eq!(Length::<M>::new(10.0).to_string(), "10 m");
        assert_eq!(Length::<Dm>::new(11.1).to_string(), "11.1 dm");
        assert_eq!(Length::<Cm>::new(25.0).to_string(), "25 cm");
        assert_eq!(Length::<Mm>::new(101.01).to_string(), "101.01 mm");
        assert_eq!(Length::<Um>::new(3.9).to_string(), "3.9 um");
        assert_eq!(Length::<Mi>::new(2.22).to_string(), "2.22 mi");
        assert_eq!(Length::<Ft>::new(0.5).to_string(), "0.5 ft");
        assert_eq!(Length::<In>::new(6.).to_string(), "6 in");
        assert_eq!(Length::<Yd>::new(100.0).to_string(), "100 yd");

        assert_eq!(Area::<M>::new(1.0).to_string(), "1 m²");
        assert_eq!(Area::<In>::new(18.5).to_string(), "18.5 in²");
    }

    #[test]
    fn convert() {
        assert_eq!(Length::<Ft>::new(1.0).to(), Length::<In>::new(12.0));
        assert_eq!(Length::<Yd>::new(1.0).to(), Length::<Ft>::new(3.0));
        assert_eq!(Length::<Yd>::new(1.0).to(), Length::<In>::new(36.0));
        assert_eq!(Length::<Mi>::new(1.0).to(), Length::<Ft>::new(5280.0));
        assert_eq!(Length::<M>::new(1.0).to(), Length::<Km>::new(0.001));
        assert_eq!(Length::<Cm>::new(110.0).to(), Length::<M>::new(1.1));
        assert_eq!(
            Length::<Cm>::new(1.0).to(),
            Length::<In>::new(0.393_700_787_401_57)
        );
    }

    #[test]
    fn add() {
        assert_eq!(
            Length::<M>::new(1.0) + Length::<M>::new(1.0),
            Length::<M>::new(2.0)
        );
        assert_eq!(
            Length::<M>::new(1.0) + Length::<M>::new(1.0),
            Length::<Cm>::new(200.0).to()
        );
        assert_eq!(
            Length::<In>::new(6.0) + Length::<In>::new(6.0),
            Length::<Ft>::new(1.0).to()
        );
    }

    #[test]
    fn sub() {
        assert_eq!(
            Length::<Km>::new(5.0) - Length::<Km>::new(1.0),
            Length::<Km>::new(4.0)
        );
        assert_eq!(
            Length::<Mm>::new(500.0) - Length::<Dm>::new(1.0).to(),
            Length::<Mm>::new(400.0)
        );
    }

    #[test]
    fn mul() {
        assert_eq!(
            Length::<M>::new(3.0) * Length::<M>::new(3.0),
            Area::<M>::new(9.0)
        );
        assert_eq!(Length::<M>::new(3.0) * 3.0, Length::<M>::new(9.0));
        assert_eq!(3.0 * Length::<M>::new(3.0), Length::<M>::new(9.0));
        assert_eq!(
            Length::<In>::new(10.0) * Length::<In>::new(5.0),
            Area::<In>::new(50.0)
        );
    }
}
