package com.patrones.u2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Servicio de Exportacion de Reportes")
class ReportExportServiceTest {

    private ReportExportService service;
    private List<GradeRecord> sampleRecords;

    @BeforeEach
    void setUp() {
        service = new ReportExportService();
        sampleRecords = List.of(
                new GradeRecord("20231001", "Ana Torres", "IS-301", 4.2),
                new GradeRecord("20231002", "Luis Rey", "IS-301", 3.8)
        );
    }

    @Test
    @DisplayName("Exportacion PDF debe contener encabezado, cuerpo y pie correspondientes")
    void testExportPdf() {
        String result = service.export("pdf", sampleRecords, "UDES");
        assertNotNull(result);
        assertTrue(result.contains("[PDF:encabezado]"));
        assertTrue(result.contains("[PDF:cuerpo]"));
        assertTrue(result.contains("[PDF:pie]"));
        assertTrue(result.contains("Ana Torres"));
        assertTrue(result.contains("Luis Rey"));
    }

    @Test
    @DisplayName("Exportacion Excel debe contener encabezado, cuerpo y pie XLSX")
    void testExportExcel() {
        String result = service.export("excel", sampleRecords, "UDES");
        assertNotNull(result);
        assertTrue(result.contains("[XLSX:encabezado]"));
        assertTrue(result.contains("[XLSX:cuerpo]"));
        assertTrue(result.contains("[XLSX:pie]"));
        assertTrue(result.contains("Ana Torres;IS-301;4.2"));
    }

    @Test
    @DisplayName("Exportacion HTML debe contener etiquetas y estructura HTML")
    void testExportHtml() {
        String result = service.export("html", sampleRecords, "UDES");
        assertNotNull(result);
        assertTrue(result.contains("[HTML:encabezado]"));
        assertTrue(result.contains("<table>"));
        assertTrue(result.contains("<tr><td>Ana Torres</td><td>IS-301</td><td>4.2</td></tr>"));
        assertTrue(result.contains("</table>"));
        assertTrue(result.contains("[HTML:pie]"));
    }

    @Test
    @DisplayName("Exportacion con ExportConfig debe inyectar metadatos de configuracion")
    void testExportWithConfig() {
        ExportConfig config = new ExportConfig.Builder("pdf")
                .pageSize("A4")
                .orientation("LANDSCAPE")
                .watermarkText("OFICIAL")
                .build();

        String result = service.export(config, sampleRecords, "UDES");
        assertNotNull(result);
        assertTrue(result.contains("[config] pageSize=A4 orientation=LANDSCAPE locale=es-CO watermark=OFICIAL compress=false"));
        assertTrue(result.contains("[PDF:encabezado]"));
        assertTrue(result.contains("[PDF:cuerpo]"));
        assertTrue(result.contains("[PDF:pie]"));
    }
}
