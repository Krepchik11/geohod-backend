package me.geohod.geohodbackend.service.link;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.geohod.geohodbackend.configuration.properties.GeohodProperties;

class BinaryLinkGeneratorTest {

    private BinaryLinkGenerator binaryLinkGenerator;
    private GeohodProperties properties;
    private GeohodProperties.LinkTemplates linkTemplates;
    private UUID testEventId;

    @BeforeEach
    void setUp() {
        properties = mock(GeohodProperties.class);
        linkTemplates = mock(GeohodProperties.LinkTemplates.class);
        when(properties.linkTemplates()).thenReturn(linkTemplates);
        when(linkTemplates.startappLink()).thenReturn("https://t.me/testbot?startapp=");
        
        binaryLinkGenerator = new BinaryLinkGenerator(properties);
        testEventId = UUID.randomUUID();
    }

    @Test
    void testParseLink() {
        String link = binaryLinkGenerator.generateLink(LinkAction.REGISTER_FOR_EVENT, testEventId);
        String encodedPart = link.substring(30);
        
        BinaryLinkGenerator.LinkData parsed = binaryLinkGenerator.parseLink(encodedPart);
        
        assertEquals(LinkAction.REGISTER_FOR_EVENT, parsed.action());
        assertEquals(testEventId, parsed.eventId());
    }

    @Test
    void testParseInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> {
            binaryLinkGenerator.parseLink("invalid");
        });
    }

    @Test
    void testParseInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            binaryLinkGenerator.parseLink("AAAAAAAAAAAAAAAAAAAAAAA"); // 23 chars
        });
    }

    @Test
    void testLinkActionFromCode() {
        assertEquals(LinkAction.OPEN_EVENT, LinkAction.fromCode((byte) 0x01));
        assertEquals(LinkAction.REGISTER_FOR_EVENT, LinkAction.fromCode((byte) 0x02));
        assertEquals(LinkAction.REVIEW_EVENT, LinkAction.fromCode((byte) 0x03));
        
        assertThrows(IllegalArgumentException.class, () -> {
            LinkAction.fromCode((byte) 0x99);
        });
    }
}