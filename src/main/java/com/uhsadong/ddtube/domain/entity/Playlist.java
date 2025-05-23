package com.uhsadong.ddtube.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = @Index(name = "idx_code", columnList = "code"))
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
    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "now_play_video_id")
    private Video nowPlayVideo;


    public static Playlist toEntity(String code, String title, String description, String thumbnailUrl ,LocalDateTime lastLoginAt) {
        return Playlist.builder()
            .code(code)
            .title(title)
            .description(description)
            .thumbnailUrl(thumbnailUrl)
            .lastLoginAt(lastLoginAt)
            .build();
    }

}
