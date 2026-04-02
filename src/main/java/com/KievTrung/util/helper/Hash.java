package com.KievTrung.util.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
  public static String hash256(String msg) throws NoSuchAlgorithmException {
	MessageDigest md = MessageDigest.getInstance("SHA-256");
	byte[] hashBytes = md.digest(msg.getBytes());

	// convert to hex
	StringBuilder hex = new StringBuilder();
	for (byte b : hashBytes) {
	  hex.append(String.format("%02x", b));
	}

	return hex.toString();
  }
}
