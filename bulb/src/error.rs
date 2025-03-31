// Copyright (C) 2022-2025  Minnesota Department of Transportation
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
use ntcip::dms::multi::SyntaxError;
use ntcip::dms::tfon;

/// Bulb errors
#[derive(Debug, thiserror::Error)]
pub enum Error {
    /// Fetch request error
    #[error("Fetch request error")]
    FetchRequest(),

    /// Invalid font error
    #[error("Invalid font {0}")]
    InvalidFont(#[from] tfon::Error),

    /// Sign rendering error
    #[error("Rendering {0}")]
    Render(#[from] rendzina::Error),

    /// MULTI syntax error
    #[error("MULTI syntax error {0}")]
    MultiSyntax(#[from] SyntaxError),

    /// Fetch response "Unauthorized 401"
    #[error("Unauthorized")]
    FetchResponseUnauthorized(),

    /// Fetch response "Forbidden 403"
    #[error("Forbidden")]
    FetchResponseForbidden(),

    /// Fetch response "Not Found 404"
    #[error("Not Found")]
    FetchResponseNotFound(),

    /// Fetch response "Conflict 409"
    #[error("Conflict")]
    FetchResponseConflict(),

    /// Fetch response "Unprocessable Entity 422"
    #[error("Unprocessable")]
    FetchResponseUnprocessable(),

    /// Fetch response other error
    #[error("Status code {0}")]
    FetchResponseOther(u16),

    /// Element ID not found
    #[error("Elem id {0} not found")]
    ElemIdNotFound(&'static str),

    /// Card mismatch (added / deleted)
    #[error("card mismatch")]
    CardMismatch(),

    /// Serde JSON error
    #[error("Serialization error")]
    SerdeJson(#[from] serde_json::Error),

    /// Serde wasm-bindgen error
    #[error("Serialization error")]
    SerdeWasmBindgen(#[from] serde_wasm_bindgen::Error),

    /// GIF error
    #[error("GIF error")]
    Gift(#[from] gift::Error),
}

/// Bulb result
pub type Result<T> = std::result::Result<T, Error>;
