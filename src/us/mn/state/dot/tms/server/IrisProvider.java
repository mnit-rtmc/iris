/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2015  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.server;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import us.mn.state.dot.sonar.server.AuthProvider;
import us.mn.state.dot.sonar.server.UserImpl;
import us.mn.state.dot.tms.utils.Base64;

/**
 * Authentication provider for IRIS users.
 *
 * @author Douglas Lau
 */
public class IrisProvider implements AuthProvider {

	/** Number of iterations for generating hash */
	static private final int N_ITERATIONS = 10 * 1024;

	/** Number of bits for salt (encoded as 20 chars in Base64) */
	static private final int SALT_BITS = 120;

	/** Number of bits for key (encoded as 44 chars in Base64) */
	static private final int KEY_BITS = 256;

	/** Check if a string blank or null */
	static private boolean isBlank(String dn) {
		return dn == null || dn.isEmpty();
	}

	/** Random number generator for creating salt */
	private final SecureRandom rng;

	/** Factory for hashing keys */
	private final SecretKeyFactory factory;

	/** Create a new IRIS authentication provider */
	public IrisProvider() throws NoSuchAlgorithmException {
		rng = SecureRandom.getInstance("SHA1PRNG");
		factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	}

	/** Generate a salted hash of a password */
	private String generateHash(char[] pwd, byte[] salt)
		throws InvalidKeySpecException
	{
		PBEKeySpec kspec = new PBEKeySpec(pwd, salt, N_ITERATIONS,
			KEY_BITS);
		SecretKey key = factory.generateSecret(kspec);
		return Base64.encode(key.getEncoded());
	}

	/** Create a hash of a password */
	public String createHash(char[] pwd) throws InvalidKeySpecException {
		byte[] salt = new byte[SALT_BITS / 8];
		rng.nextBytes(salt);
		return Base64.encode(salt) + generateHash(pwd, salt);
	}

	/** Authenticate a user */
	public boolean authenticate(UserImpl user, char[] pwd) {
		try {
			return isBlank(user.getDn()) &&
			       user instanceof IrisUserImpl &&
			       check((IrisUserImpl)user, pwd);
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/** Check a password against a stored hash */
	private boolean check(IrisUserImpl user, char[] pwd) throws IOException,
		InvalidKeySpecException
	{
		String stored = user.getPassword();
		int s_len = Base64.numCharacters(SALT_BITS);
		int k_len = Base64.numCharacters(KEY_BITS);
		if(stored.length() == s_len + k_len) {
			String s = stored.substring(0, s_len);
			String p = stored.substring(s_len);
			String h = generateHash(pwd, Base64.decode(s));
			return h.equals(p);
		} else
			return false;
	}
}
