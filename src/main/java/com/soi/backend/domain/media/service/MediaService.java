package com.soi.backend.domain.media.service;

import com.soi.backend.external.awsS3.S3Uploader;
import com.soi.backend.global.exception.CustomException;
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

    public List<String> uploadMedia(List<String> types, Long id, List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();

        if (types.size() != files.size()) {
            throw new CustomException("파일 수와 타입 수가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        
        for (int i=0; i<files.size(); i++) {
            MultipartFile file = files.get(i);
            String fileType = types.get(i);

            switch (fileType) {
                case "image" :
                case "video" :
                case "audio" :
                    urls.add(s3Uploader.upload(file, fileType, id));
                    break;
                default :
                    throw new CustomException("지원하지 않는 미디어 타입", HttpStatus.BAD_REQUEST);
            }
        }
        return urls;
    }

    public List<String> getPresignedUrlByKey(List<String> key) {
        List<String> urls = new ArrayList<>();
        for (String url : key) {
            urls.add(s3Uploader.getPressigneUrl(url));
        }
        return urls;
    }
}
