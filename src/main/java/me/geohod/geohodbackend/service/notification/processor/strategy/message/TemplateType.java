package me.geohod.geohodbackend.service.notification.processor.strategy.message;

public enum TemplateType {
    TELEGRAM,
    IN_APP;

    public boolean supportsRichFormatting() {
        return switch (this) {
            case TELEGRAM -> true;
            case IN_APP -> false;
        };
    }
    
    public int getMaxLength() {
        return switch (this) {
            case TELEGRAM -> 4096;
            case IN_APP -> 1024;
        };
    }
    
    public boolean supportsMarkdown() {
        return switch (this) {
            case TELEGRAM -> true;
            case IN_APP -> false;
        };
    }
    
    public String getMarkupFormat() {
        return switch (this) {
            case TELEGRAM -> "MarkdownV2";
            default -> null;
        };
    }
}