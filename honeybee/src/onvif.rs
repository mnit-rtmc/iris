//
// Copyright (C) 2026  Minnesota Department of Transportation
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
use crate::error::{Error, Result};
use crate::soap;
use crate::xml::{XmlDocument, XmlWriter};
use xml::EmitterConfig;

use serde::{Deserialize, Serialize};

use std::collections::HashMap;
use std::fmt;
use std::io;

use tower_sessions::Session;

/// ONVIF namespaces
const TT: &str = "http://www.onvif.org/ver10/schema";
const VELOCITY_SPACE: &str =
    "http://www.onvif.org/ver10/tptz/PanTiltSpaces/VelocityGenericSpace";
const DEVICE_WSDL: &str = "http://www.onvif.org/ver10/device/wsdl";
const MEDIA_WSDL: &str = "http://www.onvif.org/ver10/media/wsdl";
const PTZ_WSDL: &str = "http://www.onvif.org/ver20/ptz/wsdl";

/// Session keys for ONVIF operations from a user
/// Top-level key, map of device address, device state
pub const DEVICE_STATES_KEY: &str = "onvif_devices";

/// Create ONVIF GetSystemDateAndTime XML
fn get_system_date_and_time_document<W: io::Write>(writer: &mut XmlWriter<W>) {
    writer.single_element_ns(
        "wsdl:GetSystemDateAndTime",
        ("wsdl", DEVICE_WSDL),
        "",
        &[],
    );
    writer.finish();
}

/// Create ONVIF GetProfiles XML
fn get_profiles_document<W: io::Write>(writer: &mut XmlWriter<W>) {
    writer.single_element_default_ns("GetProfiles", MEDIA_WSDL, "", &[]);
    writer.finish();
}

/// Create ONVIF GetCapabilities XML
fn get_capabilities_document<W: io::Write>(
    writer: &mut XmlWriter<W>,
    category: &[Category],
) {
    writer.start_element_default_ns("GetCapabilities", DEVICE_WSDL, &[]);
    for cat in category {
        writer.single_element(
            "Category",
            match cat {
                Category::All => "All",
                Category::Analytics => "Analytics",
                Category::Device => "Device",
                Category::Events => "Events",
                Category::Imaging => "Imaging",
                Category::Media => "Media",
                Category::Ptz => "PTZ",
            },
            &[],
        );
    }
    writer.finish();
}

/// Create ONVIF PTZ ContinuousMove XML
fn continuous_move_document<W: io::Write>(
    writer: &mut XmlWriter<W>,
    profile: String,
    p: f64,
    t: f64,
    z: f64,
) {
    writer.start_element_default_ns("ContinuousMove", PTZ_WSDL, &[]);
    writer.single_element("ProfileToken", &profile, &[]);
    writer.start_element("Velocity", &[]);
    writer.single_element_default_ns(
        "PanTilt",
        TT,
        "",
        &[
            ("x", &p.to_string()),
            ("y", &t.to_string()),
            ("space", VELOCITY_SPACE),
        ],
    );
    writer.single_element_default_ns(
        "Zoom",
        TT,
        "",
        &[("z", &z.to_string()), ("space", VELOCITY_SPACE)],
    );
    writer.finish();
}

/// Create ONVIF PTZ Stop XML
fn stop_document<W: io::Write>(writer: &mut XmlWriter<W>, profile: String) {
    writer.start_element_default_ns("Stop", PTZ_WSDL, &[]);
    writer.single_element("ProfileToken", &profile, &[]);
    writer.finish();
}

/// CapabilityCategory for GetCapabilities
#[derive(Debug, Clone, Copy)]
#[allow(dead_code)]
pub enum Category {
    All,
    Analytics,
    Device,
    Events,
    Imaging,
    Media,
    Ptz,
}

/// Supported ONVIF operations and associated responses
#[derive(Debug)]
pub enum OnvifOperation<'a> {
    /// Continuous pan/tilt/zoom movement on a device
    /// (ProfileToken, Pan, Tilt, Zoom)
    ContinuousMove(String, f64, f64, f64),
    /// No child nodes in ONVIF spec, nothing to parse
    ContinuousMoveResponse,

    /// Get ONVIF capabilities of a device, including service addresses
    /// Takes a list of CapabilityCategory items
    GetCapabilities(&'a [Category]),
    GetCapabilitiesResponse(XmlDocument),

    /// Get the media profiles of a device (needed for PTZ)
    GetProfiles,
    GetProfilesResponse(XmlDocument),

    /// Get the date/time of a device
    /// Unused, but may be needed if auth timestamp is too far off
    #[allow(dead_code)]
    GetSystemDateAndTime,
    #[allow(dead_code)]
    GetSystemDateAndTimeResponse(XmlDocument),

    /// Stop pan/tilt/zoom movements on a device
    #[allow(dead_code)]
    Stop(String),
    /// No child nodes in ONVIF spec, nothing to parse
    StopResponse,

    /// Any SOAP/ONVIF Fault response
    Fault(XmlDocument),
}

/// "Display" as the corresponding SOAP action, contained message, or empty
impl fmt::Display for OnvifOperation<'_> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let action = match self {
            OnvifOperation::ContinuousMove(_, _, _, _) => {
                "http://www.onvif.org/ver20/ptz/wsdl/ContinuousMove"
            }
            OnvifOperation::GetCapabilities(_) => {
                "http://www.onvif.org/ver10/device/wsdl/GetCapabilities"
            }
            OnvifOperation::GetProfiles => {
                "http://www.onvif.org/ver10/media/wsdl/GetProfiles"
            }
            OnvifOperation::GetSystemDateAndTime => {
                "http://www.onvif.org/ver10/device/wsdl/GetSystemDateAndTime"
            }
            OnvifOperation::Stop(_) => {
                "http://www.onvif.org/ver20/ptz/wsdl/Stop"
            }
            OnvifOperation::Fault(doc) => &format!("{doc}"),
            _ => &format!("{:#?}", self),
        };
        if !action.is_empty() {
            write!(f, "{action}")
        } else {
            Err(fmt::Error)
        }
    }
}

impl OnvifOperation<'_> {
    /// Get the name of the ONVIF service the message belongs to
    fn get_service(&self) -> &'static str {
        match self {
            Self::ContinuousMove(_, _, _, _) => "PTZ",
            Self::GetCapabilities(_) => "Device",
            Self::GetProfiles => "Media",
            Self::GetSystemDateAndTime => "Device",
            Self::Stop(_) => "PTZ",
            _ => "Device",
        }
    }

    /// Set the media profile token of ONVIF PTZ operation
    fn set_profile(&mut self, profile: String) {
        match self {
            Self::ContinuousMove(p, _, _, _) | Self::Stop(p) => {
                if p.is_empty() {
                    *p = profile;
                }
            }
            _ => (),
        }
    }

    /// Encode this ONVIF operation as XML into the buffer
    fn encode(&self, buf: &mut Vec<u8>, userpass: Option<(&str, &str)>) {
        let mut xml_writer = XmlWriter::new(
            EmitterConfig::new()
                .line_separator("") // default: "\n"
                .indent_string("") // default: "  "
                .perform_indent(false) // default: false
                .write_document_declaration(false) // default: true
                .normalize_empty_elements(true) // default: true
                .cdata_to_characters(false) // default: false
                .keep_element_names_stack(true) //default: true
                .autopad_comments(false) // default: true
                .pad_self_closing(false) // default: true
                .create_writer(buf),
        );
        soap::get_base_document(&mut xml_writer, userpass);
        match self {
            OnvifOperation::ContinuousMove(profile, p, t, z) => {
                continuous_move_document(
                    &mut xml_writer,
                    profile.clone(),
                    *p,
                    *t,
                    *z,
                );
            }
            OnvifOperation::GetCapabilities(categories) => {
                get_capabilities_document(&mut xml_writer, categories);
            }
            OnvifOperation::GetProfiles => {
                get_profiles_document(&mut xml_writer);
            }
            OnvifOperation::GetSystemDateAndTime => {
                get_system_date_and_time_document(&mut xml_writer);
            }
            OnvifOperation::Stop(profile) => {
                stop_document(&mut xml_writer, profile.clone());
            }
            _ => unimplemented!(),
        }
    }
}

/// Attempts to map an XML Document to the enum
impl TryFrom<XmlDocument> for OnvifOperation<'_> {
    type Error = Error;

    fn try_from(doc: XmlDocument) -> Result<Self> {
        let body_opt = doc.find("Body");
        let body = if let Some(b) = body_opt {
            b
        } else {
            log::error!("Couldn't find Body element in {doc}");
            Err(Error::InvalidValue)?
        };
        for child in doc.get_child_elements(body) {
            let name = child.get_local_name();
            match name.as_ref() {
                "ContinuousMoveResponse" => {
                    return Ok(Self::ContinuousMoveResponse);
                }
                "GetCapabilitiesResponse" => {
                    return Ok(Self::GetCapabilitiesResponse(doc));
                }
                "GetProfilesResponse" => {
                    return Ok(Self::GetProfilesResponse(doc));
                }
                "GetSystemDateAndTimeResponse" => {
                    return Ok(Self::GetSystemDateAndTimeResponse(doc));
                }
                "StopResponse" => {
                    return Ok(Self::StopResponse);
                }
                "Fault" => {
                    return Ok(Self::Fault(doc));
                }
                _ => (),
            }
        }

        Err(Error::InvalidValue)
    }
}

/// Attempts to create OnvifOperation from bytes by converting to XmlDocument
impl TryFrom<Vec<u8>> for OnvifOperation<'_> {
    type Error = Error;

    fn try_from(source: Vec<u8>) -> Result<Self> {
        let b: &[u8] = &source;
        let doc = XmlDocument::from(b);
        Self::try_from(doc)
    }
}

/// Attempts to create OnvifOperation from bytes by converting to XmlDocument
impl TryFrom<&[u8]> for OnvifOperation<'_> {
    type Error = Error;

    fn try_from(source: &[u8]) -> Result<Self> {
        let doc = XmlDocument::from(source);
        Self::try_from(doc)
    }
}

/// Device data stored in session for reuse
//TODO: track authentication Faults, add valid_auth flag
#[derive(Clone, Debug, Default, Serialize, Deserialize)]
struct DeviceState {
    /// If the device hasn't responded to the last message
    pending: bool,
    /// Username for ONVIF requests
    user: String,
    /// Password for ONVIF requests
    pass: String,
    /// ONVIF media profile for PTZ operations
    media_profile: String,
    /// Map of ONVIF service entrypoints
    xaddrs: HashMap<String, String>,
}

impl DeviceState {
    /// If the current device state has fetched the PTZ media profile
    fn has_media_profile(&self) -> bool {
        !self.media_profile.is_empty()
    }

    /// If the current device state has fetched a service XAddr
    fn has_xaddr(&self, service: &str) -> bool {
        service == "Device" || self.xaddrs.contains_key(service)
    }

    /// Get the XAddr for the service
    /// Device service address is part of the ONVIF spec
    fn get_xaddr(&self, service: &str) -> String {
        if service == "Device" {
            return String::from("/onvif/device_service");
        }
        match self.xaddrs.get(service) {
            Some(s) => s.to_owned(),
            None => String::new(),
        }
    }
}

/// Messenger to an ONVIF-conformant device
#[derive(Debug)]
pub struct OnvifMessenger<'a> {
    /// Hostname of device
    host: String,
    /// Username for ONVIF authentication
    user: String,
    /// Password for ONVIF authentication
    pass: String,
    /// Session reference for persistent data
    session: &'a Session,
}

impl<'a> OnvifMessenger<'a> {
    /// Create a new ONVIF messenger
    pub async fn new(
        host: &str,
        _port: u16,
        user: &str,
        pass: &str,
        session: &'a Session,
    ) -> Result<Self> {
        Ok(OnvifMessenger {
            host: host.to_owned(),
            user: user.to_owned(),
            pass: pass.to_owned(),
            session,
        })
    }

    /// Fetch the media profile token from the device if not cached
    async fn get_and_store_media_profile(
        &mut self,
        msg: &mut OnvifOperation<'_>,
    ) -> Result<()> {
        let mut state = self.get_device_state().await;

        // If the stored state already has the profile, don't request one
        if !state.has_media_profile() {
            log::debug!("State doesn't have media profile: {state:#?}");
            let profile = self.fetch_media_profile().await?;
            state.media_profile = profile.clone();
            msg.set_profile(profile);
            self.store_device_state(state.clone()).await;
        }

        Ok(())
    }

    /// Fetch the media profile token from the device
    async fn fetch_media_profile(&mut self) -> Result<String> {
        let msg = OnvifOperation::GetProfiles;
        let res_bytes = self.send_message(msg).await?;

        let profiles = OnvifOperation::try_from(res_bytes)?;

        if let OnvifOperation::GetProfilesResponse(doc) = profiles {
            let ptz_config = doc.find("PTZConfiguration");
            if let Some(ptz_config) = ptz_config {
                let parent = doc.get_parent_element(ptz_config);
                let token_opt = parent.get("token");
                if let Some(token) = token_opt {
                    return Ok(token);
                }
            } else {
                log::error!("Couldn't find PTZ config!");
            }
        } else if let OnvifOperation::Fault(doc) = profiles {
            log::error!("Fault: {doc}");
        } else {
            log::error!("Not Fault or Response: {profiles}");
        }

        Err(Error::InvalidValue)?
    }

    /// Update the session with the host's pending status
    pub async fn update_pending(&self, pending: bool) {
        let mut state = self.get_device_state().await;

        state.pending = pending;

        self.store_device_state(state).await;
    }

    /// If the messenger has a username and password set
    pub fn has_user_pass(&self) -> bool {
        !self.user.is_empty() || !self.pass.is_empty()
    }

    /// Set the device's user/pass in the session store
    pub async fn set_user_pass(&mut self, user: &str, pass: &str) {
        let mut state = self.get_device_state().await;

        self.user = user.to_owned();
        self.pass = pass.to_owned();
        state.user = self.user.clone();
        state.pass = self.pass.clone();

        self.store_device_state(state).await;
    }

    /// Get the device's user/pass from the session store
    /// Returns true if they were in store
    pub async fn get_user_pass_from_store(&mut self) -> bool {
        let state = self.get_device_state().await;

        self.user = state.user.clone();
        self.pass = state.pass.clone();

        self.has_user_pass()
    }

    /// Whether a given PTZ value is equivalent to a Stop command
    fn is_stop(ptz: Option<(f64, f64, f64)>) -> bool {
        if let Some(ptz) = ptz {
            let eps = 1e-10;
            ptz.0.abs() < eps && ptz.1.abs() < eps && ptz.2.abs() < eps
        } else {
            false
        }
    }

    /// Fetch pending status from state
    pub async fn is_pending(&self) -> bool {
        let devices: HashMap<String, DeviceState> =
            match self.session.get(DEVICE_STATES_KEY).await {
                Ok(Some(d)) => d,
                Ok(None) => {
                    // No device states found -> not pending
                    return false;
                }
                Err(e) => {
                    log::error!("Couldn't load devices from store: {e}");
                    return false;
                }
            };

        let device = if let Some(d) = devices.get(&self.host) {
            d
        } else {
            // No state for this device -> not pending
            return false;
        };

        device.pending
    }

    /// True if device has no pending ONVIF request, or if sending a stop
    pub async fn should_send(&self, ptz: Option<(f64, f64, f64)>) -> bool {
        if Self::is_stop(ptz) {
            // Always send stop operations
            return true;
        }

        // If not pending response, should send
        !self.is_pending().await
    }

    /// Gets the path from the session store, or a default
    /// Does not update the session store
    async fn message_path(&mut self, msg: &OnvifOperation<'_>) -> String {
        let state = self.get_device_state().await;
        state.get_xaddr(msg.get_service())
    }

    /// Fetch the ONVIF service addresses from the device
    async fn fetch_device_xaddrs(&mut self) -> Result<HashMap<String, String>> {
        let msg =
            OnvifOperation::GetCapabilities(&[Category::Ptz, Category::Media]);
        let res_bytes = self.send_message(msg).await?;

        let capabilities = OnvifOperation::try_from(res_bytes)?;

        if let OnvifOperation::GetCapabilitiesResponse(doc) = capabilities {
            let xaddrs = doc.find_all("XAddr");
            let mut map = HashMap::new();

            for xaddr in &xaddrs {
                let parent = doc.get_parent_element(xaddr);
                let nm = parent.get_local_name();
                let text = doc.get_text(xaddr);
                map.insert(nm, text);
            }
            return Ok(map);
        } else if let OnvifOperation::Fault(doc) = capabilities {
            log::error!("Fault: {doc}");
        } else {
            log::error!("Not Fault or Response: {capabilities}");
        }

        Err(Error::InvalidValue)?
    }

    /// Fetch the ONVIF service addresses from the device if not cached
    async fn get_and_store_xaddrs(
        &mut self,
        msg: &OnvifOperation<'_>,
    ) -> Result<()> {
        let mut state = self.get_device_state().await;

        // If the stored state already has the XAddr, don't request one
        if !state.has_xaddr(msg.get_service()) {
            log::debug!("State doesn't have xaddr: {state:#?}");
            let addrs = self.fetch_device_xaddrs().await?;
            state.xaddrs = addrs;
            self.store_device_state(state).await;
        }

        Ok(())
    }

    /// Get the list of device states stored in the session
    /// Indexed by URI/hostname of device
    async fn get_devices(&self) -> HashMap<String, DeviceState> {
        let devices: HashMap<String, DeviceState> = HashMap::new();
        match self.session.get(DEVICE_STATES_KEY).await {
            Ok(Some(d)) => d,
            Ok(None) => {
                self.session
                    .insert(DEVICE_STATES_KEY, devices)
                    .await
                    .expect(
                        "Couldn't insert DEVICE_STATES_KEY {DEVICE_STATES_KEY}",
                    );
                self.session.get(DEVICE_STATES_KEY).await.unwrap().expect(
                    "Couldn't get DEVICE_STATES_KEY {DEVICE_STATES_KEY} after setting",
                )
            }
            Err(e) => {
                log::debug!("Error getting devices from session: {e}");
                self.session
                    .insert(DEVICE_STATES_KEY, devices)
                    .await
                    .expect(
                        "Couldn't insert DEVICE_STATES_KEY {DEVICE_STATES_KEY}",
                    );
                self.session.get(DEVICE_STATES_KEY).await.unwrap().expect(
                    "Couldn't get DEVICE_STATES_KEY {DEVICE_STATES_KEY} after setting",
                )
            }
        }
    }

    /// Get this device's state from the store, or add to store if not present
    async fn get_device_state(&self) -> DeviceState {
        let mut devices = self.get_devices().await;
        let device = if let Some(d) = devices.get_mut(&self.host) {
            d
        } else {
            devices.insert(self.host.clone(), Default::default());
            devices
                .get(&self.host)
                .expect("Couldn't get device after inserting")
        };
        device.clone()
    }

    /// Save a state to the session store for this device
    async fn store_device_state(&self, device: DeviceState) {
        let mut devices = self.get_devices().await;
        devices.insert(self.host.clone(), device);
        self.session
            .insert(DEVICE_STATES_KEY, devices)
            .await
            .expect("Couldn't insert DEVICE_STATES_KEY {DEVICE_STATES_KEY}");
        self.session.save().await.expect("Couldn't save session");
    }

    /// Create the ONVIF envelope then send the SOAP message
    async fn send_message(
        &mut self,
        msg: OnvifOperation<'_>,
    ) -> Result<Vec<u8>> {
        let mut envelope = Vec::new();
        msg.encode(&mut envelope, Some((&self.user, &self.pass)));
        let envelope = match str::from_utf8(&envelope) {
            Ok(s) => s,
            Err(e) => {
                log::error!("Error {e:?} parsing envelope: {envelope:?}");
                Err(Error::InvalidValue)?
            }
        }
        .to_owned();

        let path = self.message_path(&msg).await;
        if path.is_empty() {
            return Err(Error::NotFound)?;
        }

        soap::send(self.host.clone(), path, msg.to_string(), envelope).await
    }

    /// Send an ONVIF operation to the device
    /// Fetches the service address and media ProfileToken if needed
    async fn send_onvif(
        &mut self,
        mut msg: OnvifOperation<'_>,
    ) -> Result<OnvifOperation<'_>> {
        self.get_and_store_xaddrs(&msg).await?;
        self.get_and_store_media_profile(&mut msg).await?;
        let res_bytes = self.send_message(msg).await?;

        let decoded = OnvifOperation::try_from(res_bytes)?;
        log::debug!("decoded message from device: {decoded:#?}");

        Ok(decoded)
    }

    /// Send a PTZ command to a camera
    pub async fn send_ptz(&mut self, ptz: (f64, f64, f64)) -> Result<()> {
        let profile = self.get_device_state().await.media_profile;

        // Round near-stop to exactly 0.0
        let op = if Self::is_stop(Some(ptz)) {
            // NOTE: some cameras send a StopResponse but still don't stop
            // Most compatible option is just PTZ (0, 0, 0)
            OnvifOperation::ContinuousMove(profile, 0.0, 0.0, 0.0)
        } else {
            OnvifOperation::ContinuousMove(profile, ptz.0, ptz.1, ptz.2)
        };

        self.send_onvif(op).await?;

        Ok(())
    }
}
