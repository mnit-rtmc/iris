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
//! Rust units of measure.
//!
//! There are many similar Rust crates.  I couldn't find one which fit my
//! needs, so I made another one!  Some features:
//!
//! * Units are not discarded when creating quantities.  In keeping with Rust
//!   philosohpy, conversions must be done manually (using the `to` method).
//! * No external dependencies -- fast compile time
//! * The API is easy to understand and use
//!
//! Limitations:
//!
//! * Quantities are not generic -- f64 only
//! * Small set of quantities and units implemented
//!
//! ```rust
//! use honeybee::units::{Length, length::{Ft, M}};
//!
//! let m: Length<M> = Length::<Ft>::new(3.5).to();
//! assert_eq!(m.to_string(), "1.0668 m");
//! ```
use std::fmt;
use std::marker::PhantomData;
use std::ops::{Add, Div, Mul, Sub};

/// Length is a base quantity with a specific [length unit].
///
/// ## Operations
///
/// * Length `+` Length `=>` Length
/// * Length `-` Length `=>` Length
/// * Length `*` f64 `=>` Length
/// * Length `*` Length `=>` [Area]
/// * Length `/` f64 `=>` Length
///
/// Units must be the same for operations with two Length operands.  The [to]
/// method can be used for conversion.
///
/// ```rust
/// use honeybee::units::{Length, length::In};
///
/// let a = Length::<In>::new(5.5);
/// let b = Length::<In>::new(4.5);
/// println!("{} + {} = {}", a, b, a + b);
/// println!("{} - {} = {}", a, b, a - b);
/// ```
///
/// [Area]: struct.Area.html
/// [length unit]: length/index.html
/// [to]: struct.Length.html#method.to
///
#[derive(Debug, Copy, Clone, PartialEq)]
pub struct Length<U> where U: length::Unit {
    /// Length quantity
    pub quantity: f64,
    /// Measurement unit
    unit: PhantomData<U>,
}

/// Area is a derived quantity with a specific [length unit] squared.
///
/// ## Operations
///
/// * Area `+` Area `=>` Area
/// * Area `-` Area `=>` Area
/// * Area `*` f64 `=>` Area
/// * Area `*` [Length] `=>` [Volume]
/// * Area `/` f64 `=>` Area
/// * Area `/` [Length] `=>` [Length]
///
/// [length unit]: length/index.html
/// [Length]: struct.Length.html
/// [Volume]: struct.Volume.html
///
#[derive(Debug, Copy, Clone, PartialEq)]
pub struct Area<U> where U: length::Unit {
    /// Area quantity
    pub quantity: f64,
    /// Measurement unit
    unit: PhantomData<U>,
}

/// Volume is a derived quantity with a specific [length unit] cubed.
///
/// ## Operations
///
/// * Volume `+` Volum `=>` Volume
/// * Volume `-` Volume `=>` Volume
/// * Volume `*` f64 `=>` Volume
/// * Volume `/` f64 `=>` Volume
/// * Volume `/` [Length] `=>` [Area]
/// * Volume `/` [Area] `=>` [Length]
///
/// [Area]: struct.Area.html
/// [length unit]: length/index.html
/// [Length]: struct.Length.html
///
#[derive(Debug, Copy, Clone, PartialEq)]
pub struct Volume<U> where U: length::Unit {
    /// Volume quantity
    pub quantity: f64,
    /// Measurement unit
    unit: PhantomData<U>,
}

/// Time is a base quantity with a specific [time unit].
///
/// ## Operations
///
/// * Time `+` Time `=>` Time
/// * Time `-` Time `=>` Time
/// * Time `*` f64 `=>` Time
///
/// Units must be the same for operations with two Time operands.  The [to]
/// method can be used for conversion.
///
/// ```rust
/// use honeybee::units::{Time, time::Min};
///
/// let a = Time::<Min>::new(15.0);
/// let b = Time::<Min>::new(5.5);
/// println!("{} + {} = {}", a, b, a + b);
/// println!("{} - {} = {}", a, b, a - b);
/// ```
///
/// [time unit]: time/index.html
/// [to]: struct.Time.html#method.to
///
#[derive(Debug, Copy, Clone, PartialEq)]
pub struct Time<U> where U: time::Unit {
    /// Time quantity
    pub quantity: f64,
    /// Measurement unit
    unit: PhantomData<U>,
}

impl<U> Length<U> where U: length::Unit {
    /// Create a new length measurement
    pub fn new(quantity: f64) -> Self {
        Length::<U> { quantity, unit: PhantomData }
    }

    /// Convert to specified units
    pub fn to<T: length::Unit>(self) -> Length<T> {
        let quantity = self.quantity * U::factor::<T>();
        Length::<T> { quantity, unit: PhantomData }
    }
}

impl<U> fmt::Display for Length<U> where U: length::Unit {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}", self.quantity, U::ABBREVIATION)
    }
}

impl<U> Add for Length<U> where U: length::Unit {
    type Output = Self;

    fn add(self, other: Self) -> Self::Output {
        let quantity = self.quantity + other.quantity;
        Self { quantity, unit: PhantomData }
    }
}

impl<U> Sub for Length<U> where U: length::Unit {
    type Output = Self;

    fn sub(self, other: Self) -> Self::Output {
        let quantity = self.quantity - other.quantity;
        Self { quantity, unit: PhantomData }
    }
}

impl<U> Mul<f64> for Length<U> where U: length::Unit {
    type Output = Self;

    fn mul(self, other: f64) -> Self::Output {
        let quantity = self.quantity * other;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Mul<Length<U>> for f64 where U: length::Unit {
    type Output = Length<U>;

    fn mul(self, other: Length<U>) -> Self::Output {
        let quantity = self * other.quantity;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Mul for Length<U> where U: length::Unit {
    type Output = Area<U>;

    fn mul(self, other: Self) -> Self::Output {
        let quantity = self.quantity * other.quantity;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Div<f64> for Length<U> where U: length::Unit {
    type Output = Self;

    fn div(self, other: f64) -> Self::Output {
        let quantity = self.quantity / other;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Area<U> where U: length::Unit {
    /// Create a new area measurement
    pub fn new(quantity: f64) -> Self {
        Area::<U> { quantity, unit: PhantomData }
    }

    /// Convert to specified units
    pub fn to<T: length::Unit>(self) -> Area<T> {
        let factor = U::factor::<T>() * U::factor::<T>();
        let quantity = self.quantity * factor;
        Area::<T> { quantity, unit: PhantomData }
    }
}

impl<U> fmt::Display for Area<U> where U: length::Unit {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}²", self.quantity, U::ABBREVIATION)
    }
}

impl<U> Add for Area<U> where U: length::Unit {
    type Output = Self;

    fn add(self, other: Self) -> Self::Output {
        let quantity = self.quantity + other.quantity;
        Self { quantity, unit: PhantomData }
    }
}

impl<U> Sub for Area<U> where U: length::Unit {
    type Output = Self;

    fn sub(self, other: Self) -> Self::Output {
        let quantity = self.quantity - other.quantity;
        Self { quantity, unit: PhantomData }
    }
}

impl<U> Mul<f64> for Area<U> where U: length::Unit {
    type Output = Self;

    fn mul(self, other: f64) -> Self::Output {
        let quantity = self.quantity * other;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Mul<Area<U>> for f64 where U: length::Unit {
    type Output = Area<U>;

    fn mul(self, other: Area<U>) -> Self::Output {
        let quantity = self * other.quantity;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Mul<Length<U>> for Area<U> where U: length::Unit {
    type Output = Volume<U>;

    fn mul(self, other: Length<U>) -> Self::Output {
        let quantity = self.quantity * other.quantity;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Div<f64> for Area<U> where U: length::Unit {
    type Output = Self;

    fn div(self, other: f64) -> Self::Output {
        let quantity = self.quantity / other;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Div<Length<U>> for Area<U> where U: length::Unit {
    type Output = Length<U>;

    fn div(self, other: Length<U>) -> Self::Output {
        let quantity = self.quantity / other.quantity;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Volume<U> where U: length::Unit {
    /// Create a new volume measurement
    pub fn new(quantity: f64) -> Self {
        Volume::<U> { quantity, unit: PhantomData }
    }

    /// Convert to specified units
    pub fn to<T: length::Unit>(self) -> Volume<T> {
        let factor = U::factor::<T>() * U::factor::<T>() * U::factor::<T>();
        let quantity = self.quantity * factor;
        Volume::<T> { quantity, unit: PhantomData }
    }
}

impl<U> fmt::Display for Volume<U> where U: length::Unit {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}³", self.quantity, U::ABBREVIATION)
    }
}

impl<U> Add for Volume<U> where U: length::Unit {
    type Output = Self;

    fn add(self, other: Self) -> Self::Output {
        let quantity = self.quantity + other.quantity;
        Self { quantity, unit: PhantomData }
    }
}

impl<U> Sub for Volume<U> where U: length::Unit {
    type Output = Self;

    fn sub(self, other: Self) -> Self::Output {
        let quantity = self.quantity - other.quantity;
        Self { quantity, unit: PhantomData }
    }
}

impl<U> Mul<f64> for Volume<U> where U: length::Unit {
    type Output = Self;

    fn mul(self, other: f64) -> Self::Output {
        let quantity = self.quantity * other;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Mul<Volume<U>> for f64 where U: length::Unit {
    type Output = Volume<U>;

    fn mul(self, other: Volume<U>) -> Self::Output {
        let quantity = self * other.quantity;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Div<f64> for Volume<U> where U: length::Unit {
    type Output = Self;

    fn div(self, other: f64) -> Self::Output {
        let quantity = self.quantity / other;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Div<Length<U>> for Volume<U> where U: length::Unit {
    type Output = Area<U>;

    fn div(self, other: Length<U>) -> Self::Output {
        let quantity = self.quantity / other.quantity;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Div<Area<U>> for Volume<U> where U: length::Unit {
    type Output = Length<U>;

    fn div(self, other: Area<U>) -> Self::Output {
        let quantity = self.quantity / other.quantity;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Time<U> where U: time::Unit {
    /// Create a new time measurement
    pub fn new(quantity: f64) -> Self {
        Time::<U> { quantity, unit: PhantomData }
    }

    /// Convert to specified units
    pub fn to<T: time::Unit>(self) -> Time<T> {
        let quantity = self.quantity * U::factor::<T>();
        Time::<T> { quantity, unit: PhantomData }
    }
}

impl<U> fmt::Display for Time<U> where U: time::Unit {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}", self.quantity, U::ABBREVIATION)
    }
}

impl<U> Add for Time<U> where U: time::Unit {
    type Output = Self;

    fn add(self, other: Self) -> Self::Output {
        let quantity = self.quantity + other.quantity;
        Self { quantity, unit: PhantomData }
    }
}

impl<U> Sub for Time<U> where U: time::Unit {
    type Output = Self;

    fn sub(self, other: Self) -> Self::Output {
        let quantity = self.quantity - other.quantity;
        Self { quantity, unit: PhantomData }
    }
}

impl<U> Mul<f64> for Time<U> where U: time::Unit {
    type Output = Self;

    fn mul(self, other: f64) -> Self::Output {
        let quantity = self.quantity * other;
        Self::Output { quantity, unit: PhantomData }
    }
}

impl<U> Mul<Time<U>> for f64 where U: time::Unit {
    type Output = Time<U>;

    fn mul(self, other: Time<U>) -> Self::Output {
        let quantity = self * other.quantity;
        Self::Output { quantity, unit: PhantomData }
    }
}

/// Base units of length.
///
/// Each unit is defined relative to meters with a conversion factor.
pub mod length {

    /// Unit definition for Length
    pub trait Unit {
        /// Multiplication factor to convert to meters
        fn m_factor() -> f64;
        /// Multiplication factor to convert to another unit
        fn factor<T: Unit>() -> f64 {
            // Use 14 digits precision for conversion constants.
            // The significand of f64 is 52 bits, which is about 15 digits.
            const PRECISION: f64 = 100_000_000_000_000.0;
            // This gets compiled down to a constant value
            ((Self::m_factor() / T::m_factor()) * PRECISION).round() / PRECISION
        }
        /// Unit abbreviation
        const ABBREVIATION: &'static str;
    }

    macro_rules! length_unit {
        ($(#[$meta:meta])* $unit:ident, $m_factor:expr, $abbreviation:expr) => {

            $(#[$meta])*
            #[derive(Debug, Copy, Clone, PartialEq)]
            pub struct $unit;

            impl Unit for $unit {
                fn m_factor() -> f64 { $m_factor }
                const ABBREVIATION: &'static str = { $abbreviation };
            }
        };
    }

    length_unit!(/** Kilometer / Kilometre */ Km, 1_000.0, "km");
    length_unit!(/** Meter / Metre */ M, 1.0, "m");
    length_unit!(/** Decimeter / Decimetre */ Dm, 0.1, "dm");
    length_unit!(/** Centimeter / Centimetre */ Cm, 0.01, "cm");
    length_unit!(/** Millimeter / Millimetre */ Mm, 0.001, "mm");
    length_unit!(/** Micrometer / Micrometre */ Um, 0.000_001, "μm");
    length_unit!(/** Nanometer / Nanometre */ Nm, 0.000_000_001, "nm");
    length_unit!(/** Mile */ Mi, 1609.344, "mi");
    length_unit!(/** Foot */ Ft, 0.3048, "ft");
    length_unit!(/** Inch */ In, 0.0254, "in");
    length_unit!(/** Yard */ Yd, 0.9144, "yd");
}

/// Base units of time.
///
/// Each unit is defined relative to seconds with a conversion factor.
pub mod time {

    /// Unit definition for Time
    pub trait Unit {
        /// Multiplication factor to convert to seconds
        fn s_factor() -> f64;
        /// Multiplication factor to convert to another unit
        fn factor<T: Unit>() -> f64 {
            Self::s_factor() / T::s_factor()
        }
        /// Unit abbreviation
        const ABBREVIATION: &'static str;
    }

    macro_rules! time_unit {
        ($(#[$meta:meta])* $unit:ident, $s_factor:expr, $abbreviation:expr) => {

            $(#[$meta])*
            #[derive(Debug, Copy, Clone, PartialEq)]
            pub struct $unit;

            impl Unit for $unit {
                fn s_factor() -> f64 { $s_factor }
                const ABBREVIATION: &'static str = { $abbreviation };
            }
        };
    }

    time_unit!(/** 14 Days */ Fortnight, 14.0 * 24.0 * 60.0 * 60.0, "fortnight");
    time_unit!(/** Week */ Wk, 7.0 * 24.0 * 60.0 * 60.0, "wk");
    time_unit!(/** Day */ Day, 24.0 * 60.0 * 60.0, "day");
    time_unit!(/** Hour */ Hr, 60.0 * 60.0, "hr");
    time_unit!(/** Minute */ Min, 60.0, "min");
    time_unit!(/** Second */ S, 1.0, "s");
    time_unit!(/** Decisecond */ Ds, 0.1, "ds");
    time_unit!(/** Millisecond */ Ms, 0.001, "ms");
    time_unit!(/** Microsecond */ Us, 0.000_001, "μs");
    time_unit!(/** Nanosecond */ Ns, 0.000_000_001, "ns");
}

#[cfg(test)]
mod test {
    use super::*;
    use super::length::*;
    use super::time::*;

    #[test]
    fn len_display() {
        assert_eq!(Length::<Km>::new(2.5).to_string(), "2.5 km");
        assert_eq!(Length::<M>::new(10.0).to_string(), "10 m");
        assert_eq!(Length::<Dm>::new(11.1).to_string(), "11.1 dm");
        assert_eq!(Length::<Cm>::new(25.0).to_string(), "25 cm");
        assert_eq!(Length::<Mm>::new(101.01).to_string(), "101.01 mm");
        assert_eq!(Length::<Um>::new(3.9).to_string(), "3.9 μm");
        assert_eq!(Length::<Mi>::new(2.22).to_string(), "2.22 mi");
        assert_eq!(Length::<Ft>::new(0.5).to_string(), "0.5 ft");
        assert_eq!(Length::<In>::new(6.).to_string(), "6 in");
        assert_eq!(Length::<Yd>::new(100.0).to_string(), "100 yd");
    }

    #[test]
    fn area_display() {
        assert_eq!(Area::<M>::new(1.0).to_string(), "1 m²");
        assert_eq!(Area::<In>::new(18.5).to_string(), "18.5 in²");
    }

    #[test]
    fn volume_display() {
        assert_eq!(Volume::<Um>::new(123.0).to_string(), "123 μm³");
        assert_eq!(Volume::<In>::new(54.3).to_string(), "54.3 in³");
    }

    #[test]
    fn time_display() {
        assert_eq!(Time::<S>::new(23.7).to_string(), "23.7 s");
        assert_eq!(Time::<Hr>::new(3.25).to_string(), "3.25 hr");
    }

    #[test]
    fn len_to() {
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
    fn area_to() {
        assert_eq!(Area::<Ft>::new(1.0).to(), Area::<In>::new(144.0));
        assert_eq!(Area::<M>::new(1.0).to(), Area::<Cm>::new(10_000.0));
    }

    #[test]
    fn volume_to() {
        assert_eq!(Volume::<Yd>::new(2.0).to(), Volume::<Ft>::new(54.0));
        assert_eq!(Volume::<Cm>::new(4.8).to(), Volume::<Mm>::new(4_800.0));
    }

    #[test]
    fn time_to() {
        assert_eq!(Time::<Hr>::new(4.75).to(), Time::<Min>::new(285.0));
        assert_eq!(Time::<S>::new(2.5).to(), Time::<Ms>::new(2_500.0));
    }

    #[test]
    fn len_add() {
        assert_eq!(
            Length::<M>::new(1.0) + Length::<M>::new(1.0),
            Length::<M>::new(2.0)
        );
        assert_eq!(
            Length::<Ft>::new(10.0) + Length::<Ft>::new(2.0),
            Length::<Ft>::new(12.0)
        );
        assert_eq!(
            Length::<In>::new(6.0) + Length::<In>::new(6.0),
            Length::<In>::new(12.0)
        );
    }

    #[test]
    fn area_add() {
        assert_eq!(
            Area::<Yd>::new(12.0) + Area::<Yd>::new(15.0),
            Area::<Yd>::new(27.0)
        );
        assert_eq!(
            Area::<Km>::new(25.6) + Area::<Km>::new(15.4),
            Area::<Km>::new(41.0)
        );
    }

    #[test]
    fn volume_add() {
        assert_eq!(
            Volume::<Mm>::new(25.0) + Volume::<Mm>::new(5.1),
            Volume::<Mm>::new(30.1)
        );
        assert_eq!(
            Volume::<In>::new(1.2) + Volume::<In>::new(3.8),
            Volume::<In>::new(5.0)
        );
    }

    #[test]
    fn time_add() {
        assert_eq!(
            Time::<Day>::new(3.5) + Time::<Day>::new(1.25),
            Time::<Day>::new(4.75)
        );
        assert_eq!(
            Time::<Wk>::new(1.0) + Time::<Wk>::new(2.1),
            Time::<Wk>::new(3.1)
        );
    }

    #[test]
    fn len_sub() {
        assert_eq!(
            Length::<Km>::new(5.0) - Length::<Km>::new(1.0),
            Length::<Km>::new(4.0)
        );
        assert_eq!(
            Length::<Mm>::new(500.0) - Length::<Mm>::new(100.0),
            Length::<Mm>::new(400.0)
        );
    }

    #[test]
    fn area_sub() {
        assert_eq!(
            Area::<Mi>::new(5.0) - Area::<Mi>::new(2.5),
            Area::<Mi>::new(2.5)
        );
    }

    #[test]
    fn volume_sub() {
        assert_eq!(
            Volume::<M>::new(10.0) - Volume::<M>::new(4.5),
            Volume::<M>::new(5.5)
        );
    }

    #[test]
    fn time_sub() {
        assert_eq!(
            Time::<Us>::new(567.8) - Time::<Us>::new(123.4),
            Time::<Us>::new(444.4)
        );
    }

    #[test]
    fn len_mul() {
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

    #[test]
    fn area_mul() {
        assert_eq!(Area::<Dm>::new(3.0) * 2.5, Area::<Dm>::new(7.5));
        assert_eq!(4.0 * Area::<Dm>::new(3.0), Area::<Dm>::new(12.0));
        assert_eq!(
            Area::<Mm>::new(123.0) * Length::<Mm>::new(2.0),
            Volume::<Mm>::new(246.0)
        );
    }

    #[test]
    fn volume_mul() {
        assert_eq!(Volume::<Um>::new(8.0) * 1.5, Volume::<Um>::new(12.0));
        assert_eq!(4.0 * Volume::<Km>::new(2.5), Volume::<Km>::new(10.0));
    }

    #[test]
    fn time_mul() {
        assert_eq!(Time::<Ns>::new(6.5) * 12.0, Time::<Ns>::new(78.0));
        assert_eq!(4.0 * Time::<Hr>::new(1.5), Time::<Hr>::new(6.0));
    }

    #[test]
    fn len_div() {
        assert_eq!(Length::<Ft>::new(5.0) / 5.0, Length::<Ft>::new(1.0));
    }

    #[test]
    fn area_div() {
        assert_eq!(Area::<Cm>::new(500.0) / 5.0, Area::<Cm>::new(100.0));
        assert_eq!(
            Area::<Nm>::new(40.0) / Length::<Nm>::new(10.0),
            Length::<Nm>::new(4.0)
        );
    }

    #[test]
    fn volume_div() {
        assert_eq!(Volume::<Mm>::new(50.0) / 10.0, Volume::<Mm>::new(5.0));
        assert_eq!(
            Volume::<Yd>::new(40.0) / Length::<Yd>::new(2.0),
            Area::<Yd>::new(20.0)
        );
        assert_eq!(
            Volume::<In>::new(25.0) / Area::<In>::new(5.0),
            Length::<In>::new(5.0)
        );
    }
}
