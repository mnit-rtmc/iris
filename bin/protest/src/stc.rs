//
// stc -- Driver for Hysecurity SmartTouch Controller
// Copyright (C) 2020  Minnesota Department of Transportation
//

enum CommandStatus {
	Reset,
	OpenInProgress,
	OpenComplete,
	CloseInProgress,
	CloseComplete,
	Stopped,
}

enum OperatorStatus {
	Reset,
	LearnLimitStop, LearnLimitOpen, LearnLimitClose,
	NormalStop,
	CheckPeOpen, Pep2Open, WarnB4Open,
	NormalOpen,
	Reverse2ClosePeo, WaitPeo, DelayPeo,
	CheckPeClose, Pep2Close, WarnB4Close,
	NormalClose,
	WaitVd, Reverse2OpenPec, WaitPe, DelayPe,
	Reverse2Close, Reverse2Open,
	SafetyStop, EntrapmentStop,
	Fault1, Fault2, Fault3, Fault5,
	Error1, Error2, Error6, Error8, Error9, Error10,
	Alert1, Alert2, Alert4, Alert5, Alert6,
}

struct STCController {
	address: i32,
	cmd_stat: CommandStatus,
	op_stat: OperatorStatus,
	tm: time,
	open_limit: bool,
	close_limit: bool,
}

impl STCController {

    pub fn new(address: i32) -> Self {
        STCController {
            address,
            cmd_stat: CommandStatus::Reset,
            op_stat: OperatorStatus::Reset,
            tm: Clock.currTime(),
            open_limit: false,
            close_limit: true,
        }
    }

	fn update_status(&mut self) {
		if self.tm < Clock.currTime() {
			self.update_op_status();
        }
	}

	fn update_op_status(&mut self) {
        match self.op_stat {
            OperatorStatus::NormalClose => {
                self.cmd_stat = CommandStatus::CloseComplete;
                self.op_stat = OperatorStatus::NormalStop;
                self.close_limit = true;
            }
            OperatorStatus::NormalOpen => {
                self.cmd_stat = CommandStatus::OpenComplete;
                self.op_stat = OperatorStatus::NormalStop;
                self.open_limit = true;
            }
            _ => self.op_stat = OperatorStatus::NormalStop;
		}
	}

	fn process_status(&self) -> &[u8] {
		auto rsp = appender!string();
		formattedWrite(rsp, "S%02X%02X041%1X%1X00000000000", cmd_stat,
			op_stat, open_limit, close_limit);
		return cast(ubyte[])rsp.data();
	}

	fn process_status_n(&self) -> &[u8] {
		auto rsp = appender!string();
		formattedWrite(rsp,
			"N%02X%02X041%1X%1X00000000000000000000000000000",
			cmd_stat, op_stat, open_limit, close_limit);
		return cast(ubyte[])rsp.data();
	}

	fn control_request(&mut self, pkt: &[u8]) -> &[u8] {
		if pkt.length == 9 {
            match (pkt[1], pkt[2], pkt[3]) {
                ('1', '0', '0') => self.control_open(),
			    ('0', '1', '0') => self.control_close(),
			    ('0', '0', '1') => self.control_stop(),
            }
            Ok("C")
		} else {
            Err()
        }
	}

	fn control_open(&mut self) {
		self.cmd_stat = CommandStatus::OpenInProgress;
		self.op_stat = OperatorStatus::NormalOpen;
		self.close_limit = false;
		self.tm = Clock.currTime() + dur!"seconds"(7);
	}

	fn control_close(&mut self) {
		self.cmd_stat = CommandStatus::CloseInProgress;
		self.op_stat = OperatorStatus::NormalClose;
		self.open_limit = false;
		self.tm = Clock.currTime() + dur!"seconds"(7);
	}

	fn control_stop(&mut self) {
		self.cmd_stat = CommandStatus::Stopped;
		self.op_stat = OperatorStatus::NormalStop;
	}

	pub fn process_packet(&mut self, pkt: &[u8]) -> &[u8] {
		match pkt[0] {
            'R' => (),
		    'V' => "Vh4.33, Boot Loader: V1.0\0",
		    'S' => {
    			self.update_status();
	    		return self.process_status_n();
            }
            'C' => self.control_request(pkt),
		}
		return null;
	}
}

struct STCDriver {
	rx_buf: [u8; 1024],
	tx_buf: [u8; 1024],
	controllers: Vec<STCController>,
}

impl ProtocolDriver for STCDriver {
	fn sentinel(&self) -> usize {
		for (i, b) in self.rx_buf.iter().enumerate() {
			if b == 0xFF {
                return i;
            }
		}
		rx_buf.length
	}
	void skip_garbage() {
		rx_buf = rx_buf[sentinel() .. $];
	}
	void check_packet() {
		// Check for minimum packet size
		if(rx_buf.length < 4)
			return;
		// Check for sentinel
		if(rx_buf[0] != 0xFF)
			return;
		int n_bytes = rx_buf[2] + 4;
		// Check for a full packet
		if(rx_buf.length < n_bytes)
			return;
		process_packet(rx_buf[0 .. n_bytes]);
		// Don't process that packet again
		rx_buf = rx_buf[n_bytes .. $];
	}
	void process_packet(ubyte pkt[]) {
		int xsum = 0;
		for(int i = 0; i < pkt.length; i++)
			xsum += pkt[i];
		if((xsum & 0xFF) != 0)
			return;
		// Check for valid message size
		if(pkt[2] < 1 || pkt[2] > 254)
			return;
		int address = pkt[1];
		STCController c = controllers[address];
		if(c !is null)
			process_packet(pkt[3 .. $-1], c);
	}
	void process_packet(ubyte pkt[], STCController c) {
		ubyte[] msg = c.process_packet(pkt);
		if(msg !is null) {
			ubyte[] rsp = [cast(ubyte)0xFF, cast(ubyte)0x00,
				cast(ubyte)msg.length] ~ msg;
			int xsum = 0;
			for(int i = 0; i < rsp.length; i++)
				xsum += rsp[i];
			rsp ~= cast(ubyte)((~xsum) + 1);
			tx_buf ~= rsp;
		}
	}
public:
	this() {
		foreach(int a; 1 .. 8)
			controllers[a] = new STCController(a);
	}
	ubyte[] recv(ubyte[] buf) {
		tx_buf.length = 0;
		rx_buf ~= buf;
		skip_garbage();
		check_packet();
		return tx_buf;
	}
}
