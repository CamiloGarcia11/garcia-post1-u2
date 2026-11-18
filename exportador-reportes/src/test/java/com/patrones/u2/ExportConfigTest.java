package com.patrones.u2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Builder de ExportConfig")
class ExportConfigTest {

    @Test
    @DisplayName("Debe construir ExportConfig con valores por defecto validos")
    void testDefaultValues() {
        ExportConfig config = new ExportConfig.Builder("pdf").build();

        assertEquals("pdf", config.getFormat());
        assertNull(config.getOutputPath());
        assertEquals("A4", config.getPageSize());
        assertEquals("PORTRAIT", config.getOrientation());
        assertEquals("es-CO", config.getLocale());
        assertNull(config.getWatermarkText());
        assertTrue(config.isIncludeLogo());
        assertFalse(config.isCompress());
        assertEquals(40, config.getMaxRowsPerPage());
    }

    @Test
    @DisplayName("Debe asignar correctamente todos los parametros opcionales personalizados")
    void testCustomValues() {
        ExportConfig config = new ExportConfig.Builder("excel")
                .outputPath("/tmp/reportes/actas.xlsx")
                .pageSize("LETTER")
                .orientation("LANDSCAPE")
                .locale("en-US")
                .watermarkText("CONFIDENCIAL")
                .includeLogo(false)
                .compress(true)
                .maxRowsPerPage(20)
                .build();

        assertEquals("excel", config.getFormat());
        assertEquals("/tmp/reportes/actas.xlsx", config.getOutputPath());
        assertEquals("LETTER", config.getPageSize());
        assertEquals("LANDSCAPE", config.getOrientation());
        assertEquals("en-US", config.getLocale());
        assertEquals("CONFIDENCIAL", config.getWatermarkText());
        assertFalse(config.isIncludeLogo());
        assertTrue(config.isCompress());
        assertEquals(20, config.getMaxRowsPerPage());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el formato es nulo o vacio")
    void testValidationNullOrBlankFormat() {
        assertThrows(IllegalArgumentException.class, () -> new ExportConfig.Builder(null));
        assertThrows(IllegalArgumentException.class, () -> new ExportConfig.Builder("  "));
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException si compress=true pero outputPath es null")
    void testValidationCompressWithoutOutputPath() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            new ExportConfig.Builder("pdf").compress(true).build();
        });

        assertTrue(ex.getMessage().contains("compress=true requiere especificar outputPath"));
    }

    @Test
    @DisplayName("Debe permitir compress=true si se proporciona outputPath valido")
    void testValidCompressWithOutputPath() {
        assertDoesNotThrow(() -> {
            ExportConfig config = new ExportConfig.Builder("pdf")
                    .outputPath("reporte.zip")
                    .compress(true)
                    .build();
            assertTrue(config.isCompress());
            assertEquals("reporte.zip", config.getOutputPath());
        });
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException si maxRowsPerPage es menor o igual a 0")
    void testValidationInvalidMaxRowsPerPage() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            new ExportConfig.Builder("pdf").maxRowsPerPage(0).build();
        });

        assertTrue(ex.getMessage().contains("maxRowsPerPage debe ser mayor que 0"));
    }
}
