package com.iuh.printshop.printshop_be.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Integer productId;
    private String productName;
    private Integer userId;
    private String userName;
    private String userEmail;
    private Integer rating;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}

