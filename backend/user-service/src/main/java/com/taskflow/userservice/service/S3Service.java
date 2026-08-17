package com.taskflow.userservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String uploadFile(MultipartFile file);

    void deleteFile(String fileName);

    String extractKeyFromUrl(String imageUrl);
}