package io.github.socialping.repository;

import io.github.socialping.entity.FacebookPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacebookPageRepository extends JpaRepository<FacebookPage, String> { }
