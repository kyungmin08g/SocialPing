package io.github.socialping.dto;

import io.github.socialping.entity.FacebookPage;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class FacebookPageDto {
    private String pageId;
    private String pageName;
    private String instagramId;
    private String instagramUsername;
    private String pageAccessToken;

    public FacebookPage toEntity() {
        return new FacebookPage(pageId, null, pageName, instagramId, instagramUsername, pageAccessToken);
    }
}
