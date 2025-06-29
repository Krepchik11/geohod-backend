package me.geohod.geohodbackend.service.notification;

import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.User;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record EventContext(Event event, User author) {
    public Optional<String> getContactInfo() {
        String fullName = getAuthorFullName();
        String tgUsername = author.getTgUsername();

        if (StringUtils.isBlank(fullName) && StringUtils.isBlank(tgUsername)) {
            return Optional.empty();
        }

        return Optional.of(formatContactInfo(fullName, tgUsername));
    }

    private String getAuthorFullName() {
        return Stream.of(author.getFirstName(), author.getLastName())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" "));
    }

    private String formatContactInfo(String fullName, String tgUsername) {
        if (StringUtils.isBlank(tgUsername)) {
            return String.format("Организатор: %s", fullName);
        }

        if (StringUtils.isBlank(fullName)) {
            return String.format("Организатор: @%s", tgUsername);
        }

        return String.format("Организатор: %s @%s", fullName, tgUsername);
    }
} 