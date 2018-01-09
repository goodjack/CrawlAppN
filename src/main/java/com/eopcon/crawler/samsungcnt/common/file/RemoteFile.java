package com.eopcon.crawler.samsungcnt.common.file;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RemoteFile {

	private String basePath = null;
	private String relativePath = null;
	private String fileName = null;
	private long mtime;
	private boolean delete = false;

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getMtime() {
		return mtime;
	}

	public void setMtime(long mtime) {
		this.mtime = mtime;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public String getAbsolutePath() {
		return basePath + relativePath + "/" + fileName;
	}

	@Override
	public String toString() {
		return String.format("absolutePath : %s, mtime : %s", new Object[] { getAbsolutePath(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(getMtime())) });
	}
}
