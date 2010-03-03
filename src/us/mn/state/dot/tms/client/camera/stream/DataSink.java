package us.mn.state.dot.tms.client.camera.stream;

public interface DataSink {

	/** Flush the data down the sink */
	public void flush(byte[] data);
}
