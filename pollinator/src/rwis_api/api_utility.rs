use crate::http;
use resin::{Error, Result};
use serde::Deserialize;
use serde::de::Error as _;
use serde_json::{Value, json};
use std::time::{Duration, SystemTime};

/// Holds authorization token and expiry, checked in update_auth.
#[derive(Deserialize, Debug)]
struct Auth {
    access_token: String,
    expires_in: u64,
}

/// Utility to access API and refresh auth if needed.
pub struct ApiUtility {
    client: http::Client,
    username: String,
    password: String,
    auth: Option<Auth>,
    auth_time: SystemTime,
    organization_id: String,
    assets: Option<Value>,
    datastreams: Option<Value>,
}

impl ApiUtility {
    /// Creates a new ApiUtility class.
    pub async fn new(
        base_url: &str,
        username: &str,
        password: &str,
        organization_id: &str,
    ) -> Self {
        let mut c = http::Client::new(base_url);
        let a_res = Self::get_auth(c.clone(), username, password).await;
        let a_opt = match a_res {
            Ok(a) => {
                c.set_bearer_token(format!(
                    "Bearer {}",
                    a.access_token.clone()
                ));
                Some(a)
            }
            Err(_) => None,
        };
        let ds_opt = if let Some(ref a) = a_opt {
            let mut c = c.clone();
            c.set_bearer_token(format!("Bearer {}", a.access_token.clone()));
            Self::get_datastreams(c, organization_id).await.ok()
        } else {
            None
        };
        ApiUtility {
            client: c,
            username: username.to_string(),
            password: password.to_string(),
            auth: a_opt,
            auth_time: SystemTime::now(),
            organization_id: organization_id.to_string(),
            assets: None,
            datastreams: ds_opt,
        }
    }

    /// Requests new authorization tokens from the API.
    async fn get_auth(client: http::Client, u: &str, p: &str) -> Result<Auth> {
        let body = format!(
            "{{ \
                \"username\": \"{u}\", \
                \"password\": \"{p}\", \
                \"client_id\": \"cloud\", \
                \"grant_type\": \"password\" \
            }}"
        );
        let resp = client.post("api/v1/tokens", &body).await?;
        Ok(serde_json::from_slice(&resp)?)
    }

    /// Gets available datastreams from the API.
    async fn get_datastreams(
        client: http::Client,
        org_id: &str,
    ) -> Result<Value> {
        let endpoint = format!(
            "api/v1/organizations/{}/datastreams?limit={}",
            org_id,
            i32::MAX
        );
        let resp = client.get(&endpoint).await?;
        Ok(serde_json::from_slice(&resp)?)
    }

    /// Checks if the auth has expired, and refreshes it if so.
    pub async fn update_auth(&mut self) -> Result<()> {
        if let Some(auth) = &self.auth {
            let now = SystemTime::now();
            if now.duration_since(self.auth_time).unwrap_or_default()
                < Duration::from_secs(auth.expires_in)
            {
                return Ok(());
            }
            self.auth_time = SystemTime::now();
        } else {
            log::error!("Couldn't authenticate with CampbellCloud API.");
        }
        let a =
            Self::get_auth(self.client.clone(), &self.username, &self.password)
                .await?;
        self.client
            .set_bearer_token(format!("Bearer {}", a.access_token.clone()));
        self.auth = Some(a);
        Ok(())
    }

    /// Sends a GET request to the API endpoint and returns the result.
    async fn get_request(&self, endpoint: &str) -> Result<Value> {
        let response = self.client.get(endpoint).await?;
        match serde_json::from_slice(&response) {
            Ok(val) => Ok(val),
            Err(e) => Err(Error::SerdeJson(e)),
        }
    }

    /// Gets organization's assets from the API.
    pub async fn get_assets(&mut self) -> Result<Value> {
        if let Some(a) = &self.assets {
            return Ok(a.to_owned());
        }
        let endpoint =
            format!("api/v1/organizations/{}/assets", self.organization_id);
        let a = self.get_request(&endpoint).await?;
        self.assets = Some(a.clone());
        Ok(a)
    }

    /// Gets API ID for asset specified by serial number.
    pub async fn get_id_from_serial(&mut self, s: &str) -> Option<Value> {
        if let Ok(assets) = self.get_assets().await {
            for a in assets.as_array()? {
                if a["metadata"]["serial"] == json!(s) {
                    return Some(a["id"].clone());
                }
            }
        }
        None
    }

    /// Gets the last datapoint from the API for a given datastream.
    async fn last_datapoint(&self, datastream: &str) -> Result<Value> {
        let endpoint = format!(
            "api/v1/organizations/{}/datastreams/{}/datapoints/last",
            self.organization_id, datastream
        );
        self.get_request(&endpoint).await
    }

    /// Gets the last datapoint for a given asset and datastream name.
    /// Finds datastream ID by measurement type ("field") and asset.
    pub async fn get_asset_last_datapoint_value(
        &self,
        asset_id: &str,
        datastream_name: &str,
    ) -> Result<(Value, u64)> {
        let datastreams = self
            .datastreams
            .clone()
            .ok_or(Error::InvalidConfig("Datastreams not received"))?;
        for d in datastreams.as_array().cloned().iter().flatten() {
            if d["asset_id"] == json!(asset_id)
                && d["metadata"]["field"] == json!(datastream_name)
                && let Some(id) = d["id"].as_str()
                && let Ok(data) = self.last_datapoint(id).await
            {
                return Ok((
                    data["data"][0]["value"].clone(),
                    data["data"][0]["ts"].clone().as_u64().ok_or(
                        serde_json::Error::custom(
                            "Timestamp ts not an integer!",
                        ),
                    )?,
                ));
            }
        }
        Err(serde_json::Error::custom(
            "Couldn't get last datapoint value",
        ))?
    }
}
