package io.github.socialping.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class PageController {

    @GetMapping(value = "/instagram/accounts")
    public String instagramAccount() {
        return "MainPage";
    }

}
