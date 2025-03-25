package com.uhsadong.ddtube.domain.repository;

import com.uhsadong.ddtube.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
