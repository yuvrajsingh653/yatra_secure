package com.tourist.Yatra_secure.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="safety_zones")
public class SafetyZone {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=120) private String name;
 @Column(length=1000) private String description;
 @Column(nullable=false,length=16) private String zoneType;
 @Lob @Column(nullable=false) private String boundaryGeoJson;
 @Column(nullable=false,length=120) private String source;
 @Column(nullable=false) private boolean verified;
 @Column(nullable=false) private boolean active;
 @Column(nullable=false,updatable=false) private Instant createdAt;
 @Column(nullable=false) private Instant updatedAt;
 public SafetyZone() {}
 public void setFields(String name,String description,String type,String boundary,String source,boolean verified,boolean active){this.name=name;this.description=description;this.zoneType=type;this.boundaryGeoJson=boundary;this.source=source;this.verified=verified;this.active=active;this.updatedAt=Instant.now();}
 @PrePersist void created(){createdAt=Instant.now();updatedAt=createdAt;}
 public Long getId(){return id;} public String getName(){return name;} public String getDescription(){return description;} public String getZoneType(){return zoneType;} public String getBoundaryGeoJson(){return boundaryGeoJson;} public String getSource(){return source;} public boolean isVerified(){return verified;} public boolean isActive(){return active;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
