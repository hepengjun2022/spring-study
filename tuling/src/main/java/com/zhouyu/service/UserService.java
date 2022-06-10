package com.zhouyu.service;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

//@Component

public class UserService implements SmartInitializingSingleton {

//	@Autowired
	private OrderService orderService;


	private String beanName;


	public void test() {
		System.out.println(beanName);
	}

	@Override
	public void afterSingletonsInstantiated() {

	}
}

