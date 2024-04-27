package com.example.exampleclient;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Controller
public class HelloWorldController {

    @GetMapping(value={"", "/", "/hello"})
    public String helloWorldPage(Model model) {
        model.addAttribute("randomUUID", UUID.randomUUID().toString());
        return "index";
    }

}
