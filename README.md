# Post-contenido — Unidad 2: Patrones Creacionales

**Estudiante:** Camilo García  
**Curso:** Patrones de Diseño de Software — Sexto Semestre  
**Unidad:** Unidad 2 — Patrones de Diseño Creacionales  
**Actividad:** Post-contenido — Exportación de Reportes Académicos: Elección Justificada de Patrones Creacionales  
**Repositorio:** `garcia-post1-u2`  
**Plataforma:** NPLAD UFPS  

---

## Descripción
Repositorio del post-contenido de la Unidad 2 de Patrones de Diseño de Software — Sexto Semestre. Contiene un único proyecto Maven (`exportador-reportes/`) que resuelve la exportación de reportes y actas de calificaciones académicas en múltiples formatos de salida (Parte 1) y se extiende con configuración compleja e inmutable vía Builder y evaluación justificada de Singleton (Parte 2).

---

## Cómo ejecutar

Desde la raíz del proyecto Maven:
```bash
cd exportador-reportes
mvn compile
mvn exec:java -Dexec.mainClass="com.patrones.u2.Main"
```

---

## Decisiones de diseño

### Decisión 1 — Factory Method vs. Abstract Factory (Parte 1)

* **Patrón elegido:** **Abstract Factory** (`ReportFormatFactory` con implementaciones concretas `PdfReportFactory`, `ExcelReportFactory` y `HtmlReportFactory`).
* **Justificación basada en las preguntas diagnósticas:**
  1. *¿El sistema crea un único tipo de producto que varía por formato, o varios productos relacionados que deben mantenerse coherentes entre sí dentro del mismo formato?*  
     **Respuesta:** El sistema crea **una familia de productos relacionados y dependientes entre sí**: el cuerpo del reporte (`ReportBody`) y el encabezado/pie de página (`ReportHeaderFooter`). No se trata de un único artefacto atómico aislado, sino de dos piezas estructurales complementarias que deben pertenecer estrictamente al mismo formato de salida para que el documento resultante sea válido y coherente.
  2. *Al agregar el futuro formato CSV, ¿basta con agregar una nueva implementación de un único producto, o hay que agregar una familia completa de piezas relacionadas?*  
     **Respuesta:** Es necesario agregar **una familia completa de piezas relacionadas**: `CsvReportBody` (renderizado de registros delimitados por comas), `CsvHeaderFooter` (metadatos de encabezado y conteo de filas en CSV) y la fábrica correspondiente `CsvReportFactory`.
  3. *¿El riesgo real del problema es "se instancia la clase equivocada" o es "se mezclan piezas de familias distintas y el documento queda inconsistente"?*  
     **Respuesta:** El riesgo crítico del dominio es **la mezcla accidental de piezas pertenecientes a familias distintas** (por ejemplo, combinar un encabezado PDF con membrete institucional con una grilla de datos Excel o etiquetas HTML). Abstract Factory neutraliza este riesgo por construcción: el cliente (`ReportExportService`) solo interactúa con la fábrica abstracta resuelta, garantizando que tanto el cuerpo como el encabezado/pie provengan invariablemente de la misma familia compatible.
* **Por qué se descartó Factory Method:**  
  *Factory Method* se enfoca en delegar la instanciación de un *único producto jerárquico* a subclases o métodos de fábrica individuales (`createReport()`). Si hubiésemos optado por Factory Method, habríamos tenido que forzar la unificación artificial del cuerpo y el encabezado en un solo objeto monolítico (eliminando la separación de responsabilidades y la composición modular), o bien mantener métodos de fábrica desarticulados que no ofrecen ninguna garantía a nivel de tipos para evitar que un cliente combine un encabezado de una tecnología con el cuerpo de otra. Abstract Factory modela con precisión la necesidad de producir familias completas y consistentes.

---

### Decisión 2 — Mecanismo de extensibilidad de formatos (Parte 1)

* **Opción elegida:** **Registro dinámico desacoplado** basado en `Map<String, Supplier<ReportFormatFactory>>` dentro de `ReportFactoryRegistry`.
* **Justificación:**  
  Aplica rigurosamente el Principio de Abierto/Cerrado (**OCP** - *Open/Closed Principle*) y el Principio de Inversión de Dependencias (**DIP**). El registro asocia identificadores en minúsculas con constructores diferidos (`Supplier<ReportFormatFactory>` mediante referencias a métodos `::new`). Esto permite que nuevos formatos (como el futuro formato CSV) se incorporen en cualquier momento mediante `ReportFactoryRegistry.register("csv", CsvReportFactory::new)` **sin tocar ni una sola línea de código existente** ni requerir recompilación del despachador central o de los servicios consumidores. La resolución se realiza en tiempo constante $O(1)$.
* **Por qué se descartó switch / if-else encadenados:**  
  Una sentencia `switch(format)` o una cascada de `if/else` introduce un acoplamiento rígido directo con cada una de las clases concretas de fábricas (`PdfReportFactory`, `ExcelReportFactory`, etc.). Cada vez que el equipo de datos agregue un formato nuevo, dicho método debería ser abierto y modificado, violando directamente OCP e incrementando la complejidad ciclomática y el riesgo de regresiones en código previamente probado.
