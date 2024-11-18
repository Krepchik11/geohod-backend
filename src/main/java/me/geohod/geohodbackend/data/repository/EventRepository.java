package me.geohod.geohodbackend.data.repository;

import me.geohod.geohodbackend.data.Event;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventRepository extends CrudRepository<Event, UUID> {
}