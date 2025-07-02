package me.geohod.geohodbackend.api.controller.v2;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.review.ReviewCreateRequest;
import me.geohod.geohodbackend.api.dto.review.ReviewResponse;
import me.geohod.geohodbackend.api.mapper.ReviewApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.data.model.review.Review;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import me.geohod.geohodbackend.service.IReviewService;
import me.geohod.geohodbackend.service.IUserRatingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final IReviewService reviewService;
    private final ReviewApiMapper reviewApiMapper;

    @PostMapping
    public ApiResponse<ReviewResponse> submitReview(
            @RequestBody ReviewCreateRequest request, 
            @AuthenticationPrincipal TelegramPrincipal principal) {
        Review review = reviewService.submitReview(principal.userId(), request);
        return ApiResponse.success(reviewApiMapper.map(review));
    }

    @PatchMapping("/{id}/hide")
    public ApiResponse<Void> hideReview(
            @PathVariable UUID id, 
            @AuthenticationPrincipal TelegramPrincipal principal) {
        reviewService.hideReview(id, principal.userId());
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/unhide")
    public ApiResponse<Void> unhideReview(
            @PathVariable UUID id, 
            @AuthenticationPrincipal TelegramPrincipal principal) {
        reviewService.unhideReview(id, principal.userId());
        return ApiResponse.success(null);
    }
} 