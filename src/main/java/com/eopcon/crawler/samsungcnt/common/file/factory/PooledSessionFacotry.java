package com.eopcon.crawler.samsungcnt.common.file.factory;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;

public class PooledSessionFacotry<T> implements SessionFacotry<T> {

	protected static final Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_BATCH);

	private static final int DEFAULT_MIN_SESSION = 5;
	private static final int DEFAULT_MAX_SESSION = 10;
	private static final int DEFAULT_IDLE_TIMEOUT = 5 * 60 * 1000;

	private int minimumSessions;
	private int maximumSessions;

	private transient Queue<Session> freeSessions = new ConcurrentLinkedQueue<Session>();
	private transient Map<T, Session> usedSessions = new ConcurrentHashMap<T, Session>();

	private transient boolean shuttingDown = false;
	private transient boolean shutdownComplete = false;
	private transient Thread thread = null;

	private SessionFacotry<T> factory = null;

	public PooledSessionFacotry(SessionFacotry<T> factory, int minimumSessions, int maximumSessions, int shutdownCourtesyTimeout) throws Exception {
		this.factory = factory;
		this.minimumSessions = minimumSessions;
		this.maximumSessions = maximumSessions;

		for (int i = 0; i < minimumSessions; i++) {
			freeSessions.add(getNewSession());
		}

		thread = new Thread(new MaintenanceRunner(shutdownCourtesyTimeout));
		thread.start();
	}

	public PooledSessionFacotry(SessionFacotry<T> factory, int minimumSessions, int maximumSessions) throws Exception {
		this(factory, minimumSessions, maximumSessions, MaintenanceRunner.DEFAULT_SHUTDOWN_TIMEOUT);
	}

	public PooledSessionFacotry(SessionFacotry<T> factory) throws Exception {
		this(factory, DEFAULT_MIN_SESSION, DEFAULT_MAX_SESSION, MaintenanceRunner.DEFAULT_SHUTDOWN_TIMEOUT);
	}

	@Override
	public synchronized T getSession() throws Exception {
		Session result = null;

		if (shuttingDown) {
			throw new IllegalStateException("The session pool had been shutdown!");
		}

		result = getSessionInternal();

		if (result == null) {
			throw new IllegalStateException("The session pool can not create new session!!");
		}
		return result.getSession();
	}

	public synchronized T getSessionWait() throws Exception {
		Session result = null;

		while ((!shuttingDown) && ((result = getSessionInternal()) == null)) {
			try {
				this.wait();
			} catch (InterruptedException ex) {
			}
		}

		if (shuttingDown) {
			throw new IllegalStateException("The session pool had been shutdown!");
		}

		return result.getSession();
	}

	public synchronized T getSessionWait(int timeout) throws Exception {
		Session result = null;

		if (shuttingDown) {
			throw new IllegalStateException("The session pool had been shutdown!");
		}

		result = getSessionInternal();

		if (result != null) {
			return result.getSession();
		}

		try {
			this.wait(timeout);
		} catch (InterruptedException ex) {
		}

		if (shuttingDown) {
			throw new IllegalStateException("The session pool had been shutdown!");
		}

		result = getSessionInternal();

		if (result == null) {
			throw new IllegalStateException("The session pool can not create new session!!");
		} else {
			return result.getSession();
		}
	}

	@Override
	public synchronized void releaseSession(T session) {
		if (usedSessions.containsKey(session)) {
			Session result = usedSessions.get(session);
			result.setTimestamp(System.currentTimeMillis());
			usedSessions.remove(session);
			freeSessions.add(result);
		} else {
			throw new IllegalStateException("The session was not registered!");
		}
		this.notifyAll();
	}

	@Override
	public boolean isAlive(T session) {
		return factory.isAlive(session);
	}

	public synchronized void shutdown() {
		shuttingDown = true;
		this.notifyAll();

		while (!shutdownComplete) {
			try {
				this.wait();
			} catch (InterruptedException ex) {
			}
		}
	}

	public synchronized int getSize() {
		return usedSessions.size() + freeSessions.size();
	}

	public synchronized int getUseCount() {
		return usedSessions.size();
	}

	private Session getSessionInternal() {
		Session result = null;

		try {
			if (!freeSessions.isEmpty()) {
				result = freeSessions.poll();

				if (result.getTimestamp() < (System.currentTimeMillis() - DEFAULT_IDLE_TIMEOUT)) {
					closeChannel(result.getSession());
					result = getNewSession();
				} else {
					boolean alive = isAlive(result.getSession());
					if (!alive)
						result = getNewSession();
				}
			} else if (usedSessions.size() < maximumSessions) {
				result = getNewSession();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		if (result != null) {
			usedSessions.put(result.getSession(), result);
		}

		return result;
	}

	private Session getNewSession() throws Exception {
		return new Session(factory.getSession(), System.currentTimeMillis());
	}

	private void closeChannel(T session) {
		factory.releaseSession(session);
	}

	class Session {

		private T session;
		private long timestamp;

		Session(T session, long timestamp) {
			this.session = session;
			this.timestamp = timestamp;
		}

		public T getSession() {
			return session;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
	}

	class MaintenanceRunner implements Runnable {

		private static final int DEFAULT_SHUTDOWN_TIMEOUT = 30 * 1000;
		private int shutdownCourtesyTimeout;

		public MaintenanceRunner(int timeout) {
			this.shutdownCourtesyTimeout = timeout;
		}

		public MaintenanceRunner() {
			this(DEFAULT_SHUTDOWN_TIMEOUT);
		}

		public void run() {
			synchronized (PooledSessionFacotry.this) {
				maintLoop();
				shutdown();
			}
		}

		@SuppressWarnings("unused")
		private void maintLoop() {
			while (!shuttingDown) {

				for (int i = getSize(); freeSessions.size() > minimumSessions; i--) {
					Session result = freeSessions.poll();
					closeChannel(result.getSession());
				}

				for (int i = getSize(); (i < minimumSessions); i++) {
					try {
						Session session = getNewSession();
						freeSessions.add(session);
					} catch (Exception e) {
					}
				}

				try {
					PooledSessionFacotry.this.wait();
				} catch (InterruptedException ex) {
				}
			}
		}

		private void shutdown() {

			if ((shutdownCourtesyTimeout > -1) && !usedSessions.isEmpty()) {
				if (shutdownCourtesyTimeout == 0) {
					while (!usedSessions.isEmpty()) {
						try {
							PooledSessionFacotry.this.wait(shutdownCourtesyTimeout);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					if (!usedSessions.isEmpty()) {
						try {
							PooledSessionFacotry.this.wait(shutdownCourtesyTimeout);
						} catch (InterruptedException ex) {
						}
					}
				}
			}

			while (!freeSessions.isEmpty()) {
				Session result = freeSessions.poll();
				closeChannel(result.getSession());
			}

			Iterator<T> sessions = usedSessions.keySet().iterator();

			while (sessions.hasNext()) {
				T session = sessions.next();
				closeChannel(session);
			}

			shutdownComplete = true;
			PooledSessionFacotry.this.notifyAll();
		}
	}
}
