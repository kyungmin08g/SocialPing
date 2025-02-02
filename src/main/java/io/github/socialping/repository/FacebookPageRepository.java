package io.github.socialping.repository;

import io.github.socialping.entity.FacebookPage;
import io.github.socialping.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacebookPageRepository extends JpaRepository<FacebookPage, String> {
    Optional<FacebookPage> findByMemberId(MemberEntity memberId);
}
