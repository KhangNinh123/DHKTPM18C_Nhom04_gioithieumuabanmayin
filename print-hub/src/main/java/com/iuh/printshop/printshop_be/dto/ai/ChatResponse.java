package com.iuh.printshop.printshop_be.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String reply; // Câu trả lời từ AI
    private List<Integer> recommendedProductIds; // IDs sản phẩm được gợi ý
}

