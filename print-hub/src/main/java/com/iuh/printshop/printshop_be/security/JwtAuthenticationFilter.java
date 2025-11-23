package com.iuh.printshop.printshop_be.security;

import com.iuh.printshop.printshop_be.service.JwtService;
import com.iuh.printshop.printshop_be.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    // Danh sách các đường dẫn public sẽ được bỏ qua bởi filter này
    private final List<String> publicPaths = Arrays.asList(
            "/api/auth/",
            "/api/products/",
            "/api/brands/",
            "/api/reviews/product/",
            "/chat/",
            "/swagger-ui",
            "/v3/api-docs"
    );

    /**
     * Quyết định xem có nên bỏ qua filter này cho request hiện tại không.
     * @return true nếu đường dẫn là public, false nếu cần kiểm tra JWT.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return publicPaths.stream().anyMatch(path::startsWith);
    }

    /**
     * Logic filter này CHỈ chạy cho các endpoint private (cần xác thực).
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Đối với endpoint private, không có token là lỗi -> để filter chain xử lý và trả về 401/403
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Nếu có lỗi khi parse JWT (token hết hạn, sai chữ ký...),
            // xóa context để đảm bảo không có thông tin xác thực nào được thiết lập.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
