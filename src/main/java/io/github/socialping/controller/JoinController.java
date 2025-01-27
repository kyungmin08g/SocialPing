package io.github.socialping.controller;

import io.github.socialping.dto.JoinDto;
import io.github.socialping.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
public class JoinController {

    private final MemberService memberService;

    @Autowired
    public JoinController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping(value = "/signup")
    public String signUp() {
        return "SignUpPage";
    }

    @PostMapping("/join")
    public String join(SecurityContext context, JoinDto dto) {
        memberService.join(context, dto);
        return "redirect:/";
    }

}
