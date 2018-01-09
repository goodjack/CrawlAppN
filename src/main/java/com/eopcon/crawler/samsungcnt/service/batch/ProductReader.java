package com.eopcon.crawler.samsungcnt.service.batch;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eopcon.crawler.samsungcnt.model.LogDetail;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreCrawler;

public class ProductReader {

	private static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_BATCH);

	public final static int STATUS_FINISHED = -1;
	public final static int STATUS_WAIT = 0;
	public final static int STATUS_RUNNING = 1;

	private Long jobExecutionId;

	private OnlineStoreCrawler crawler;

	private LinkedBlockingQueue<LogDetail> queue = new LinkedBlockingQueue<>(1000);
	private Worker worker;

	private AtomicInteger status = new AtomicInteger(STATUS_WAIT);

	private int pageSize = 1000;

	public ProductReader(Long jobExecutionId, OnlineStoreCrawler crawler) {
		this.jobExecutionId = jobExecutionId;
		this.crawler = crawler;
		this.worker = new Worker();
	}

	public LogDetail read() throws InterruptedException {
		synchronized (status) {
			if (status.get() != STATUS_FINISHED && queue.size() == 0) {
				status.set(STATUS_WAIT);
				status.wait();
			}
		}
		return queue.poll();
	}

	public void start() {
		worker.start();
	}

	public void interrupt() {
		worker.interrupt();

		synchronized (status) {
			status.set(STATUS_FINISHED);
			status.notifyAll();
		}
	}

	public int getStatus() {
		return status.get();
	}

	private class Worker extends Thread {

		private final int WAIT_TIME = 1000;
		private String WORKER_NAME = "ProductReader";

		Worker() {
			setName(WORKER_NAME);
		}

		public void run() {
			
		
			try {
				long id = 0;
				
				logger.debug("Thread.interrupted() : " + Thread.interrupted());
				while (!Thread.interrupted()) {

					List<LogDetail> list = crawler.getNotAppliedGoodsLogs(jobExecutionId, id, pageSize);
					
					if (logger.isDebugEnabled())
						logger.debug("# NotAppliedGoodsLogs Size -> {}", list.size());

					if (list.size() == 0) {
						break;
					} else {
						LogDetail detail = list.get(list.size() - 1);
						id = detail.getId();
					}

					for (LogDetail logDetail : list) {
						queue.put(logDetail);

						synchronized (status) {
							status.set(STATUS_RUNNING);
							status.notify();
						}
					}
					Thread.sleep(WAIT_TIME);
				}

				synchronized (status) {
					status.set(STATUS_FINISHED);
					status.notifyAll();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
