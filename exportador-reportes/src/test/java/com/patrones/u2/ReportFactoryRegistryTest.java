package com.patrones.u2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Registro Dinamico de Fabricas")
class ReportFactoryRegistryTest {

    @Test
    @DisplayName("Debe resolver fabricas predeterminadas (pdf, excel, html) insensible a mayusculas")
    void testResolvePreRegisteredFactories() {
        ReportFormatFactory pdfFactory = ReportFactoryRegistry.resolve("pdf");
        assertNotNull(pdfFactory);
        assertInstanceOf(PdfReportFactory.class, pdfFactory);

        ReportFormatFactory excelFactory = ReportFactoryRegistry.resolve("EXCEL");
        assertNotNull(excelFactory);
        assertInstanceOf(ExcelReportFactory.class, excelFactory);

        ReportFormatFactory htmlFactory = ReportFactoryRegistry.resolve("Html");
        assertNotNull(htmlFactory);
        assertInstanceOf(HtmlReportFactory.class, htmlFactory);
    }

    @Test
    @DisplayName("Debe permitir registrar dinamicamente un nuevo formato (ej. CSV) sin modificar el registro (OCP)")
    void testRegisterNewFormatDynamically() {
        // Fabrica mock para CSV
        ReportFormatFactory mockCsvFactory = new ReportFormatFactory() {
            @Override
            public ReportBody createBody() {
                return records -> "[CSV:cuerpo]";
            }

            @Override
            public ReportHeaderFooter createHeaderFooter() {
                return new ReportHeaderFooter() {
                    @Override
                    public String renderHeader(String institutionName) {
                        return "[CSV:encabezado] " + institutionName;
                    }

                    @Override
                    public String renderFooter(int pageNumber) {
                        return "[CSV:pie] " + pageNumber;
                    }
                };
            }
        };

        ReportFactoryRegistry.register("csv", () -> mockCsvFactory);

        ReportFormatFactory resolved = ReportFactoryRegistry.resolve("csv");
        assertNotNull(resolved);
        assertEquals("[CSV:cuerpo]", resolved.createBody().render(List.of()));
        assertEquals("[CSV:encabezado] UFPS", resolved.createHeaderFooter().renderHeader("UFPS"));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException con mensaje descriptivo si el formato no esta registrado")
    void testResolveUnknownFormatThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ReportFactoryRegistry.resolve("formato_inexistente");
        });

        assertTrue(ex.getMessage().contains("Formato de reporte no registrado: formato_inexistente"));
        assertTrue(ex.getMessage().contains("Formatos disponibles:"));
    }
}
