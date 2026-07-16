package com.timegrid.enums;

import com.timegrid.exception.BusinessRuleException;

public enum ExportFormat {

    CSV("csv", "text/csv;charset=UTF-8");

    private final String extension;
    private final String contentType;

    ExportFormat(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String extension() {
        return extension;
    }

    public String contentType() {
        return contentType;
    }

    public static ExportFormat parse(String value) {
        for (ExportFormat format : values()) {
            if (format.extension.equalsIgnoreCase(value)) {
                return format;
            }
        }
        throw new BusinessRuleException("export.invalid-format", value);
    }

}