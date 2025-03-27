package com.uhsadong.ddtube.domain.entity;

import com.uhsadong.ddtube.domain.dto.YoutubeOEmbedDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Playlist playlist;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @Column(nullable = false, length = 5, unique = true)
    @Length(min = 5, max = 5)
    private String code; // 5자리 AlphaNumeric

    @Length(max = 255)
    @Column(nullable = false, length = 255)
    private String title;
    @Length(max = 100)
    @Column(nullable = false, length = 100)
    private String authorName;
    @Length(max = 1000)
    @Column(nullable = false, length = 1000)
    private String url;
    private Integer height;
    private Integer width;
    @Length(max = 1000)
    @Column(nullable = false, length = 1000)
    private String thumbnailUrl;
    private Integer thumbnailHeight;
    private Integer thumbnailWidth;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Video toEntity(Playlist playlist, User user, String code, String videoUrl, YoutubeOEmbedDTO youtubeInfo) {
        return Video.builder()
            .playlist(playlist)
            .user(user)
            .code(code)
            .title(youtubeInfo.title())
            .authorName(youtubeInfo.author_name())
            .url(videoUrl)
            .height(youtubeInfo.height())
            .width(youtubeInfo.width())
            .thumbnailUrl(youtubeInfo.thumbnail_url())
            .thumbnailHeight(Integer.valueOf(youtubeInfo.thumbnail_height()))
            .thumbnailWidth(Integer.valueOf(youtubeInfo.thumbnail_width()))
            .build();
    }
}
