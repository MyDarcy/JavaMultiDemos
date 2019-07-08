package com.darcy.demo.processor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * https://blog.csdn.net/caihaijiang/article/details/35552859
 */
public class PostProcessorMain {
  public static void main(String[] args) {
    ApplicationContext context = new ClassPathXmlApplicationContext("postprocessor.xml");
    MyJavaBean bean = (MyJavaBean) context.getBean("myJavaBean");
    System.out.println("===============下面输出结果============");
    System.out.println("描述：" + bean.getDesc());
    System.out.println("备注：" + bean.getRemark());

  }
}
/*
BeanFactoryPostProcessor在bean实例化之前执行，
之后实例化bean（调用构造函数，并调用set方法注入属性值），
然后在调用两个初始化方法前后，执行了BeanPostProcessor。初始化方法的执行顺序是，先执行afterPropertiesSet，再执行init-method。
 */

/* output

调用MyBeanFactoryPostProcessor的postProcessBeanFactory

MyJavaBean的构造函数被执行啦
调用setDesc方法
调用setRemark方法

BeanPostProcessor，对象myJavaBean调用初始化方法之前的数据： [描述：原始的描述信息， 备注：在BeanFactoryPostProcessor中修改之后的备忘信息]
调用afterPropertiesSet方法
调用initMethod方法
BeanPostProcessor，对象myJavaBean调用初始化方法之后的数据：[描述：在初始化方法中修改之后的描述信息， 备注：在BeanFactoryPostProcessor中修改之后的备忘信息]

===============下面输出结果============
描述：在初始化方法中修改之后的描述信息
备注：在BeanFactoryPostProcessor中修改之后的备忘信息

 */