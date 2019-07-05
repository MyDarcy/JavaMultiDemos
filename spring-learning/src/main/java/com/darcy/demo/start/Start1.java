package com.darcy.demo.start;

import com.darcy.demo.bean.TestBean;
import com.darcy.demo.configuration.TestConfiguration1;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Start1 {

  /**
   * 即使web.xml中没有配置 载入的外部 XML 上下文文件;
   * @param args
   */
  public static void main(String[] args) {
    // 1.
    ApplicationContext context = new AnnotationConfigApplicationContext(TestConfiguration1.class);


    // 如果加载spring-context.xml文件：
    // ApplicationContext context = new
    // ClassPathXmlApplicationContext("spring-context.xml");

    //获取bean
    TestBean tb = (TestBean) context.getBean("testBean");
    tb.sayHello();
    System.out.println(tb);

    TestBean tb2 = (TestBean) context.getBean("testBean");
    tb2.sayHello();
    System.out.println(tb2);
  }

}
