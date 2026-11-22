package com.tourist.Yatra_secure.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourist.Yatra_secure.Repository.SafetyZoneRepository;
import com.tourist.Yatra_secure.dto.SafetyZoneRequest;
import com.tourist.Yatra_secure.entity.SafetyZone;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SafetyZoneService {
 private final SafetyZoneRepository repository; private final ObjectMapper mapper;
 public SafetyZoneService(SafetyZoneRepository repository,ObjectMapper mapper){this.repository=repository;this.mapper=mapper;}
 public SafetyZone save(SafetyZone zone,SafetyZoneRequest request){validateGeoJson(request.boundaryGeoJson());zone.setFields(request.name().trim(),request.description(),request.zoneType(),request.boundaryGeoJson(),request.source().trim(),request.verified(),request.active());return repository.save(zone);}
 public JsonNode validateGeoJson(String raw){try{JsonNode root=mapper.readTree(raw);if(!"Feature".equals(root.path("type").asText())) throw invalid("Boundary must be a GeoJSON Feature");JsonNode geometry=root.path("geometry");String type=geometry.path("type").asText();JsonNode coordinates=geometry.path("coordinates");if("Polygon".equals(type)){validatePolygon(coordinates);}
 else if("MultiPolygon".equals(type)){if(!coordinates.isArray()||coordinates.isEmpty())throw invalid("MultiPolygon needs polygon coordinates");for(JsonNode polygon:coordinates)validatePolygon(polygon);}
 else throw invalid("Geometry must be Polygon or MultiPolygon");return geometry;}catch(AuthException e){throw e;}catch(Exception e){throw invalid("Boundary is not valid GeoJSON");}}
 private void validatePolygon(JsonNode rings){if(!rings.isArray()||rings.isEmpty())throw invalid("Polygon must contain an outer ring");for(JsonNode ring:rings){if(!ring.isArray()||ring.size()<4)throw invalid("Each ring needs at least four coordinates");JsonNode first=ring.get(0),last=ring.get(ring.size()-1);if(!first.equals(last))throw invalid("Polygon rings must be closed");for(JsonNode c:ring){if(!c.isArray()||c.size()<2||!c.get(0).isNumber()||!c.get(1).isNumber())throw invalid("Coordinates must be [longitude, latitude]");double lon=c.get(0).asDouble(),lat=c.get(1).asDouble();if(!Double.isFinite(lon)||!Double.isFinite(lat)||lon < -180||lon>180||lat < -90||lat>90)throw invalid("Coordinate is outside longitude/latitude bounds");}}}
 public List<Map<String,Object>> checkPoint(double lat,double lon){List<Map<String,Object>> result=new ArrayList<>();for(SafetyZone z:repository.findByActiveTrueAndVerifiedTrue()){JsonNode geom=validateGeoJson(z.getBoundaryGeoJson());boolean inside=geom.path("type").asText().equals("Polygon")?containsPolygon(geom.path("coordinates"),lon,lat):false;if(geom.path("type").asText().equals("MultiPolygon"))for(JsonNode p:geom.path("coordinates"))inside|=containsPolygon(p,lon,lat);if(inside)result.add(Map.of("id",z.getId(),"name",z.getName(),"zoneType",z.getZoneType(),"description",Objects.toString(z.getDescription(),""),"source",z.getSource(),"verified",true,"advisory",advisory(z.getZoneType())));}return result;}
 private boolean containsPolygon(JsonNode rings,double x,double y){if(!inRing(rings.get(0),x,y))return false;for(int i=1;i<rings.size();i++)if(inRing(rings.get(i),x,y))return false;return true;}
 private boolean inRing(JsonNode ring,double x,double y){boolean inside=false;for(int i=0,j=ring.size()-1;i<ring.size();j=i++){double xi=ring.get(i).get(0).asDouble(),yi=ring.get(i).get(1).asDouble(),xj=ring.get(j).get(0).asDouble(),yj=ring.get(j).get(1).asDouble();if(onSegment(x,y,xi,yi,xj,yj))return true;if((yi>y)!=(yj>y)&&x<(xj-xi)*(y-yi)/(yj-yi)+xi)inside=!inside;}return inside;}
 private boolean onSegment(double x,double y,double x1,double y1,double x2,double y2){double cross=(x-x1)*(y2-y1)-(y-y1)*(x2-x1);return Math.abs(cross)<1e-10&&x>=Math.min(x1,x2)-1e-10&&x<=Math.max(x1,x2)+1e-10&&y>=Math.min(y1,y2)-1e-10&&y<=Math.max(y1,y2)+1e-10;}
 private String advisory(String type){return switch(type){case "CAUTION"->"Caution zone reported by a verified source. Use judgment and consult current local guidance.";case "RESTRICTED"->"This area is marked restricted by a verified source. Follow official local guidance.";default->"This area is marked SAFE by a verified source; conditions may change. Use current local guidance.";};}
 private AuthException invalid(String m){return new AuthException(HttpStatus.BAD_REQUEST,m);}
}
