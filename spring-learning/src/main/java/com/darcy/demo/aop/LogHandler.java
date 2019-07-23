package com.darcy.demo.aop;

import org.springframework.stereotype.Component;

/**
 * 横切关注点，打日志;
 */
@Component
public class LogHandler {

  public void LogBefore() {
    System.out.println("Log before method");
  }

  public void LogAfter() {
    System.out.println("Log after method");
  }

}
