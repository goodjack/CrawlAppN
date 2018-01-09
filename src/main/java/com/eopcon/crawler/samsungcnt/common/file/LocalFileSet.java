package com.eopcon.crawler.samsungcnt.common.file;

import java.util.ArrayList;
import java.util.List;

public class LocalFileSet {

	private List<String> dirs = new ArrayList<String>();
	private List<LocalFile> files = new ArrayList<LocalFile>();

	public LocalFileSet(List<String> dirs, List<LocalFile> files) {
		this.dirs = dirs;
		this.files = files;
	}

	public List<String> getDirs() {
		return dirs;
	}

	public List<LocalFile> getFiles() {
		return files;
	}
}
