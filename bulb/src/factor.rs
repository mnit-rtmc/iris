// Copyright (C) 2024  Minnesota Department of Transportation
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

/// Prime factor iterator
struct PrimeFactors {
    /// Value to factorize
    value: i32,

    /// Current value
    current: i32,
}

impl PrimeFactors {
    /// Create a new prime factor iterator
    fn new(value: i32) -> Self {
        PrimeFactors { value, current: 2 }
    }
}

impl Iterator for PrimeFactors {
    type Item = i32;

    fn next(&mut self) -> Option<Self::Item> {
        while self.current < self.value {
            if self.value % self.current == 0 {
                break;
            } else {
                self.current += 1;
            }
        }
        if self.value >= self.current {
            self.value /= self.current;
            Some(self.current)
        } else {
            None
        }
    }
}

/// Get an iterator of integer prime factors
pub fn prime(value: i32) -> impl Iterator<Item = i32> {
    PrimeFactors::new(value)
}

/// Get an iterator of unique integer factors
pub fn unique(value: i32) -> impl Iterator<Item = i32> {
    let mut primes: Vec<_> = prime(value).collect();
    let len = primes.len();
    let mut factors = Vec::with_capacity(len * len);
    while let Some(f) = primes.pop() {
        factors.push(f);
        for i in 1..=primes.len() {
            for win in primes.windows(i) {
                let mut v = f;
                for w in win {
                    v *= w;
                }
                factors.push(v);
            }
        }
    }
    factors.sort();
    factors.dedup();
    factors.into_iter()
}
