package com.soi.backend.domain.media.service;

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

@Service
@RequiredArgsConstructor

public class MediaService {

    private final S3Uploader s3Uploader;
    private final MediaRepository mediaRepository;

    @Transactional
    public List<String> uploadMedia(List<FileType> types, List<UsageType> usageTypes, Long id, Long refId, List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();

        if (types.size() != files.size()) {
            throw new CustomException("파일 수와 타입 수가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        
        for (int i=0; i<files.size(); i++) {
            MultipartFile file = files.get(i);
            FileType fileType = types.get(i);
            UsageType usageType = usageTypes.get(i);

            switch (fileType) {
                case IMAGE:
                case VIDEO:
                case AUDIO:
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
}
