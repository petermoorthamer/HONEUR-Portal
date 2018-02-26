package com.jnj.honeur.portal.controller;

import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jnj.honeur.aws.s3.AmazonS3Service;
import com.jnj.honeur.security.SecurityUtils2;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AmazonS3Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonS3Controller.class);

    private static final String PERMISSION_BUCKET_READ_PREFIX = "bucket-read";
    private static final String PERMISSION_BUCKET_WRITE_PREFIX = "bucket-write";
    private static final String PERMISSION_BUCKET_SEPARATOR = ":";

    private AmazonS3Service honeurCentralAmazonS3Service;
    private AmazonS3Service honeurLocalAmazonS3Service;
    private AmazonS3Service defaultAmazonS3Service;
    private List<String> bucketList;

    public AmazonS3Controller(@Autowired AmazonS3Service honeurCentralAmazonS3Service, @Autowired AmazonS3Service honeurLocalAmazonS3Service, @Autowired AmazonS3Service moorthamerAmazonS3Service) {
        this.honeurCentralAmazonS3Service = honeurCentralAmazonS3Service;
        this.honeurLocalAmazonS3Service = honeurLocalAmazonS3Service;
        this.defaultAmazonS3Service = moorthamerAmazonS3Service;
        this.initBucketList();
    }

    private void initBucketList() {
        this.bucketList = new ArrayList<>();
        this.bucketList.add("honeur-in");
        this.bucketList.add("honeur-out");
        this.bucketList.add("peter.moorthamer");
    }

    @RequiresAuthentication
    @RequestMapping("/amazonS3")
    public String home(HttpServletRequest request, Model model) {

        Subject subject = SecurityUtils.getSubject();

        model.addAttribute("subjectName", SecurityUtils2.getSubjectName(subject));
        model.addAttribute("readBucketList", getBucketList(subject, PERMISSION_BUCKET_READ_PREFIX));
        model.addAttribute("writeBucketList", getBucketList(subject, PERMISSION_BUCKET_WRITE_PREFIX));

        return "amazonS3";
    }

    @RequiresAuthentication
    @RequestMapping("/amazonS3bucket/{bucketName:.+}")
    public String bucket(HttpServletRequest request, Model model, @PathVariable String bucketName) {

        final Subject subject = SecurityUtils.getSubject();
        model.addAttribute("subjectName", SecurityUtils2.getSubjectName(subject));
        boolean hasWritePermission = subject.isPermitted(generatePermission(PERMISSION_BUCKET_WRITE_PREFIX, bucketName));
        model.addAttribute("subjectHasWritePermission", hasWritePermission);
        model.addAttribute("bucketName", bucketName);

        AmazonS3Service amazonS3Service = getAmazonS3Service(subject);
        ListObjectsV2Result result = amazonS3Service.getObjects(bucketName);
        List<S3ObjectSummary> summaryList = result.getObjectSummaries();

        model.addAttribute("objectSummaryList", summaryList);

        return "amazonS3bucket";
    }

    @RequiresAuthentication
    @RequestMapping("/amazonS3object/{bucketName:.+}/{objectKey:.+}")
    public ResponseEntity<Object> downloadObject(HttpServletRequest request, Model model, @PathVariable String bucketName, @PathVariable("objectKey") String objectKey) {

        final Subject subject = SecurityUtils.getSubject();

        try {
            AmazonS3Service amazonS3Service = getAmazonS3Service(subject);

            //File tmpFile = amazonS3Service.getObjectFile(bucketName, objectKey);
            File tmpFile = amazonS3Service.createTempFile(objectKey);
            amazonS3Service.downloadFile(bucketName, objectKey, tmpFile);
            LOGGER.info("Downloaded file @ server: " + tmpFile.getAbsolutePath());

            Path path = Paths.get(tmpFile.getAbsolutePath());
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + objectKey);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(tmpFile.length())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(resource);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<Object>(
                    "File cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<Object>(
                    "File cannot be downloaded!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
    }

    @RequiresAuthentication
    @PostMapping("/amazonS3object/{bucketName:.+}")
    public String uploadObject(@RequestParam("file") MultipartFile file, @PathVariable String bucketName) {
        if(!file.isEmpty()) {
            try {
                final Subject subject = SecurityUtils.getSubject();
                AmazonS3Service amazonS3Service = getAmazonS3Service(subject);
                File tmpFile = amazonS3Service.createTempFile(file.getOriginalFilename());
                file.transferTo(tmpFile);
                amazonS3Service.uploadFile(bucketName, file.getOriginalFilename(), tmpFile);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return "redirect:/amazonS3bucket/" + bucketName;
    }

    @RequiresAuthentication
    @DeleteMapping("/amazonS3object/{bucketName:.+}/{objectKey:.+}")
    public String deleteObject(@PathVariable String bucketName, @PathVariable("objectKey") String objectKey) {
        final Subject subject = SecurityUtils.getSubject();
        AmazonS3Service amazonS3Service = getAmazonS3Service(subject);
        amazonS3Service.deleteObject(bucketName, objectKey);
        return "redirect:/amazonS3bucket/" + bucketName;
    }

    private List<String> getBucketList(Subject subject, String permissionPrefix) {
        List<String> filteredBucketList = new ArrayList<>();
        for(String bucketName:bucketList) {
            String permission = generatePermission(permissionPrefix, bucketName);
            if(subject.isPermitted(permission)) {
                filteredBucketList.add(bucketName);
            }
        }
        return filteredBucketList;
    }

    private String generatePermission(String permissionPrefix, String bucketName) {
        return permissionPrefix + PERMISSION_BUCKET_SEPARATOR + bucketName;
    }

    /**
     * !!! Temporary implementation !!!
     */
    private AmazonS3Service getAmazonS3Service(Subject subject) {
        String subjectName = SecurityUtils2.getSubjectName(subject);
        if(subjectName.contains("local")) {
            return honeurLocalAmazonS3Service;
        } else if(subjectName.contains("central")) {
            return honeurCentralAmazonS3Service;
        } else {
            return defaultAmazonS3Service;
        }
    }

}
