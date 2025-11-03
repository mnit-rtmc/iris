use futures_util::{StreamExt, pin_mut};
use tokio::io::AsyncWriteExt;
use tokio_tungstenite::connect_async;
use tungstenite::client::IntoClientRequest;

#[tokio::main]
async fn main() {
    let request = "ws://127.0.0.1".into_client_request().unwrap();
    /* request
    .headers_mut()
    .insert(HeaderName::AUTHORIZATION, "BEARER xxxxxxxx".try_into().unwrap()); */
    let (ws_stream, _response) =
        connect_async(request).await.expect("Failed to connect");
    println!("WebSocket handshake completed");

    let (_write, read) = ws_stream.split();
    let ws_to_stdout = {
        read.for_each(|message| async {
            let data = message.unwrap().into_data();
            tokio::io::stdout().write_all(&data).await.unwrap();
        })
    };
    pin_mut!(ws_to_stdout);
    ws_to_stdout.await;
}
