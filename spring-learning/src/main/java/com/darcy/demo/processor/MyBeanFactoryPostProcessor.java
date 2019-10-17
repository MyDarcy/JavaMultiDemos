package com.darcy.demo.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * https://blog.csdn.net/caihaijiang/article/details/35552859
 * 1. 实现该接口，可以在spring的bean创建之前，修改bean的定义属性。也就是说，Spring允许BeanFactoryPostProcessor在容器实例化任何其它
 * bean之前读取配置元数据，并可以根据需要进行修改，例如可以把bean的scope从singleton改为prototype，也可以把property的值给修改掉。
 * 可以同时配置多个BeanFactoryPostProcessor，并通过设置'order'属性来控制各个BeanFactoryPostProcessor的执行次序。
 *
 * 2. BeanFactoryPostProcessor是在spring容器加载了bean的定义文件之后，在bean实例化之前执行的。
 * 接口方法的入参是ConfigurrableListableBeanFactory，使用该参数，可以获取到相关bean的定义信息
 *
 * 3. spring中，有内置的一些BeanFactoryPostProcessor实现类，常用的有：
 * org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
 * org.springframework.beans.factory.config.PropertyOverrideConfigurer
 * org.springframework.beans.factory.config.CustomEditorConfigurer：用来注册自定义的属性编辑器
 *
 */
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    System.out.println("调用MyBeanFactoryPostProcessor的postProcessBeanFactory");
    // 获取bean的定义信息并修改property;
    BeanDefinition bd = beanFactory.getBeanDefinition("myJavaBean");
    MutablePropertyValues pv =  bd.getPropertyValues();
    if (pv.contains("remark")) {
      pv.addPropertyValue("remark", "在BeanFactoryPostProcessor中修改之后的备忘信息");
    }
  }

}

