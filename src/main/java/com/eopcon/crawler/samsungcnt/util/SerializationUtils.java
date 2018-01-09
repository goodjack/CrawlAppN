package com.eopcon.crawler.samsungcnt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.apache.commons.io.IOUtils;

public class SerializationUtils extends org.apache.commons.lang.SerializationUtils {

	public static void serialize(File file, Object object) {
		FileOutputStream fos = null;
		ObjectOutput out = null;
		try {
			fos = new FileOutputStream(file);
			out = new ObjectOutputStream(fos);
			
			out.writeObject(object);
			out.flush();
			
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(fos);
			try {
				out.close();
			} catch (Exception e) {

			}
		}
	}

	public static Object deserialize(File file) {
		if (file.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				fis.close();
				return SerializationUtils.deserialize(new FileInputStream(file));
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			} finally {
				IOUtils.closeQuietly(fis);
			}
		}
		return null;
	}

}
