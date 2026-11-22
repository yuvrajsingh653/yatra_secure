package com.tourist.Yatra_secure.Repository;

import com.tourist.Yatra_secure.entity.DigitalTouristId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DigitalTouristIdRepository extends JpaRepository<DigitalTouristId, Long> {
    Optional<DigitalTouristId> findByTourist_Email(String email);
    Optional<DigitalTouristId> findByTouristIdAndActiveTrue(String touristId);
    Optional<DigitalTouristId> findByTouristId(String touristId);
}
