package com.tourist.Yatra_secure.Repository;
import com.tourist.Yatra_secure.entity.Incident;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface IncidentRepository extends JpaRepository<Incident,Long>{List<Incident> findByTourist_EmailOrderByCreatedAtDesc(String email);List<Incident> findAllByOrderByCreatedAtDesc();List<Incident> findByStatusOrderByCreatedAtDesc(String status);}
