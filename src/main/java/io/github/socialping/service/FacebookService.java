package io.github.socialping.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.socialping.dto.FacebookPageDto;
import org.springframework.security.core.context.SecurityContext;

import java.util.List;
import java.util.Map;

public interface FacebookService {
    List<Map<String, String>> getFacebookPages(SecurityContext securityContext) throws JsonProcessingException;
    String getInstagramBusinessAccountUserName(SecurityContext securityContext, String pageId, String pageAccessToken) throws JsonProcessingException;
    void setFacebookPageConnect(SecurityContext securityContext, FacebookPageDto dto);
    void webhookSetting(SecurityContext securityContext) throws JsonProcessingException;
}
