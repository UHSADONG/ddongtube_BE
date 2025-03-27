package com.uhsadong.ddtube.domain.repository;

import com.uhsadong.ddtube.domain.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // 플레이리스트와 사용자 이름으로 고유한 유저가 있는지 여부 확인
    Optional<User> findFirstByPlaylistCodeAndName(String playlistCode, String name);

    Optional<User> findByCode(String code);
}
