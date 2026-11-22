package com.tourist.Yatra_secure.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="tourist_locations")
public class TouristLocation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id",nullable=false,unique=true) private user tourist;
    @Column(nullable=false) private double latitude;
    @Column(nullable=false) private double longitude;
    @Column(nullable=false) private boolean sharingEnabled;
    @Column(nullable=false) private Instant updatedAt;
    protected TouristLocation() {}
    public TouristLocation(user tourist,double latitude,double longitude,boolean sharingEnabled,Instant updatedAt){this.tourist=tourist;this.latitude=latitude;this.longitude=longitude;this.sharingEnabled=sharingEnabled;this.updatedAt=updatedAt;}
    public Long getId(){return id;} public user getTourist(){return tourist;} public double getLatitude(){return latitude;} public double getLongitude(){return longitude;} public boolean isSharingEnabled(){return sharingEnabled;} public void setLatitude(double value){latitude=value;} public void setLongitude(double value){longitude=value;} public void setSharingEnabled(boolean value){sharingEnabled=value;} public void setUpdatedAt(Instant value){updatedAt=value;} public Instant getUpdatedAt(){return updatedAt;}
}
