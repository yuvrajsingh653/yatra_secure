package com.tourist.Yatra_secure.service;
import com.tourist.Yatra_secure.Repository.*;import com.tourist.Yatra_secure.entity.*;import org.springframework.http.HttpStatus;import org.springframework.stereotype.Service;import org.springframework.beans.factory.annotation.Value;import java.time.Instant;import java.util.*;
@Service public class DigitalTouristIdService{
 private final DigitalTouristIdRepository ids;private final UserRepository users;@Value("${app.public-app-base-url:http://localhost:5173}") private String publicAppBaseUrl;
 public DigitalTouristIdService(DigitalTouristIdRepository ids,UserRepository users){this.ids=ids;this.users=users;}
 public DigitalTouristId issue(String email){user tourist=users.findByEmail(email).orElseThrow(()->new AuthException(HttpStatus.NOT_FOUND,"Tourist account not found"));var existing=ids.findByTourist_Email(email);if(existing.isPresent()){DigitalTouristId current=existing.get();if(current.isActive())return current;current.reissue(newTouristId(),Instant.now());return ids.save(current);}return ids.save(new DigitalTouristId(newTouristId(),tourist,Instant.now()));}
 private String newTouristId(){String key;do{key="YTS-"+UUID.randomUUID().toString().replace("-","");}while(ids.findByTouristId(key).isPresent());return key;}
 public DigitalTouristId own(String email){return ids.findByTourist_Email(email).orElseThrow(()->new AuthException(HttpStatus.NOT_FOUND,"Digital Tourist ID has not been issued"));}
 public Map<String,Object> publicVerify(String id){return ids.findByTouristId(id).map(x->Map.<String,Object>of("touristId",x.getTouristId(),"status",x.isActive()?"VALID":"REVOKED","valid",x.isActive())).orElseGet(()->Map.of("touristId",id,"status","NOT_FOUND","valid",false));}
 public List<Map<String,Object>> adminList(){return ids.findAll().stream().map(x->{Map<String,Object> view=new LinkedHashMap<>(privateView(x));view.put("id",x.getId());return view;}).toList();}
 public Map<String,Object> revoke(Long id){DigitalTouristId x=ids.findById(id).orElseThrow(()->new AuthException(HttpStatus.NOT_FOUND,"Digital ID not found"));x.revoke();return privateView(ids.save(x));}
 public Map<String,Object> privateView(DigitalTouristId x){String verify=publicAppBaseUrl.replaceAll("/+$","")+"/verify/"+x.getTouristId();return Map.of("touristId",x.getTouristId(),"status",x.isActive()?"ACTIVE":"REVOKED","issuedAt",x.getIssuedAt(),"createdAt",x.getCreatedAt(),"updatedAt",x.getUpdatedAt(),"verificationUrl",verify);}
}
