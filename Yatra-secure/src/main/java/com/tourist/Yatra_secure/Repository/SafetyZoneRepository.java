package com.tourist.Yatra_secure.Repository;
import com.tourist.Yatra_secure.entity.SafetyZone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SafetyZoneRepository extends JpaRepository<SafetyZone,Long>{ List<SafetyZone> findByActiveTrueAndVerifiedTrue(); }
