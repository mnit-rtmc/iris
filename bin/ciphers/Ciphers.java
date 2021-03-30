import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class Ciphers {
	static public void main(String[] args) throws Exception {
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, null, null);
		SSLEngine engine = context.createSSLEngine();
		List<String> protocols = Arrays.asList(
			engine.getEnabledProtocols());
		System.out.println("Enabled Protocols:");
		for (String sp: engine.getSupportedProtocols()) {
			if (protocols.contains(sp))
				System.out.println("  " + sp);
		}
		System.out.println("Disabled Protocols:");
		for (String sp: engine.getSupportedProtocols()) {
			if (!protocols.contains(sp))
				System.out.println("  " + sp);
		}
		List<String> suites = Arrays.asList(
			engine.getEnabledCipherSuites());
		System.out.println("Enabled Cipher Suites:");
		for (String cs: engine.getSupportedCipherSuites()) {
			if (suites.contains(cs))
				System.out.println("  " + cs);
		}
		System.out.println("Disabled Cipher Suites:");
		for (String cs: engine.getSupportedCipherSuites()) {
			if (!suites.contains(cs))
				System.out.println("  " + cs);
		}
	}
}
