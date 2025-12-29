package me.geohod.geohodbackend.service.link;

public enum LinkAction {
    OPEN_EVENT(0x01),
    REGISTER_FOR_EVENT(0x02),
    REVIEW_EVENT(0x03);
    
    private final byte code;
    
    LinkAction(int code) {
        this.code = (byte) code;
    }
    
    public byte getCode() {
        return code;
    }
    
    public static LinkAction fromCode(byte code) {
        for (LinkAction action : values()) {
            if (action.code == code) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown action code: " + code);
    }
}