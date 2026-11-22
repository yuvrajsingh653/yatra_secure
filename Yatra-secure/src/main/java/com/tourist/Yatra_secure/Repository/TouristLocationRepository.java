package com.tourist.Yatra_secure.Repository;

import com.tourist.Yatra_secure.entity.TouristLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TouristLocationRepository extends JpaRepository<TouristLocation,Long> {
    Optional<TouristLocation> findByTourist_Email(String email);
    List<TouristLocation> findBySharingEnabledTrue();
}
