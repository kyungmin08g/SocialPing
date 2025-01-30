package io.github.socialping.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.socialping.entity.MemberEntity;
import io.github.socialping.repository.MemberRepository;
import io.github.socialping.security.user.OAuth2FacebookUser;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
@AllArgsConstructor
public class PageServiceImpl implements FacebookService {

    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create("https://graph.facebook.com");

    @Override
    public List<Map<String, String>> getFacebookPages(SecurityContext securityContext) throws JsonProcessingException {
        // 최종 데이터를 담을 곳
        List<Map<String, String>> pages = new ArrayList<>();

        // 인증된 사용자
        OAuth2FacebookUser user = (OAuth2FacebookUser) securityContext.getAuthentication().getPrincipal();
        Optional<MemberEntity> member = memberRepository.findById(user.getUserId());

        if (member.isPresent()) {
            String facebookAccessToken = member.get().getFacebook_access_token();

            // 페이스북 페이지 요청
            String pageJsonStr = restClient.get().uri(uriBuilder -> uriBuilder.path("/v17.0/me/accounts")
                    .queryParam("access_token", facebookAccessToken)
                    .build()
            ).retrieve().body(String.class);

            List<JsonNode> filteringPageDataList = new ArrayList<>();

            // 데이터 가공
            JsonNode pageDataJson = objectMapper.readTree(pageJsonStr).get("data");
            pageDataJson.forEach(page -> {
                String instagramAccountIdJsonStr = restClient.get().uri(uriBuilder -> uriBuilder.path("/v22.0/" + page.get("id").asText())
                        .queryParam("fields", "instagram_business_account")
                        .queryParam("access_token", page.get("access_token").asText())
                        .build()
                ).retrieve().body(String.class);

                try {
                    if (objectMapper.readTree(instagramAccountIdJsonStr).get("instagram_business_account") != null) filteringPageDataList.add(page);
                } catch (JsonProcessingException e) { throw new RuntimeException(e); }
            });

            filteringPageDataList.forEach(page -> {
                Map<String, String> pageData = new HashMap<>();
                pageData.put("pageAccessToken", page.get("access_token").asText());
                pageData.put("pageName", page.get("name").asText());
                pageData.put("pageId", page.get("id").asText());
                pages.add(pageData);
            });
        }

        // 데이터가 잘 담겼는지 확인
        pages.forEach(System.out::println);

        return pages;
    }

    @Override
    public String getInstagramUserName(SecurityContext securityContext, String pageId, String pageAccessToken) throws JsonProcessingException {
        OAuth2FacebookUser user = (OAuth2FacebookUser) securityContext.getAuthentication().getPrincipal();
        Optional<MemberEntity> member = memberRepository.findById(user.getUserId());

        if (member.isPresent()) {
            String instagramAccountIdJsonStr = restClient.get().uri(uriBuilder -> uriBuilder.path("/v22.0/" + pageId)
                    .queryParam("fields", "instagram_business_account")
                    .queryParam("access_token", pageAccessToken)
                    .build()
            ).retrieve().body(String.class);

            JsonNode instrAccountJson = objectMapper.readTree(instagramAccountIdJsonStr);
            JsonNode instagramBusinessAccountNode = instrAccountJson.get("instagram_business_account");

            if (instagramBusinessAccountNode != null) {
                String instrUsernameJsonStr = restClient.get().uri(uriBuilder -> uriBuilder.path("/v22.0/" + instagramBusinessAccountNode.get("id").asText())
                        .queryParam("fields", "username")
                        .queryParam("access_token", pageAccessToken)
                        .build()
                ).retrieve().body(String.class);

                return objectMapper.readTree(instrUsernameJsonStr).get("username").asText();
            }
        }

        return null;
    }
}
