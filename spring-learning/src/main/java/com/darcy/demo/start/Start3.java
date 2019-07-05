package com.darcy.demo.start;

import com.darcy.demo.bean.DataSource;
import com.darcy.demo.bean.TestBean;
import com.darcy.demo.configuration.TestConfiguration3;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Start3 {

  public static void main(String[] args) {
    // @Configuration注解的spring容器加载方式，用AnnotationConfigApplicationContext替换ClassPathXmlApplicationContexts
    ApplicationContext context = new AnnotationConfigApplicationContext(TestConfiguration3.class);

    //bean
    TestBean tb = (TestBean) context.getBean("testBean");
    tb.sayHello();

    DataSource ds = (DataSource) context.getBean("dataSource");
    System.out.println(ds);
  }

}
