package me.geohod.geohodbackend.api.mapper;

import me.geohod.geohodbackend.api.dto.review.ReviewResponse;
import me.geohod.geohodbackend.data.model.review.Review;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface ReviewApiMapper {
    ReviewResponse map(Review review);
} 