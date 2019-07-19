package com.darcy.demo.aop;

import org.springframework.stereotype.Component;

@Component
public class LogHandler {

  public void LogBefore() {
    System.out.println("Log before method");
  }

  public void LogAfter() {
    System.out.println("Log after method");
  }

}
