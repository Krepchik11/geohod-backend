package me.geohod.geohodbackend.api.controller.v2;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.response.CurrentUserDetailsResponse;
import me.geohod.geohodbackend.api.dto.response.UserDetailsResponse;
import me.geohod.geohodbackend.api.dto.response.UserResponse;
import me.geohod.geohodbackend.api.dto.response.UserStatsResponse;
import me.geohod.geohodbackend.api.dto.review.ReviewResponse;
import me.geohod.geohodbackend.api.mapper.ReviewApiMapper;
import me.geohod.geohodbackend.api.mapper.UserApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.api.response.PageResponse;
import me.geohod.geohodbackend.data.dto.UserDto;
import me.geohod.geohodbackend.data.dto.UserRatingDto;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.security.principal.AppPrincipal;
import me.geohod.geohodbackend.service.IReviewService;
import me.geohod.geohodbackend.service.IUserRatingService;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.IUserStatsService;

@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
public class UserController {
    private final UserApiMapper userMapper;
    private final ReviewApiMapper reviewApiMapper;
    private final IUserService userService;
    private final IUserRatingService userRatingService;
    private final IReviewService reviewService;
    private final IUserStatsService userStatsService;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user details")
    public ApiResponse<CurrentUserDetailsResponse> getCurrentUser(@AuthenticationPrincipal AppPrincipal principal) {
        User user = userService.getUser(principal.userId());
        return ApiResponse.success(userMapper.mapToCurrentUserDetails(user));
    }

    @GetMapping("/by-tg-id/{tgId}")
    public ApiResponse<UserResponse> userByTgId(@PathVariable String tgId) {
        UserDto user = userService.getUserByTelegramId(tgId);
        UserResponse response = userMapper.map(user);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> getUserReviews(@PathVariable UUID id, @PageableDefault(size = 10) Pageable pageable, @AuthenticationPrincipal AppPrincipal principal) {
        Page<ReviewResponse> reviewsPage = reviewService.getReviewsWithAuthorForUser(id, principal.userId(), pageable)
                .map(reviewApiMapper::map);
        return ApiResponse.success(new PageResponse<>(reviewsPage));
    }

    @GetMapping("/{id}/rating")
    public ApiResponse<Double> getUserRating(@PathVariable UUID id) {
        UserRatingDto rating = userRatingService.getUserRating(id);
        return ApiResponse.success(rating.averageRating().doubleValue());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user details by ID")
    public ApiResponse<UserDetailsResponse> getUserDetails(@PathVariable UUID id) {
        User user = userService.getUser(id);
        UserDetailsResponse response = userMapper.mapToDetails(user);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get user statistics by ID")
    public ApiResponse<UserStatsResponse> getUserStats(@PathVariable UUID id) {
        UserStatsResponse stats = userStatsService.getUserStats(id);
        return ApiResponse.success(stats);
    }
}


