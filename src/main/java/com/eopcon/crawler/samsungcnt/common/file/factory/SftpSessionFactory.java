package com.eopcon.crawler.samsungcnt.common.file.factory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SftpSessionFactory implements SessionFacotry<ChannelSftp>, InitializingBean {

	private final int TIME_OUT = 30000;
	private final int SERVER_ALIVE_INTERVAL = 60000;

	private String privateKeyLocation = null;

	private String host = null;
	private int port = 22;

	private String username = null;
	private String password = null;

	private JSch jsch = null;

	public void setPrivateKeyLocation(String privateKeyLocation) {
		this.privateKeyLocation = privateKeyLocation;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		jsch = new JSch();
		if (privateKeyLocation != null)
			jsch.addIdentity(privateKeyLocation);
	}

	@Override
	public ChannelSftp getSession() throws Exception {
		
		jsch.addIdentity(privateKeyLocation);
		
		Session session = jsch.getSession(username, host, port);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setTimeout(TIME_OUT);
		session.setServerAliveInterval(SERVER_ALIVE_INTERVAL);
		session.setPassword(password);
		session.connect();
		
		Channel channel = session.openChannel("sftp");
		channel.connect();
		
		return (ChannelSftp) channel;
	}

	@Override
	public void releaseSession(ChannelSftp session) {
		if (session != null) {
			try {
				session.exit();
				session.getSession().disconnect();
			} catch (JSchException e) {
			}
		}
	}

	@Override
	public boolean isAlive(ChannelSftp session) {
		if (session != null && session.isConnected()) {
			try {
				Session s = session.getSession();

				ChannelExec testChannel = (ChannelExec) s.openChannel("exec");
				testChannel.setCommand("true");
				testChannel.connect();
				testChannel.disconnect();
				return true;
			} catch (JSchException e) {
			}
		}
		return false;
	}
}
