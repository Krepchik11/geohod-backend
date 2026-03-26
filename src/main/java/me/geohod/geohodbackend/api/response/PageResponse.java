package me.geohod.geohodbackend.api.response;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel.PageMetadata;

public record PageResponse<T>(
        List<T> content,
        PageMetadata page) {
    public PageResponse(Page<T> page) {
        this(
                page.getContent(),
                new PageMetadata(
                        page.getSize(),
                        page.getNumber(),
                        page.getTotalElements(),
                        page.getTotalPages()));
    }
}