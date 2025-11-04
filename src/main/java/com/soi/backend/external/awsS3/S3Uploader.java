package com.soi.backend.external.awsS3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.soi.backend.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
@Service
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucket;

    // MultipartFile을 전달받아 File로 전환한 후 S3에 업로드
    public String upload(MultipartFile multipartFile, String dirName, Long id) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new CustomException("MultipartFile -> File 전환 실패", HttpStatus.INTERNAL_SERVER_ERROR));

        // 파일 경로 생성
//        String filePath = buildPath(dirName, id, multipartFile.getOriginalFilename());

        // key 발급
        String key = buildKey(dirName, id, multipartFile.getOriginalFilename());

        // S3 업로드 (private로)
        amazonS3Client.putObject(new PutObjectRequest(bucket, key, uploadFile));

        // 로컬 임시 파일 삭제
        removeNewFile(uploadFile);

        return key;
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        } else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    public String getPressigneUrl(String key) {
        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + 1000 * 60 * 60);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        URL presignedUrl = amazonS3Client.generatePresignedUrl(request);
        return presignedUrl.toString();
    }

    private Optional<File> convert(MultipartFile file) {
        try {
            // 안전한 임시 파일 생성
            String safeName = file.getOriginalFilename()
                    .replaceAll("[^a-zA-Z0-9.\\-_]", "_");

            // /tmp 디렉토리는 EC2와 Docker 환경에서도 항상 쓰기 가능
            File convertFile = File.createTempFile("upload-", "_" + safeName);

            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }

            return Optional.of(convertFile);

        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // key 생성 규칙: {yyyy/MM/dd}/user_{id}/{type}_{uuid}_{filename}
    private String buildKey(String category, Long id, String originalFilename) {
        LocalDate now = LocalDate.now();
        String datePath = String.format("%04d/%02d/%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String uuid = UUID.randomUUID().toString();
        String safeName = originalFilename == null ? "" : originalFilename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        return String.format("%s/user_%d/%s_%s_%s", datePath, id, category, uuid, safeName);
    }

}