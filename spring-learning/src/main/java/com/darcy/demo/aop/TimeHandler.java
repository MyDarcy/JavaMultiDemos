package com.darcy.demo.aop;

import org.springframework.stereotype.Component;

@Component
public class TimeHandler {
  public void printTime() {
    System.out.println("CurrentTime = " + System.currentTimeMillis());
  }
}