package com.vimeanbaby.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.vimeanbaby.exception.BadRequestException;
import com.vimeanbaby.media.dto.MediaUploadResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final Cloudinary cloudinary;

    @Value("${app.cloudinary.cloud-name}")
    private String cloudName;

    public MediaUploadResponse upload(MultipartFile file, String folder) {
        validateFile(file);
        String targetFolder = StringUtils.hasText(folder) ? folder.trim() : "vimeanbaby/products";

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", targetFolder,
                    "resource_type", "image",
                    "overwrite", false,
                    "unique_filename", true
            ));

            String publicId = stringVal(result.get("public_id"));
            String secureUrl = stringVal(result.get("secure_url"));
            String url = stringVal(result.get("url"));
            String format = stringVal(result.get("format"));
            Integer width = intVal(result.get("width"));
            Integer height = intVal(result.get("height"));

            return new MediaUploadResponse(
                    url,
                    secureUrl,
                    publicId,
                    format,
                    width,
                    height,
                    targetFolder,
                    thumbnailUrl(publicId, 600)
            );
        } catch (IOException ex) {
            throw new BadRequestException("Failed to upload image: " + ex.getMessage());
        }
    }

    public void delete(String publicId) {
        if (!StringUtils.hasText(publicId)) {
            throw new BadRequestException("publicId is required");
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException ex) {
            throw new BadRequestException("Failed to delete image: " + ex.getMessage());
        }
    }

    public String thumbnailUrl(String publicId, int width) {
        if (!StringUtils.hasText(publicId) || !StringUtils.hasText(cloudName)) {
            return null;
        }
        return cloudinary.url()
                .secure(true)
                .transformation(new Transformation<>()
                        .width(width)
                        .crop("limit")
                        .fetchFormat("auto")
                        .quality("auto"))
                .generate(publicId);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("File size must be <= 5 MB");
        }

        String contentType = file.getContentType() != null
                ? file.getContentType().toLowerCase(Locale.ROOT)
                : "";
        String filename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase(Locale.ROOT)
                : "";
        String extension = "";
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot < filename.length() - 1) {
            extension = filename.substring(dot + 1);
        }

        boolean typeOk = ALLOWED_CONTENT_TYPES.contains(contentType);
        boolean extOk = ALLOWED_EXTENSIONS.contains(extension);
        if (!typeOk && !extOk) {
            throw new BadRequestException("Only jpg, png, and webp images are allowed");
        }
    }

    private String stringVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer intVal(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }
}
