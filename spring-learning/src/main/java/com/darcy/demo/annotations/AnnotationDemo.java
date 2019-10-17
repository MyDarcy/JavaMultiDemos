package com.darcy.demo.annotations;

import javax.annotation.PostConstruct;

public class AnnotationDemo {

  /**
   * 依赖注入时有效;
   */
  @PostConstruct
  public void init() {
    System.out.println("----init----");
  }

  public static void main(String[] args) throws InterruptedException {
    new AnnotationDemo();
    Thread.sleep(2000);
  }

}
