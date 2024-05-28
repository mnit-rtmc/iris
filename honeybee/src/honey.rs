// honey.rs
//
// Copyright (C) 2021-2024  Minnesota Department of Transportation
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
use crate::access::Access;
use crate::cred::Credentials;
use crate::error::{Error, Result};
use crate::permission;
use crate::query;
use crate::sonar::{Error as SonarError, Messenger, Name};
use crate::Database;
use axum::body::Body;
use axum::extract::{Json, Path as AxumPath, State};
use axum::http::{header, StatusCode};
use axum::response::sse::{Event, KeepAlive};
use axum::response::Sse;
use axum::routing::get;
use axum::Router;
use axum_extra::TypedHeader;
use headers::{ETag, IfNoneMatch};
use http::header::HeaderName;
use resources::Res;
use serde_json::map::Map;
use serde_json::Value;
use std::collections::HashMap;
use std::convert::Infallible;
use std::sync::{Arc, Mutex};
use std::time::SystemTime;
use tokio::fs::metadata;
use tokio::sync::mpsc::{unbounded_channel, UnboundedSender};
use tokio_postgres::types::ToSql;
use tokio_stream::wrappers::UnboundedReceiverStream;
use tokio_stream::Stream;
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

/// Single-header response result
type Resp1 =
    std::result::Result<([(HeaderName, &'static str); 1], String), StatusCode>;

/// Two-header response result
type Resp2 =
    std::result::Result<([(HeaderName, &'static str); 2], String), StatusCode>;

/// Two-header response result
type Resp2b =
    std::result::Result<([(HeaderName, &'static str); 2], Body), StatusCode>;

/// Three-header response result
type Resp3 = std::result::Result<([(HeaderName, String); 3], Body), StatusCode>;

/// Create an HTML response
fn html_resp(html: &str) -> Resp1 {
    Ok(([(header::CONTENT_TYPE, "text/html")], html.to_string()))
}

/// Create a JSON response
fn json_resp(json: String) -> Resp2 {
    Ok((
        [
            (header::CACHE_CONTROL, "private, no-store"),
            (header::CONTENT_TYPE, "application/json"),
        ],
        json,
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
            .merge(login_post(self.clone()))
            .merge(access_get(self.clone()))
            .merge(notify_resource(self.clone()))
            .merge(permission_resource(self.clone()))
            .merge(other_resource(self.clone()))
            .merge(permission_object(self.clone()))
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
        let perm = permission::get_by_name(&self.db, user, name).await?;
        let acc = Access::new(perm.access_n).ok_or(Error::Unauthorized)?;
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
async fn file_stream(fname: &str, content_type: &'static str) -> Resp2b {
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
    let etag = file_etag(fname).await.map_err(|_e| SonarError::NotFound)?;
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
) -> Resp3 {
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
    ) -> Resp3 {
        log::info!("GET {fname}");
        file_stream_etag(&fname, "application/json", if_none_match).await
    }
    Router::new().route("/:fname", get(handler))
}

/// `GET` JSON file from LUT directory
fn lut_dir_get() -> Router {
    async fn handler(AxumPath(fname): AxumPath<String>) -> Resp2b {
        let fname = format!("lut/{fname}");
        log::info!("GET {fname}");
        file_stream(&fname, "application/json").await
    }
    Router::new().route("/lut/:fname", get(handler))
}

/// `GET` file from sign img directory
fn img_dir_get() -> Router {
    async fn handler(AxumPath(fname): AxumPath<String>) -> Resp2b {
        let fname = format!("img/{fname}");
        log::info!("GET {fname}");
        file_stream(&fname, "image/gif").await
    }
    Router::new().route("/img/:fname", get(handler))
}

/// `GET` file from tfon directory
fn tfon_dir_get() -> Router {
    async fn handler(AxumPath(fname): AxumPath<String>) -> Resp2b {
        let fname = format!("tfon/{fname}");
        log::info!("GET {fname}");
        file_stream(&fname, "text/plain").await
    }
    Router::new().route("/tfon/:fname", get(handler))
}

/// `GET` file from gif directory
fn gif_dir_get() -> Router {
    async fn handler(AxumPath(fname): AxumPath<String>) -> Resp2b {
        let fname = format!("gif/{fname}");
        log::info!("GET {fname}");
        file_stream(&fname, "image/gif").await
    }
    Router::new().route("/gif/:fname", get(handler))
}

/// Handle `POST` to login page
fn login_post(honey: Honey) -> Router {
    /// Handle `GET` request
    async fn handle_get(session: Session) -> Resp2 {
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
        State(honey): State<Honey>,
        Json(cred): Json<Credentials>,
    ) -> Resp1 {
        log::info!("POST login");
        session
            .cycle_id()
            .await
            .map_err(|_e| StatusCode::INTERNAL_SERVER_ERROR)?;
        honey.authenticate(cred.clone()).await?;
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
    async fn handler(session: Session, State(honey): State<Honey>) -> Resp2 {
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
        Err(SonarError::InvalidValue)?;
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
    ) -> Resp1 {
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

/// Router for permission resource
fn permission_resource(honey: Honey) -> Router {
    /// Handle `GET` request
    async fn handle_get(
        session: Session,
        State(honey): State<Honey>,
        TypedHeader(if_none_match): TypedHeader<IfNoneMatch>,
    ) -> Resp3 {
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
        let resource_n = attrs.get("resource_n");
        if let (Some(Value::String(role)), Some(Value::String(resource_n))) =
            (role, resource_n)
        {
            let role = role.to_string();
            let resource_n = resource_n.to_string();
            permission::post_role_res(&honey.db, &role, &resource_n).await?;
            return Ok(StatusCode::CREATED);
        }
        Err(SonarError::InvalidValue)?
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
    ) -> Resp3 {
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
            _ => Err(SonarError::InvalidValue)?,
        }
    }

    Router::new()
        .route("/:type_n", get(handle_get).post(handle_post))
        .with_state(honey)
}

/// Router for permission object
fn permission_object(honey: Honey) -> Router {
    /// Handle `GET` request
    async fn handle_get(
        session: Session,
        State(honey): State<Honey>,
        AxumPath(id): AxumPath<i32>,
    ) -> Resp2 {
        let nm = Name::from(Res::Permission).obj(&id.to_string())?;
        log::info!("GET {nm}");
        let cred = Credentials::load(&session).await?;
        honey.name_access(cred.user(), &nm, Access::View).await?;
        let perm = permission::get_by_id(&honey.db, id).await?;
        match serde_json::to_value(perm) {
            Ok(body) => json_resp(body.to_string()),
            Err(_e) => Err(StatusCode::BAD_REQUEST),
        }
    }

    /// Handle `PATCH` request
    async fn handle_patch(
        session: Session,
        State(honey): State<Honey>,
        AxumPath(id): AxumPath<i32>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::from(Res::Permission).obj(&id.to_string())?;
        log::info!("PATCH {nm}");
        let cred = Credentials::load(&session).await?;
        honey
            .name_access(cred.user(), &nm, Access::Configure)
            .await?;
        permission::patch_by_id(&honey.db, id, attrs).await?;
        Ok(StatusCode::NO_CONTENT)
    }

    /// Handle `DELETE` request
    async fn handle_delete(
        session: Session,
        State(honey): State<Honey>,
        AxumPath(id): AxumPath<i32>,
    ) -> Resp0 {
        let nm = Name::from(Res::Permission).obj(&id.to_string())?;
        log::info!("DELETE {nm}");
        let cred = Credentials::load(&session).await?;
        honey
            .name_access(cred.user(), &nm, Access::Configure)
            .await?;
        permission::delete_by_id(&honey.db, id).await?;
        Ok(StatusCode::NO_CONTENT)
    }

    Router::new()
        .route(
            "/permission/:id",
            get(handle_get).patch(handle_patch).delete(handle_delete),
        )
        .with_state(honey)
}

/// Router for other objects
fn other_object(honey: Honey) -> Router {
    /// Handle `GET` request
    async fn handle_get(
        session: Session,
        State(honey): State<Honey>,
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
    ) -> Resp2 {
        let nm = Name::new(&type_n)?.obj(&obj_n)?;
        log::info!("GET {nm}");
        let cred = Credentials::load(&session).await?;
        honey.name_access(cred.user(), &nm, Access::View).await?;
        let sql = one_sql(nm.res_type);
        let name = nm.object_n().ok_or(StatusCode::BAD_REQUEST)?;
        let body = if nm.res_type == Res::ControllerIo {
            get_array_by_pkey(&honey.db, sql, &name).await
        } else {
            get_by_pkey(&honey.db, sql, &name).await
        }?;
        json_resp(body)
    }

    /// Handle `PATCH` request
    async fn handle_patch(
        session: Session,
        State(honey): State<Honey>,
        AxumPath((type_n, obj_n)): AxumPath<(String, String)>,
        Json(attrs): Json<Map<String, Value>>,
    ) -> Resp0 {
        let nm = Name::new(&type_n)?.obj(&obj_n)?;
        log::info!("PATCH {nm}");
        let cred = Credentials::load(&session).await?;
        // *At least* Operate access needed (further checks below)
        let access =
            honey.name_access(cred.user(), &nm, Access::Operate).await?;
        for key in attrs.keys() {
            let attr = &key[..];
            let required = Access::required_patch(nm.res_type, attr);
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
            "/:type_n/:obj_n",
            get(handle_get).patch(handle_patch).delete(handle_delete),
        )
        .with_state(honey)
}

/// Get the SQL query one record of a given resource type
const fn one_sql(res: Res) -> &'static str {
    use Res::*;
    match res {
        Alarm => query::ALARM_ONE,
        Beacon => query::BEACON_ONE,
        CabinetStyle => query::CABINET_STYLE_ONE,
        Camera => query::CAMERA_ONE,
        CommConfig => query::COMM_CONFIG_ONE,
        CommLink => query::COMM_LINK_ONE,
        Controller => query::CONTROLLER_ONE,
        ControllerIo => query::CONTROLLER_IO_ONE,
        Detector => query::DETECTOR_ONE,
        Dms => query::DMS_ONE,
        FlowStream => query::FLOW_STREAM_ONE,
        Font => query::FONT_ONE,
        GateArm => query::GATE_ARM_ONE,
        GateArmArray => query::GATE_ARM_ARRAY_ONE,
        GeoLoc => query::GEO_LOC_ONE,
        Gps => query::GPS_ONE,
        Graphic => query::GRAPHIC_ONE,
        LaneMarking => query::LANE_MARKING_ONE,
        LcsArray => query::LCS_ARRAY_ONE,
        LcsIndication => query::LCS_INDICATION_ONE,
        Modem => query::MODEM_ONE,
        MsgLine => query::MSG_LINE_ONE,
        MsgPattern => query::MSG_PATTERN_ONE,
        Permission => query::PERMISSION_ONE,
        RampMeter => query::RAMP_METER_ONE,
        Role => query::ROLE_ONE,
        SignConfig => query::SIGN_CONFIG_ONE,
        SignDetail => query::SIGN_DETAIL_ONE,
        SignMessage => query::SIGN_MSG_ONE,
        TagReader => query::TAG_READER_ONE,
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
        | (LaneMarking, "pin")
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
        .map_err(|_e| SonarError::NotFound)?;
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
        .map_err(|_e| SonarError::NotFound)?;
    Ok(row.get::<usize, String>(0))
}
