package me.geohod.geohodbackend.auth.api.dto;

public record EmailOtpVerifyRequest(String email, String code) {}
