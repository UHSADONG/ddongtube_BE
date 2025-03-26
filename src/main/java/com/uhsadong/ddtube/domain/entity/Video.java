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
import java.time.LocalTime;
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

    @Length(max=255)
    @Column(nullable = false, length = 255)
    private String title;
    @Length(max=100)
    @Column(nullable = false, length = 100)
    private String author_name;
    @Length(max=1000)
    @Column(nullable = false, length = 1000)
    private String url;
    private Integer height;
    private Integer width;
    @Length(max=1000)
    @Column(nullable = false, length = 1000)
    private String thumbnail_url;
    private Integer thumbnail_height;
    private Integer thumbnail_width;



    // 해당 정보는 어디서 받아올 수 있을지 체크해야함
//    @Column(nullable = false)
//    private LocalTime duration;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Video toEntity(Playlist playlist, String videoUrl, YoutubeOEmbedDTO youtubeInfo) {
        return Video.builder()
            .playlist(playlist)
            .title(youtubeInfo.title())
            .author_name(youtubeInfo.author_name())
            .url(videoUrl)
            .height(youtubeInfo.height())
            .width(youtubeInfo.width())
            .thumbnail_url(youtubeInfo.thumbnail_url())
            .thumbnail_height(Integer.valueOf(youtubeInfo.thumbnail_height()))
            .thumbnail_width(Integer.valueOf(youtubeInfo.thumbnail_width()))
            .build();
    }
}
