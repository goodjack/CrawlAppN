package com.eopcon.crawler.samsungcnt.service;

import java.io.File;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.eopcon.crawler.samsungcnt.common.file.LocalFile;
import com.eopcon.crawler.samsungcnt.common.file.service.RemoteFileService;

public class ImageUploader {

	@Autowired
	private RemoteFileService remoteFileService;

	@Value("${file.remote.directory}")
	private String remoteBasePath;

	
	public void uploadImage(String relativePath, File file) throws Exception {
		LocalFile local = new LocalFile();

		local.setFileName(file.getName());
		local.setFile(file);
		local.setRelativePath(relativePath);
		remoteFileService.write(remoteBasePath, Arrays.asList(local));
	}
	
}
