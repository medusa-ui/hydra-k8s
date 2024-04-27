package com.example.exampleclient;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Controller
public class SecureController {

    @GetMapping( "/secure-hello")
    public String secureHelloWorldPage(Model model) {
        model.addAttribute("randomUUID", UUID.randomUUID().toString());
        return "secure";
    }

}
