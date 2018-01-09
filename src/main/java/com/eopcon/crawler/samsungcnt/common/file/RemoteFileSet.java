package com.eopcon.crawler.samsungcnt.common.file;

import java.util.ArrayList;
import java.util.List;

public class RemoteFileSet {

	private List<String> dirs = new ArrayList<String>();
	private List<RemoteFile> files = new ArrayList<RemoteFile>();

	public RemoteFileSet(List<String> dirs, List<RemoteFile> files) {
		this.dirs = dirs;
		this.files = files;
	}

	public List<String> getDirs() {
		return dirs;
	}

	public List<RemoteFile> getFiles() {
		return files;
	}
}
