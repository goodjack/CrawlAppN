package com.ssfashion.bigdata.crawler.file.aws;

import static com.ssfashion.bigdata.crawler.file.aws.AwsConfig.AWS_ACCESS_KEY;
import static com.ssfashion.bigdata.crawler.file.aws.AwsConfig.BUCKET_NAME;
import static com.ssfashion.bigdata.crawler.file.aws.AwsConfig.IMG_DIR_PREFIX;
import static com.ssfashion.bigdata.crawler.file.aws.AwsConfig.RAW_DATA_DIR_PREFIX;
import static com.ssfashion.bigdata.crawler.file.aws.AwsConfig.SECRET_KEY;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.net.URLEncoder;
import javax.imageio.ImageIO;

import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.ServiceConfig;
import com.eopcon.crawler.samsungcnt.service.net.HttpRequestService;
import com.eopcon.crawler.samsungcnt.service.net.Result;
import com.ssfashion.bigdata.crawler.util.DateUtil;

public class RawDataFileManagement {

	protected static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_COMMON);
	private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
	
	private String siteName;
	private String imgDirectoryNm;
	private String rawDataDirectoryNm;
	protected ServiceConfig config;
	private AmazonS3 s3Client;
    
    public RawDataFileManagement() {    	
        AWSCredentials credentials = new BasicAWSCredentials(AWS_ACCESS_KEY, SECRET_KEY);
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);
        
        this.s3Client = AmazonS3ClientBuilder.standard()
        		        .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        
        AccessControlList acl = new AccessControlList();	
        acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
    }
    
    /**
     * siteName 을 받아서 초기화
     * siteName 은 S3 버킷의 업로드 경로에 어떤 수집사이트인지 구분짓기 위해 사용됨.
     * @param siteName
     */
    public RawDataFileManagement(String siteName) {
    	AWSCredentials credentials = new BasicAWSCredentials(AWS_ACCESS_KEY, SECRET_KEY);
    	ClientConfiguration clientConfig = new ClientConfiguration();
    	clientConfig.setProtocol(Protocol.HTTP);
    	this.s3Client = AmazonS3ClientBuilder.standard()
    			.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    	this.siteName = siteName;
    	this.imgDirectoryNm = IMG_DIR_PREFIX + siteName;
    	// /rawData/sites/year/month/date/www_gmarket_co_kr_imvely.json :gmarket 입점의 경우
    	// /rawData/sites/year/month/date/www_musinsa_co_kr.json        :자체사이트의 경우
    	this.rawDataDirectoryNm  = RAW_DATA_DIR_PREFIX + siteName;// <-- 이 설정은 현재 사용되지 않음. 위의 형태로 디렉터리 작성하기 때문에
    }
    
	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	// aws 버킷 목록 가져오기
    public List<Bucket> getBucketList() {
        return s3Client.listBuckets();
    }
     
    // 버킷 생성
    public Bucket createBucket(String bucketName) {
        return s3Client.createBucket(bucketName);
    }
     
    // 폴더 생성 (폴더는 파일명 뒤에 "/"를 붙여야한다.)
    public void createFolder(String bucketName, String folderName) {
    	s3Client.putObject(bucketName, folderName + "/", new ByteArrayInputStream(new byte[0]), new ObjectMetadata());
    }
    
    /**
     * s3 저장소에 이미지 파일을 업로드 한다.
     * @param imgUrl
     * @param fileName
     * @param request
     * @return
     * @throws Exception
     */
	//public String fileUpload_zara(String imgUrl, String fileName, HttpRequestService request) throws Exception {
	public String fileUpload_zara(String fileName, File file) throws Exception {

		String s3ImageUrl = null;
		//String imageFileName = convertFileName(imgUrl, fileName, request);
		
		System.out.println((getClass().getSimpleName() + "=================================================="));
		System.out.println((getClass().getSimpleName() + "converted image file name=" + fileName));
		System.out.println((getClass().getSimpleName() + "=================================================="));

		String ext = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());

		// S3에 파일이 없으면 수행
		String s3DirectoryNm = BUCKET_NAME + IMG_DIR_PREFIX + siteName;
		if (isNotExist(BUCKET_NAME, imgDirectoryNm + "/" + fileName)) {

			//ByteArrayInputStream imgStream = new ByteArrayInputStream(fos, 0, fos.length);

			// 업로드 객체의 메타데이터 설정
			//ObjectMetadata imgObjectMeta = new ObjectMetadata();
			//업로드하는 객체의 MIME-TYPE 세팅
			//imgObjectMeta.setContentType("image/" + ext);
			//업로드 객체의 컨텐츠 길이 세팅. 이 세팅을 하지 않으면 업로드시 지속적으로 WARN 이 발생함. WARN 때문에 문제가 발행하지는 않으나 설정해둠.
			//imgObjectMeta.setContentLength(fos.length);
			s3Client.putObject(BUCKET_NAME, imgDirectoryNm + "/" + fileName, file);
			// 두번째 인자인 키 로 식별되는 S3 의 객체에 접근권한을 public-read 로 설정함.
			s3Client.setObjectAcl(BUCKET_NAME, imgDirectoryNm + "/" + fileName, CannedAccessControlList.PublicRead);
		}

		s3ImageUrl = s3Client.getUrl(BUCKET_NAME, imgDirectoryNm + "/" + fileName).toString();
		System.out.println("=============s3ImageUrl1===============");
		System.out.println("s3ImageUrl1 --> " + s3ImageUrl);
		System.out.println("=============s3ImageUrl2===============");
		return s3ImageUrl;
	}
    /**
     * s3 저장소에 이미지 파일을 업로드 한다.
     * @param imgUrl
     * @param fileName
     * @param request
     * @return
     * @throws Exception
     */
	public String fileUpload(String imgUrl, String fileName, HttpRequestService request) throws Exception {

		String s3ImageUrl = null;
		//imgUrl = URLEncoder.encode(imgUrl, "UTF-8");
		long startTime = System.currentTimeMillis();
		String imageFileName = convertFileName_new(imgUrl, fileName, request);
		long endTime = System.currentTimeMillis();
		
		System.out.println("Time ==========> " + (endTime - startTime) );
		System.out.println((getClass().getSimpleName() + "=================================================="));
		System.out.println((getClass().getSimpleName() + "converted image file name=" + imageFileName));
		System.out.println((getClass().getSimpleName() + "=================================================="));

		String ext = imageFileName.substring(imageFileName.lastIndexOf('.') + 1, imageFileName.length());

		// S3에 파일이 없으면 수행
		String s3DirectoryNm = BUCKET_NAME + IMG_DIR_PREFIX + siteName;
		if (isNotExist(BUCKET_NAME, imgDirectoryNm + "/" + imageFileName) && ext.length() > 0) {
			System.out.println("imgUrl ==> " + imgUrl );
			URL url = new URL(imgUrl);
			BufferedImage img = ImageIO.read(url);

			final ByteArrayOutputStream output = new ByteArrayOutputStream() {
				@Override
				public synchronized byte[] toByteArray() {
					return this.buf;
				}
			};
			ImageIO.write(img, ext, output);

			ByteArrayInputStream imgStream = new ByteArrayInputStream(output.toByteArray(), 0, output.size());

			// 업로드 객체의 메타데이터 설정
			ObjectMetadata imgObjectMeta = new ObjectMetadata();
			//업로드하는 객체의 MIME-TYPE 세팅
			imgObjectMeta.setContentType("image/" + ext);
			//업로드 객체의 컨텐츠 길이 세팅. 이 세팅을 하지 않으면 업로드시 지속적으로 WARN 이 발생함. WARN 때문에 문제가 발행하지는 않으나 설정해둠.
			imgObjectMeta.setContentLength(output.size());
			
			s3Client.putObject(BUCKET_NAME, imgDirectoryNm + "/" + imageFileName, imgStream, imgObjectMeta);
			// 두번째 인자인 키 로 식별되는 S3 의 객체에 접근권한을 public-read 로 설정함.
			s3Client.setObjectAcl(BUCKET_NAME, imgDirectoryNm + "/" + imageFileName, CannedAccessControlList.PublicRead);
		}

		s3ImageUrl = s3Client.getUrl(BUCKET_NAME, imgDirectoryNm + "/" + imageFileName).toString();
		
		logger.info(getClass().getSimpleName() , "++++++++++++++++++++++++++++++++++");
		logger.info(getClass().getSimpleName() , "imgDirectoryNm + \"/\" + imageFileName=" + imgDirectoryNm + "/" + imageFileName);
		logger.info(getClass().getSimpleName() , "s3ImageUrl=" + s3ImageUrl);
		logger.info(getClass().getSimpleName() , "++++++++++++++++++++++++++++++++++");
		System.out.println(getClass().getSimpleName() + "++++++++++++++++++++++++++++++++++");
		System.out.println(getClass().getSimpleName() + "imgDirectoryNm + \"/\" + imageFileName=" + imgDirectoryNm + "/" + imageFileName);
		System.out.println(getClass().getSimpleName() + "s3ImageUrl=" + s3ImageUrl);
		System.out.println(getClass().getSimpleName() + "++++++++++++++++++++++++++++++++++");

		return s3ImageUrl;
	}
    
    /**
     * gmarket 입점인 stylenanda, imvely, ain 의 경우 이미지파일이 CDN에 있으므로 imageURL에서 이미지 이름이 드러나지 않는다.
     * 따라서 이미지명을 해당 imgaeURL로 HTTP 요청을 하여 헤더를 읽어 파일의 타입을 추출하여 확장자를 붙임.
     * @param imageURL
     * @param imageFileName
     * @param request
     * @return
     */
	public String convertFileName(String imageURL, String imageFileName, HttpRequestService request) {

		Result result = null;

		try {
			Pattern pattern = Pattern.compile("\\.(jpg|jpeg|gif|png|bmp)$", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(imageURL.replaceAll("\\?.*", ""));
			boolean found = matcher.find();

			if (found)
				imageFileName += "." + matcher.group(1).toLowerCase();
			else {
				request.openConnection(imageURL);
				request.addRequestHeader("User-Agent", USER_AGENT);

				result = request.executeWithHead(false);
				int responseCode = result.getResponseCode();

				if (responseCode == HttpStatus.SC_OK) {
					// 확장자가 없을 시에는 Response 헤더의 Content-Type을 가져온다.
					if (!found) {
						Map<String, List<String>> header = result.getHeader();
						if (header.containsKey("Content-Type")) {
							String contentType = header.get("Content-Type").get(0);
							if (contentType.matches("^image/.+$")) {
								imageFileName += "." + contentType.replaceAll("^image/(.+)$", "$1");
							}
						}
					}	
				} else if (responseCode == HttpStatus.SC_NOT_FOUND) {

			}
			}			
			

		} catch (Exception e) {
			
		} finally {
			
		}
		imageFileName = imageFileName.replace("/", "_");
		return imageFileName;
	}
	
	public String convertFileName_new(String imageURL, String imageFileName, HttpRequestService request) throws Exception {

		//ZARA 경우 주석  result2 관련 모두 주석
		Result result = null;
		//String[] arrUrl = null;
		String compareUrl = imageURL;

		try {
			Pattern pattern = Pattern.compile("\\.(jpg|jpeg|gif|png|bmp)$", Pattern.CASE_INSENSITIVE);
			
			Matcher matcher = pattern.matcher(compareUrl.replaceAll("\\?.*", ""));
			boolean found = matcher.find();

			if (found) {
				imageFileName += "." + matcher.group(1).toLowerCase();
				System.out.println("imageFileName +found " + imageFileName);
			}
			else {
				request.openConnection(imageURL);
				request.addRequestHeader("User-Agent", USER_AGENT);

				result = request.executeWithHead(false);
				int responseCode = result.getResponseCode();

				if (responseCode == HttpStatus.SC_OK) {
					// 확장자가 없을 시에는 Response 헤더의 Content-Type을 가져온다.
					if (!found) {
						Map<String, List<String>> header = result.getHeader();
						System.out.println("header-Type " + header.toString());
						if (header.containsKey("Content-Type")) {
							String contentType = header.get("Content-Type").get(0);
							System.out.println("Content-Type " + contentType);
							if (contentType.matches("^image/.+$")) {
								imageFileName += "." + contentType.replaceAll("^image/(.+)$", "$1");
								System.out.println("imageFileName found " + imageFileName);
							}
						}
					}	
				} else if (responseCode == HttpStatus.SC_NOT_FOUND) {

			}
			}			
			

		} catch (Exception e) {
			logger.error(getClass().getSimpleName() , e.getMessage());
			throw e;
		} finally {
			
		}
		
		imageFileName = imageFileName.replace("/", "_");
		
		System.out.println("imageFileName--->" + imageFileName);
		return imageFileName;
	}

    // 파일 삭제
    public void fileDelete(String bucketName, String fileName) {
    	s3Client.deleteObject(bucketName, fileName);
    }
     
    // 파일 유무 : 없으면 TRUE
	public boolean isNotExist(String bucket, String key) {
		boolean isNotExist = true;
		try {
//			int idx = key.lastIndexOf('.');
//			key = key.substring(idx + 1, key.length());
			ObjectMetadata objectMetadata = s3Client.getObjectMetadata(bucket, key);
			
			logger.debug(getClass().getSimpleName(), objectMetadata.getInstanceLength() + ",	" + objectMetadata.getLastModified());
			System.out.println(getClass().getSimpleName() + objectMetadata.getInstanceLength() + ",	" + objectMetadata.getLastModified());
			
			isNotExist = false;
		} catch (AmazonS3Exception s3e) {
			if (s3e.getStatusCode() == 404) {
				isNotExist = true;
			} else {
				throw s3e; // rethrow all S3 exceptions other than 404
			}
		} catch (Exception e) {
			throw e;
		}

		logger.debug(getClass().getSimpleName(), "============================!!!!!!!!!!!!!!!!!");
		logger.debug(getClass().getSimpleName(), "buket=" + bucket + ",key=" + key);
		logger.debug(getClass().getSimpleName(), isNotExist ? "파일 없음" : "파일 있음");
		logger.debug(getClass().getSimpleName(), "============================!!!!!!!!!!!!!!!!!");
		
		return isNotExist;
	}
	
	
	/**
	 * 사이트별 크롤링 정보가 저장된 json raw-data 파일 1개를 s3로 전송함.
	 * @param dateTime
	 * @param siteName
	 * @param file
	 * @return
	 */
	public boolean saveTotalRawFile(String collectDay, File file) {
		boolean result = false;
		try {
//			String year = DateUtil.getTodayStr(dateTime, "yyyy");
//			String month = DateUtil.getTodayStr(dateTime, "MM");
//			String date = DateUtil.getTodayStr(dateTime, "dd");
			
			String year = collectDay.substring(0, 4);
			String month = collectDay.substring(4, 6);
			String date = collectDay.substring(6, 8);

			String keyName = RAW_DATA_DIR_PREFIX + year + "/" + month + "/" + date + "/" + file.getName();
//			logger.info(getClass().getSimpleName(),"keyName=" + keyName);

			if (isNotExist(BUCKET_NAME, keyName)) {
				s3Client.putObject(new PutObjectRequest(BUCKET_NAME, keyName, file));
				//file.delete();
				result = true;
			} else {
				logger.debug(getClass().getSimpleName(), "isNotExist(" + BUCKET_NAME + "," + keyName + ")=false");
			}
		} catch (Exception e) {
			logger.error(getClass().getSimpleName(), e.getMessage());
//			 logger.info(LogUtil.getPrintStackTrace(e));
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	
	public void makeZipFile() throws Exception {
		long startTime = System.currentTimeMillis();
        
        BufferedReader in = new BufferedReader(new FileReader("test.txt"));
        BufferedOutputStream out =
                    new BufferedOutputStream(
                               new GZIPOutputStream(
                                           new FileOutputStream("test.gz")));
        logger.info(getClass().getSimpleName(), "Writing file -> {}, zip->{}", "test.txt", "test.gz");
        int c;
        while((c=in.read()) != -1)
              out.write(c);
        in.close();
        out.close();
       
        long endTime = System.currentTimeMillis();
        logger.info(getClass().getSimpleName(), "압축시간 : " + (endTime - startTime) + " ms");

	}
    
}
