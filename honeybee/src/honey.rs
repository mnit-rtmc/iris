// honey.rs
//
// Copyright (C) 2021-2025  Minnesota Department of Transportation
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
use crate::Database;
use crate::access::Access;
use crate::cred::Credentials;
use crate::domain;
use crate::error::{Error, Result};
use crate::event::{self, EventTp};
use crate::permission;
use crate::query;
use crate::sonar::{Messenger, Name};
use crate::xff::XForwardedFor;
use axum::Router;
use axum::body::Body;
use axum::extract::{ConnectInfo, Json, Path as AxumPath, Query, State};
use axum::http::{StatusCode, header};
use axum::response::sse::{Event, KeepAlive};
use axum::response::{IntoResponse, Sse};
use axum::routing::get;
use axum_extra::TypedHeader;
use headers::{ETag, IfNoneMatch};
use http::header::HeaderName;
use resources::Res;
use serde::Deserialize;
use serde_json::Value;
use serde_json::map::Map;
use std::collections::HashMap;
use std::convert::Infallible;
use std::net::SocketAddr;
use std::sync::{Arc, Mutex};
use std::time::SystemTime;
use tokio::fs::metadata;
use tokio::sync::mpsc::{UnboundedSender, unbounded_channel};
use tokio_postgres::types::ToSql;
use tokio_stream::Stream;
use tokio_stream::wrappers::UnboundedReceiverStream;
use tokio_util::io::ReaderStream;
use tower_sessions::session::Id;
use tower_sessions::{Expiry, Session, SessionManagerLayer};
use tower_sessions_moka_store::MokaStore;

/// Sonar host name for IRIS
const SONAR_HOST: &str = "localhost.localdomain";

/// Expiration for notifier activity
const EXPIRATION: std::time::Duration =
    std::time::Duration::from_secs(4 * 3600);

/// SSE event result
type EventResult = std::result::Result<Event, Infallible>;

/// Client SSE notifier
struct SseNotifier {
    /// Listening channel names
    channels: Vec<Name>,
    /// Sse stream sender
    tx: Option<UnboundedSender<EventResult>>,
    /// Previous activity time
    activity: SystemTime,
}

/// Honeybee server state
#[derive(Clone)]
pub struct Honey {
    /// Debug mode (bypass sonar communication)
    debug: bool,
    /// Database connection pool
    db: Database,
    /// Sse notifiers for each session
    notifiers: Arc<Mutex<HashMap<Id, SseNotifier>>>,
}

/// No-header response result
type Resp0 = std::result::Result<StatusCode, StatusCode>;

/// Two-header response result
type Resp2 =
    std::result::Result<([(HeaderName, &'static str); 2], Body), StatusCode>;

/// Three-header response result
type Resp3 = std::result::Result<([(HeaderName, String); 3], Body), StatusCode>;

/// Create an HTML response
fn html_resp(html: &'static str) -> Resp2 {
    Ok((
        [
            (header::CACHE_CONTROL, "private, no-store"),
            (header::CONTENT_TYPE, "text/html"),
        ],
        Body::from(html),
    ))
}

/// Create a JSON response
fn json_resp(json: String) -> Resp2 {
    Ok((
        [
            (header::CACHE_CONTROL, "private, no-store"),
            (header::CONTENT_TYPE, "application/json"),
        ],
        Body::from(json),
    ))
}

impl SseNotifier {
    /// Check if notifier is listening to a channel
    fn is_listening(&self, nm: &Name) -> bool {
        for chan in &self.channels {
            if nm.res_type == chan.res_type {
                match (nm.object_n(), chan.object_n()) {
                    (Some(nn), Some(cn)) => {
                        if nn == cn {
                            return true;
                        }
                    }
                    (None, _) => return true,
                    _ => (),
                }
            }
        }
        false
    }
}

impl Honey {
    /// Create honey state
    pub fn new(debug: bool, db: &Database) -> Self {
        let db = db.clone();
        let notifiers = Arc::new(Mutex::new(HashMap::new()));
        Honey {
            debug,
            db,
            notifiers,
        }
    }

    /// Authenticate with IRIS server
    pub async fn authenticate(
        &self,
        cred: Credentials,
    ) -> Result<Option<Messenger>> {
        if self.debug {
            Ok(None)
        } else {
            let mut msn = Messenger::new(SONAR_HOST, 1037).await?;
            msn.login(cred.user(), cred.password()).await?;
            Ok(Some(msn))
        }
    }

    /// Build root route
    pub fn route_root(&self) -> Router {
        Router::new()
            .merge(root_get())
            .nest("/iris", self.route_iris())
    }

    /// Build iris route
    fn route_iris(&self) -> Router {
        Router::new()
            .merge(index_get())
            .merge(public_dir_get())
            .merge(lut_dir_get())
            .merge(img_dir_get())
            .merge(gif_dir_get())
            .merge(tfon_dir_get())
            .nest("/api", self.route_api())
    }

    /// Build authenticated api route
    fn route_api(&self) -> Router {
        let store = MokaStore::new(Some(100));
        let session_layer = SessionManagerLayer::new(store)
            .with_name("honeybee")
            .with_expiry(Expiry::OnInactivity(time::Duration::hours(9)));
        Router::new()
            .merge(login_resource(self.clone()))
            .merge(access_get(self.clone()))
            .merge(notify_resource(self.clone()))
            .merge(other_resource(self.clone()))
            .merge(other_object(self.clone()))
            .layer(session_layer)
    }

    /// Lookup access for a name
    async fn name_access(
        &self,
        user: &str,
        name: &Name,
        access: Access,
    ) -> Result<Access> {
        log::debug!("name_access {user} {name}");
        let access_lvl = permission::name_access(&self.db, user, name).await?;
        let acc = Access::new(access_lvl).ok_or(Error::Forbidden)?;
        acc.check(access)?;
        Ok(acc)
    }

    /// Check that the user has view access to selected channels
    async fn check_view_channels(
        &self,
        session: &Session,
        channels: &[Name],
    ) -> Result<()> {
        let cred = Credentials::load(session).await?;
        let user = cred.user();
        for nm in channels {
            self.name_access(user, nm, Access::View).await?;
        }
        Ok(())
    }

    /// Store channel names for a session Id
    fn store_channels(&self, id: Id, names: Vec<Name>) {
        let mut map = self.notifiers.lock().unwrap();
        match map.get_mut(&id) {
            Some(notifier) => {
                notifier.channels = names;
            }
            None => {
                let notifier = SseNotifier {
                    channels: names,
                    tx: None,
                    activity: SystemTime::now(),
                };
                map.insert(id, notifier);
            }
        }
    }

    /// Store SSE sender for a session Id
    fn store_sender(&self, id: Id, tx: UnboundedSender<EventResult>) {
        let mut map = self.notifiers.lock().unwrap();
        log::debug!("Adding SSE sender for {id}");
        match map.get_mut(&id) {
            Some(notifier) => {
                if notifier.tx.is_some() {
                    log::info!("SSE sender exists {id}");
                }
                notifier.tx = Some(tx);
            }
            None => {
                let notifier = SseNotifier {
                    channels: Vec::new(),
                    tx: Some(tx),
                    activity: SystemTime::now(),
                };
                map.insert(id, notifier);
            }
        }
    }

    /// Notify all SSE listeners
    pub async fn notify_sse(&self, nm: Name) {
        log::debug!("Notify SSE {nm}");
        let mut map = self.notifiers.lock().unwrap();
        for (id, notifier) in map.iter_mut() {
            log::debug!("checking {nm} for {id}");
            if let Some(tx) = &notifier.tx {
                if notifier.is_listening(&nm) {
                    notifier.activity = SystemTime::now();
                    log::debug!("SSE notify: {nm} to {id}");
                    let ev = Event::default().data(nm.to_string());
                    if let Err(e) = tx.send(Ok(ev)) {
                        log::warn!("SSE notification: {e}");
                        notifier.tx = None;
                    }
                }
            }
        }
    }

    // Purge notifiers with no recent activity
    pub fn purge_expired(&self) {
        log::debug!("purge_expired");
        let now = SystemTime::now();
        let mut map = self.notifiers.lock().unwrap();
        map.retain(|_id, notifier| {
            match now.duration_since(notifier.activity) {
                Ok(dur) => dur < EXPIRATION,
                Err(_e) => false,
            }
        });
    }
}

/// Build a stream from a file (with max-age 1 day)
async fn file_stream(fname: &str, content_type: &'static str) -> Resp2 {
    let file = tokio::fs::File::open(fname)
        .await
        .map_err(|_e| StatusCode::NOT_FOUND)?;
    let stream = ReaderStream::new(file);
    Ok((
        [
            (header::CACHE_CONTROL, "max-age=86400"),
            (header::CONTENT_TYPE, content_type),
        ],
        Body::from_stream(stream),
    ))
}

/// Build a stream from a file (and calculate ETag)
async fn file_stream_etag(
    fname: &str,
    content_type: &'static str,
    if_none_match: IfNoneMatch,
) -> Resp3 {
    let etag = file_etag(fname).await.map_err(|_e| Error::NotFound)?;
    log::trace!("ETag: {etag} ({fname})");
    let tag = etag.parse::<ETag>().map_err(|_e| Error::InvalidETag)?;
    if if_none_match.precondition_passes(&tag) {
        log::trace!("opening {fname}");
        let file = tokio::fs::File::open(fname)
            .await
            .map_err(|_e| StatusCode::NOT_FOUND)?;
        let stream = ReaderStream::new(file);
        Ok((
            [
                (header::ETAG, etag),
                (header::CACHE_CONTROL, "no-cache".to_string()),
                (header::CONTENT_TYPE, content_type.to_string()),
            ],
            Body::from_stream(stream),
        ))
    } else {
        Err(StatusCode::NOT_MODIFIED)
    }
}

/// Get a static file ETag
async fn file_etag(path: &str) -> Result<String> {
    let meta = metadata(path).await?;
    let modified = meta.modified()?;
    let dur = modified.duration_since(SystemTime::UNIX_EPOCH)?.as_millis();
    Ok(format!("\"{dur:x}\""))
}

/// Handler for index page
async fn index_handler(
    TypedHeader(if_none_match): TypedHeader<IfNoneMatch>,
) -> impl IntoResponse {
    file_stream_etag("index.html", "text/html; charset=utf-8", if_none_match)
        .await
}

/// Build route for index html
fn root_get() -> Router {
    Router::new().route("/iris/", get(index_handler))
}

/// Build route for index html
fn index_get() -> Router {
    Router::new().route("/index.html", get(index_handler))
}

/// `GET` JSON file from public directory
fn public_dir_get() -> Router {
    async fn handler(
        TypedHeader(if_none_match): TypedHeader<IfNoneMatch>,
        AxumPath(fname): AxumPath<String>,
    ) -> impl IntoResponse {
        log::info!("GET {fname}");
        file_stream_etag(&fname, "application/json", if_none_match).await
    }
    Router::new().route("/{fname}", get(handler))
}

/// `GET` JSON file from LUT directory
fn lut_dir_get() -> Router {
    async fn handler(AxumPath(fname): AxumPath<String>) -> impl IntoResponse {
        let fname = format!("lut/{fname}");
        log::info!("GET {fname}");
        file_stream(&fname, "application/json").await
    }
    Router::new().route("/lut/{fname}", get(handler))
}

/// `GET` file from sign img directory
fn img_dir_get() -> Router {
    async fn handler(AxumPath(fname): AxumPath<String>) -> impl IntoResponse {
        let fname = format!("img/{fname}");
        log::info!("GET {fname}");
        file_stream(&fname, "image/gif").await
    }
    Router::new().route("/img/{fname}", get(handler))
}

/// `GET` file from tfon directory
fn tfon_dir_get() -> Router {
    async fn handler(AxumPath(fname): AxumPath<String>) -> impl IntoResponse {
        let fname = format!("tfon/{fname}");
        log::info!("GET {fname}");
        file_stream(&fname, "text/plain").await
    }
    Router::new().route("/tfon/{fname}", get(handler))
}

/// `GET` file from gif directory
fn gif_dir_get() -> Router {
    async fn handler(AxumPath(fname): AxumPath<String>) -> impl IntoResponse {
        let fname = format!("gif/{fname}");
        log::info!("GET {fname}");
        file_stream(&fname, "image/gif").await
    }
    Router::new().route("/gif/{fname}", get(handler))
}

/// Router for login resource
fn login_resource(honey: Honey) -> Router {
    /// Handle `GET` request
    async fn handle_get(session: Session) -> impl IntoResponse {
        log::info!("GET login");
        let cred = Credentials::load(&session).await?;
        let mut resp = String::new();
        resp.push('"');
        resp.push_str(cred.user());
        resp.push('"');
        json_resp(resp)
    }

    /// Handle `POST` request
    async fn handle_post(
        session: Session,
        ConnectInfo(addr): ConnectInfo<SocketAddr>,
        XForwardedFor(xff): XForwardedFor,
        State(honey): State<Honey>,
        Json(cred): Json<Credentials>,
    ) -> impl IntoResponse {
        log::info!("POST login from {addr}");
        session
            .cycle_id()
            .await
            .map_err(|_e| StatusCode::INTERNAL_SERVER_ERROR)?;
        honey.authenticate(cred.clone()).await?;
        let user = cred.user();
        let domains = domain::query_by_user(&honey.db, user).await?;
        match domain::any_contains(&domains, addr.ip()) {
            Ok(true) => (),
            _ => {
                event::insert_client(
                    &honey.db,
                    EventTp::FailDomain,
                    &addr.to_string(),
                    user,
                )
                .await?;
                return Err(Error::Forbidden)?;
            }
        }
        for addr in xff {
            match domain::any_contains(&domains, addr) {
                Ok(true) => (),
                _ => {
                    event::insert_client(
                        &honey.db,
                        EventTp::FailDomainXff,
                        &addr.to_string(),
                        user,
                    )
                    .await?;
                    return Err(Error::Forbidden)?;
                }
            }
        }
        cred.store(&session)
            .await
            .map_err(|_e| StatusCode::INTERNAL_SERVER_ERROR)?;
        html_resp("<html>Authenticated</html>")
    }

    Router::new()
        .route("/login", get(handle_get).post(handle_post))
        .with_state(honey)
}

/// `GET` access permissions
fn access_get(honey: Honey) -> Router {
    async fn handler(
        session: Session,
        State(honey): State<Honey>,
    ) -> impl IntoResponse {
        log::info!("GET access");
        let cred = Credentials::load(&session).await?;
        let perms = permission::get_by_user(&honey.db, cred.user()).await?;
        match serde_json::to_value(perms) {
            Ok(body) => json_resp(body.to_string()),
            Err(_e) => Err(StatusCode::BAD_REQUEST),
        }
    }
    Router::new()
        .route("/access", get(handler))
        .with_state(honey)
}

/// Try to make a sonar names from notify channels
fn try_names_from_channels(channels: &[String]) -> Result<Vec<Name>> {
    if channels.len() > 32 {
        log::info!("Too many notification channels");
        Err(Error::InvalidValue)?;
    }
    let mut names = Vec::with_capacity(channels.len());
    for chan in channels {
        let nm = if let Some((type_n, obj_n)) = chan.split_once('$') {
            Name::new(type_n)?.obj(obj_n)?
        } else {
            Name::new(chan)?
        };
        names.push(nm);
    }
    Ok(names)
}

/// Router for notify resource
fn notify_resource(honey: Honey) -> Router {
    /// Handle `GET` request (sse)
    async fn handle_get(
        session: Session,
        State(honey): State<Honey>,
    ) -> Sse<impl Stream<Item = EventResult>> {
        log::info!("GET notify");
        let id = session.id().unwrap_or_default();
        let (tx, rx) = unbounded_channel();
        honey.store_sender(id, tx);
        let stream = UnboundedReceiverStream::new(rx);
        Sse::new(stream).keep_alive(
            KeepAlive::new()
                .interval(std::time::Duration::from_secs(60))
                .text("keep alive"),
        )
    }

    /// Handle `POST` request
    async fn handle_post(
        session: Session,
        State(honey): State<Honey>,
        Json(channels): Json<Vec<String>>,
    ) -> impl IntoResponse {
        log::info!("POST notify");
        let names = try_names_from_channels(&channels)?;
        honey.check_view_channels(&session, &names).await?;
        let id = session.id().unwrap_or_default();
        honey.store_channels(id, names);
        html_resp("<html>Ok</html>")
    }

    Router::new()
        .route("/notify", get(handle_get).post(handle_post))
        .with_state(honey)
}

/// Router for permission resource (REMOVE?)
#[allow(dead_code)]
fn permission_resource(honey: Honey) -> Router {
    /// Handle `GET` request
    async fn handle_get(
        session: Session,
        State(honey): State<Honey>,
        TypedHeader(if_none_match): TypedHeader<IfNoneMatch>,
    ) -> impl IntoResponse {
        log::info!("GET api/permission");
        let nm = Name::from(Res::Permission);
        let cred = Credentials::load(&session).await?;
        honey.name_access(cred.user(), &nm, Access::View).await?;
        let fname = format!("api/{nm}");
        file_stream_etag(&fname, "application/json", if_none_match).await
    }

    /// Handle `POST` request
    async fn handle_post(
        session: Session,
        State(honey): State<Honey>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::from(Res::Permission);
        log::info!("POST {nm}");
        let cred = Credentials::load(&session).await?;
        honey
            .name_access(cred.user(), &nm, Access::Configure)
            .await?;
        let role = attrs.get("role");
        let base_resource = attrs.get("base_resource");
        if let (Some(Value::String(role)), Some(Value::String(base_resource))) =
            (role, base_resource)
        {
            let role = role.to_string();
            let base_resource = base_resource.to_string();
            permission::post_role_res(&honey.db, &role, &base_resource).await?;
            return Ok(StatusCode::CREATED);
        }
        Err(Error::InvalidValue)?
    }

    Router::new()
        .route("/permission", get(handle_get).post(handle_post))
        .with_state(honey)
}

/// Router for other resource
fn other_resource(honey: Honey) -> Router {
    /// Handle `GET` request
    async fn handle_get(
        session: Session,
        State(honey): State<Honey>,
        TypedHeader(if_none_match): TypedHeader<IfNoneMatch>,
        AxumPath(type_n): AxumPath<String>,
    ) -> impl IntoResponse {
        log::info!("GET api/{type_n}");
        let nm = Name::new(&type_n)?;
        let cred = Credentials::load(&session).await?;
        honey.name_access(cred.user(), &nm, Access::View).await?;
        let fname = format!("api/{type_n}");
        file_stream_etag(&fname, "application/json", if_none_match).await
    }

    /// Handle `POST` request
    async fn handle_post(
        session: Session,
        AxumPath(type_n): AxumPath<String>,
        State(honey): State<Honey>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::new(&type_n)?;
        log::info!("POST {nm}");
        let cred = Credentials::load(&session).await?;
        let required = Access::required_post(nm.res_type);
        honey.name_access(cred.user(), &nm, required).await?;
        match attrs.get("name") {
            Some(Value::String(name)) => {
                let name = nm.obj(name)?;
                if let Some(mut msn) = honey.authenticate(cred).await? {
                    // first, set attributes on phantom object
                    for (key, value) in attrs.iter() {
                        let attr = &key[..];
                        if attr != "name" {
                            let anm = name.attr_n(attr)?;
                            log::debug!("{anm} = {value} (phantom)");
                            msn.update_object(&anm, value).await?;
                        }
                    }
                    log::debug!("creating {name}");
                    msn.create_object(&name.to_string()).await?;
                }
                Ok(StatusCode::CREATED)
            }
            _ => Err(Error::InvalidValue)?,
        }
    }

    Router::new()
        .route("/{type_n}", get(handle_get).post(handle_post))
        .with_state(honey)
}

/// Router for permission object (REMOVE?)
#[allow(dead_code)]
fn permission_object(honey: Honey) -> Router {
    /// Handle `GET` request
    async fn handle_get(
        session: Session,
        State(honey): State<Honey>,
        AxumPath(obj_n): AxumPath<String>,
    ) -> impl IntoResponse {
        let nm = Name::from(Res::Permission).obj(&obj_n)?;
        log::info!("GET {nm}");
        let cred = Credentials::load(&session).await?;
        honey.name_access(cred.user(), &nm, Access::View).await?;
        let perm = permission::get_one(&honey.db, &obj_n).await?;
        match serde_json::to_value(perm) {
            Ok(body) => json_resp(body.to_string()),
            Err(_e) => Err(StatusCode::BAD_REQUEST),
        }
    }

    /// Handle `PATCH` request
    async fn handle_patch(
        session: Session,
        State(honey): State<Honey>,
        AxumPath(obj_n): AxumPath<String>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::from(Res::Permission).obj(&obj_n)?;
        log::info!("PATCH {nm}");
        let cred = Credentials::load(&session).await?;
        honey
            .name_access(cred.user(), &nm, Access::Configure)
            .await?;
        permission::patch_by_name(&honey.db, &obj_n, attrs).await?;
        Ok(StatusCode::NO_CONTENT)
    }

    /// Handle `DELETE` request
    async fn handle_delete(
        session: Session,
        State(honey): State<Honey>,
        AxumPath(obj_n): AxumPath<String>,
    ) -> Resp0 {
        let nm = Name::from(Res::Permission).obj(&obj_n)?;
        log::info!("DELETE {nm}");
        let cred = Credentials::load(&session).await?;
        honey
            .name_access(cred.user(), &nm, Access::Configure)
            .await?;
        permission::delete_by_name(&honey.db, &obj_n).await?;
        Ok(StatusCode::NO_CONTENT)
    }

    Router::new()
        .route(
            "/permission/:obj_n",
            get(handle_get).patch(handle_patch).delete(handle_delete),
        )
        .with_state(honey)
}

/// Query parameters
#[derive(Debug, Deserialize)]
struct QueryParams {
    /// Associatied resource (for GeoLoc)
    res: Option<String>,
}

/// Get name to use for access checks
fn check_name(type_n: &str, obj_n: &str, params: &QueryParams) -> Result<Name> {
    let nm = Name::new(type_n)?;
    match (nm.res_type, &params.res) {
        // FIXME: check for DevicePreset
        (Res::GeoLoc, Some(res)) => {
            // Use "res" query parameter for GeoLoc access check
            Ok(Name::new(res)?.obj(obj_n)?)
        }
        (Res::GeoLoc, None) => Err(Error::InvalidValue),
        (_, Some(_r)) => Err(Error::InvalidValue),
        _ => Ok(nm.obj(obj_n)?),
    }
}

/// Router for other objects
fn other_object(honey: Honey) -> Router {
    /// Handle `GET` request
    async fn handle_get(
        session: Session,
        State(honey): State<Honey>,
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
        params: Query<QueryParams>,
    ) -> impl IntoResponse {
        log::info!("GET {type_n}/{obj_n} {params:?}");
        let nm = Name::new(&type_n)?.obj(&obj_n)?;
        let ck_nm = check_name(&type_n, &obj_n, &params.0)?;
        // get precent-decoded object name
        let obj_n = nm.object_n().ok_or(Error::InvalidValue)?;
        let sql = one_sql(nm.res_type);
        let cred = Credentials::load(&session).await?;
        honey.name_access(cred.user(), &ck_nm, Access::View).await?;
        let body = match nm.res_type {
            Res::GeoLoc => {
                get_by_pkey2(&honey.db, sql, &obj_n, ck_nm.res_type.as_str())
                    .await?
            }
            Res::ControllerIo => {
                get_array_by_pkey(&honey.db, sql, &obj_n).await?
            }
            _ => get_by_pkey(&honey.db, sql, &obj_n).await?,
        };
        json_resp(body)
    }

    /// Handle `PATCH` request
    async fn handle_patch(
        session: Session,
        State(honey): State<Honey>,
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
        params: Query<QueryParams>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        log::info!("PATCH {type_n}/{obj_n} {params:?}");
        let nm = Name::new(&type_n)?.obj(&obj_n)?;
        let ck_nm = check_name(&type_n, &obj_n, &params.0)?;
        let cred = Credentials::load(&session).await?;
        // *At least* Operate access needed (further checks below)
        let access = honey
            .name_access(cred.user(), &ck_nm, Access::Operate)
            .await?;
        for key in attrs.keys() {
            let attr = &key[..];
            let required = Access::required_patch(ck_nm.res_type, attr);
            access.check(required)?;
        }
        if let Some(mut msn) = honey.authenticate(cred).await? {
            // first pass
            for (key, value) in attrs.iter() {
                let attr = &key[..];
                if patch_first_pass(nm.res_type, attr) {
                    let anm = nm.attr_n(attr)?;
                    log::debug!("{anm} = {value}");
                    msn.update_object(&anm, value).await?;
                }
            }
            // second pass
            for (key, value) in attrs.iter() {
                let attr = &key[..];
                if !patch_first_pass(nm.res_type, attr) {
                    let anm = nm.attr_n(attr)?;
                    log::debug!("{anm} = {value}");
                    msn.update_object(&anm, value).await?;
                }
            }
        }
        Ok(StatusCode::NO_CONTENT)
    }

    /// Handle `DELETE` request
    async fn handle_delete(
        session: Session,
        State(honey): State<Honey>,
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
    ) -> Resp0 {
        let nm = Name::new(&type_n)?.obj(&obj_n)?;
        log::info!("DELETE {nm}");
        let cred = Credentials::load(&session).await?;
        honey
            .name_access(cred.user(), &nm, Access::Configure)
            .await?;
        if let Some(mut msn) = honey.authenticate(cred).await? {
            msn.remove_object(&nm.to_string()).await?;
        }
        Ok(StatusCode::ACCEPTED)
    }

    Router::new()
        .route(
            "/{type_n}/{obj_n}",
            get(handle_get).patch(handle_patch).delete(handle_delete),
        )
        .with_state(honey)
}

/// Get the SQL query one record of a given resource type
const fn one_sql(res: Res) -> &'static str {
    use Res::*;
    match res {
        ActionPlan => query::ACTION_PLAN_ONE,
        Alarm => query::ALARM_ONE,
        Beacon => query::BEACON_ONE,
        CabinetStyle => query::CABINET_STYLE_ONE,
        Camera => query::CAMERA_ONE,
        CameraPreset => query::CAMERA_PRESET_ONE,
        CommConfig => query::COMM_CONFIG_ONE,
        CommLink => query::COMM_LINK_ONE,
        Controller => query::CONTROLLER_ONE,
        ControllerIo => query::CONTROLLER_IO_ONE,
        DayMatcher => query::DAY_MATCHER_ONE,
        DayPlan => query::DAY_PLAN_ONE,
        Detector => query::DETECTOR_ONE,
        DeviceAction => query::DEVICE_ACTION_ONE,
        Dms => query::DMS_ONE,
        Domain => query::DOMAIN_ONE,
        EncoderStream => query::ENCODER_STREAM_ONE,
        EncoderType => query::ENCODER_TYPE_ONE,
        FlowStream => query::FLOW_STREAM_ONE,
        Font => query::FONT_ONE,
        GateArm => query::GATE_ARM_ONE,
        GateArmArray => query::GATE_ARM_ARRAY_ONE,
        GeoLoc => query::GEO_LOC_ONE,
        Gps => query::GPS_ONE,
        Graphic => query::GRAPHIC_ONE,
        IncidentDetail => query::INCIDENT_DETAIL_ONE,
        IncAdvice => query::INC_ADVICE_ONE,
        IncDescriptor => query::INC_DESCRIPTOR_ONE,
        IncLocator => query::INC_LOCATOR_ONE,
        Lcs => query::LCS_ONE,
        LcsState => query::LCS_STATE_ONE,
        Modem => query::MODEM_ONE,
        MonitorStyle => query::MONITOR_STYLE_ONE,
        MsgLine => query::MSG_LINE_ONE,
        MsgPattern => query::MSG_PATTERN_ONE,
        Permission => query::PERMISSION_ONE,
        PlanPhase => query::PLAN_PHASE_ONE,
        PlayList => query::PLAY_LIST_ONE,
        RoadAffix => query::ROAD_AFFIX_ONE,
        RampMeter => query::RAMP_METER_ONE,
        Role => query::ROLE_ONE,
        SignConfig => query::SIGN_CONFIG_ONE,
        SignDetail => query::SIGN_DETAIL_ONE,
        SignMessage => query::SIGN_MSG_ONE,
        TagReader => query::TAG_READER_ONE,
        TimeAction => query::TIME_ACTION_ONE,
        TollZone => query::TOLL_ZONE_ONE,
        User => query::USER_ONE,
        VideoMonitor => query::VIDEO_MONITOR_ONE,
        WeatherSensor => query::WEATHER_SENSOR_ONE,
        Word => query::WORD_ONE,
        _ => unimplemented!(),
    }
}

/// Check if resource type / attribute should be patched first
fn patch_first_pass(res: Res, att: &str) -> bool {
    use Res::*;
    match (res, att) {
        (Alarm, "pin")
        | (Beacon, "pin")
        | (Beacon, "verify_pin")
        | (Detector, "pin")
        | (Dms, "pin")
        | (RampMeter, "pin")
        | (WeatherSensor, "pin") => true,
        _ => false,
    }
}

/// Query one row by primary key
async fn get_by_pkey<PK: ToSql + Sync>(
    db: &Database,
    sql: &'static str,
    pkey: PK,
) -> Result<String> {
    let client = db.client().await?;
    let query = format!("SELECT row_to_json(r)::text FROM ({sql}) r");
    let row = client
        .query_one(&query, &[&pkey])
        .await
        .map_err(|_e| Error::NotFound)?;
    Ok(row.get::<usize, String>(0))
}

/// Query one row by primary key, with an additional param
async fn get_by_pkey2<PK: ToSql + Sync, P2: ToSql + Sync>(
    db: &Database,
    sql: &'static str,
    pkey: PK,
    p2: P2,
) -> Result<String> {
    let client = db.client().await?;
    let query = format!("SELECT row_to_json(r)::text FROM ({sql}) r");
    let row = client
        .query_one(&query, &[&pkey, &p2])
        .await
        .map_err(|_e| Error::NotFound)?;
    Ok(row.get::<usize, String>(0))
}

/// Query rows as an array by primary key
async fn get_array_by_pkey<PK: ToSql + Sync>(
    db: &Database,
    sql: &'static str,
    pkey: PK,
) -> Result<String> {
    let client = db.client().await?;
    let query =
        format!("SELECT COALESCE(json_agg(r), '[]')::text FROM ({sql}) r");
    let row = client
        .query_one(&query, &[&pkey])
        .await
        .map_err(|_e| Error::NotFound)?;
    Ok(row.get::<usize, String>(0))
}
