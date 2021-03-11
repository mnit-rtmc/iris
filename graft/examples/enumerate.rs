use anyhow::Result;
use async_std::task;
use graft::sonar;

const HOST: &str = &"localhost.localdomain";

fn main() -> Result<()> {
    task::block_on(async {
        let mut c = sonar::Connection::new(HOST, 1037).await?;
        let msg = c.login("admin", "atms_242").await?;
        println!("Logged in from {}", msg);
        let nm = "comm_config/cfg_3";
        c.enumerate_object(nm, |att, val| {
            println!("{}: {}", att, val);
            Ok(())
        })
        .await?;
        Ok(())
    })
}
