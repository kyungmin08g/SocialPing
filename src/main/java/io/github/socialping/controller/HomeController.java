package io.github.socialping.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController {

    @GetMapping(value = "/")
    public String home(SecurityContext context, Model model) {
        String principal = context.getAuthentication().getPrincipal().toString();

        if (!principal.equals("anonymousUser")) {
            model.addAttribute("userId", principal.substring(0, principal.indexOf("_")));
            model.addAttribute("username", principal.substring(principal.indexOf("_") + 1));
        } else model.addAttribute("username", "undefined");

        return "HomePage";
    }

}
