package com.darcy.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

/**
 * 横切关注点，打印时间； 但是around类型的通知
 */
@Component
public class TimeHandler2 {
  public void printTime(ProceedingJoinPoint joinPoint) throws Throwable {
    System.out.println("CurrentTime = " + System.currentTimeMillis());
    joinPoint.proceed();
    System.out.println("CurrentTime = " + System.currentTimeMillis());
  }
}