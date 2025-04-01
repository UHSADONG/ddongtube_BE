package com.uhsadong.ddtube.global.util;

import com.uhsadong.ddtube.domain.dto.YoutubeOEmbedDTO;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
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

        try {
            return restTemplate.getForObject(uri, YoutubeOEmbedDTO.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new GeneralException(ErrorStatus._YOUTUBE_OEMBED_BAD_REQUEST);
            }
            throw e; // 다른 에러는 그대로 던짐
        }
    }
}
