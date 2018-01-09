package com.eopcon.crawler.samsungcnt.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
//import java.nio.charset.StandardCharsets;

//import org.apache.commons.io.IOUtils;
//import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eopcon.crawler.samsungcnt.model.Category;

public class Marker {
	
	private static Logger logger = LoggerFactory.getLogger(Marker.class);
	//protected static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_COMMON);

	private BufferedWriter bos;
	private File logFile;
	private FileReader fr = null;
	private BufferedReader br = null;
	private boolean finished = false;

	public Marker(File dir) {
		initialize(dir);
	}

	private void initialize(File dir) {
		try {
			logger.debug("dir : " + dir);
			logFile = new File(dir, "marker.log");
			bos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), java.nio.charset.StandardCharsets.UTF_8));
		} catch (Exception e) {
			logger.error("1" + e.getMessage() , e);
		}
	}

	/**
	 * 마킹을 수행한다.
	 * 
	 * @param mark
	 * @param category
	 */
	public void mark(Mark mark, Category category) {
		try {
			int categoryNumber = mark.getCategoryNumber();
			int page = mark.getPage();
			int size = mark.getSize();

			String message = String.format("%s//%s//%s//%s", categoryNumber, page, size, category == null ? StringUtils.EMPTY : category.toString());

			bos.write(message);
			bos.write('\n');
			bos.flush();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	/**
	 * 마지막 마킹 정보를 반환한다.
	 * 
	 * @return
	 */
	public Mark getLastMark() {
		logger.debug("getLastMark1" );
		if (logFile.exists()) {
			logger.debug("getLastMark2" );
			org.apache.commons.io.input.ReversedLinesFileReader reader = null;

			try {
				logger.debug("getLastMark3" );
				logger.debug("logFile.path : " + logFile.getPath() );
				reader = new org.apache.commons.io.input.ReversedLinesFileReader(logFile, java.nio.charset.StandardCharsets.UTF_8);
				logger.debug("getLastMark4" );
				String lastLine = reader.readLine();

				if (lastLine != null) {
					String[] temp = lastLine.split("//");

					int categoryNumber = Integer.parseInt(temp[0]);
					int page = Integer.parseInt(temp[1]);
					int size = Integer.parseInt(temp[2]);

					finished = categoryNumber == -1;

					return new Mark(categoryNumber, page, size);
				}
				
				reader.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				org.apache.commons.io.IOUtils.closeQuietly(reader);
			}
		}
		return null;
	}
	
	public Mark getLastMark_new() {
		if (logFile.exists()) {
			try {
				logger.debug("getLastMark3" );
				logger.debug("logFile.path : " + logFile.getPath() );
				fr = new FileReader(logFile);//(logFile, java.nio.charset.StandardCharsets.UTF_8);
				br = new BufferedReader(fr);
				String readLine = "";
				String lastLine = "";
				while((readLine=br.readLine())!=null){
					if(readLine == "") {
						lastLine = readLine;
					}
				}
				if(lastLine != "") {
					String[] temp = lastLine.split("//");
					
					int categoryNumber = Integer.parseInt(temp[0]);
					int page = Integer.parseInt(temp[1]);
					int size = Integer.parseInt(temp[2]);

					finished = categoryNumber == -1;

					return new Mark(categoryNumber, page, size);
				}
				br.close();
				fr.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				try {
					br.close();
					fr.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				//org.apache.commons.io.IOUtils.closeQuietly(reader);
			}
		}
		return null;
	}
	
	/**
	 * 마킹 완료 처리를 수행한다.
	 */
	public void finish() {
		Mark mark = new Mark();
		mark.setCategoryNumber(-1);

		mark(mark, null);
		finished = true;
	}

	/**
	 * 마킹 완료 여부를 반환한다.
	 * 
	 * @return
	 */
	public boolean isFinished() {
		return finished;
	}
	
	public void destroy() {
		org.apache.commons.io.IOUtils.closeQuietly(bos);
	}
	

	public static class Mark {
		private int categoryNumber;
		private int page;
		private int size;

		public Mark() {

		}

		public Mark(int categoryNumber, int page, int size) {
			this.categoryNumber = categoryNumber;
			this.page = page;
			this.size = size;
		}

		public int getCategoryNumber() {
			return categoryNumber;
		}

		public void setCategoryNumber(int categoryNumber) {
			this.categoryNumber = categoryNumber;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}
	}
}
