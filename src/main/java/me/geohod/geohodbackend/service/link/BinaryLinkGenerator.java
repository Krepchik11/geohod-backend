package me.geohod.geohodbackend.service.link;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;

@Service
@RequiredArgsConstructor
public class BinaryLinkGenerator {
    
    private final GeohodProperties properties;
    
    public String generateLink(LinkAction action, UUID eventId) {
        byte[] binaryData = createBinaryData(action, eventId);
        String encoded = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(binaryData);
        return properties.linkTemplates().startappLink() + encoded;
    }
    
    private byte[] createBinaryData(LinkAction action, UUID eventId) {
        ByteBuffer buffer = ByteBuffer.allocate(17);
        
        buffer.put(action.getCode());
        
        buffer.putLong(eventId.getMostSignificantBits());
        buffer.putLong(eventId.getLeastSignificantBits());
        
        return buffer.array();
    }
    
    public LinkData parseLink(String encodedData) {
        try {
            byte[] binaryData = java.util.Base64.getUrlDecoder().decode(encodedData);
            
            if (binaryData.length != 17) {
                throw new IllegalArgumentException("Invalid binary data length: " + binaryData.length);
            }
            
            ByteBuffer buffer = ByteBuffer.wrap(binaryData);
            
            byte actionCode = buffer.get();
            LinkAction action = LinkAction.fromCode(actionCode);
            
            long msb = buffer.getLong();
            long lsb = buffer.getLong();
            UUID eventId = new UUID(msb, lsb);
            
            return new LinkData(action, eventId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse binary link: " + e.getMessage(), e);
        }
    }
    
    public static record LinkData(LinkAction action, UUID eventId) {}
}