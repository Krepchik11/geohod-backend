package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.review.ReviewCreateRequest;
import me.geohod.geohodbackend.data.dto.ReviewWithAuthorDto;
import me.geohod.geohodbackend.data.model.review.Review;
import me.geohod.geohodbackend.data.model.repository.ReviewRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.service.IReviewService;
import me.geohod.geohodbackend.service.IUserRatingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final IUserRatingService userRatingService;

    @Override
    @Transactional
    public Review submitReview(UUID authorId, ReviewCreateRequest request) {
        // Get the event to validate it exists and get the author (target)
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + request.eventId()));
        
        Review review = new Review(
                request.eventId(),
                authorId,
                request.rating(),
                request.comment()
        );
        
        Review savedReview = reviewRepository.save(review);
        
        // Update user rating for the event author asynchronously
        userRatingService.updateUserRatingAsync(event.getAuthorId());
        
        return savedReview;
    }

    @Override
    @Transactional
    public void hideReview(UUID reviewId, UUID authorId) {
        Review review = reviewRepository.findByIdAndAuthorId(reviewId, authorId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        review.hide();
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void unhideReview(UUID reviewId, UUID authorId) {
        Review review = reviewRepository.findByIdAndAuthorId(reviewId, authorId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        review.unhide();
        reviewRepository.save(review);
    }

    @Override
    public Page<Review> getReviewsForUser(UUID userId, Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        
        List<Review> reviews = reviewRepository.findByEventAuthorIdWithPaging(userId, limit, offset);
        long totalElements = reviewRepository.countByEventAuthorId(userId);
        
        return new PageImpl<>(reviews, pageable, totalElements);
    }

    @Override
    public Page<ReviewWithAuthorDto> getReviewsWithAuthorForUser(UUID userId, UUID requestingUserId, Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        boolean showHidden = userId.equals(requestingUserId);

        List<ReviewRepository.ReviewWithAuthorProjection> projections = reviewRepository.findReviewsWithAuthorForUser(userId, showHidden, limit, offset);
        long totalElements = reviewRepository.countReviewsWithAuthorForUser(userId, showHidden);

        List<ReviewWithAuthorDto> reviewsWithAuthor = projections.stream()
                .map(p -> new ReviewWithAuthorDto(
                        p.getId(),
                        p.getEventId(),
                        p.getAuthorId(),
                        p.getAuthorUsername(),
                        p.getAuthorImageUrl(),
                        p.getRating(),
                        p.getComment(),
                        p.getIsHidden(),
                        p.getCreatedAt()
                ))
                .toList();

        return new PageImpl<>(reviewsWithAuthor, pageable, totalElements);
    }
} 