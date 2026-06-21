package com.fishcam.application.export;

import jakarta.annotation.PostConstruct; // NOUVEAU
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.net.URI;

@Slf4j
@Service
public class CloudflareR2StorageService {

    @Value("${cloudflare.r2.endpoint}")
    private String endpoint;
    @Value("${cloudflare.r2.access-key}")
    private String accessKey;
    @Value("${cloudflare.r2.secret-key}")
    private String secretKey;
    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    private S3Client s3Client; // NOUVEAU

    @PostConstruct
    public void init() {
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    public void uploadBackup(File file) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file.getName())
                    .build();

            s3Client.putObject(putObjectRequest, file.toPath());
            log.info("✅ Sauvegarde envoyée sur Cloudflare R2 : {}", file.getName());
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi sur Cloudflare R2 : {}", e.getMessage());
            throw new RuntimeException("Erreur Cloudflare R2", e);
        }
    }
}