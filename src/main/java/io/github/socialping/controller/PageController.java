package io.github.socialping.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.socialping.dto.FacebookPageDto;
import io.github.socialping.security.user.OAuth2FacebookUser;
import io.github.socialping.service.PageServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class PageController {

    private final PageServiceImpl pageService;

    @GetMapping(value = "/instagram/accounts")
    public String instagramAccount(SecurityContext context, Model model) {
        if (!context.getAuthentication().getPrincipal().toString().equals("anonymousUser")) {
            OAuth2FacebookUser user = (OAuth2FacebookUser) context.getAuthentication().getPrincipal();
            model.addAttribute("userId", user.getUserId());
            model.addAttribute("username", user.getName());
            return "MainPage";
        }

        return "redirect:/login";
    }

    @GetMapping(value = "/instagram/pages")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> instagramPages(SecurityContext securityContext) throws JsonProcessingException {
        List<Map<String, String>> pages = pageService.getFacebookPages(securityContext);
        return ResponseEntity.status(201).body(pages);
    }

    @GetMapping(value = "/facebook/page/business-account/{id}/{token}")
    @ResponseBody
    public ResponseEntity<String> instagramAccountId(
            SecurityContext securityContext,
            @PathVariable("id") String pageId,
            @PathVariable("token") String pageToken
    ) throws JsonProcessingException {
        String instagramAccountId = pageService.getInstagramBusinessAccountUserName(securityContext, pageId, pageToken);
        return ResponseEntity.status(201).body(instagramAccountId);
    }

    @PostMapping(value = "/facebook/page/connection")
    @ResponseBody
    public ResponseEntity<?> pageConnect(SecurityContext securityContext, @RequestBody FacebookPageDto pageDto) {
        pageService.setFacebookPageConnect(securityContext, pageDto);
        return ResponseEntity.status(201).build();
    }

}
