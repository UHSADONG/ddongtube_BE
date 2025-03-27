package com.uhsadong.ddtube.global.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class S3Util {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public String upload(MultipartFile file) {
        String fileName = buildFileName(file.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName,
                fileName,
                file.getInputStream(),
                metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead);

            amazonS3.putObject(putObjectRequest);

        } catch (IOException e) {
            throw new GeneralException(ErrorStatus._FILE_UPLOAD_ERROR);
        }

        return amazonS3.getUrl(bucketName, fileName).toString(); // 업로드된 파일의 URL 반환
    }

    private String buildFileName(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        return String.format("/%s%s", uuid, ext); // 예: images/uuid.jpg
    }

    public boolean isS3Url(String url){
        String s3Prefix1 = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);

        return url.startsWith(s3Prefix1);
    }

}
