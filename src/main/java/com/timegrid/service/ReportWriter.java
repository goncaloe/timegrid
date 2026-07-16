package com.timegrid.service;

import com.timegrid.enums.ExportFormat;

public interface ReportWriter {

    ExportFormat format();

    byte[] write(ReportTable table);

}