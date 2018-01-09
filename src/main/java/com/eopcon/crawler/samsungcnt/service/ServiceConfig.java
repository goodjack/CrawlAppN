package com.eopcon.crawler.samsungcnt.service;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.eopcon.crawler.samsungcnt.util.DateUtils;

public class ServiceConfig implements InitializingBean {

	@Autowired
	protected Properties properties;

	private OnlineStoreConst constant;

	private File baseDirectory;
	private File backupDirectory;
	private File inputDirectory;
	private File inputQueueDirectory;
	private File inputBackupDirectory;
	private File outputDirectory;
	private File markerDirectory;
	private File imageDirectory;

	private String imageRelativePath;
	private String collectDayString;

	private int year;
	private int weekOfYear;

	public ServiceConfig(OnlineStoreConst constant, String collectDay) {
		this.constant = constant;
		this.collectDayString = collectDay;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		String dir = properties.getProperty("crawler.base.directory");

		Date date = DateUtils.parseDate(collectDayString);

		Calendar c = Calendar.getInstance();
		c.setTime(date);

		year = c.get(Calendar.YEAR);
		weekOfYear = c.get(Calendar.WEEK_OF_YEAR);

		imageRelativePath = String.format("%s/%s", properties.getProperty("crawler.image.relative.path"), constant.name());

		baseDirectory = new File(new File(dir, collectDayString), constant.name());
		backupDirectory = new File(baseDirectory, OnlineStoreConst.CONFIG_BACKUP_DIR);
		inputDirectory = new File(baseDirectory, OnlineStoreConst.CONFIG_INPUT_DIR);
		inputQueueDirectory = new File(baseDirectory, OnlineStoreConst.CONFIG_INPUT_QUEUE_DIR);
		inputBackupDirectory = new File(baseDirectory, OnlineStoreConst.CONFIG_INPUT_BAK_DIR);
		outputDirectory = new File(baseDirectory, OnlineStoreConst.CONFIG_OUTPUT_DIR);
		markerDirectory = new File(baseDirectory, OnlineStoreConst.CONFIG_MARKER_DIR);
		imageDirectory = new File(properties.getProperty("crawler.image.base.directory"), imageRelativePath);

		if (!backupDirectory.exists())
			backupDirectory.mkdirs();
		if (!inputDirectory.exists())
			inputDirectory.mkdirs();
		if (!inputQueueDirectory.exists())
			inputQueueDirectory.mkdirs();
		if (!inputBackupDirectory.exists())
			inputBackupDirectory.mkdirs();
		if (!outputDirectory.exists())
			outputDirectory.mkdirs();
		if (!markerDirectory.exists())
			markerDirectory.mkdirs();
		if (!getImageDirectory().exists())
			getImageDirectory().mkdirs();
	}

	public File getBaseDirectory() {
		return baseDirectory;
	}

	public File getBackupDirectory() {
		return backupDirectory;
	}

	public File getInputDirectory() {
		return inputDirectory;
	}

	public File getInputQueueDirectory() {
		return inputQueueDirectory;
	}

	public File getInputBackupDirectory() {
		return inputBackupDirectory;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public File getMarkerDirectory() {
		return markerDirectory;
	}

	public File getImageDirectory() {
		return imageDirectory;
	}

	public String getImageRelativePath() {
		return imageRelativePath;
	}

	public String getImageRelativePath(String goodsNum) {
		return String.format("%s/%s", imageRelativePath, goodsNum);
	}

	public String getCollectDayString() {
		return collectDayString;
	}

	public int getYear() {
		return year;
	}

	public int getWeekOfYear() {
		return weekOfYear;
	}
}
