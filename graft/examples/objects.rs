use anyhow::Result;
use graft::sonar;

const HOST: &str = &"localhost.localdomain";

#[tokio::main]
async fn main() -> Result<()> {
    let mut c = sonar::Connection::new(HOST, 1037).await?;
    let msg = c.login("admin", "atms_242").await?;
    println!("Logged in from {}", msg);
    let nm = "comm_config/test";
    c.create_object(nm).await?;
    println!("created {}", nm);
    c.remove_object(nm).await?;
    println!("removed {}", nm);
    Ok(())
}
