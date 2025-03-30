package com.uhsadong.ddtube.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5, unique = true)
    @Length(min = 5, max = 5)
    private String code; // 5자리 AlphaNumeric

    @Column(nullable = false, length = 100)
    @Length(min = 1, max = 100)
    private String title;

    @Column(nullable = true, length = 255)
    @Length(max = 255)
    private String description;

    @Column(nullable = true)
    private String thumbnailUrl;

    @Column(nullable = false)
    private LocalDateTime willDeleteAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;


    public static Playlist toEntity(String code, String title, String description, String thumbnailUrl ,LocalDateTime willDeleteAt) {
        return Playlist.builder()
            .code(code)
            .title(title)
            .description(description)
            .thumbnailUrl(thumbnailUrl)
            .willDeleteAt(willDeleteAt)
            .build();
    }

}
