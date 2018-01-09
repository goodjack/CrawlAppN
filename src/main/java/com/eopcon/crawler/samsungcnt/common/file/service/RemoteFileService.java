package com.eopcon.crawler.samsungcnt.common.file.service;

import java.util.List;

import com.eopcon.crawler.samsungcnt.common.file.LocalFile;
import com.eopcon.crawler.samsungcnt.common.file.RemoteFile;
import com.eopcon.crawler.samsungcnt.common.file.RemoteFileSet;

public interface RemoteFileService {

	public void write(String remotePath, List<LocalFile> files) throws Exception;

	public void read(String localPath, List<RemoteFile> files) throws Exception;

	public void mkdir(String basePath, List<String> paths) throws Exception;

	public RemoteFileSet ls(String basePath, boolean recursive) throws Exception;
}
