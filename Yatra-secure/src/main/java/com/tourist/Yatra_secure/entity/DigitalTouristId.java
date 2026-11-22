package com.tourist.Yatra_secure.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "digital_tourist_ids", uniqueConstraints = @UniqueConstraint(columnNames = "tourist_id"))
public class DigitalTouristId {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tourist_id", nullable = false, unique = true, length = 64) private String touristId;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true) private user tourist;
    @Column(nullable = false) private Instant issuedAt;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    protected DigitalTouristId() {}
    public DigitalTouristId(String touristId, user tourist, Instant issuedAt) { this.touristId=touristId; this.tourist=tourist; this.issuedAt=issuedAt; }
    @PrePersist void created(){createdAt=Instant.now();updatedAt=createdAt;}
    @PreUpdate void updated(){updatedAt=Instant.now();}
    public Long getId() { return id; }
    public String getTouristId() { return touristId; }
    public user getTourist() { return tourist; }
    public Instant getIssuedAt() { return issuedAt; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
    public void revoke(){active=false;}
    public void reissue(String replacementTouristId, Instant replacementIssuedAt){touristId=replacementTouristId;issuedAt=replacementIssuedAt;active=true;}
}
