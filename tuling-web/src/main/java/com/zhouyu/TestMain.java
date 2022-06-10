package com.zhouyu;

import com.zhouyu.service.ZhouyuService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestMain {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
		ZhouyuService zhouyuService = (ZhouyuService) applicationContext.getBean("zhouyuService");
		zhouyuService.test();
	}
}
