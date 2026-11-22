package com.tourist.Yatra_secure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourist.Yatra_secure.Security.JwtService;
import org.springframework.web.cors.CorsConfigurationSource;
import com.tourist.Yatra_secure.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:yatra_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.public-app-base-url=http://192.168.1.5:5173",
        "app.admin.email=admin@yatrasecure.test",
        "app.admin.password=Test-admin-password-123"
})
class YatraSecureApplicationTests {
    @Value("${local.server.port}") private int port;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository users;
    @Autowired private CorsConfigurationSource corsConfigurationSource;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void touristAndAdminFlowsRespectJwtAndRoleBoundaries() throws Exception {
        Map<String, Object> registration = Map.of("name", "Integration Tourist", "email", "tourist@yatrasecure.test", "password", "Tourist-password-123");
        assertEquals(HttpStatus.CREATED, call("POST", "/api/auth/register", null, registration).status());

        String touristToken = login("tourist@yatrasecure.test", "Tourist-password-123");
        String adminToken = login("admin@yatrasecure.test", "Test-admin-password-123");
        assertEquals("tourist@yatrasecure.test", jwtService.extractEmail(touristToken));
        assertEquals("ROLE_TOURIST", jwtService.extractRole(touristToken));
        assertEquals("ROLE_TOURIST", users.findByEmail("tourist@yatrasecure.test").orElseThrow().getRole());
        assertEquals(HttpStatus.OK, call("GET", "/api/auth/profile", touristToken, null).status());
        assertEquals(HttpStatus.UNAUTHORIZED, call("GET", "/api/auth/profile", null, null).status());
        assertEquals(HttpStatus.FORBIDDEN, call("GET", "/api/admin/dashboard", touristToken, null).status());
        assertEquals(HttpStatus.OK, call("GET", "/api/admin/dashboard", adminToken, null).status());

        Map<?, ?> profile = (Map<?, ?>) call("GET", "/api/user/profile", touristToken, null).body();
        assertEquals("tourist@yatrasecure.test", profile.get("email"));
        Map<?, ?> updated = (Map<?, ?>) call("PUT", "/api/user/profile", touristToken,
                Map.of("name", "Updated Tourist", "mobileNumber", "+919876543210", "address", "Test address")).body();
        assertEquals("Updated Tourist", updated.get("name"));

        Map<String, Object> location = Map.of("latitude", 0.5, "longitude", 0.5);
        assertEquals(HttpStatus.OK, call("PUT", "/api/location/sharing", touristToken, location).status());
        assertEquals(true, ((Map<?, ?>) call("GET", "/api/location/sharing", touristToken, null).body()).get("sharingEnabled"));
        assertEquals(HttpStatus.FORBIDDEN, call("GET", "/api/location/admin/shared", touristToken, null).status());
        assertEquals(HttpStatus.OK, call("GET", "/api/location/admin/shared", adminToken, null).status());

        Map<String, Object> incident = Map.of("incidentType", "SAFETY", "description", "Integration test incident", "priority", "LOW");
        assertEquals(HttpStatus.OK, call("POST", "/api/incidents", touristToken, incident).status());
        assertEquals(HttpStatus.OK, call("GET", "/api/incidents/mine", touristToken, null).status());
        assertEquals(HttpStatus.FORBIDDEN, call("GET", "/api/incidents/admin", touristToken, null).status());
        assertEquals(HttpStatus.OK, call("GET", "/api/incidents/admin", adminToken, null).status());
        assertEquals(HttpStatus.UNAUTHORIZED, call("POST", "/api/incidents/sos", null, incident).status());
        assertEquals(HttpStatus.FORBIDDEN, call("POST", "/api/incidents/sos", adminToken, incident).status());
        Map<String, Object> locatedSosRequest = new java.util.LinkedHashMap<>(incident);
        locatedSosRequest.put("latitude", 12.9716);
        locatedSosRequest.put("longitude", 77.5946);
        ApiResponse sosResponse = call("POST", "/api/incidents/sos", touristToken, locatedSosRequest);
        assertEquals(HttpStatus.OK, sosResponse.status());
        Map<?, ?> sos = (Map<?, ?>) sosResponse.body();
        assertEquals(profile.get("id"), sos.get("touristId"));
        assertEquals("SOS", sos.get("incidentType"));
        assertEquals("CRITICAL", sos.get("priority"));
        assertEquals("OPEN", sos.get("status"));
        assertEquals(12.9716, ((Number) sos.get("latitude")).doubleValue());
        assertEquals(77.5946, ((Number) sos.get("longitude")).doubleValue());
        assertNotNull(sos.get("createdAt"));
        Map<?, ?> noGpsSos = (Map<?, ?>) call("POST", "/api/incidents/sos", touristToken, incident).body();
        assertNull(noGpsSos.get("latitude"));
        assertNull(noGpsSos.get("longitude"));
        List<?> savedIncidents = (List<?>) call("GET", "/api/incidents/mine", touristToken, null).body();
        assertTrue(savedIncidents.stream().anyMatch(row -> "SOS".equals(((Map<?, ?>) row).get("incidentType"))));
        Map<?, ?> updatedSos = (Map<?, ?>) call("PATCH", "/api/incidents/admin/" + sos.get("id"), adminToken,
                Map.of("status", "IN_PROGRESS", "assignedTo", "Safety team")).body();
        assertEquals("IN_PROGRESS", updatedSos.get("status"));

        Map<?, ?> issued = (Map<?, ?>) call("POST", "/api/tourist-id", touristToken, null).body();
        String touristId = (String) issued.get("touristId");
        assertEquals("http://192.168.1.5:5173/verify/" + touristId, issued.get("verificationUrl"));
        Map<?, ?> publicVerification = (Map<?, ?>) call("GET", "/api/verify/tourist/" + touristId, null, null).body();
        assertEquals(Boolean.TRUE, publicVerification.get("valid"));
        assertEquals("VALID", publicVerification.get("status"));
        assertEquals(touristId, publicVerification.get("touristId"));
        assertEquals(java.util.Set.of("touristId", "status", "valid"), publicVerification.keySet());
        var cors = corsConfigurationSource.getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/verify/tourist/" + touristId));
        assertEquals(List.of("http://localhost:5173", "http://127.0.0.1:5173", "http://192.168.1.5:5173"), cors.getAllowedOrigins());
        assertEquals(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"), cors.getAllowedMethods());
        assertTrue(cors.getAllowedHeaders().containsAll(List.of("Authorization", "Content-Type")));
        assertFalse(cors.getAllowCredentials());
        assertNull(cors.checkOrigin("http://evil.example"));
        assertCorsPreflight("http://localhost:5173");
        assertCorsPreflight("http://127.0.0.1:5173");
        List<?> ids = (List<?>) call("GET", "/api/admin/tourist-ids", adminToken, null).body();
        Long recordId = ((Number) ((Map<?, ?>) ids.get(0)).get("id")).longValue();
        assertEquals(HttpStatus.OK, call("DELETE", "/api/admin/tourist-ids/" + recordId, adminToken, null).status());
        Map<?, ?> revokedVerification = (Map<?, ?>) call("GET", "/api/verify/tourist/" + touristId, null, null).body();
        assertEquals("REVOKED", revokedVerification.get("status"));
        assertEquals(Boolean.FALSE, revokedVerification.get("valid"));
        assertEquals(java.util.Set.of("touristId", "status", "valid"), revokedVerification.keySet());
        Map<?, ?> reissued = (Map<?, ?>) call("POST", "/api/tourist-id", touristToken, null).body();
        assertNotEquals(touristId, reissued.get("touristId"));

        String feature = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}}";
        Map<String, Object> zone = Map.of("name", "Test zone", "description", "Test", "zoneType", "CAUTION", "boundaryGeoJson", feature, "source", "Integration test", "verified", true, "active", true);
        Map<?, ?> createdZone = (Map<?, ?>) call("POST", "/api/safety-zones", adminToken, zone).body();
        assertEquals(HttpStatus.FORBIDDEN, call("GET", "/api/safety-zones", touristToken, null).status());
        assertEquals(HttpStatus.OK, call("GET", "/api/safety-zones/active", touristToken, null).status());
        Map<?, ?> zoneCheck = (Map<?, ?>) call("GET", "/api/safety-zones/check?latitude=0.5&longitude=0.5", touristToken, null).body();
        assertEquals("ZONE_MATCH", zoneCheck.get("status"));
        Long zoneId = ((Number) createdZone.get("id")).longValue();
        assertEquals(HttpStatus.OK, call("PATCH", "/api/safety-zones/" + zoneId + "/status", adminToken, Map.of("active", false)).status());

        assertEquals(HttpStatus.OK, call("DELETE", "/api/location/sharing", touristToken, null).status());
        assertEquals(false, ((Map<?, ?>) call("GET", "/api/location/sharing", touristToken, null).body()).get("sharingEnabled"));
        ApiResponse assistant = call("POST", "/api/assistant", touristToken, Map.of("message", "General travel tips"));
        assertEquals(HttpStatus.OK, assistant.status());
        assertEquals(true, ((Map<?, ?>) assistant.body()).get("available"));
        assertEquals("local", ((Map<?, ?>) assistant.body()).get("source"));

        ApiResponse badCredentials = call("POST", "/api/auth/login", null,
                Map.of("email", "tourist@yatrasecure.test", "password", "incorrect-password"));
        assertEquals(HttpStatus.UNAUTHORIZED, badCredentials.status());
        assertEquals("Invalid email or password", ((Map<?, ?>) badCredentials.body()).get("message"));
    }

    private String login(String email, String password) throws Exception {
        ApiResponse response = call("POST", "/api/auth/login", null, Map.of("email", email, "password", password));
        assertEquals(HttpStatus.OK, response.status());
        return (String) ((Map<?, ?>) response.body()).get("token");
    }

    private ApiResponse call(String method, String path, String token, Object payload) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Accept", "application/json");
        if (token != null) request.header("Authorization", "Bearer " + token);
        HttpRequest.BodyPublisher body = payload == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload));
        if (payload != null) request.header("Content-Type", "application/json");
        HttpResponse<String> response = client.send(request.method(method, body).build(), HttpResponse.BodyHandlers.ofString());
        Object parsed = response.body().isBlank() ? null : json.readValue(response.body(), Object.class);
        return new ApiResponse(HttpStatus.valueOf(response.statusCode()), parsed);
    }

    private void assertCorsPreflight(String origin) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Origin", origin)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type,authorization")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(origin, response.headers().firstValue("Access-Control-Allow-Origin").orElse(null));
        assertTrue(response.headers().firstValue("Access-Control-Allow-Methods").orElse("").contains("POST"));
    }

    private record ApiResponse(HttpStatus status, Object body) {}
}
