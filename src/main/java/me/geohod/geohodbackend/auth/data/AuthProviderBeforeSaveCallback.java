package me.geohod.geohodbackend.auth.data;

import me.geohod.geohodbackend.auth.data.model.AuthProviderEntity;
import org.springframework.data.relational.core.conversion.MutableAggregateChange;
import org.springframework.data.relational.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuthProviderBeforeSaveCallback implements BeforeSaveCallback<AuthProviderEntity> {

    @Override
    public AuthProviderEntity onBeforeSave(AuthProviderEntity entity, MutableAggregateChange<AuthProviderEntity> aggregateChange) {
        entity.setLastModifiedDate(Instant.now());
        return entity;
    }
}
