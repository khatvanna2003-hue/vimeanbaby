package com.vimeanbaby.media.dto;

public record MediaUploadResponse(
        String url,
        String secureUrl,
        String publicId,
        String format,
        Integer width,
        Integer height,
        String folder,
        String thumbnailUrl
) {
}
