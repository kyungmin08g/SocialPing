package io.github.socialping.controller;

import io.github.socialping.security.user.OAuth2FacebookUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController {

    @GetMapping(value = "/")
    public String home(SecurityContext context, Model model) {
        if (!context.getAuthentication().getPrincipal().toString().equals("anonymousUser")) {
            OAuth2FacebookUser user = (OAuth2FacebookUser) context.getAuthentication().getPrincipal();
            model.addAttribute("userId", user.getUserId());
            model.addAttribute("username", user.getName());
        } else model.addAttribute("username", "undefined");

        return "HomePage";
    }

}
