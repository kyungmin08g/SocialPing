package io.github.socialping.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "facebook_page")
public class FacebookPage {

    @Id
    @Column(name = "page_id")
    private String pageId;

    @OneToOne
    @JoinColumn(name = "member_id")
    private MemberEntity memberId;

    @Column(name = "page_name")
    private String pageName;

    @Column(name = "instagram_user_name")
    private String instagramUsername;

}
