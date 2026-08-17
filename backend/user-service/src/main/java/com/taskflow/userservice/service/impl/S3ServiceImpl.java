package com.taskflow.userservice.service.impl;

import com.taskflow.userservice.exception.InvalidFileException;
import com.taskflow.userservice.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;// 5 MB
    private static final Logger logger =
            LoggerFactory.getLogger(S3ServiceImpl.class);

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null ||
                !(contentType.equals("image/jpeg")
                        || contentType.equals("image/png")
                        || contentType.equals("image/jpg"))) {

            throw new InvalidFileException(
                    "Only JPG, JPEG and PNG images are allowed.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    "Profile image size must not exceed 5 MB.");
        }
        try {

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(file.getBytes())
            );

            return "https://" + bucketName +
                    ".s3.ap-south-1.amazonaws.com/" +
                    fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    @Override
    public void deleteFile(String fileName) {

        if (fileName == null || fileName.isBlank()) {
            return;
        }
        try {

            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build();

            s3Client.deleteObject(request);

            logger.info("Deleted file from S3. Key: {}", fileName);

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException("Failed to delete old image.", e);
        }
    }
    @Override
    public String extractKeyFromUrl(String imageUrl) {

        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }





}