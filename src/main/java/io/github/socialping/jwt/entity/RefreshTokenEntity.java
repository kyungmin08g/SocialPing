package io.github.socialping.jwt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table(name = "refreshtoken")
public class RefreshTokenEntity {

    @Id
    @Column(name = "user_name")
    private String name;

    @Column(name = "refresh_token", length = 512)
    private String refresh_token;
}
