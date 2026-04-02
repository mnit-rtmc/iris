use serde::{Deserialize};
use serde_json::{Value, json};
use resin::{Error, Result};
use std::time::{Duration, SystemTime};
use crate::http;

#[derive(Deserialize, Debug)]
struct Auth {
    access_token: String,
    expires_in: u64,
}

pub struct ApiUtility {
    client: http::HttpsClient,
    username: String,
    password: String,
    auth: Option<Auth>,
    auth_time: SystemTime,
    organization_id: String,
    assets: Option<Value>,
    datastreams: Option<Value>,
}

impl ApiUtility {
    /** Request new authorization tokens from the API */
    async fn get_auth(client: http::HttpsClient, u: &str, p: &str) -> Result<Auth> {
        let body = format!("{{\
            \"username\": \"{u}\", \
            \"password\": \"{p}\", \
            \"client_id\": \"cloud\", \
            \"grant_type\": \"password\" \
        }}");
        let resp = client.post("api/v1/tokens", &body).await?;
        Ok(serde_json::from_slice(&resp)?)
    }

    /**
     * Create a new ApiUtility class.
     * Used to access API programmatically and update the auth if needed.
     */
    pub async fn new(base_url: &str, username: &str, password: &str, organization_id: &str) -> Self {
        let mut c = http::HttpsClient::new(base_url);
        let a_res = Self::get_auth(c.clone(), username, password).await;
        let a_opt = match a_res {
            Ok(a) => {
                c.set_bearer_token(a.access_token.clone());
                Some(a)
            },
            Err(_) => None
        };
        ApiUtility {
            client: c,
            username: username.to_string(),
            password: password.to_string(),
            auth: a_opt,
            auth_time: SystemTime::now(),
            organization_id: organization_id.to_string(),
            assets: None,
            datastreams: None,
        }
    }

    /** Check if the auth has expired, and refresh it if so */
    async fn update_auth(&mut self) -> Result<()> {
        if let Some(auth) = &self.auth {
            let now = SystemTime::now();
            if now.duration_since(self.auth_time).unwrap_or_default() < Duration::from_secs(auth.expires_in) {
                return Ok(());
            }
            self.auth_time = SystemTime::now();
        } else {
            log::error!("Couldn't authenticate with CampbellCloud API.");
        }
        let a = Self::get_auth(self.client.clone(), &self.username, &self.password).await?;
        self.client.set_bearer_token(a.access_token.clone());
        self.auth = Some(a);
        Ok(())
    }

    /** Send a GET request defined by the endpoint and return the result if successful */
    pub async fn get_request(&mut self, endpoint: &str) -> Result<Value> {
        self.update_auth().await?;

        let response = self.client.get(endpoint).await?;
        match serde_json::from_slice(&response) {
            Ok(val) => {
                Ok(val)
            },
            Err(e) => {
                return Err(Error::SerdeJson(e))
            }
        }
    }

    /** Request to list-datastreams API endpoint */
    pub async fn list_datastreams(&mut self) -> Result<Value> {
        if let Some(ds) = &self.datastreams {
            return Ok(ds.to_owned());
        }
        let endpoint = format!(
            "api/v1/organizations/{}/datastreams?limit={}",
            self.organization_id, i32::MAX
        );
        let ds = self.get_request(&endpoint).await?;
        self.datastreams = Some(ds.clone());
        Ok(ds)
    }

    /** Request to get-datastream-datapoints-last API endpoint */
    pub async fn last_datapoint(&mut self, datastream: &str) -> Result<Value> {
        let endpoint = format!(
            "api/v1/organizations/{}/datastreams/{}/datapoints/last",
            self.organization_id, datastream
        );
        self.get_request(&endpoint).await
    }

    /** Request to list-assets API endpoint */
    pub async fn list_assets(&mut self) -> Result<Value> {
        if let Some(a) = &self.assets {
            return Ok(a.to_owned());
        }
        let endpoint = format!(
            "api/v1/organizations/{}/assets",
            self.organization_id
        );
        let a = self.get_request(&endpoint).await?;
        self.assets = Some(a.clone());
        Ok(a)
    }

    /** Takes a serial number of an asset, and returns the ID for that asset */
    pub async fn get_id_from_serial(&mut self, s: &str) -> Option<Value> {
        if let Ok(assets) = self.list_assets().await {
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
    pub async fn get_asset_last_datapoint_value(&mut self, asset_id: &str, datastream_name: &str) -> Result<Value> {
        let datastreams = self.list_datastreams().await;
        let mut res = Ok(json!({}));
        if let Ok(ds) = datastreams {
            for d in ds.as_array().unwrap() {
                if d["asset_id"] == json!(asset_id) && d["metadata"]["field"] == json!(datastream_name) {
                    let id = d["id"].as_str().unwrap();
                    if let Ok(data) = self.last_datapoint(id).await {
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
