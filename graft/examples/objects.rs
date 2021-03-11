use anyhow::Result;
use async_std::task;
use graft::sonar;

const HOST: &str = &"localhost.localdomain";

fn main() -> Result<()> {
    task::block_on(async {
        let mut c = sonar::Connection::new(HOST, 1037).await?;
        let msg = c.login("admin", "atms_242").await?;
        println!("Logged in from {}", msg);
        let nm = "comm_config/test";
        c.create_object(nm).await?;
        println!("created {}", nm);
        c.remove_object(nm).await?;
        println!("removed {}", nm);
        Ok(())
    })
}
