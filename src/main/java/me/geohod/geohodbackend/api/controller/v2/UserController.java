package me.geohod.geohodbackend.api.controller.v2;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.response.UserResponse;
import me.geohod.geohodbackend.api.dto.review.ReviewResponse;
import me.geohod.geohodbackend.api.mapper.ReviewApiMapper;
import me.geohod.geohodbackend.api.mapper.UserApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.data.dto.UserDto;
import me.geohod.geohodbackend.data.dto.UserRatingDto;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import me.geohod.geohodbackend.service.IReviewService;
import me.geohod.geohodbackend.service.IUserRatingService;
import me.geohod.geohodbackend.service.IUserService;

@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
public class UserController {
    private final UserApiMapper userMapper;
    private final ReviewApiMapper reviewApiMapper;
    private final IUserService userService;
    private final IUserRatingService userRatingService;
    private final IReviewService reviewService;

    @GetMapping("/by-tg-id/{tgId}")
    public ApiResponse<UserResponse> userByTgId(@PathVariable String tgId) {
        UserDto user = userService.getUserByTelegramId(tgId);
        UserResponse response = userMapper.map(user);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}/reviews")
    public ApiResponse<List<ReviewResponse>> getUserReviews(@PathVariable UUID id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @AuthenticationPrincipal TelegramPrincipal principal) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> reviewsPage = reviewService.getReviewsWithAuthorForUser(id, principal.userId(), pageable)
                .map(reviewApiMapper::map);
        return ApiResponse.success(reviewsPage.getContent());
    }

    @GetMapping("/{id}/rating")
    public ApiResponse<Double> getUserRating(@PathVariable UUID id) {
        UserRatingDto rating = userRatingService.getUserRating(id);
        return ApiResponse.success(rating.averageRating().doubleValue());
    }
}
