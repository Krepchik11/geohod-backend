package me.geohod.geohodbackend.api.mapper;

import me.geohod.geohodbackend.api.dto.review.ReviewResponse;
import me.geohod.geohodbackend.data.dto.ReviewWithAuthorDto;
import me.geohod.geohodbackend.data.model.review.Review;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface ReviewApiMapper {
    ReviewResponse map(Review review);
    
    @Mapping(target = "authorUsername", source = "user.tgUsername")
    @Mapping(target = "authorImageUrl", source = "user.tgImageUrl")
    @Mapping(target = "id", source = "review.id")
    @Mapping(target = "createdAt", source = "review.createdAt")
    ReviewResponse mapWithUser(Review review, User user);
    
    ReviewResponse map(ReviewWithAuthorDto reviewWithAuthor);
} 