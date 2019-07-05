package com.darcy.demo.configuration;

import com.darcy.demo.bean.TestBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @Configuration标注在类上，相当于把该类作为spring的xml配置文件中的<beans>，作用为：配置spring容器(应用上下文)
 * 相当于配置了applicationContext.xml (里面配置了<beans><beans/>)
 */
@Configuration
@ComponentScan(basePackageClasses = TestBean.class)
public class TestConfiguration1 {

  public TestConfiguration1() {
    System.out.println(getClass().getName() + " 容器启动！");
  }


  /**
   * @Bean的作用是注册bean对象，那么完全可以使用@Component、@Controller、@Service、@Ripository等注解注册bean，当然需要配置@ComponentScan注解进行自动扫描。
   * @Bean 支持两种属性，即 initMethod 和destroyMethod，这些属性可用于定义生命周期方法。在实例化 bean 或即将销毁它时，容器便可调用生命周期方法。生命周期方法也称为回调方法，因为它将由容器调用。
   * @return
   */

  // 配置了componentScan的话，这里就可以注释了；

  // @Bean注解注册bean,同时可以指定初始化和销毁方法
//   @Bean(name="testBean",initMethod="start",destroyMethod="cleanUp")
////  @Bean
//  @Scope("prototype")
//  public TestBean testBean() {
//    return new TestBean();
//  }

}
