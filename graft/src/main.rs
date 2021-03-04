mod sonar;

use anyhow::Result;
use async_std::task;

const HOST: &str = &"localhost.localdomain";

fn main() -> Result<()> {
    task::block_on(async {
        let mut c = sonar::Connection::new(HOST, 1037).await?;
        let msg = c.login("admin", "atms_242").await?;
        println!("Logged in from {}", msg);
        c.create_object("comm_config/test").await?;
        c.remove_object("comm_config/test").await
    })
}
