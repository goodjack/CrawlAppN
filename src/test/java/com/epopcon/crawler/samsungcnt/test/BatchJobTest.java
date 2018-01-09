package com.epopcon.crawler.samsungcnt.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.instrument.classloading.tomcat.TomcatLoadTimeWeaver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eopcon.crawler.samsungcnt.common.file.LocalFile;
import com.eopcon.crawler.samsungcnt.common.file.LocalFileSet;
import com.eopcon.crawler.samsungcnt.common.file.service.LocalFileService;
import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.service.CategoryMapper;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.net.Result;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:config/spring/spring-test.xml" })
public class BatchJobTest {

	private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";

	private static Logger logger = LoggerFactory.getLogger(BatchJobTest.class);

	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private JobLauncher jobLauncher;
	@Autowired
	private JobRepository jobRepository;
	@Autowired
	private Properties properties;

	private Map<OnlineStoreConst, OnlineStoreCrawler> crawlers = new HashMap<>();

	private String collectDay = new SimpleDateFormat("yyyyMMdd").format(new Date());
	private String imagePath;

	@Before
	public void setUp() {
		imagePath = properties.getProperty("crawler.image.base.directory") + properties.getProperty("crawler.image.relative.path");
	}

	//@Test
	public void testImport() {
		CSVReader reader = null;
		InputStream in = null;

		try {
			in = new FileInputStream(new File("C:/IMAGE.csv"));
			reader = new CSVReader(new InputStreamReader(in, "euc-kr"), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1);

			String[] s;
			while ((s = reader.readNext()) != null) {
				String site = s[0];
				String brandCode = s[1];
				String skuNum = s[2];
				String goodsImage = s[3];
				String path = s[4];
				String exists = s[5];
				
				if (!goodsImage.startsWith("http://") && !goodsImage.startsWith("https://")) {
					goodsImage = "http://211.253.26.85:19080/public/crawler" + goodsImage;
				}
				
				if(exists.equalsIgnoreCase("false")) {
					OnlineStoreConst type = OnlineStoreConst.valueOf(site.matches("^\\d+.*$") ? "_" + site : site);
					OnlineStoreCrawler crawler = crawlers.get(type);

					if (crawler == null) {
						crawler = (OnlineStoreCrawler) applicationContext.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_CRAWLER, type, collectDay);
						crawlers.put(type, crawler);
					}

					goodsImage = crawler.saveImage(skuNum, goodsImage);

					String sourcePath = imagePath + goodsImage;
					File source = new File(sourcePath);
					
					String targetPath = imagePath + "/" + brandCode + "_" + source.getName();
					File target = new File(targetPath);

					if (source.exists()) {
						File dir = new File(target.getParent());
						if (!dir.exists())
							dir.mkdirs();
						FileUtils.copyFile(source, target);
					}
					System.out.println(target.getAbsolutePath() + " | " + target.exists());
				} else {
					/*
					OnlineStoreConst type = OnlineStoreConst.valueOf(site.matches("^\\d+.*$") ? "_" + site : site);
					OnlineStoreCrawler crawler = crawlers.get(type);

					if (crawler == null) {
						crawler = (OnlineStoreCrawler) applicationContext.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_CRAWLER, type, collectDay);
						crawlers.put(type, crawler);
					}

					goodsImage = crawler.saveImage(skuNum, goodsImage);

					String sourcePath = imagePath + goodsImage;
					File source = new File(sourcePath);
					
					String targetPath = imagePath + "/" + brandCode + "_" + source.getName();
					File target = new File(targetPath);

					if (source.exists()) {
						File dir = new File(target.getParent());
						if (!dir.exists())
							dir.mkdirs();
						FileUtils.copyFile(source, target);
					}
					System.out.println(target.getAbsolutePath() + " | " + target.exists());
					*/
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(reader);
		}
	}
	
	//@Test
	public void testImport2() {
		LocalFileService lfs = new LocalFileService();
		LocalFileSet fileSet = lfs.ls("C:/TEMP/images/product/이전전달용", true);
		LocalFileSet fileSet2 = lfs.ls("C:/TEMP/images/product3", true);
		
		System.out.println(fileSet.getFiles().size());
		
		Set<String> list = new HashSet<>();
		for(LocalFile lf : fileSet.getFiles()){
			String fileName = lf.getFile().getName();
			list.add(fileName);
		}
		
		
		for(LocalFile lf : fileSet2.getFiles()){
			String fileName = lf.getFile().getName();
			
			if(list.contains(fileName)){
				lf.getFile().delete();
				System.out.println(lf.getFile().getAbsolutePath() + " | " + lf.getFile().exists());
			}
		}
	}

	@Test
	public void testCsv() {
		CSVReader reader = null;
		CSVWriter writer = null;

		InputStream in = null;
		OutputStream out = null;

		final String[] HEADER = new String[] { "SITE","BRAND_CODE","STD_CATE_NAME_1","STD_CATE_NAME_2","STD_CATE_NAME_3","STD_CATE_NAME_4","GOODS_NUM","SKU_NUM","SKU_NAME","COLOR","GOODS_IMAGE" };
		//final String[] HEADER = new String[] { "COLLECT_WEEK", "COLLECT_START_DAY", "COLLECT_END_DAY", "SITE","BRAND_CODE","STD_CATE_NAME_1","STD_CATE_NAME_2","STD_CATE_NAME_3","STD_CATE_NAME_4","GOODS_NUM","SKU_NUM","SKU_NAME","COLOR","GOODS_IMAGE","GOODS_IMAGE_PATH","SELL_AMOUNT","RNUM" };

		try {

			in = new FileInputStream(new File("C:/TEST5.csv"));
			out = new FileOutputStream(new File("C:/TEST5OfCopy.csv"));

			reader = new CSVReader(new InputStreamReader(in, "utf8"), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1);
			writer = new CSVWriter(new OutputStreamWriter(out, "utf8"), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER);
			String[] s;

			writer.writeNext(HEADER);

			while ((s = reader.readNext()) != null) {
				int i=0;
				
				//String collect_Week = s[i++];
				//String collectStartDay = s[i++];
				//String collectEndDay = s[i++];
				String site = s[i++];
				String brandCode = s[i++];
				String releaseDt = s[i++];
				String stdCateName1 = s[i++];
				String stdCateName2 = s[i++];
				String stdCateName3 = s[i++];
				String stdCateName4 = s[i++];
				String goodsNum = s[i++];
				String skuNum = s[i++];
				String skuName = s[i++];
				String color = s[i++];
				String goodsImage = s[i++];
				//String sellAmount = s[i++];
				//String rnum = s[i++];

				String imagePath = String.format("/%s/%s", site, goodsImage.replaceAll("^.+/([^/]+)$", "$1"));
				File image = null;
				
				try{
					image = new File(this.imagePath, imagePath);					
				}catch(Exception e) {
				}
				
				if (!goodsImage.startsWith("http://") && !goodsImage.startsWith("https://")) {
					goodsImage = "http://211.253.26.85:19080/public/crawler" + goodsImage;
				}
				
				if(image == null || !image.exists()) {
					OnlineStoreConst type = OnlineStoreConst.valueOf((site.matches("^\\d+.*$") ? "_" + site : site).toUpperCase());
					OnlineStoreCrawler crawler = crawlers.get(type);

					if (crawler == null) {
						crawler = (OnlineStoreCrawler) applicationContext.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_CRAWLER, type, collectDay);
						crawlers.put(type, crawler);
					}
					String url = goodsImage;
					
					url = url.replaceAll("\\[", "%5B");
					url = url.replaceAll("\\]", "%5D");
					
					imagePath = crawler.saveImage(skuNum, url);
					System.out.println(goodsNum);
				}
				
				writer.writeNext(new String[]{site, brandCode, releaseDt, stdCateName1, stdCateName2, stdCateName3, stdCateName4, goodsNum, skuNum, skuName, color, goodsImage, imagePath});
				//writer.writeNext(new String[]{collect_Week, collectStartDay, collectEndDay, site, brandCode, stdCateName1, stdCateName2, stdCateName3, stdCateName4, goodsNum, skuNum, skuName, color, goodsImage, imagePath, sellAmount, rnum});
			}
			Thread.sleep(5000);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(out);
		}
	}

	//@Test
	public void testMapping() {
		CSVReader reader = null;
		InputStream in = null;

		Map<OnlineStoreConst, CategoryMapper> mappers = new HashMap<>();

		try {
			in = new FileInputStream(new File("C:/TEST.csv"));
			reader = new CSVReader(new InputStreamReader(in, "utf-8"), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 0);

			String[] s;
			while ((s = reader.readNext()) != null) {
				String site = s[0];
				String cate1 = s[1];
				String cate2 = s[2];
				String cate3 = s[3];
				String cate4 = s[4];

				CategoryMapper mapper = null;
				if (mappers.containsKey(OnlineStoreConst.valueOf(site))) {
					mapper = mappers.get(OnlineStoreConst.valueOf(site));
					mappers.put(OnlineStoreConst.valueOf(site), mapper);
				} else
					mapper = (CategoryMapper) applicationContext.getBean(OnlineStoreConst.BEAN_NAME_CATEGORY_MAPPER, OnlineStoreConst.valueOf(site));

				Category category = new Category();

				if (StringUtils.isNotEmpty(cate1))
					category.addCategoryName(cate1);
				if (StringUtils.isNotEmpty(cate2))
					category.addCategoryName(cate2);
				if (StringUtils.isNotEmpty(cate3))
					category.addCategoryName(cate3);
				if (StringUtils.isNotEmpty(cate4))
					category.addCategoryName(cate4);
				
				Map<String, String> meta = new HashMap<String, String>();
				mapper.mappingCategory(meta, Arrays.asList(category));
				
				int status = Integer.parseInt(meta.get(OnlineStoreConst.KEY_STATUS));
				
				System.out.println(meta);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(reader);
		}
	}

	// @Test
	public void testSaveImage() {
		CSVReader reader = null;
		CSVWriter writer = null;

		InputStream in = null;
		OutputStream out = null;

		final String[] HEADER = new String[] { "GOODS_NUM", "SKU_NUM", "GOODS_IMAGE", "BRAND_CODE", "RELEASE_DT", "SELL_AMOUNT", "PRICE", "COLLECT_DAY", "MATERIALS", "COLOR" };

		try {

			in = new FileInputStream(new File("C:/TEST3.csv"));
			out = new FileOutputStream(new File("C:/TEST4.csv"));

			reader = new CSVReader(new InputStreamReader(in, "utf8"), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1);
			writer = new CSVWriter(new OutputStreamWriter(out, "euckr"), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER);
			String[] s;

			writer.writeNext(HEADER);

			while ((s = reader.readNext()) != null) {
				String site = s[0];
				String goodsNum = s[1];
				String goodsImage = s[2];

				OnlineStoreCrawler crawler = crawlers.get(OnlineStoreConst.valueOf(site));

				if (crawler == null) {
					crawler = (OnlineStoreCrawler) applicationContext.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_CRAWLER, OnlineStoreConst.valueOf(site), collectDay);
					crawlers.put(OnlineStoreConst.valueOf(site), crawler);
				}

				System.out.println(crawler.saveImage("1", goodsImage));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(writer);
		}
	}

	//@Test
	public void testUniqloJob() throws Exception {

		Job job = applicationContext.getBean("productJob", Job.class);

		JobParametersBuilder builder = new JobParametersBuilder();

		builder.addString("type", OnlineStoreConst.UNIQLO.toString());
		builder.addString("collectDay", collectDay);
		// builder.addString("collectDay", "20170303");
		// builder.addLong("timestamp", System.currentTimeMillis());

		JobLauncherTestUtils util = new JobLauncherTestUtils();

		util.setJob(job);
		util.setJobLauncher(jobLauncher);
		util.setJobRepository(jobRepository);

		JobExecution jobExecution = util.launchJob(builder.toJobParameters());

		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}

	// @Test
	public void testSpaoJob() throws Exception {
		Job job = applicationContext.getBean("productJob", Job.class);

		JobParametersBuilder builder = new JobParametersBuilder();

		builder.addString("type", OnlineStoreConst.SPAO.toString());
		// builder.addString("collectDay", "20170319");
		builder.addString("collectDay", collectDay);
		builder.addLong("timestamp", System.currentTimeMillis());

		JobLauncherTestUtils util = new JobLauncherTestUtils();

		util.setJob(job);
		util.setJobLauncher(jobLauncher);
		util.setJobRepository(jobRepository);

		JobExecution jobExecution = util.launchJob(builder.toJobParameters());

		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

	}

	// @Test
	public void testLfmallJob() throws Exception {

		Job job = applicationContext.getBean("productJob", Job.class);

		JobParametersBuilder builder = new JobParametersBuilder();

		builder.addString("type", OnlineStoreConst.LFMALL.toString());
		builder.addString("collectDay", collectDay);
		// builder.addString("collectDay", "20170222");
		builder.addLong("timestamp", System.currentTimeMillis());

		JobLauncherTestUtils util = new JobLauncherTestUtils();

		util.setJob(job);
		util.setJobLauncher(jobLauncher);
		util.setJobRepository(jobRepository);

		JobExecution jobExecution = util.launchJob(builder.toJobParameters());

		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}

	// @Test
	public void testMixxoJob() throws Exception {

		Job job = applicationContext.getBean("productJob", Job.class);

		JobParametersBuilder builder = new JobParametersBuilder();

		builder.addString("type", OnlineStoreConst.MIXXO.toString());
		builder.addString("collectDay", collectDay);
		// builder.addString("collectDay", "20170306");
		builder.addLong("timestamp", System.currentTimeMillis());

		JobLauncherTestUtils util = new JobLauncherTestUtils();

		util.setJob(job);
		util.setJobLauncher(jobLauncher);
		util.setJobRepository(jobRepository);

		JobExecution jobExecution = util.launchJob(builder.toJobParameters());
		// JobExecution jobExecution = util.launchStep("product.step.2", builder.toJobParameters());

		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}
}
