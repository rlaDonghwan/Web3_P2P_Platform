package com.inhatc.SafeCommerce.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ItemImageDTO {
    private Long id;
    private MultipartFile imageData;
    private String base64Image; // 이미지 미리보기용 Base64 인코딩 데이터

    public ItemImageDTO() {}

    public ItemImageDTO(Long id, MultipartFile imageData, String base64Image) {
        this.id = id;
        this.imageData = imageData;
        this.base64Image = base64Image;
    }
}