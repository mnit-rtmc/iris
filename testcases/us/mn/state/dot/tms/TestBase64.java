import java.io.IOException;
import us.mn.state.dot.tms.Base64;

public class TestBase64 {

	/** Round-trip encode then decode data, comparing the results */
	static protected void round_trip(byte[] m) throws IOException {
		String v = Base64.encode(m);
		byte[] b = Base64.decode(v);
		if(!java.util.Arrays.equals(b, m))
			throw new IOException("Round trip: " + m);
	}

	/** Test Base64 encoding */
	static public void main(String[] args) {
		try {
			for(int l = 1; l < 80; l++) {
				byte[] m = new byte[l];
				for(int i = 0; i < 256; i++) {
					for(int j = 0; j < l; j++)
						m[j] = (byte)i;
					round_trip(m);
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
