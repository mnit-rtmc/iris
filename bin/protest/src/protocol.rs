trait ProtocolDriver {
    fn send(&[u8]) -> Result<()>;
	fn recv() -> Result<&[u8]>;
}

struct EchoDriver {
}

impl ProtocolDriver for EchoDriver {
}
