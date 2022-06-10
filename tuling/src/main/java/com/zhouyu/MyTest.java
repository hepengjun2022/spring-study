package com.zhouyu;

import com.zhouyu.service.MyBeanFactoryPostProcessor1;
import com.zhouyu.service.MyBeanFactoryPostProcessor2;
import com.zhouyu.service.OrderService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MyTest {

	public static void main(String[] args) {

//		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
//		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext("com.zhouyu.service");
//		applicationContext.addBeanFactoryPostProcessor(new MyBeanFactoryPostProcessor1());
//		applicationContext.addBeanFactoryPostProcessor(new MyBeanFactoryPostProcessor2());

		ClassPathXmlApplicationContext applicationContext=new ClassPathXmlApplicationContext("spring.xml");
		OrderService orderService = (OrderService) applicationContext.getBean("orderService");
		orderService.test();
	}
}
