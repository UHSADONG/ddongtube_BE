package com.uhsadong.ddtube.global.util;

import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Util {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public String upload(MultipartFile file) {

        String fileName = buildFileName(file.getOriginalFilename());
        String contentType = file.getContentType();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .contentLength(file.getSize())
                .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest,
                RequestBody.fromBytes(file.getBytes()));

            if (response.sdkHttpResponse().statusText().orElse("FAIL").equals("OK")) {

            }
        } catch (IOException ie) {
            log.error("파일을 읽어들이는데 에러가 발생했습니다.");
            log.error(ie.getMessage());
            throw new GeneralException(ErrorStatus._S3_IO_EXCEPTION);
        } catch (S3Exception | IllegalStateException ae) {
            log.error("AWS와 통신에 문제가 발생했습니다.");
            log.error(ae.getMessage());
            throw new GeneralException(ErrorStatus._S3_COMMUNICATION_EXCEPTION);
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
    }

    private String buildFileName(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        return String.format("%s%s", uuid, ext); // 예: images/uuid.jpg
    }

    public boolean isS3Url(String url) {
        String s3Prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);

        return url.startsWith(s3Prefix);
    }

}
