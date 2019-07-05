package com.darcy.demo.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class TestBean {

  private String username;
  private String url;
  private String password;

  public void sayHello() {
    System.out.println("TestBean sayHello...");
  }

  public void start() {
    System.out.println("TestBean 初始化。。。");
  }

  public void cleanUp() {
    System.out.println("TestBean 销毁。。。");
  }

}
