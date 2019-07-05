package com.darcy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

  @RequestMapping("/hello")
  public String hello(@RequestParam(defaultValue = "苟") String name, Model model) {
    model.addAttribute("name", name);
    return "hello";
  }

  @RequestMapping("/index")
  public String index() {
    return "index";
  }
}
