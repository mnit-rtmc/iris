mod sonar;

use anyhow::{bail, Result};
use async_std::task;

const HOST: &str = &"localhost.localdomain";

fn main() -> Result<()> {
    task::block_on(async {
        let mut c = sonar::Connection::new(HOST, 1037).await?;
        let mut buf = vec![];
        sonar::Message::Login("admin", "atms_242").encode(&mut buf);
        c.send(&buf[..]).await?;
        c.recv(|m| {
            match m {
                sonar::Message::Type("") => Ok(()),
                _ => bail!("Expected `Type` message"),
            }
        })
        .await?;
        c.recv(|m| {
            match m {
                sonar::Message::Show(txt) => {
                    println!("show: {}", txt);
                    Ok(())
                }
                _ => bail!("Expected `Show` message"),
            }
        })
        .await?;
        buf.clear();
        //sonar::Message::Object("comm_config/test").encode(&mut buf);
        sonar::Message::Remove("comm_config/test").encode(&mut buf);
        c.send(&buf[..]).await?;
        c.recv(|m| {
            match m {
                sonar::Message::Show(txt) => {
                    println!("show: {}", txt);
                    Ok(())
                }
                _ => Ok(()),
            }
        })
        .await
    })
}
