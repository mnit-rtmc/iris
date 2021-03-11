use convert_case::{Case, Casing};
use graft::sonar;
use std::borrow::Cow;
use tide::Request;

#[async_std::main]
async fn main() -> tide::Result<()> {
    let mut app = tide::new();
    app.at("/comm_config").get(get_comm_config);
    app.at("/comm_config").post(create_comm_config);
    app.listen("127.0.0.1:8080").await?;
    Ok(())
}

const HOST: &str = &"localhost.localdomain";

async fn get_comm_config(req: Request<()>) -> tide::Result {
    let name = request_name(&req)?;
    let mut c = sonar::Connection::new(HOST, 1037).await?;
    let msg = c.login("admin", "atms_242").await?;
    println!("Logged in from {}", msg);
    let nm = format!("comm_config/{}", name);
    let mut res = json::object!();
    c.enumerate_object(&nm, |att, val| {
        let att = att.to_case(Case::Snake);
        res[att] = val.into();
        Ok(())
    })
    .await?;
    Ok(res.to_string().into())
}

fn request_name(req: &Request<()>) -> tide::Result<Cow<str>> {
    for pair in req.url().query_pairs() {
        if pair.0 == "name" {
            return Ok(pair.1);
        }
    }
    panic!();
}

async fn create_comm_config(req: Request<()>) -> tide::Result {
    let name = request_name(&req)?;
    Ok(format!("name: {}", name).into())
}
