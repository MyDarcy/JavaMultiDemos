package com.darcy.demo.bean;

public class DataSource {

  private String dbUser;
  private String dbPass;
  public String getDbUser() {
    return dbUser;
  }
  public void setDbUser(String dbUser) {
    this.dbUser = dbUser;
  }
  public String getDbPass() {
    return dbPass;
  }
  public void setDbPass(String dbPass) {
    this.dbPass = dbPass;
  }
  @Override
  public String toString() {
    return "DataSource [dbUser=" + dbUser + ", dbPass=" + dbPass + "]";
  }

}
