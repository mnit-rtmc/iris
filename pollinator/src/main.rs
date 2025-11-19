mod error;
mod http;
mod rtms_echo;

#[tokio::main]
async fn main() {
    let mut args = std::env::args();
    let _prog = args.next();
    let host = args.next().unwrap_or(String::from("127.0.0.1"));
    let user = args.next().unwrap_or(String::from("user"));
    let pass = args.next().unwrap_or(String::from("pass"));
    rtms_echo::collect_vehicle_data(&host, &user, &pass)
        .await
        .unwrap();
}
