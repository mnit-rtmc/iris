use reqwest::blocking::Client;
use reqwest::Error;
use serde::{Deserialize};
use serde_json::{Value, json};
use std::time::{Duration, SystemTime};

#[derive(Deserialize)]
struct Auth {
    access_token: String,
    expires_in: u64,
}

pub struct ApiUtility {
    client: Client,
    base_url: String,
    username: String,
    password: String,
    auth: Auth,
    auth_time: SystemTime,
    organization_id: String,
    assets: Option<Value>,
    datastreams: Option<Value>,
}

impl ApiUtility {
    /** Request new authorization tokens from the API */
    fn get_auth(base_url: &str, u: &str, p: &str) -> Auth {
        let auth_url = format!("{}/api/v1/tokens", base_url);
        let raw_response = Client::new()
            .post(&auth_url)
            .json(&json!({
                "client_id": "cloud",
                "grant_type": "password",
                "username": u,
                "password": p
            }))
            .send()
            .expect("Failed to send auth request")
            .text()
            .expect("Failed to read response body");

        serde_json::from_str(&raw_response)
            .expect("Failed to parse auth response as JSON")
    }

    /**
     * Create a new ApiUtility class.
     * Used to access API programmatically and update the auth if needed.
     */
    pub fn new(base_url: &str, username: &str, password: &str, organization_id: &str) -> Self {
        ApiUtility {
            client: Client::new(),
            base_url: base_url.to_string(),
            username: username.to_string(),
            password: password.to_string(),
            auth: Self::get_auth(base_url, username, password),
            auth_time: SystemTime::now(),
            organization_id: organization_id.to_string(),
            assets: None,
            datastreams: None,
        }
    }

    /** Check if the auth has expired, and refresh it if so */
    fn update_auth(&mut self) {
        let now = SystemTime::now();
        if now.duration_since(self.auth_time).unwrap_or_default() < Duration::from_secs(self.auth.expires_in) {
            return;
        }
        self.auth_time = SystemTime::now();
        self.auth = Self::get_auth(&self.base_url, &self.username, &self.password);
    }

    /** Send a GET request defined by the endpoint and return the result if successful */
    pub fn get_request(&mut self, endpoint: &str) -> Result<Value, Error> {
        self.update_auth();

        let request_url = format!("{}/{}", self.base_url, endpoint);
        let response = self
            .client
            .get(&request_url)
            .bearer_auth(&self.auth.access_token)
            .send()?;

        if response.status().is_success() {
            let json = response.json()?;
            Ok(json)
        } else {
            if let Err(e) = response.error_for_status_ref() {
                Err(e)
            } else {
                Ok(json!("Couldn't parse error."))
            }
        }
    }

    /** Request to list-datastreams API endpoint */
    pub fn list_datastreams(&mut self) -> Result<Value, Error> {
        if let Some(ds) = &self.datastreams {
            return Ok(ds.to_owned());
        }
        let endpoint = format!(
            "api/v1/organizations/{}/datastreams?limit={}",
            self.organization_id, i32::MAX
        );
        let ds = self.get_request(&endpoint)?;
        self.datastreams = Some(ds.clone());
        Ok(ds)
    }

    /** Request to get-datastream-datapoints-last API endpoint */
    pub fn last_datapoint(&mut self, datastream: &str) -> Result<Value, Error> {
        let endpoint = format!(
            "api/v1/organizations/{}/datastreams/{}/datapoints/last",
            self.organization_id, datastream
        );
        self.get_request(&endpoint)
    }

    /** Request to list-assets API endpoint */
    pub fn list_assets(&mut self) -> Result<Value, Error> {
        if let Some(a) = &self.assets {
            return Ok(a.to_owned());
        }
        let endpoint = format!(
            "api/v1/organizations/{}/assets",
            self.organization_id
        );
        let a = self.get_request(&endpoint)?;
        self.assets = Some(a.clone());
        Ok(a)
    }

    /** Takes a serial number of an asset, and returns the ID for that asset */
    pub fn get_id_from_serial(&mut self, s: &str) -> Option<Value> {
        if let Ok(assets) = self.list_assets() {
            for a in assets.as_array()? {
                if a["metadata"]["serial"] == json!(s) {
                    return Some(a["id"].clone());
                }
            }
        }
        None
    }

    /**
     * Return the value of the last datapoint of datastream for an asset.
     * Wraps last_datapoint, finding the ID by measurement name ("field") and asset.
     */
    pub fn get_asset_last_datapoint_value(&mut self, asset_id: &str, datastream_name: &str) -> Result<Value, Error> {
        let datastreams = self.list_datastreams();
        let mut res = Ok(json!({}));
        if let Ok(ds) = datastreams {
            for d in ds.as_array().unwrap() {
                if d["asset_id"] == json!(asset_id) && d["metadata"]["field"] == json!(datastream_name) {
                    let id = d["id"].as_str().unwrap();
                    if let Ok(data) = self.last_datapoint(id) {
                        res = Ok(data["data"][0]["value"].clone());
                        break;
                    }
                }
            }
        } else {
            res = datastreams
        }
        res
    }
}
