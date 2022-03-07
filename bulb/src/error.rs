// Copyright (C) 2022  Minnesota Department of Transportation
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

/// Bulb errors
#[derive(Debug, thiserror::Error)]
pub enum Error {
    /// Fetch request error
    #[error("Fetch request error")]
    FetchRequest(),

    /// Fetch response error
    #[error("Status code {0}")]
    FetchResponse(u16),

    /// Name missing
    #[error("Name missing")]
    NameMissing(),

    /// Parse error
    #[error("Parse error")]
    ParseError(),

    /// Serde JSON error
    #[error("Serialization error")]
    SerdeJson(#[from] serde_json::Error),
}

/// Bulb result
pub type Result<T> = std::result::Result<T, Error>;
