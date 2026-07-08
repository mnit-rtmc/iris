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
//use crate::tls;

use base64::{Engine as _, engine::general_purpose::STANDARD as b64};
use chrono::{SecondsFormat, Utc};
use http_body_util::BodyExt;
use hyper::body::Incoming;
use hyper::{Request, Response};
use hyper_util::rt::TokioIo;
//use rustls::pki_types::ServerName;
use serde::{Deserialize, Serialize};
use sha1::{Digest, Sha1};

use std::collections::HashMap;
use std::fmt;
use std::io;
//use std::net::ToSocketAddrs;

//use tokio::io::{AsyncReadExt, AsyncWriteExt};
use tokio::net::TcpStream;
//use tokio_rustls::client::TlsStream;
use tower_sessions::Session;

use xml::reader::{EventReader, XmlEvent as ReaderEvent};
use xml::writer::{EmitterConfig, EventWriter, XmlEvent};

/// SOAP/XML namespaces for ONVIF
const SOAP: &str = "http://www.w3.org/2003/05/soap-envelope";
const TT: &str = "http://www.onvif.org/ver10/schema";
const WSSE: &str = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
const WSU: &str = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
const XSD: &str = "http://www.w3.org/2001/XMLSchema";
const XSI: &str = "http://www.w3.org/2001/XMLSchema-instance";
const VELOCITY_SPACE: &str =
    "http://www.onvif.org/ver10/tptz/PanTiltSpaces/VelocityGenericSpace";
const DEVICE_WSDL: &str = "http://www.onvif.org/ver10/device/wsdl";
const MEDIA_WSDL: &str = "http://www.onvif.org/ver10/media/wsdl";
const PTZ_WSDL: &str = "http://www.onvif.org/ver20/ptz/wsdl";

/// Session keys for ONVIF operations from a user
/// Top-level key, map of device address, device state
pub const DEVICE_STATES_KEY: &str = "onvif_devices";

fn is_stop(ptz: Option<(f64, f64, f64)>) -> bool {
    if let Some(ptz) = ptz {
        let eps = 1e-10;
        ptz.0.abs() < eps && ptz.1.abs() < eps && ptz.2.abs() < eps
    } else {
        false
    }
}

/// Parse HTTP response
async fn parse_response(mut res: Response<Incoming>) -> Result<Vec<u8>> {
    let status = res.status();

    let mut body = Vec::<u8>::new();
    while let Some(next) = res.frame().await {
        let frame = next?;
        if let Some(chunk) = frame.data_ref() {
            body.extend(chunk);
        }
    }

    if !status.is_success() {
        let body_str = String::from_utf8(body.clone())
            .unwrap_or("Could not parse string from response body".to_owned());
        log::debug!("Not successful: {status}. Body: {body_str}");
    }

    Ok(body)
}

struct XmlWriter<W> {
    writer: EventWriter<W>,
}

impl<W: io::Write> XmlWriter<W> {
    fn new(writer: EventWriter<W>) -> Self {
        XmlWriter { writer }
    }

    fn write(&mut self, event: XmlEvent) {
        if let Err(e) = self.writer.write(event) {
            panic!("Write error: {e}")
        }
    }

    fn start_element(&mut self, name: &str, attr: &[(&str, &str)]) {
        self.start_element_ns(name, ("", ""), attr);
    }

    fn start_element_default_ns(
        &mut self,
        name: &str,
        ns: &str,
        attr: &[(&str, &str)],
    ) {
        let mut event = XmlEvent::start_element(name);
        event = event.default_ns(ns);
        for (a, v) in attr {
            event = event.attr(*a, v);
        }
        self.write(event.into());
    }

    fn start_element_ns(
        &mut self,
        name: &str,
        ns: (&str, &str),
        attr: &[(&str, &str)],
    ) {
        let mut event = XmlEvent::start_element(name);
        event = event.ns(ns.0, ns.1);
        for (a, v) in attr {
            event = event.attr(*a, v);
        }
        self.write(event.into());
    }

    fn start_body(&mut self) {
        self.write(
            XmlEvent::start_element("s:Body")
                .ns("xsd", XSD)
                .ns("xsi", XSI)
                .into(),
        );
    }

    fn end_element(&mut self, name: Option<&str>) {
        let mut event = XmlEvent::end_element();
        if let Some(name) = name {
            event = event.name(name);
        }
        self.write(event.into());
    }

    fn finish(&mut self) {
        // loop until no more elements to end
        loop {
            let event = XmlEvent::end_element();
            match self.writer.write(event) {
                Ok(_) => (),
                Err(_) => return,
            }
        }
    }

    fn characters(&mut self, chars: &str) {
        let event = XmlEvent::Characters(chars);
        self.write(event);
    }

    fn single_element(
        &mut self,
        name: &str,
        chars: &str,
        attr: &[(&str, &str)],
    ) {
        self.single_element_ns(name, ("", ""), chars, attr);
    }

    fn single_element_default_ns(
        &mut self,
        name: &str,
        ns: &str,
        chars: &str,
        attr: &[(&str, &str)],
    ) {
        self.start_element_default_ns(name, ns, attr);
        self.characters(chars);
        self.end_element(Some(name));
    }

    fn single_element_ns(
        &mut self,
        name: &str,
        ns: (&str, &str),
        chars: &str,
        attr: &[(&str, &str)],
    ) {
        self.start_element_ns(name, ns, attr);
        self.characters(chars);
        self.end_element(Some(name));
    }
}

fn get_system_date_and_time_document<W: io::Write>(writer: &mut XmlWriter<W>) {
    writer.single_element_ns(
        "wsdl:GetSystemDateAndTime",
        ("wsdl", DEVICE_WSDL),
        "",
        &[],
    );
    writer.finish();
}

fn get_profiles_document<W: io::Write>(writer: &mut XmlWriter<W>) {
    writer.single_element_default_ns("GetProfiles", MEDIA_WSDL, "", &[]);
    writer.finish();
}

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

fn stop_document<W: io::Write>(writer: &mut XmlWriter<W>, profile: String) {
    writer.start_element_default_ns("Stop", PTZ_WSDL, &[]);
    writer.single_element("ProfileToken", &profile, &[]);
    writer.finish();
}

/// Category for GetCapabilities
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

/// ONVIF message
#[derive(Debug)]
pub enum OnvifOperation {
    ContinuousMove(String, f64, f64, f64),
    ContinuousMoveResponse,
    GetCapabilities(&'static [Category]),
    GetCapabilitiesResponse(HashMap<String, String>),
    GetProfiles,
    GetProfilesResponse(HashMap<String, String>),
    #[allow(dead_code)]
    GetSystemDateAndTime,
    GetSystemDateAndTimeResponse(HashMap<String, String>),
    #[allow(dead_code)]
    Stop(String),
    StopResponse,
    OtherResponse(String, String),
    Fault(String),
}

impl fmt::Display for OnvifOperation {
    /// "Display" format will be the corresponding SOAP action, the contained message, or empty
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
            OnvifOperation::OtherResponse(ns, res) => &format!("{ns}: {res}"),
            OnvifOperation::Fault(res) => res,
            _ => "",
        };
        if !action.is_empty() {
            write!(f, "{action}")
        } else {
            Err(fmt::Error)
        }
    }
}

impl OnvifOperation {
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

    fn get_map(&mut self) -> Option<&mut HashMap<String, String>> {
        match self {
            Self::GetSystemDateAndTimeResponse(map)
            | Self::GetCapabilitiesResponse(map)
            | Self::GetProfilesResponse(map) => Some(map),
            _ => None,
        }
    }

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

    /// Returns (password, nonce, created)
    fn get_digest(pass: &str) -> Option<(String, String, String)> {
        if pass.is_empty() {
            log::debug!("Password is empty, not creating digest.");
            return None;
        }
        let mut nonce_bytes = [0u8; 16];
        rand::fill(&mut nonce_bytes[..]);
        let pass_bytes = pass.as_bytes();
        let mut hasher = Sha1::new();
        let created = Utc::now().to_rfc3339_opts(SecondsFormat::Millis, true);
        hasher.update(nonce_bytes);
        hasher.update(created.as_bytes());
        hasher.update(pass_bytes);
        let digest = hasher.finalize();
        let digest_b64 = b64.encode(digest);
        Some((digest_b64, b64.encode(nonce_bytes), created))
    }

    /// Writes a WSSE security header
    fn add_security_header<W: io::Write>(
        writer: &mut XmlWriter<W>,
        user: &str,
        pass: &str,
    ) {
        if let Some((password, nonce, created)) =
            Self::get_digest(pass).as_ref()
        {
            writer.start_element_default_ns(
                "Security",
                WSSE,
                &[("s:mustUnderstand", "1")],
            );
            writer.start_element("UsernameToken", &[]);
            writer.single_element("Username", user, &[]);
            writer.single_element("Password", password, &[
                ("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest")
            ]);
            writer.single_element("Nonce", nonce, &[
                ("EncodingType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary")
            ]);
            writer.single_element_default_ns("Created", WSU, created, &[]);
            // Don't call .finish(), since we need to write the Body
            writer.end_element(Some("UsernameToken"));
            writer.end_element(Some("Security"));
        }
    }

    fn get_base_document<W: io::Write>(
        writer: &mut XmlWriter<W>,
        userpass: Option<(&str, &str)>,
    ) {
        writer.start_element_ns("s:Envelope", ("s", SOAP), &[]);
        writer.start_element("s:Header", &[]);
        if let Some((user, pass)) = userpass {
            Self::add_security_header(writer, user, pass);
        } else {
            log::debug!("No security headers added for {userpass:?}");
        }
        writer.end_element(Some("s:Header"));
        writer.start_body();
    }

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
                .create_writer(buf), //.clone())
        );
        Self::get_base_document(&mut xml_writer, userpass);
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

/// Implement FromStr to allow parsing SOAP Body into a map
impl std::str::FromStr for OnvifOperation {
    type Err = Error;
    fn from_str(s: &str) -> Result<Self> {
        let mut msg = Self::OtherResponse("".to_owned(), "".to_owned());

        let reader = EventReader::new(s.as_bytes());
        let mut current_path = String::new();
        let mut in_body = false;
        for event in reader {
            match event {
                Ok(ReaderEvent::StartElement {
                    name,
                    attributes,
                    namespace,
                }) => {
                    let mut name = name.local_name.clone();
                    if name.contains("Fault") {
                        return Ok(Self::Fault(s.to_owned()));
                    }
                    if !in_body {
                        if name == "Body" {
                            in_body = true;
                        }
                        continue;
                    }

                    if name.contains("Response") {
                        msg = match name.as_ref() {
                            "ContinuousMoveResponse" => {
                                Self::ContinuousMoveResponse
                            }
                            "GetCapabilitiesResponse" => {
                                Self::GetCapabilitiesResponse(HashMap::new())
                            }
                            "GetProfilesResponse" => {
                                Self::GetProfilesResponse(HashMap::new())
                            }
                            "GetSystemDateAndTimeResponse" => {
                                Self::GetSystemDateAndTimeResponse(
                                    HashMap::new(),
                                )
                            }
                            "StopResponse" => Self::StopResponse,
                            _ => {
                                return Ok(Self::OtherResponse(
                                    format!("{namespace:?}:{name}"),
                                    s.to_owned(),
                                ));
                            }
                        };
                    }

                    if let Self::GetProfilesResponse(_) = msg {
                        // Add token to name if it has one
                        for attr in attributes {
                            if attr.name.local_name.to_lowercase() == "token" {
                                name.push_str(&format!("[{}]", attr.value));
                                break;
                            }
                        }
                    }

                    // NOTE: top-level element has a leading '.' by design
                    current_path.push_str(&format!(".{name}"));
                }
                Ok(ReaderEvent::EndElement { name }) => {
                    let name = name.local_name.clone();
                    if in_body {
                        if name == "Body" {
                            in_body = false;
                            continue;
                        }
                    } else {
                        continue;
                    }

                    // 'Pop' element name from current path
                    // Removes children if not last in path
                    // Gives error if not found
                    if let Some((path, _)) =
                        current_path.rsplit_once(&format!(".{name}"))
                    {
                        current_path = path.to_owned();
                    } else {
                        // Never reached if well-formed XML and parsed correctly
                        log::error!(
                            "{name} not the last element! Current path: {current_path}"
                        );
                    }
                }
                Ok(ReaderEvent::Characters(text)) => {
                    if in_body && let Some(map) = msg.get_map() {
                        map.insert(current_path.clone(), text);
                    }
                }
                // StartDocument, EndDocument, ProcessingInstruction, CData, Comment, Whitespace, Doctype:
                Ok(_) => (),
                Err(e) => {
                    log::error!("Reader event error: {e:?}");
                }
            }
        }

        Ok(msg)
    }
}

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
    fn get_default_xaddr(&self, service: &str) -> String {
        match service {
            "Imaging" => "/onvif/imaging_service",
            "Media" => "/onvif/media_service",
            "PTZ" => "/onvif/ptz_service",
            _ => "/onvif/device_service",
        }
        .to_owned()
    }

    fn has_media_profile(&self) -> bool {
        !self.media_profile.is_empty()
    }

    fn has_xaddr(&self, service: &str) -> bool {
        self.xaddrs.contains_key(service)
    }

    fn get_xaddr(&self, service: &str) -> String {
        self.xaddrs
            .get(service)
            .unwrap_or(&self.get_default_xaddr(service))
            .to_owned()
    }
}

/// Messenger to an ONVIF-conformant device
#[derive(Debug)]
pub struct OnvifMessenger<'a> {
    /// Hostname
    host: String,
    /// Hostname
    user: String,
    /// Hostname
    pass: String,
    ///// TLS encrypted stream
    //stream: TlsStream<TcpStream>,
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
        //let addr = (host, port)
        //    .to_socket_addrs()?
        //    .next()
        //    .ok_or_else(|| Error::NotFound)?;
        //let tcp_stream = TcpStream::connect(&addr).await?;
        //let connector = tls::connector();
        //let domain = ServerName::try_from(host)
        //    .map_err(|_| {
        //        tokio::io::Error::new(io::ErrorKind::InvalidValue, "invalid dnsname")
        //    })?
        //    .to_owned();
        //let tls_stream = connector.connect(domain, tcp_stream).await?;
        Ok(OnvifMessenger {
            host: host.to_owned(),
            user: user.to_owned(),
            pass: pass.to_owned(),
            //stream: tls_stream,
            session,
        })
    }

    async fn get_and_store_media_profile(
        &mut self,
        msg: &mut OnvifOperation,
    ) -> Result<()> {
        let mut state = self.get_device_state().await;

        // If the stored state already has the profile, don't request one
        if !state.has_media_profile() {
            log::debug!("Doesn't have media profile already, pulling...");
            let profile = self.pull_media_profile().await?;
            state.media_profile = profile.clone();
            msg.set_profile(profile);
            self.set_device_state(state.clone()).await;
        }

        Ok(())
    }

    async fn pull_media_profile(&mut self) -> Result<String> {
        let msg = OnvifOperation::GetProfiles;
        let res_bytes = self.send_message(msg).await?;

        let body = &String::from_utf8(res_bytes)
            .map_err(|_| Error::UnexpectedResponse)?;
        let capabilities: OnvifOperation = body.parse()?;

        if let OnvifOperation::GetProfilesResponse(mut map) = capabilities {
            // Retain only profiles with PTZ configurations, then use first
            map.retain(|k, _| k.contains("PTZConfiguration"));

            let mut tokens: Vec<String> = map
                .into_keys()
                .map(|k| {
                    if let Some((_, rest)) = k.split_once("Profiles[")
                        && let Some((token, _)) = rest.split_once("]")
                    {
                        return token.to_owned();
                    }
                    "".to_owned()
                })
                .collect();
            tokens.sort();
            return Ok(tokens[0].clone());
        }

        Err(Error::InvalidValue)
    }

    /// Update the session with the host's pending status
    pub async fn update_pending(&self, pending: bool) {
        let mut state = self.get_device_state().await;

        state.pending = pending;

        self.set_device_state(state.clone()).await;
    }

    pub fn has_user_pass(&self) -> bool {
        !self.user.is_empty() || !self.pass.is_empty()
    }

    pub async fn set_user_pass(&mut self, user: &str, pass: &str) {
        let mut state = self.get_device_state().await;

        self.user = user.to_owned();
        self.pass = pass.to_owned();
        state.user = self.user.clone();
        state.pass = self.pass.clone();

        self.set_device_state(state.clone()).await;
    }

    pub async fn set_user_pass_from_session(&mut self) -> bool {
        let state = self.get_device_state().await;

        self.user = state.user.clone();
        self.pass = state.pass.clone();

        self.set_device_state(state.clone()).await;

        self.has_user_pass()
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

    /// Should send to device if URI has no active request, or if now stopping PTZ
    pub async fn should_send(&self, ptz: Option<(f64, f64, f64)>) -> bool {
        if is_stop(ptz) {
            // Always send stop operations
            return true;
        }

        // If not pending response, should send
        !self.is_pending().await
    }

    /// Gets the path from the session store, or a default
    /// Does not update the session store
    async fn message_path(&mut self, msg: &OnvifOperation) -> String {
        let state = self.get_device_state().await;
        state.get_xaddr(msg.get_service())
    }

    async fn pull_device_xaddrs(&mut self) -> Result<HashMap<String, String>> {
        let msg =
            OnvifOperation::GetCapabilities(&[Category::Ptz, Category::Media]);
        let res_bytes = self.send_message(msg).await?;

        let body = &String::from_utf8(res_bytes)
            .map_err(|_| Error::UnexpectedResponse)?;
        let capabilities: OnvifOperation = body.parse()?;

        if let OnvifOperation::GetCapabilitiesResponse(mut map) = capabilities {
            // Keep just the XAddr mappings, map to services as keys
            map.retain(|k, _| k.to_lowercase().ends_with("xaddr"));
            return Ok(map
                .into_iter()
                .map(|(key, value)| {
                    // (GetCapabilitiesResponse.Capabilities.[...].PTZ, XAddr)
                    let prefix = key.rsplit_once('.').unwrap_or((&key, &key)).0;
                    // (GetCapabilitiesResponse.Capabilities.[...], PTZ)
                    let service = prefix
                        .rsplit_once('.')
                        .unwrap_or((prefix, prefix))
                        .1
                        .to_string();
                    (service, value)
                })
                .collect());
        }
        Err(Error::InvalidValue)
    }

    /// Populate ONVIF service entrypoints if needed
    async fn get_and_store_xaddrs(
        &mut self,
        msg: &OnvifOperation,
    ) -> Result<()> {
        let mut state = self.get_device_state().await;

        // If the stored state already has the xaddr, don't request one
        if !state.has_xaddr(msg.get_service()) {
            log::debug!("Doesn't have xaddr already, pulling...");
            let addrs = self.pull_device_xaddrs().await?;
            state.xaddrs = addrs;
            self.set_device_state(state.clone()).await;
        }

        Ok(())
    }

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

    async fn set_device_state(&self, device: DeviceState) {
        let mut devices = self.get_devices().await;
        devices.insert(self.host.clone(), device);
        self.session
            .insert(DEVICE_STATES_KEY, devices)
            .await
            .expect("Couldn't insert DEVICE_STATES_KEY {DEVICE_STATES_KEY}");
        self.session.save().await.expect("Couldn't save session");
    }

    async fn send_message(&mut self, msg: OnvifOperation) -> Result<Vec<u8>> {
        let mut envelope = Vec::new();
        msg.encode(&mut envelope, Some((&self.user, &self.pass)));
        let envelope = match str::from_utf8(&envelope) {
            Ok(s) => s,
            Err(e) => {
                log::error!("Error {e:?} parsing envelope: {envelope:?}");
                ""
            }
        };

        let hostport = format!("{}:80", self.host);
        let stream = TcpStream::connect(&hostport).await?;
        let io = TokioIo::new(stream);
        let (mut sender, conn) =
            hyper::client::conn::http1::handshake(io).await?;

        // Don't await a conn_task, just handle in task itself
        tokio::spawn(async move {
            if let Err(err) = conn.await {
                log::error!("Connection failed: {:?}", err);
            }
        });

        let path = self.message_path(&msg).await;
        let mut builder = Request::post(path.to_owned())
            .header("Accept", "application/soap+xml")
            .header("Host", self.host.to_owned())
            .header("Accept-Encoding", "gzip, deflate")
            .header("Connection", "Close");
        let mut content_type =
            String::from("application/soap+xml; charset=utf-8");
        let action = msg.to_string();
        if !action.is_empty() {
            content_type.push_str(&format!("; action=\"{}\"", action));
            builder = builder.header("SOAPAction", action);
        }
        builder = builder.header("Content-Type", content_type);
        let req = builder.body(envelope.to_owned())?;

        log::debug!("Sending request: {req:#?}");

        let res = sender.send_request(req).await?;

        parse_response(res).await
    }

    async fn send_onvif(
        &mut self,
        mut msg: OnvifOperation,
    ) -> Result<OnvifOperation> {
        self.get_and_store_xaddrs(&msg).await?;
        self.get_and_store_media_profile(&mut msg).await?;
        let res_bytes = self.send_message(msg).await?;

        let body = &String::from_utf8(res_bytes)
            .map_err(|_| Error::UnexpectedResponse)?;
        log::debug!("response from device: {body:?}");

        let decoded = body.parse()?;
        log::debug!("decoded message from device: {decoded:#?}");

        Ok(decoded)
    }

    /// Send an ONVIF PTZ command to a camera
    pub async fn send_ptz(&mut self, ptz: (f64, f64, f64)) -> Result<()> {
        let profile = self.get_device_state().await.media_profile;

        // Round near-stop to exactly 0.0
        let op = if is_stop(Some(ptz)) {
            // NOTE: some cameras send a StopResponse but still don't stop
            // Most compatible option is just PTZ (0, 0, 0)
            OnvifOperation::ContinuousMove(profile, 0.0, 0.0, 0.0)
        } else {
            OnvifOperation::ContinuousMove(profile, ptz.0, ptz.1, ptz.2)
        };

        self.send_onvif(op).await?;

        Ok(())
    }

    pub async fn _get_datetime(&mut self) -> Result<OnvifOperation> {
        self.send_onvif(OnvifOperation::GetSystemDateAndTime).await
    }
}
