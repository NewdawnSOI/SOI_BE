package com.soi.backend.domain.media.service;

import com.soi.backend.domain.media.dto.MediaPresignReqDto;
import com.soi.backend.domain.media.dto.MediaPresignRespDto;
import com.soi.backend.domain.media.dto.MediaRegisterUploadedReqDto;
import com.soi.backend.domain.media.entity.FileType;
import com.soi.backend.domain.media.entity.Media;
import com.soi.backend.domain.media.entity.UsageType;
import com.soi.backend.domain.media.repository.MediaRepository;
import com.soi.backend.external.awsS3.S3Uploader;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor

public class MediaService {

    private final S3Uploader s3Uploader;
    private final MediaRepository mediaRepository;

    @Transactional
    public List<String> uploadMedia(List<String> types, List<String> usageTypes, Long id, Long refId, List<MultipartFile> files, Long usageCount) throws IOException {
        List<String> urls = new ArrayList<>();
        int index = 0;

        if (types.size() != files.size()) {
            throw new CustomException("파일 수와 타입 수가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        for (int i=0; i<usageCount*types.size(); i++) {
            if (i == usageCount) {
                index++;
            }
            MultipartFile file = files.get(index);
            String fileType = types.get(index);
            String usageType = usageTypes.get(index);

            switch (fileType) {
                case "IMAGE":
                case "VIDEO":
                case "AUDIO":
                    String url = s3Uploader.upload(file, fileType, id);
                    urls.add(url);
                    saveMedia(new Media(url, id,fileType, usageType, refId));
                    break;
                default :
                    throw new CustomException("지원하지 않는 미디어 타입", HttpStatus.BAD_REQUEST);
            }
        }
        return urls;
    }

    public List<MediaPresignRespDto> createUploadPresignedUrls(Long userId, MediaPresignReqDto mediaPresignReqDto) {
        List<MediaPresignRespDto> responses = new ArrayList<>();

        for (var file : mediaPresignReqDto.getFiles()) {
            String fileType = normalizeFileType(file.getFileType());
            String key = s3Uploader.buildUploadKey(fileType, userId, file.getOriginalFileName());
            String uploadUrl = s3Uploader.createPutPresignedUrl(key, file.getContentType());

            responses.add(new MediaPresignRespDto(
                    key,
                    uploadUrl,
                    file.getContentType()
            ));
        }

        return responses;
    }

    @Transactional
    public List<String> registerUploadedMedia(Long userId, MediaRegisterUploadedReqDto mediaRegisterUploadedReqDto) {
        if (mediaRegisterUploadedReqDto.getKeys().size() != mediaRegisterUploadedReqDto.getUsageTypes().size()) {
            throw new CustomException("key 수와 usageType 수가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        List<String> savedKeys = new ArrayList<>();

        for (int i = 0; i < mediaRegisterUploadedReqDto.getKeys().size(); i++) {
            String key = mediaRegisterUploadedReqDto.getKeys().get(i);
            String usageType = normalizeUsageType(mediaRegisterUploadedReqDto.getUsageTypes().get(i));

            validateUploaderOwnership(key, userId);

            if (mediaRepository.findByMediaKey(key).isPresent()) {
                savedKeys.add(key);
                continue;
            }

            String mediaType = s3Uploader.extractFileTypeFromKey(key);
            saveMedia(new Media(
                    key,
                    userId,
                    mediaType,
                    usageType,
                    mediaRegisterUploadedReqDto.getRefId()
            ));
            savedKeys.add(key);
        }

        return savedKeys;
    }

    @Transactional
    public void saveMedia(Media media) {
        mediaRepository.save(media);
    }

    @Transactional
    public void removeMedia(String key) {
        Media media = mediaRepository.findByMediaKey(key)
                .orElseThrow(() -> new CustomException("미디어 파일을 찾을 수 없음", HttpStatus.NOT_FOUND));
        mediaRepository.deleteById(media.getId());
        s3Uploader.delete(key);
    }

    public List<String> getPresignedUrlByKey(List<String> key) {
        List<String> urls = new ArrayList<>();
        for (String url : key) {
            urls.add(s3Uploader.getPressigneUrl(url));
        }
        return urls;
    }

    public String getPresignedUrlByKey(String key) {
        return s3Uploader.getPressigneUrl(key);
    }

    private String normalizeFileType(String fileType) {
        try {
            return FileType.valueOf(fileType.toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException e) {
            throw new CustomException("지원하지 않는 미디어 타입", HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeUsageType(String usageType) {
        try {
            return UsageType.valueOf(usageType.toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException e) {
            throw new CustomException("지원하지 않는 usageType", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUploaderOwnership(String key, Long userId) {
        String expectedPrefix = "/user_" + userId + "/";
        if (!key.contains(expectedPrefix)) {
            throw new CustomException("현재 유저가 업로드한 key가 아닙니다.", HttpStatus.BAD_REQUEST);
        }
    }
}
