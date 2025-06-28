package me.geohod.geohodbackend.api.controller.v2;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.review.ReviewCreateRequest;
import me.geohod.geohodbackend.api.dto.review.ReviewResponse;
import me.geohod.geohodbackend.api.mapper.ReviewApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.data.dto.UserRatingDto;
import me.geohod.geohodbackend.data.model.review.Review;
import me.geohod.geohodbackend.data.model.userrating.UserRating;
import me.geohod.geohodbackend.service.IReviewService;
import me.geohod.geohodbackend.service.IUserRatingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final IReviewService reviewService;
    private final IUserRatingService userRatingService;
    private final ReviewApiMapper reviewApiMapper;

    @PostMapping
    public ApiResponse<ReviewResponse> submitReview(@RequestBody ReviewCreateRequest request, @RequestParam UUID authorId) {
        Review review = reviewService.submitReview(authorId, request);
        return ApiResponse.success(reviewApiMapper.map(review));
    }

    @GetMapping("/users/{id}/reviews")
    public ApiResponse<List<ReviewResponse>> getUserReviews(@PathVariable UUID id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> reviewsPage = reviewService.getReviewsWithAuthorForUser(id, pageable)
                .map(reviewApiMapper::map);
        
        return ApiResponse.success(reviewsPage.getContent());
    }

    @GetMapping("/users/{id}/rating")
    public ApiResponse<Double> getUserRating(@PathVariable UUID id) {
        UserRatingDto rating = userRatingService.getUserRating(id);
        return ApiResponse.success(rating.averageRating().doubleValue());
    }

    @PatchMapping("/{id}/hide")
    public ApiResponse<Void> hideReview(@PathVariable UUID id) {
        reviewService.hideReview(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/unhide")
    public ApiResponse<Void> unhideReview(@PathVariable UUID id) {
        reviewService.unhideReview(id);
        return ApiResponse.success(null);
    }
} 