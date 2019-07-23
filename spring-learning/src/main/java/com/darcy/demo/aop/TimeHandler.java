package com.darcy.demo.aop;

import org.springframework.stereotype.Component;

/**
 * 横切关注点，打印时间；
 */
@Component
public class TimeHandler {
  public void printTime() {
    System.out.println("CurrentTime = " + System.currentTimeMillis());
  }
}