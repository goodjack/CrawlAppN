package com.eopcon.crawler.samsungcnt.common.file;

import java.io.File;
import java.text.SimpleDateFormat;

public class LocalFile {

	private String relativePath;
	private String fileName;
	private File file;
	private boolean delete = false;

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

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	@Override
	public String toString() {
		return String.format("absolutePath : %s, mtime : %s, isDeleted : %s", new Object[] { file.getAbsolutePath(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file.lastModified()), delete });
	}
}
