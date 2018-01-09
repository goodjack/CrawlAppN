package com.eopcon.crawler.samsungcnt.common.file.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eopcon.crawler.samsungcnt.common.file.LocalFile;
import com.eopcon.crawler.samsungcnt.common.file.RemoteFile;
import com.eopcon.crawler.samsungcnt.common.file.RemoteFileSet;
import com.eopcon.crawler.samsungcnt.common.file.factory.SessionFacotry;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class SftpFileService implements RemoteFileService {

	protected static final Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_COMMON);

	private SessionFacotry<ChannelSftp> facotry;

	public void setFacotry(SessionFacotry<ChannelSftp> facotry) {
		this.facotry = facotry;
	}

	@Override
	public void write(String remotePath, List<LocalFile> files) throws Exception {

		ChannelSftp channel = null;
		FileInputStream fis = null;
		String target = "";
		try {
			channel = facotry.getSession();
			
			for (LocalFile file : files) {
				String relativePath = file.getRelativePath();
				String fileName = file.getFileName();
				File sourceFile = file.getFile();
				target = remotePath + relativePath + "/" + fileName;
				long startTime = System.currentTimeMillis();
				
				fis = new FileInputStream(sourceFile);
				channel.put(fis, target);
				if (logger.isDebugEnabled())
					logger.debug("# ChannelSftp write file successed!! -> remote file absolutePath : {}, elapsed time : {}ms", target, System.currentTimeMillis() - startTime);
				else
					logger.error("write - >끝");
				
			}
		}
	   catch(Exception ex) {
		   if (logger.isDebugEnabled())
			   logger.error("# write file Error -> {}",ex.getMessage() );
			else
				logger.debug("# write file error!! -> remote file absolutePath : {} -> {}", target, ex.getMessage());
            
        }

		finally {
			IOUtils.closeQuietly(fis);
			facotry.releaseSession(channel);
		}
	}

	@Override
	public void read(String localPath, List<RemoteFile> files) throws Exception {

		ChannelSftp channel = null;
		FileOutputStream fos = null;

		try {
			channel = facotry.getSession();

			for (RemoteFile file : files) {

				String source = file.getAbsolutePath();
				File targetFile = new File(localPath + "/" + file.getFileName());
				long startTime = System.currentTimeMillis();

				fos = new FileOutputStream(targetFile);
				channel.get(source, fos);

				logger.debug("# ChannelSftp read file successed!! -> local file absolutePath : {}, elapsed time : {}ms", targetFile.getAbsolutePath(), System.currentTimeMillis() - startTime);
			}
		} finally {
			IOUtils.closeQuietly(fos);
			facotry.releaseSession(channel);
		}

	}

	@Override
	public void mkdir(String basePath, List<String> dirs) throws Exception {

		ChannelSftp channel = null;

		try {
			channel = facotry.getSession();

			if (dirs.size() > 0) {
				for (String dir : dirs) {
					String path = basePath + dir;
					if (!exists(channel, path)) {
						channel.mkdir(path);
					}
					logger.debug("## path : {}, isSuccess : {}", path, true);
				}
			}
		} finally {
			facotry.releaseSession(channel);
		}
	}

	@Override
	public RemoteFileSet ls(String basePath, boolean recursive) throws Exception {
		ChannelSftp channel = null;

		try {
			channel = facotry.getSession();

			FileTraverse ft = new FileTraverse(channel, basePath, recursive);
			ft.search();

			return new RemoteFileSet(ft.getDirs(), ft.getFiles());
		} finally {
			facotry.releaseSession(channel);
		}
	}

	private boolean exists(ChannelSftp channel, String path) {
		try {
			SftpATTRS stat = channel.stat(path);
			if (stat == null)
				return false;
			return true;
		} catch (SftpException e) {
		}
		return false;
	}

	private class FileTraverse {
		private List<String> dirs = new ArrayList<String>();
		private List<RemoteFile> files = new ArrayList<RemoteFile>();

		private String basePath;
		private boolean recursive;

		private final String BACKSLASH = "\\\\";
		private final String SLASH = "/";

		private ChannelSftp channel;

		FileTraverse(ChannelSftp channel, String basePath, boolean recursive) {
			this.channel = channel;
			this.basePath = basePath.replaceAll(BACKSLASH, SLASH);
			this.recursive = recursive;
		}

		void search() throws SftpException {
			traverse(basePath);
		}

		List<String> getDirs() {
			return dirs;
		}

		List<RemoteFile> getFiles() {
			return files;
		}

		private void traverse(String currentPath) throws SftpException {

			@SuppressWarnings("unchecked")
			Vector<ChannelSftp.LsEntry> list = channel.ls(currentPath);

			for (ChannelSftp.LsEntry entry : list) {
				String fileName = entry.getFilename();
				SftpATTRS attr = entry.getAttrs();

				if (fileName.matches("^\\.{1,2}$"))
					continue;

				if (attr.isDir()) {
					String thisPath = currentPath + SLASH + fileName;
					addDirectory(thisPath);
					if (recursive)
						traverse(thisPath);
				} else {
					addFile(currentPath, entry);
				}
			}
		}

		private void addFile(String thisPath, ChannelSftp.LsEntry entry) {

			String fileName = entry.getFilename();
			SftpATTRS attr = entry.getAttrs();

			String relativePath = thisPath.replaceAll(BACKSLASH, SLASH).replace(basePath, "");

			RemoteFile remotelFile = new RemoteFile();

			remotelFile.setRelativePath(relativePath);
			remotelFile.setFileName(fileName);
			remotelFile.setBasePath(basePath);
			remotelFile.setMtime(attr.getMTime() * 1000);

			files.add(remotelFile);
		}

		private void addDirectory(String thisPath) {
			String relativePath = thisPath.replaceAll(BACKSLASH, SLASH).replace(basePath, "");
			if (relativePath.length() > 0)
				dirs.add(relativePath);
		}
	}
}
