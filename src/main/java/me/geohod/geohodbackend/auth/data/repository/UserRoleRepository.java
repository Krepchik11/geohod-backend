package me.geohod.geohodbackend.auth.data.repository;

import me.geohod.geohodbackend.auth.data.model.UserRole;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, UUID> {
    @Query("SELECT role FROM user_role WHERE user_id = :userId")
    List<String> findRolesByUserId(UUID userId);
}
