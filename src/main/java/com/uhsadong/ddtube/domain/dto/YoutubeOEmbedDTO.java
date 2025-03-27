package com.uhsadong.ddtube.domain.dto;

public record YoutubeOEmbedDTO(
    String title,
    String author_name,
    String author_url,
    String type,
    int height,
    int width,
    String version,
    String provider_name,
    String provider_url,
    String thumbnail_height,
    String thumbnail_width,
    String thumbnail_url,
    String html
) {

}
