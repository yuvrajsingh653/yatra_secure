package com.tourist.Yatra_secure.service;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SafetyAssistantFallback {

    public String respond(String question) {
        String query = question == null ? "" : question.toLowerCase(Locale.ROOT).trim();

        if (containsAny(query, "emergency", "danger", "hurt", "injured", "assault", "fire", "medical", "sos", "urgent")) {
            return "If anyone is in immediate danger, move to a safer public place if you can and contact the local emergency services directly. Tell a trusted person where you are. YatraSecure's SOS button records an in-app alert for authorized administrators; it does not call responders. Add location sharing only if you choose to share it.";
        }
        if (containsAny(query, "geofence", "geo-fence", "safety zone", "zone", "restricted area", "caution area")) {
            return "YatraSecure's safety-zone map can show administrator-configured SAFE, CAUTION, or RESTRICTED areas when they are active and verified. A zone match is an advisory, not a live guarantee. Check current official local guidance, and remember location sharing stays off until you opt in.";
        }
        if (containsAny(query, "yatrasecure", "features", "digital id", "tourist id", "qr", "live location", "location sharing", "incident report")) {
            return "YatraSecure provides a tourist profile, a QR-verifiable Digital Tourist ID, opt-in live location sharing for authorized administrators, safety-zone advisories, incident reports, and in-app SOS alerts. The SOS alert does not contact emergency services, and location sharing remains under your control.";
        }
        if (containsAny(query, "passport", "lost document", "stolen", "theft", "wallet", "phone stolen")) {
            return "Move somewhere safe and contact local authorities if a theft occurred. Contact your embassy or consulate for lost travel documents, and notify your bank or mobile provider if cards or a phone are missing. Keep copies of important documents stored separately from the originals.";
        }
        if (containsAny(query, "alone", "solo", "night", "transport", "taxi", "precaution", "safe travel")) {
            return "Before travelling, check current official advisories, share your itinerary with someone you trust, use reputable transport, keep emergency contacts and document copies available offline, and stay aware of your surroundings. Advice can vary by place and conditions.";
        }
        return "For a safer trip, check current official destination guidance, plan reputable transport, keep emergency contacts and document copies available offline, and share your itinerary with someone you trust. If there is immediate danger, contact local emergency services directly. YatraSecure's SOS is an in-app administrator alert only.";
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) if (text.contains(term)) return true;
        return false;
    }
}
