use anyhow::Result;
use honeybee::sonar;

const HOST: &str = &"localhost.localdomain";

#[tokio::main]
async fn main() -> Result<()> {
    let mut m = sonar::Messenger::new(HOST, 1037).await?;
    let msg = m.login("admin", "atms_242").await?;
    println!("Logged in from {}", msg);
    let nm = "comm_config/test";
    m.create_object(nm).await?;
    println!("created {}", nm);
    m.remove_object(nm).await?;
    println!("removed {}", nm);
    Ok(())
}
