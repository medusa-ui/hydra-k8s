package io.getmedusa.hydra.controller;

import io.getmedusa.hydra.routing.DynamicRouteProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Controller
public class LoginController {

    private final ResourceLoader resourceLoader;

    public LoginController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/login")
    public String loginPage(Model model, ServerWebExchange exchange) {
        Resource resource = resourceLoader.getResource("classpath:templates/login.html");
        List<String> ref = exchange.getRequest().getQueryParams().get("ref");
        String forwardToLocationAfterLogin = "/";
        if(ref != null) {
            forwardToLocationAfterLogin = ref.get(0);
        }
        if(!DynamicRouteProvider.isAvailableRoute(forwardToLocationAfterLogin)) {
            throw new SecurityException("Invalid forward route provided");
        }
        model.addAttribute("forwarder", forwardToLocationAfterLogin);
        model.addAttribute("_csrf", exchange.getAttributes().get("org.springframework.security.web.server.csrf.CsrfToken"));

        if (resource.exists()) {
            return "login";
        } else {
            return "default-login";
        }
    }

}
