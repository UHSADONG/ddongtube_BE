package com.uhsadong.ddtube.domain.repository;

import com.uhsadong.ddtube.domain.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {

}
