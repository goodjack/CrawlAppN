package com.eopcon.crawler.samsungcnt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.support.CommandLineJobRunner;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;

public class Launch {

	private static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_BATCH);

	private ClassPathXmlApplicationContext applicationContext;

	private static final String SPRING_CONFIG = "classpath:config/spring/spring-batch.xml";

	public static void main(String[] args) {

		Launch launch = null;

		try {
			String command = "run";
			if (args.length > 0)
				command = StringUtils.defaultIfEmpty(args[0], "run");

			if (StringUtils.isEmpty(System.getProperty("spring.profiles.active")))
				System.setProperty("spring.profiles.active", command);
			if (StringUtils.isEmpty(System.getProperty("app.log.dir")))
				System.setProperty("app.log.dir", ".");

			if (command.equals("run")) { // 스케쥴러로 실행
				launch = new Launch();
				launch.loadApplicationContext();
			} else if (command.equals("execute")) { // 즉시실행
				String collectDay = null;
				List<String> arguments = new ArrayList<>();

				arguments.add(SPRING_CONFIG);

				Scanner scan = new Scanner(System.in);
				String jobName = null;
				String jobParam = null;

				System.out.print("\n\n=> Input Job Name : ");
				jobName = scan.nextLine();

				arguments.add(jobName);

				System.out.print("\n\n=> Input Job Parameter(name=value&name1=value1) : ");
				jobParam = scan.nextLine();

				if (!StringUtils.isEmpty(jobParam)) {
					String[] params = jobParam.split("&");

					for (String param : params) {
						if(param.startsWith("collectDay="))
							collectDay = param.replaceAll("^collectDay=", "");
						arguments.add(param);
						System.out.println("# Job Parameter -> " + param);
					}
				}
				
				if(StringUtils.isEmpty(collectDay))		
					arguments.add("collectDay=" + new SimpleDateFormat("yyyyMMdd").format(new Date()));
				CommandLineJobRunner.main(arguments.toArray(new String[arguments.size()]));

				scan.close();
			} else {
				System.out.print("=> Unknown Command : " + command);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void loadApplicationContext() throws Exception {
		applicationContext = new ClassPathXmlApplicationContext(new String[] { SPRING_CONFIG }, false);
		applicationContext.refresh();
	}
}
