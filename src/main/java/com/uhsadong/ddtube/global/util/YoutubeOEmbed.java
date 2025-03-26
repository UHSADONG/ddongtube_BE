package com.uhsadong.ddtube.global.util;

import com.uhsadong.ddtube.domain.dto.YoutubeOEmbedDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class YoutubeOEmbed {

    private static final String OEMBED_URL = "https://www.youtube.com/oembed";

    public static YoutubeOEmbedDTO getVideoInfo(String videoUrl) {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(OEMBED_URL)
            .queryParam("url", videoUrl)
            .queryParam("format", "json");

        String uri = builder.build(false).toUriString();

        return restTemplate.getForObject(uri, YoutubeOEmbedDTO.class);
    }
}
