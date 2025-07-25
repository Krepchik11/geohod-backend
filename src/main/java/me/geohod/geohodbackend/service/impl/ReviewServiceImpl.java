package me.geohod.geohodbackend.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.api.dto.review.ReviewCreateRequest;
import me.geohod.geohodbackend.data.dto.ReviewWithAuthorDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.ReviewRepository;
import me.geohod.geohodbackend.data.model.review.Review;
import me.geohod.geohodbackend.service.IReviewService;
import me.geohod.geohodbackend.service.IUserRatingService;

@Service
@RequiredArgsConstructor
@Slf4j
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
        
        // Check if review already exists for this user and event
        Optional<Review> existingReview = reviewRepository.findByEventIdAndAuthorId(request.eventId(), authorId);
        
        Review review;
        if (existingReview.isPresent()) {
            // Update existing review
            review = existingReview.get();
            review.setRating(request.rating());
            review.setComment(request.comment());
        } else {
            // Create new review
            review = new Review(
                    request.eventId(),
                    authorId,
                    request.rating(),
                    request.comment()
            );
        }
        
        Review savedReview = reviewRepository.save(review);
        
        // Update user rating for the event author asynchronously after transaction commit
        scheduleUserRatingUpdateAfterTransaction(event.getAuthorId());
        
        return savedReview;
    }

    @Override
    @Transactional
    public void hideReview(UUID reviewId, UUID eventAuthorId) {
        Review review = reviewRepository.findByIdAndEventAuthorId(reviewId, eventAuthorId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found or you are not the event author: " + reviewId));

        review.hide();
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void unhideReview(UUID reviewId, UUID eventAuthorId) {
        Review review = reviewRepository.findByIdAndEventAuthorId(reviewId, eventAuthorId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found or you are not the event author: " + reviewId));

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
                        p.id(),
                        p.eventId(),
                        p.authorId(),
                        p.authorUsername(),
                        p.authorImageUrl(),
                        p.rating(),
                        p.comment(),
                        p.isHidden(),
                        p.createdAt()
                ))
                .toList();

        return new PageImpl<>(reviewsWithAuthor, pageable, totalElements);
    }

    @Override
    public Optional<Review> getUserReviewForEvent(UUID userId, UUID eventId) {
        return reviewRepository.findByEventIdAndAuthorId(eventId, userId);
    }
    
    private void scheduleUserRatingUpdateAfterTransaction(UUID userId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.debug("Transaction committed, scheduling async rating update for user: {}", userId);
                    userRatingService.updateUserRatingAsync(userId);
                }
                
                @Override
                public void afterCompletion(int status) {
                    if (status != TransactionSynchronization.STATUS_COMMITTED) {
                        log.debug("Transaction rolled back, skipping rating update for user: {}", userId);
                    }
                }
            });
        } else {
            // Fallback: execute immediately if no transaction synchronization
            log.warn("No transaction synchronization active, executing rating update immediately");
            userRatingService.updateUserRatingAsync(userId);
        }
    }
}
