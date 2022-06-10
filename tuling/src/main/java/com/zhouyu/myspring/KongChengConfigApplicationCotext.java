package com.zhouyu.myspring;

import com.zhouyu.spring.*;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KongChengConfigApplicationCotext {

	private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
	private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
	private Map<String, Object> singletonObjects = new HashMap<>();
	private Class configClass;

	public KongChengConfigApplicationCotext(Class configClass) {
		this.configClass = configClass;
		//扫描cofigClass指定的扫描路径下的类
		scan(configClass);
		//遍历beanDefinitionMap，将注入（带Component）的类放入单例Map中
		for (Map.Entry<String, BeanDefinition> key : beanDefinitionMap.entrySet()) {
			String beanName = key.getKey();
			BeanDefinition beanDefinition = key.getValue();
			String scope = beanDefinition.getScope();
			if (scope.equals("singleton")) {
				Object bean = createBean(beanName, beanDefinition);
				singletonObjects.put(beanName, bean);
			}
		}
	}

	private Object createBean(String beanName, BeanDefinition beanDefinition) {
		Class aClass = beanDefinition.getType();
		Object instance = null;
		try {
			instance = aClass.getConstructor().newInstance();
//			getFields()：获得某个类的所有的公共（public）的字段，包括父类中的字段。
//			getDeclaredFields()：获得某个类的所有声明的字段，即包括public、private和proteced，但是不包括父类的申明字段。
			for (Field field : aClass.getDeclaredFields()) {
				if (field.isAnnotationPresent(Autowired.class)) {
					field.setAccessible(true);
					//给该类使用了Autowired的对象进行依赖注入
					field.set(instance, getBean(field.getName()));
				}
			}
			//如果该类实现了BeanNameAware接口，则执行setBeanName()
			if (instance instanceof BeanNameAware) {
				((BeanNameAware) instance).setBeanName(beanName);
			}
			//执行初始化前的操作
			for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
				instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
			}
			//如果该类实现了InitializingBean初始化Bean接口，则执行afterPropertiesSet()方法。
			if (instance instanceof InitializingBean) {
				((InitializingBean) instance).afterPropertiesSet();
			}
			//执行初始化后的操作
			for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
				instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
			}

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return instance;
	}

	private void scan(Class configClass) {
		if (configClass.isAnnotationPresent(ComponentScan.class)) {
			//读取改配置类上的主节，获取路径
			ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
			String path = componentScanAnnotation.value();
			String replace = path.replace(".", "/");
			//获取到相对路径，将该路径下的类注册到容器里
			ClassLoader classLoader = KongChengConfigApplicationCotext.class.getClassLoader();
			URL resource = classLoader.getResource(replace);
			File file = new File(resource.getFile());
			//如果获取到的资源是一个文件夹
			if (file.isDirectory()) {
				//遍历文件夹下的类
				for (File f : file.listFiles()) {
					String absolutePath = f.getAbsolutePath();
					absolutePath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
					absolutePath = absolutePath.replace("\\", ".");
					try {
						//加载类
						Class<?> aClass = classLoader.loadClass(absolutePath);
						//判断该类有没有component注解
						if (aClass.isAnnotationPresent(Component.class)) {
							//
							if (BeanPostProcessor.class.isAssignableFrom(aClass)) {
								BeanPostProcessor beanPostProcessor = (BeanPostProcessor) aClass.getConstructor().newInstance();
								beanPostProcessorList.add(beanPostProcessor);
							}
							Component annotation = aClass.getAnnotation(Component.class);
							String beanName = annotation.value();
							if ("".equals(beanName)) {
								beanName = Introspector.decapitalize(aClass.getSimpleName());
							}
							BeanDefinition beanDefinition = new BeanDefinition();
							beanDefinition.setType(aClass);
							//判断该类是单例还是多例
							if (aClass.isAnnotationPresent(Scope.class)) {
								Scope scope = aClass.getAnnotation(Scope.class);
								String value = scope.value();
								beanDefinition.setScope(value);
							} else {
								beanDefinition.setScope("singleton");
							}
							beanDefinitionMap.put(beanName, beanDefinition);
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}


	public Object getBean(String beanName) {
		if (!beanDefinitionMap.containsKey(beanName)) {
			throw new NullPointerException();
		}
		//从beanDefinitionMap中，获取bean定义
		BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
		String scope = beanDefinition.getScope();
		//判断该类是否是单例
		if (scope.equals("singleton")) {
			Object obj = singletonObjects.get(beanName);
			//从单例Map取出是否为空
			if (obj == null){
				Object bean = createBean(beanName, beanDefinition);//为空创建该类对象
				singletonObjects.put(beanName,bean);//并存入单例Map中
			}
			return obj;
		}
		//多例直接创建对象
		Object obj = createBean(beanName, beanDefinition);
		return obj;
	}
}
