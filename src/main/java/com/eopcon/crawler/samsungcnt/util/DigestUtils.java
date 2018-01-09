package com.eopcon.crawler.samsungcnt.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.security.MessageDigest;

import org.apache.commons.io.IOUtils;

public class DigestUtils {

	public static String getHashValue(String str) {
		String sha = "";
		try {
			MessageDigest sh = MessageDigest.getInstance("SHA-256");
			sh.update(str.getBytes());
			byte byteData[] = sh.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			sha = sb.toString();

		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return sha;
	}

	public static String getHashValue(File src) {

		String sha = "";
		int buff = 16384;
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(src, "r");
			MessageDigest hashSum = MessageDigest.getInstance("SHA-256");

			byte[] buffer = new byte[buff];
			byte[] partialHash = null;

			long read = 0;

			// calculate the hash of the hole file for the test
			long offset = file.length();
			int unitsize;
			while (read < offset) {
				unitsize = (int) (((offset - read) >= buff) ? buff : (offset - read));
				file.read(buffer, 0, unitsize);

				hashSum.update(buffer, 0, unitsize);
				read += unitsize;
			}

			partialHash = new byte[hashSum.getDigestLength()];
			partialHash = hashSum.digest();

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < partialHash.length; i++) {
				sb.append(Integer.toString((partialHash[i] & 0xff) + 0x100, 16).substring(1));
			}
			sha = sb.toString();
			file.close();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			IOUtils.closeQuietly(file);
		}
		return sha;
	}
}
