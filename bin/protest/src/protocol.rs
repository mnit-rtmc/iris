trait ProtocolDriver {
    fn send(pkt: &[u8]) -> Result<(), ()>;
    fn recv() -> Result<Vec<u8>, ()>;
}

struct EchoDriver {}

impl ProtocolDriver for EchoDriver {
    fn send(pkt: &[u8]) -> Result<(), ()> {
        todo!()
    }

    fn recv() -> Result<Vec<u8>, ()> {
        todo!()
    }
}
