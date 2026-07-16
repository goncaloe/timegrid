package com.timegrid.dto;

import com.timegrid.enums.ExportFormat;

public record ExportFile(String filename, ExportFormat format, byte[] content) {
}