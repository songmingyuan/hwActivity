package com.huiway.activiti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableScheduling //开启定时任务
@EnableSwagger2
@SpringBootApplication
public class Application {

	private static ConfigurableApplicationContext context;
	
	public static void main(String[] args) { 
		context =  SpringApplication.run(Application.class, args);
	}

	public static ConfigurableApplicationContext getContext() {
		return context;
	}
	
}
