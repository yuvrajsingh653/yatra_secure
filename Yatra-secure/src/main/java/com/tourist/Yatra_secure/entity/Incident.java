package com.tourist.Yatra_secure.entity;
import jakarta.persistence.*;import java.time.Instant;
@Entity @Table(name="incidents") public class Incident{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="tourist_id",nullable=false) private user tourist;
 @Column(nullable=false,length=32) private String incidentType;
 @Column(nullable=false,length=2000) private String description;
 private Double latitude;private Double longitude;
 @Column(nullable=false,length=16) private String priority;
 @Column(nullable=false,length=24) private String status;
 @Column(length=160) private String assignedTo;
 @Column(nullable=false,updatable=false) private Instant createdAt;
 @Column(nullable=false) private Instant updatedAt;
 protected Incident(){}
 public Incident(user tourist,String type,String description,Double lat,Double lon,String priority,String status){this.tourist=tourist;this.incidentType=type;this.description=description;this.latitude=lat;this.longitude=lon;this.priority=priority;this.status=status;}
 @PrePersist void create(){createdAt=Instant.now();updatedAt=createdAt;} @PreUpdate void update(){updatedAt=Instant.now();}
 public Long getId(){return id;}public user getTourist(){return tourist;}public String getIncidentType(){return incidentType;}public String getDescription(){return description;}public Double getLatitude(){return latitude;}public Double getLongitude(){return longitude;}public String getPriority(){return priority;}public String getStatus(){return status;}public String getAssignedTo(){return assignedTo;}public void setAssignedTo(String v){assignedTo=v;}public void setStatus(String v){status=v;}public Instant getCreatedAt(){return createdAt;}public Instant getUpdatedAt(){return updatedAt;}
}
