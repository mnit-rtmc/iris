package us.mn.state.dot.tms.client.camera.stream;

/** A DataSource distributes it's data to all connected DataSinks.
 * @author Timothy A. Johnson
 *
 */
public interface DataSource {

	/** Connect a DataSink */
	public void connectSink(DataSink sink);

	/** Disconnect a DataSink */
	public void disconnectSink(DataSink sink);

}
