package com.darcy.demo.configuration;

import com.darcy.demo.bean.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = com.darcy.demo.bean.TestBean2.class)
public class TestConfiguration3 {

  public TestConfiguration3() {
    System.out.println("TestConfiguration容器启动初始化。。。");
  }

  // 注意DataSource并没有加@Component
  @Configuration
  static class DatabaseConfig {
    @Bean
    DataSource dataSource() {
      return new DataSource();
    }
  }

}
