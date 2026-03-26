package me.geohod.geohodbackend.api.response;

public record PageMetadata(
    int size,
    int number,
    long totalElements,
    int totalPages
) {}
