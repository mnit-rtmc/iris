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
//! ## Example
//!
//! ```rust
//! use honeybee::units::{Length, length::{Ft, M}};
//!
//! let m = 3.5 * Ft;
//! assert_eq!(m.to::<M>().to_string(), "1.0668 m");
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
/// * f64 `*` Length `=>` Length
/// * f64 `*` [length unit] `=>` Length
/// * Length `*` Length `=>` [Area]
/// * Length `/` f64 `=>` Length
///
/// Units must be the same for operations with two Length operands.  The [to]
/// method can be used for conversion.
///
/// ## Example
///
/// ```rust
/// use honeybee::units::{Length, length::In};
///
/// let a = 5.5 * In;
/// let b = 4.5 * In;
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
/// ## Example
///
/// ```rust
/// use honeybee::units::{Area, Length, length::M};
///
/// let a = (10.0 * M) * (15.0 * M);
/// assert_eq!(a, Area::new(150.0));
/// assert_eq!(a.to_string(), "150 m²");
/// assert_eq!(a / (5.0 * M), 30.0 * M);
/// ```
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
/// * f64 `*` Time `=>` Time
/// * f64 `*` [time unit] `=>` Time
/// * f64 `/` Time `=>` [Frequency]
///
/// Units must be the same for operations with two Time operands.  The [to]
/// method can be used for conversion.
///
/// ```rust
/// use honeybee::units::{Time, time::Min};
///
/// let a = 15.0 * Min;
/// let b = 5.5 * Min;
/// println!("{} + {} = {}", a, b, a + b);
/// println!("{} - {} = {}", a, b, a - b);
/// ```
///
/// [Frequency]: struct.Frequency.html
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

/// Frequency is a derived quantity with a specific [time unit].
///
/// ## Operations
///
/// * Frequency `+` Frequency `=>` Frequency
/// * Frequency `-` Frequency `=>` Frequency
/// * Frequency `*` f64 `=>` Frequency
/// * f64 `*` Frequency `=>` Frequency
/// * f64 `/` [Time] `=>` Frequency
/// * f64 `/` [time unit] `=>` Frequency
/// * f64 `/` Frequency `=>` [Time]
///
/// Units must be the same for operations with two Frequency operands.  The [to]
/// method can be used for conversion.
///
/// [Time]: struct.Time.html
/// [time unit]: time/index.html
/// [to]: struct.Frequency.html#method.to
///
#[derive(Debug, Copy, Clone, PartialEq)]
pub struct Frequency<U> where U: time::Unit {
    /// Time quantity
    pub quantity: f64,
    /// Measurement unit
    unit: PhantomData<U>,
}

// Implement basic ops for a quantity struct
macro_rules! impl_base_ops {
    ($quan:ident, $unit:path) => {
        impl<U> Add for $quan<U> where U: $unit {
            type Output = Self;
            fn add(self, other: Self) -> Self::Output {
                let quantity = self.quantity + other.quantity;
                Self { quantity, unit: PhantomData }
            }
        }
        impl<U> Sub for $quan<U> where U: $unit {
            type Output = Self;
            fn sub(self, other: Self) -> Self::Output {
                let quantity = self.quantity - other.quantity;
                Self { quantity, unit: PhantomData }
            }
        }
        impl<U> Mul<f64> for $quan<U> where U: $unit {
            type Output = Self;
            fn mul(self, other: f64) -> Self::Output {
                let quantity = self.quantity * other;
                Self::Output { quantity, unit: PhantomData }
            }
        }
        impl<U> Mul<$quan<U>> for f64 where U: $unit {
            type Output = $quan<U>;
            fn mul(self, other: $quan<U>) -> Self::Output {
                let quantity = self * other.quantity;
                Self::Output { quantity, unit: PhantomData }
            }
        }
        impl<U> Div<f64> for $quan<U> where U: $unit {
            type Output = Self;
            fn div(self, other: f64) -> Self::Output {
                let quantity = self.quantity / other;
                Self::Output { quantity, unit: PhantomData }
            }
        }
    }
}

impl_base_ops!(Length, length::Unit);
impl_base_ops!(Area, length::Unit);
impl_base_ops!(Volume, length::Unit);
impl_base_ops!(Time, time::Unit);
impl_base_ops!(Frequency, time::Unit);

impl<U> fmt::Display for Length<U> where U: length::Unit {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}", self.quantity, U::ABBREVIATION)
    }
}

impl<U> fmt::Display for Area<U> where U: length::Unit {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}²", self.quantity, U::ABBREVIATION)
    }
}

impl<U> fmt::Display for Volume<U> where U: length::Unit {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}³", self.quantity, U::ABBREVIATION)
    }
}

impl<U> fmt::Display for Time<U> where U: time::Unit {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{} {}", self.quantity, U::ABBREVIATION)
    }
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

impl<U> Mul for Length<U> where U: length::Unit {
    type Output = Area<U>;

    fn mul(self, other: Self) -> Self::Output {
        let quantity = self.quantity * other.quantity;
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

impl<U> Mul<Length<U>> for Area<U> where U: length::Unit {
    type Output = Volume<U>;

    fn mul(self, other: Length<U>) -> Self::Output {
        let quantity = self.quantity * other.quantity;
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

/// Base units of length.
///
/// Each unit is defined relative to meters with a conversion factor.  They can
/// be used to conveniently create [Length]s.
///
/// ## Example
///
/// ```rust
/// use honeybee::units::length::Cm;
///
/// let a = 25.5 * Cm;
/// assert_eq!(a.to_string(), "25.5 cm");
/// ```
/// [Length]: ../struct.Length.html
///
pub mod length {
    use super::Length;
    use std::marker::PhantomData;
    use std::ops::Mul;

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

            impl Mul<$unit> for f64 {
                type Output = Length<$unit>;

                fn mul(self, _other: $unit) -> Self::Output {
                    let quantity = self;
                    Self::Output { quantity, unit: PhantomData }
                }
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
    use super::Time;
    use std::marker::PhantomData;
    use std::ops::Mul;

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
        /// Inverse unit abbreviation
        const INVERSE: &'static str;
    }

    macro_rules! time_unit {
        (
            $(#[$meta:meta])* $unit:ident,
            $s_factor:expr,
            $abbreviation:expr,
            $inverse:expr
        ) => {
            $(#[$meta])*
            #[derive(Debug, Copy, Clone, PartialEq)]
            pub struct $unit;

            impl Unit for $unit {
                fn s_factor() -> f64 { $s_factor }
                const ABBREVIATION: &'static str = { $abbreviation };
                const INVERSE: &'static str = { $inverse };
            }

            impl Mul<$unit> for f64 {
                type Output = Time<$unit>;

                fn mul(self, _other: $unit) -> Self::Output {
                    let quantity = self;
                    Self::Output { quantity, unit: PhantomData }
                }
            }
        };
    }

    time_unit!(/** Gigasecond */ Gs, 1_000_000_000.0, "Gs", "GHz");
    time_unit!(/** Megasecond */ Mgs, 1_000_000.0, "Ms", "MHz");
    time_unit!(/** 14 Days */ Fortnight, 14.0 * 24.0 * 60.0 * 60.0, "fortnight",
        "/fortnight");
    time_unit!(/** Week */ Wk, 7.0 * 24.0 * 60.0 * 60.0, "wk", "/wk");
    time_unit!(/** Day */ Day, 24.0 * 60.0 * 60.0, "day", "/day");
    time_unit!(/** Hour */ Hr, 60.0 * 60.0, "hr", "/hr");
    time_unit!(/** Minute */ Min, 60.0, "min", "/min");
    time_unit!(/** Second */ S, 1.0, "s", "Hz");
    time_unit!(/** Decisecond */ Ds, 0.1, "ds", "dHz");
    time_unit!(/** Millisecond */ Ms, 0.001, "ms", "mHz");
    time_unit!(/** Microsecond */ Us, 0.000_001, "μs", "μHz");
    time_unit!(/** Nanosecond */ Ns, 0.000_000_001, "ns", "nHz");
}

#[cfg(test)]
mod test {
    use super::*;
    use super::length::*;
    use super::time::*;

    #[test]
    fn len_display() {
        assert_eq!((2.5 * Km).to_string(), "2.5 km");
        assert_eq!((10.0 * M).to_string(), "10 m");
        assert_eq!((11.1 * Dm).to_string(), "11.1 dm");
        assert_eq!((25.0 * Cm).to_string(), "25 cm");
        assert_eq!((101.01 * Mm).to_string(), "101.01 mm");
        assert_eq!((3.9 * Um).to_string(), "3.9 μm");
        assert_eq!((2.22 * Mi).to_string(), "2.22 mi");
        assert_eq!((0.5 * Ft).to_string(), "0.5 ft");
        assert_eq!((6. * In).to_string(), "6 in");
        assert_eq!((100.0 * Yd).to_string(), "100 yd");
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
        assert_eq!((23.7 * S).to_string(), "23.7 s");
        assert_eq!((3.25 * Hr).to_string(), "3.25 hr");
    }

    #[test]
    fn len_to() {
        assert_eq!((1.0 * Ft).to(), (12.0 * In));
        assert_eq!((1.0 * Yd).to(), (3.0 * Ft));
        assert_eq!((1.0 * Yd).to(), (36.0 * In));
        assert_eq!((1.0 * Mi).to(), (5280.0 * Ft));
        assert_eq!((1.0 * M).to(), (0.001 * Km));
        assert_eq!((110.0 * Cm).to(), (1.1 * M));
        assert_eq!((1.0 * Cm).to(), 0.393_700_787_401_57 * In);
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
        assert_eq!((4.75 * Hr).to(), 285.0 * Min);
        assert_eq!((2.5 * S).to(), 2_500.0 * Ms);
    }

    #[test]
    fn len_add() {
        assert_eq!(1.0 * M + 1.0 * M, 2.0 * M);
        assert_eq!(10.0 * Ft + 2.0 * Ft, 12.0 * Ft);
        assert_eq!(6.0 * In + 6.0 * In, 12.0 * In);
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
        assert_eq!(3.5 * Day + 1.25 * Day, 4.75 * Day);
        assert_eq!(1.0 * Wk + 2.1 * Wk, 3.1 * Wk);
    }

    #[test]
    fn len_sub() {
        assert_eq!(5.0 * Km - 1.0 * Km, 4.0 * Km);
        assert_eq!(500.0 * Mm - 100.0 * Mm, 400.0 * Mm);
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
        assert_eq!(567.8 * Us - 123.4 * Us, 444.4 * Us);
    }

    #[test]
    fn len_mul() {
        assert_eq!((3.0 * M) * (3.0 * M), Area::<M>::new(9.0));
        assert_eq!((3.0 * Nm) * 3.0, 9.0 * Nm);
        assert_eq!(3.0 * (3.0 * M), 9.0 * M);
        assert_eq!((10.0 * In) * (5.0 * In), Area::<In>::new(50.0));
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
        assert_eq!((6.5 * Ns) * 12.0, 78.0 * Ns);
        assert_eq!(4.0 * (1.5 * Hr), 6.0 * Hr);
    }

    #[test]
    fn len_div() {
        assert_eq!((5.0 * Ft) / 5.0, 1.0 * Ft);
    }

    #[test]
    fn area_div() {
        assert_eq!(Area::<Cm>::new(500.0) / 5.0, Area::<Cm>::new(100.0));
        assert_eq!(Area::<Nm>::new(40.0) / Length::<Nm>::new(10.0), 4.0 * Nm);
    }

    #[test]
    fn volume_div() {
        assert_eq!(Volume::<Mm>::new(50.0) / 10.0, Volume::<Mm>::new(5.0));
        assert_eq!(Volume::<Yd>::new(40.0) / (2.0 * Yd), Area::<Yd>::new(20.0));
        assert_eq!(Volume::<In>::new(25.0) / Area::<In>::new(5.0), 5.0 * In);
    }
}
