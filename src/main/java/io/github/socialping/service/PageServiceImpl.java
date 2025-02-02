package io.github.socialping.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.socialping.dto.FacebookPageDto;
import io.github.socialping.entity.FacebookPage;
import io.github.socialping.entity.MemberEntity;
import io.github.socialping.repository.FacebookPageRepository;
import io.github.socialping.repository.MemberRepository;
import io.github.socialping.security.user.OAuth2FacebookUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class PageServiceImpl implements FacebookService {

    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
    private final FacebookPageRepository pageRepository;
    private final String verifyToken;
    private final String appId;
    private final String appSecret;

    public PageServiceImpl(
            @Value("${spring.webhooks.verify_token}") String verifyToken,
            @Value("${spring.security.oauth2.client.registration.facebook.client-id}") String appId,
            @Value("${spring.security.oauth2.client.registration.facebook.client-secret}") String appSecret,
            MemberRepository repository,
            ObjectMapper objectMapper,
            WebClient.Builder webClientBuilder,
            FacebookPageRepository pageRepository
    ) {
        this.verifyToken = verifyToken;
        this.memberRepository = repository;
        this.objectMapper = objectMapper;
        this.webClientBuilder = webClientBuilder;
        this.pageRepository = pageRepository;
        this.appId = appId;
        this.appSecret = appSecret;
    }

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
            String pageJsonStr = webClientBuilder.baseUrl("https://graph.facebook.com").build().get().uri(uriBuilder -> uriBuilder.path("/v17.0/me/accounts")
                    .queryParam("access_token", facebookAccessToken)
                    .build()
            ).retrieve().bodyToMono(String.class).block();

            // 데이터 가공
            JsonNode pageDataJson = objectMapper.readTree(pageJsonStr).get("data");
            pageDataJson.forEach(page -> {
                Map<String, String> pageData = new HashMap<>();
                pageData.put("pageAccessToken", page.get("access_token").asText());
                pageData.put("pageName", page.get("name").asText());
                pageData.put("pageId", page.get("id").asText());
                pages.add(pageData);
            });
        }

        return pages;
    }

    @Override
    public String getInstagramBusinessAccountUserName(SecurityContext securityContext, String pageId, String pageAccessToken) throws JsonProcessingException {
        OAuth2FacebookUser user = (OAuth2FacebookUser) securityContext.getAuthentication().getPrincipal();
        Optional<MemberEntity> member = memberRepository.findById(user.getUserId());

        if (member.isPresent()) {
            String instagramAccountIdJsonStr = webClientBuilder.baseUrl("https://graph.facebook.com").build().get().uri(uriBuilder -> uriBuilder.path("/v22.0/" + pageId)
                    .queryParam("fields", "instagram_business_account")
                    .queryParam("access_token", pageAccessToken)
                    .build()
            ).retrieve().bodyToMono(String.class).block();

            JsonNode instrAccountJson = objectMapper.readTree(instagramAccountIdJsonStr);
            JsonNode instagramBusinessAccountNode = instrAccountJson.get("instagram_business_account");

            if (instagramBusinessAccountNode != null) {
                String instrUsernameJsonStr = webClientBuilder.baseUrl("https://graph.facebook.com").build().get().uri(uriBuilder -> uriBuilder.path("/v22.0/" + instagramBusinessAccountNode.get("id").asText())
                        .queryParam("fields", "username")
                        .queryParam("access_token", pageAccessToken)
                        .build()
                ).retrieve().bodyToMono(String.class).block();

                return instagramBusinessAccountNode.get("id").asText() + "_" + objectMapper.readTree(instrUsernameJsonStr).get("username").asText();
            } else return "-";
        }

        return null;
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public void setFacebookPageConnect(SecurityContext securityContext, FacebookPageDto dto) {
        OAuth2FacebookUser user = (OAuth2FacebookUser) securityContext.getAuthentication().getPrincipal();
        Optional<MemberEntity> member = memberRepository.findById(user.getUserId());

        if (member.isPresent()) {
            FacebookPage page = dto.toEntity();
            page.setMemberId(member.get()); // 단방향 연관관계 설정
            pageRepository.save(page);
        }
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public void webhookSetting(SecurityContext securityContext) throws JsonProcessingException {
        OAuth2FacebookUser user = (OAuth2FacebookUser) securityContext.getAuthentication().getPrincipal();
        Optional<MemberEntity> member = memberRepository.findById(user.getUserId());

        if (member.isPresent()) {
            Optional<FacebookPage> page = pageRepository.findByMemberId(member.get());

            if (page.isPresent()) {
                String pageId = page.get().getPageId();
                String pageAccessToken = page.get().getPageAccessToken();
                String instagramId = page.get().getInstagramId();
                String accessToken = member.get().getFacebook_access_token();

                String uri = "/v17.0/" + pageId + "/instagram_accounts?access_token=" + pageAccessToken;

                System.out.println(uri);
                String json1 = webClientBuilder.baseUrl("https://graph.facebook.com").build()
                        .get()
                        .uri(uri)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode json = objectMapper.readTree(json1);
                System.out.println(json);
                String id = null;
                for (JsonNode node1 : json) {
                    for (JsonNode node : node1) { id = node.get("id").asText(); }
                }

                String uri2 = "/v17.0/" + instagramId + "?fields=name,profile_picture_url&access_token=" + pageAccessToken;
                System.out.println(uri2);
                String json2 = webClientBuilder.baseUrl("https://graph.facebook.com").build()
                        .get()
                        .uri(uri2)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode json3 = objectMapper.readTree(json2);
                System.out.println(json3);

                String uri3 = "/oauth/access_token?client_id=" + appId + "&client_secret=" + appSecret + "&grant_type=client_credentials";
                System.out.println(uri2);
                String json4 = webClientBuilder.baseUrl("https://graph.facebook.com").build()
                        .get()
                        .uri(uri3)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                String appAccessToken = objectMapper.readTree(json4).get("access_token").asText();
                System.out.println(appAccessToken);

                String uri4 = "/v17.0/debug_token?input_token=" + accessToken + "&access_token=" + accessToken;
                String json5 = webClientBuilder.baseUrl("https://graph.facebook.com").build()
                        .get()
                        .uri(uri4)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode json6 = objectMapper.readTree(json5);
                System.out.println(json6);

                // --------------------------------------------------------------------------------------------------
                Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("access_token", appAccessToken);
                jsonMap.put("object", "instagram");
                jsonMap.put("fields", List.of("comments"));
                jsonMap.put("callback_url", "https://aa3e-1-236-198-90.ngrok-free.app/webhook");
                jsonMap.put("verify_token", verifyToken);

                webClientBuilder.baseUrl("https://graph.facebook.com").build().post()
                        .uri("/v19.0/" + appId + "/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(jsonMap)
                        .retrieve()
                        .bodyToMono(String.class)
                        .doOnError(error -> System.out.println("Error: " + error.getMessage()))
                        .subscribe(response -> System.out.println("Response: " + response));
            }
        }
    }

}
