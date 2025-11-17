use futures_util::StreamExt;
use tokio::io::AsyncWriteExt;
use tokio_tungstenite::connect_async;
use tungstenite::client::IntoClientRequest;

#[tokio::main]
async fn main() {
    let request = "ws://127.0.0.1/api/v1/live-vehicle-data"
        .into_client_request()
        .unwrap();
    let (mut stream, response) =
        connect_async(request).await.expect("Connection failed");
    print!("Connected, ");
    match response.into_body() {
        Some(body) => {
            println!("{}", String::from_utf8(body).expect("Invalid UTF-8"))
        }
        None => println!("waiting..."),
    }

    loop {
        let data = stream.next().await.unwrap().unwrap().into_data();
        let msg = format!("\nreceived {} bytes\n", data.len());
        tokio::io::stdout()
            .write_all(&msg.as_bytes())
            .await
            .unwrap();
        tokio::io::stdout().write_all(&data).await.unwrap();
    }
}
