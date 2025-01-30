package io.github.socialping.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "member")
@Setter
public class MemberEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String uuid;

    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "business")
    private String business;

    @Column(name = "role")
    private String role;

    @Column(name = "facebook_access_token", nullable = false)
    private String facebook_access_token;

}
