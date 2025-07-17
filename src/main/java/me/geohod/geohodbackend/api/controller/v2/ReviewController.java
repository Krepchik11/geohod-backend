package me.geohod.geohodbackend.api.controller.v2;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.review.ReviewCreateRequest;
import me.geohod.geohodbackend.api.dto.review.ReviewResponse;
import me.geohod.geohodbackend.api.mapper.ReviewApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.data.model.review.Review;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import me.geohod.geohodbackend.service.IReviewService;

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

    @GetMapping("/event/{eventId}/my-review")
    public ApiResponse<ReviewResponse> getMyReviewForEvent(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal TelegramPrincipal principal) {
        var reviewOptional = reviewService.getUserReviewForEvent(principal.userId(), eventId);
        if (reviewOptional.isPresent()) {
            ReviewResponse response = reviewApiMapper.map(reviewOptional.get());
            return ApiResponse.success(response);
        } else {
            return new ApiResponse<>("ERROR", "Review not found for this event", null);
        }
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
