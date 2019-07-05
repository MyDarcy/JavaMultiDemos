package com.darcy.demo.start;

import com.darcy.demo.bean.TestBean;
import com.darcy.demo.bean.TestBean2;
import com.darcy.demo.configuration.WebConfig1;
import com.darcy.demo.configuration.WebConfig2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Start2 {

  public static void main(String[] args) {
    // @Configuration注解的spring容器加载方式，用AnnotationConfigApplicationContext替换ClassPathXmlApplicationContext
//    ApplicationContext context = new AnnotationConfigApplicationContext(WebConfig1.class);
    ApplicationContext context = new AnnotationConfigApplicationContext(WebConfig2.class);

    // 如果加载spring-context.xml文件：
    // ApplicationContext context = new
    // ClassPathXmlApplicationContext("spring-context.xml");

    // 获取bean
    TestBean2 tb2 = (TestBean2) context.getBean("testBean2");
    tb2.sayHello();

    TestBean tb = (TestBean) context.getBean("testBean");
    tb.sayHello();
  }

}
