package io.github.socialping.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.core.context.SecurityContext;

import java.util.List;
import java.util.Map;

public interface FacebookService {
    List<Map<String, String>> getFacebookPages(SecurityContext securityContext) throws JsonProcessingException;
    String getInstagramUserName(SecurityContext securityContext, String pageId, String pageAccessToken) throws JsonProcessingException;
}
