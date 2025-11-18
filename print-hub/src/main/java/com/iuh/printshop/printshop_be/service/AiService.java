package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.dto.ai.ChatResponse;
import com.iuh.printshop.printshop_be.entity.Product;
import com.iuh.printshop.printshop_be.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final ProductRepository productRepository;

    /**
     * Xử lý input từ người dùng, phân tích và trả về response + sản phẩm gợi ý
     */
    public ChatResponse processUserInput(String input) {
        // 1. Lấy tất cả sản phẩm từ database
        List<Product> allProducts = productRepository.findAll();

        // 2. Xây dựng context về sản phẩm
        String productContext = buildProductContext(allProducts);

        // 3. Xây dựng system prompt
        String systemPrompt = "Bạn là một chuyên gia tư vấn máy in và máy scan chuyên nghiệp. " +
                "Nhiệm vụ của bạn:\n" +
                "1. TRẢ LỜI TRỰC TIẾP câu hỏi của khách hàng dựa trên danh sách sản phẩm có sẵn\n" +
                "2. Đưa ra các sản phẩm phù hợp nhất với nhu cầu\n" +
                "3. Trong câu trả lời, nếu đề cập đến sản phẩm cụ thể, hãy ghi rõ ID sản phẩm trong format: [PRODUCT_ID:xxx]\n" +
                "4. Trả lời bằng tiếng Việt, thân thiện và chuyên nghiệp\n" +
                "5. Ở cuối câu trả lời, nếu cần thêm thông tin để tư vấn tốt hơn, bạn có thể đặt câu hỏi (nhưng không bắt buộc)\n" +
                "6. Ưu tiên trả lời trực tiếp và đưa ra sản phẩm gợi ý trước, câu hỏi tư vấn để sau";

        // 4. Gọi AI với context
        String fullPrompt = systemPrompt + "\n\n" + productContext + "\n\n" +
                "CÂU HỎI CỦA KHÁCH HÀNG: " + input + "\n\n" +
                "Hãy phân tích và đưa ra lời khuyên phù hợp cùng với các sản phẩm gợi ý.";

        String aiResponse = chatClient
                .prompt()
                .user(fullPrompt)
                .call()
                .content();

        // 5. Parse response để lấy product IDs
        List<Integer> recommendedIds = extractProductIds(aiResponse, allProducts);

        return ChatResponse.builder()
                .reply(aiResponse)
                .recommendedProductIds(recommendedIds)
                .build();
    }

    /**
     * Xây dựng context string từ danh sách sản phẩm
     */
    private String buildProductContext(List<Product> products) {
        if (products.isEmpty()) {
            return "Hiện tại không có sản phẩm nào trong hệ thống.";
        }

        StringBuilder context = new StringBuilder();
        context.append("DANH SÁCH SẢN PHẨM HIỆN CÓ:\n\n");

        for (Product product : products) {
            context.append(String.format(
                    "ID: %d\n" +
                            "Tên: %s\n" +
                            "Mô tả: %s\n" +
                            "Giá: %s VNĐ\n" +
                            "Danh mục: %s\n" +
                            "Thương hiệu: %s\n" +
                            "Tồn kho: %d\n" +
                            "---\n",
                    product.getId(),
                    product.getName(),
                    product.getDescription() != null ? product.getDescription() : "Không có mô tả",
                    product.getPrice().toString(),
                    product.getCategory() != null ? product.getCategory().getName() : "N/A",
                    product.getBrand() != null ? product.getBrand().getName() : "N/A",
                    product.getStockQuantity() != null ? product.getStockQuantity() : 0
            ));
        }

        return context.toString();
    }

    /**
     * Extract product IDs từ AI response
     * Format: [PRODUCT_ID:123] hoặc tìm tên sản phẩm trong response
     */
    private List<Integer> extractProductIds(String aiResponse, List<Product> allProducts) {
        List<Integer> ids = new ArrayList<>();

        // Pattern 1: [PRODUCT_ID:123]
        Pattern pattern = Pattern.compile("\\[PRODUCT_ID:(\\d+)\\]");
        Matcher matcher = pattern.matcher(aiResponse);
        while (matcher.find()) {
            try {
                int id = Integer.parseInt(matcher.group(1));
                if (!ids.contains(id)) {
                    ids.add(id);
                }
            } catch (NumberFormatException e) {
                // Ignore invalid IDs
            }
        }

        // Pattern 2: Tìm tên sản phẩm trong response và match với database
        for (Product product : allProducts) {
            if (aiResponse.contains(product.getName()) && !ids.contains(product.getId())) {
                ids.add(product.getId());
            }
        }

        return ids.stream().distinct().collect(Collectors.toList());
    }
}

