package com.soi.backend.external.awsS3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.soi.backend.domain.media.entity.FileType;
import com.soi.backend.global.exception.CustomException;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
@Service
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucket;
    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;


    /**
     * MultipartFile → Temp File → S3 업로드 → Temp 삭제
     */
    public String upload(MultipartFile multipartFile, String fileType, Long id) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new CustomException("MultipartFile -> File 전환 실패", HttpStatus.INTERNAL_SERVER_ERROR));

        String key = buildKey(fileType, id, multipartFile.getOriginalFilename());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(multipartFile.getContentType())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromFile(uploadFile));
        } catch (Exception e) {
            throw new CustomException("S3 업로드 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            removeNewFile(uploadFile);
        }

        return key;
    }

    /**
     * Presigned URL 생성 (GET)
     */
    public String getPressigneUrl(String key) {

        S3Presigner presigner = S3Presigner.builder()
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        accessKey,
                                        secretKey
                                )
                        )
                )
                .region(Region.AP_NORTHEAST_2)
                .build();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    /** Temp 파일 삭제 */
    public void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("Temp 파일이 삭제되었습니다.");
        } else {
            log.info("Temp 파일 삭제 실패");
        }
    }

    public void delete(String key) {
        try {
            s3Client.deleteObject(builder -> builder
                    .bucket(bucket)
                    .key(key)
                    .build()
            );
            log.info("S3 파일 삭제 완료: {}", key);
        } catch (Exception e) {
            throw new CustomException("S3 파일 삭제 실패: " + key, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** MultipartFile → File 변환 */
    private Optional<File> convert(MultipartFile file) {
        try {
            String safeName = file.getOriginalFilename()
                    .replaceAll("[^a-zA-Z0-9.\\-_]", "_");

            File convertFile = File.createTempFile("upload-", "_" + safeName);

            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }

            return Optional.of(convertFile);

        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Key 생성 규칙:
     * {yyyy/MM/dd}/user_{id}/{category}_{uuid}_{filename}
     */
    private String buildKey(String category, Long id, String originalFilename) {
        LocalDate now = LocalDate.now();
        String datePath = String.format("%04d/%02d/%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String uuid = UUID.randomUUID().toString();
        String safeName = originalFilename == null ? "" : originalFilename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");

        return String.format("%s/user_%d/%s_%s_%s", datePath, id, category, uuid, safeName);
    }
}