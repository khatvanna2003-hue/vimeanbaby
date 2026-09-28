package com.vimeanbaby.media.controller;

import com.vimeanbaby.common.ApiResponse;
import com.vimeanbaby.media.dto.MediaUploadResponse;
import com.vimeanbaby.media.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/media")
@RequiredArgsConstructor
public class AdminMediaController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "vimeanbaby/products") String folder
    ) {
        return ApiResponse.ok(cloudinaryService.upload(file, folder), "Image uploaded");
    }

    /** publicId may contain folders, e.g. vimeanbaby/products/abc123 */
    @DeleteMapping("/{*publicId}")
    public ApiResponse<Void> delete(@PathVariable("publicId") String publicId) {
        String id = publicId.startsWith("/") ? publicId.substring(1) : publicId;
        cloudinaryService.delete(id);
        return ApiResponse.ok(null, "Image deleted");
    }
}
