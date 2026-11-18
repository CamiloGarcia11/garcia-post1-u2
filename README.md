# Post-contenido — Unidad 2: Patrones Creacionales

**Estudiante:** Camilo García  
**Curso:** Patrones de Diseño de Software — Sexto Semestre  
**Unidad:** Unidad 2 — Patrones de Diseño Creacionales  
**Actividad:** Post-contenido — Exportación de Reportes Académicos: Elección Justificada de Patrones Creacionales  
**Repositorio:** `garcia-post1-u2`  
**Plataforma:** NPLAD UFPS  

---

## Descripción
Repositorio del post-contenido de la Unidad 2 de Patrones de Diseño de Software — Sexto Semestre. Contiene un único proyecto Maven (`exportador-reportes/`) desarrollado de forma incremental que resuelve la exportación de actas de calificaciones en múltiples formatos coherentes (PDF, Excel, HTML) aplicando el patrón **Abstract Factory** con registro dinámico desacoplado (Parte 1), y se extiende mediante una configuración flexible e inmutable aplicando el patrón **Builder** y una evaluación objetiva sobre la necesidad real del patrón **Singleton** (Parte 2).

---

## Cómo ejecutar

Desde la raíz del repositorio clonado, ingresar a la carpeta del proyecto Maven y compilar/ejecutar:

```bash
cd exportador-reportes
mvn compile
mvn exec:java -Dexec.mainClass="com.patrones.u2.Main"
```

Para ejecutar la suite de pruebas unitarias automatizadas:
```bash
mvn test
```

### Salida esperada por consola (`Main.java`)
```text
=== PARTE 1: Exportacion por Formato (Abstract Factory) ===
=== Exportacion PDF ===
[PDF:encabezado] UDES — Acta de Calificaciones (membrete institucional)
[PDF:cuerpo]
 20231001 | Ana Torres | IS-301 | 4.2
 20231002 | Luis Rey | IS-301 | 3.8
 20231003 | Marta Diaz | IS-301 | 4.7
[PDF:pie] Pagina 1 — documento apto para firma digital
=== Exportacion Excel ===
[XLSX:encabezado] Hoja: UDES - Notas (fila congelada)
[XLSX:cuerpo] Estudiante;Curso;Nota
 Ana Torres;IS-301;4.2
 Luis Rey;IS-301;3.8
 Marta Diaz;IS-301;4.7
[XLSX:pie] Total de filas exportadas en hoja 1
=== Exportacion HTML ===
[HTML:encabezado] <header>UDES — Portal de Estudiantes</header>
[HTML:cuerpo] <table>
 <tr><td>Ana Torres</td><td>IS-301</td><td>4.2</td></tr>
 <tr><td>Luis Rey</td><td>IS-301</td><td>3.8</td></tr>
 <tr><td>Marta Diaz</td><td>IS-301</td><td>4.7</td></tr>
</table>[HTML:pie] <footer>Vista 1 — generado dinamicamente</footer>

=== PARTE 2: Configuracion Compleja (Builder) ===
=== Exportacion con configuracion por defecto ===
[config] pageSize=A4 orientation=PORTRAIT locale=es-CO watermark=ninguna compress=false
[PDF:encabezado] UDES — Acta de Calificaciones (membrete institucional)
[PDF:cuerpo]
 20231001 | Ana Torres | IS-301 | 4.2
 20231002 | Luis Rey | IS-301 | 3.8
 20231003 | Marta Diaz | IS-301 | 4.7
[PDF:pie] Pagina 1 — documento apto para firma digital
=== Exportacion con configuracion personalizada ===
[config] pageSize=LETTER orientation=LANDSCAPE locale=en-US watermark=BORRADOR compress=false
[XLSX:encabezado] Hoja: UDES - Notas (fila congelada)
[XLSX:cuerpo] Estudiante;Curso;Nota
 Ana Torres;IS-301;4.2
 Luis Rey;IS-301;3.8
 Marta Diaz;IS-301;4.7
[XLSX:pie] Total de filas exportadas en hoja 1
=== Validacion de estado inconsistente ===
Rechazado correctamente: compress=true requiere especificar outputPath: no se puede comprimir un resultado en memoria
```

---

## Decisiones de diseño

### Decisión 1 — Factory Method vs. Abstract Factory (Parte 1)

* **Patrón elegido:** **Abstract Factory** (`ReportFormatFactory` con implementaciones concretas `PdfReportFactory`, `ExcelReportFactory` y `HtmlReportFactory`).
* **Justificación basada en las tres preguntas diagnósticas:**
  1. *¿El sistema crea un único tipo de producto que varía por formato, o varios productos relacionados que deben mantenerse coherentes entre sí dentro del mismo formato?*  
     **Respuesta y análisis:** El sistema crea **una familia de productos relacionados y dependientes entre sí**: el cuerpo del reporte (`ReportBody`) y el encabezado/pie de página (`ReportHeaderFooter`). No se trata de un artefacto atómico aislado, sino de dos piezas estructurales complementarias que deben coincidir estrictamente dentro del mismo formato de salida para que el documento final sea coherente. No es válido combinar un encabezado PDF con un cuerpo Excel.
  2. *Al agregar el futuro formato CSV, ¿basta con agregar una nueva implementación de un único producto, o hay que agregar una familia completa de piezas relacionadas?*  
     **Respuesta y análisis:** Se debe agregar **una familia completa de piezas relacionadas**: `CsvReportBody` (renderizado de registros delimitados por comas), `CsvHeaderFooter` (metadatos de encabezado y conteo de filas en CSV) y la fábrica correspondiente `CsvReportFactory`. La necesidad futura reafirma la existencia de familias de productos concurrentes.
  3. *¿El riesgo real del problema es "se instancia la clase equivocada" o es "se mezclan piezas de familias distintas y el documento queda inconsistente"?*  
     **Respuesta y análisis:** El riesgo crítico del dominio es **la mezcla accidental de piezas pertenecientes a familias distintas** (por ejemplo, generar un encabezado con membrete institucional PDF para una tabla de Excel o un pie de página HTML para un documento PDF imprimible). Abstract Factory neutraliza este riesgo por construcción a nivel del sistema de tipos: el cliente (`ReportExportService`) solo interactúa con la interfaz abstracta `ReportFormatFactory`, garantizando que tanto el cuerpo como el encabezado/pie provengan invariablemente de la misma fábrica concreta y, por tanto, pertenezcan a la misma familia compatible.
* **Por qué se descartó Factory Method:**  
  *Factory Method* se orienta a delegar la instanciación de un *único producto jerárquico* a subclases o métodos de fábrica individuales (`createReport()`). Si hubiésemos optado por Factory Method, habríamos tenido que forzar la unificación artificial del cuerpo y el encabezado en un solo objeto monolítico (eliminando la separación de responsabilidades y la composición modular), o bien mantener métodos de fábrica desarticulados que no ofrecen ninguna garantía a nivel de tipos para evitar que un cliente combine un encabezado de una tecnología con el cuerpo de otra. Abstract Factory modela con fidelidad la naturaleza del dominio de familias de productos dependientes.

---

### Decisión 2 — Mecanismo de extensibilidad de formatos (Parte 1)

* **Opción elegida:** **Registro dinámico desacoplado** basado en `Map<String, Supplier<ReportFormatFactory>>` dentro de `ReportFactoryRegistry`.
* **Justificación:**  
  Aplica rigurosamente el Principio de Abierto/Cerrado (**OCP** - *Open/Closed Principle*) y el Principio de Inversión de Dependencias (**DIP**). El registro asocia identificadores de formato en minúsculas con constructores diferidos (`Supplier<ReportFormatFactory>` mediante referencias a métodos `::new`). Esto permite que nuevos formatos (como el futuro formato CSV) se incorporen en cualquier momento mediante `ReportFactoryRegistry.register("csv", CsvReportFactory::new)` **sin tocar ni una sola línea de código existente** ni requerir recompilación del despachador central o de los servicios consumidores. La resolución se realiza en tiempo constante $O(1)$ y de forma polimórfica diferida (`factorySupplier.get()`), evitando instanciar fábricas antes de que se requieran.
* **Por qué se descartó switch / if-else encadenados:**  
  Una sentencia `switch(format)` o una cascada de `if/else` introduce un acoplamiento rígido directo con cada una de las clases concretas de fábricas (`PdfReportFactory`, `ExcelReportFactory`, etc.). Cada vez que el equipo de datos agregue un formato nuevo, dicho método debería ser abierto y modificado, violando directamente OCP e incrementando la complejidad ciclomática y el riesgo de regresiones en código previamente probado.

---

### Decisión 3 — Builder vs. constructor telescópico vs. setters (Parte 2)

* **Patrón elegido:** Patrón **Builder** interno estático (`ExportConfig.Builder`).
* **Justificación:**  
  La clase `ExportConfig` posee 1 parámetro obligatorio (`format`) y 8 parámetros opcionales con valores por defecto razonables (`outputPath`, `pageSize`, `orientation`, `locale`, `watermarkText`, `includeLogo`, `compress`, `maxRowsPerPage`).  
  El patrón Builder proporciona una interfaz fluida y legible (`method chaining`), garantiza la **inmutabilidad** del objeto resultante (todos los campos de `ExportConfig` son `private final` sin setters), y centraliza las reglas de negocio e invariantes en un único punto atómico: el método `build()`. En `build()`, se validan consistencias cruzadas de estado antes de permitir la existencia de la instancia (por ejemplo, validando que si `compress=true`, obligatoriamente se debe haber configurado un `outputPath`, ya que no es posible comprimir un resultado en memoria sin ruta de destino; y que `maxRowsPerPage > 0`).
* **Por qué se descartaron las otras opciones:**
  1. *Constructor único con 9 parámetros:*  
     Fuerza al cliente a recordar y suministrar los 9 argumentos en un orden estricto. Dado que varios parámetros comparten el mismo tipo primitivo (`String`, `boolean`, `int`), es sumamente fácil cometer errores de inversión de argumentos (por ejemplo, invertir `locale` y `watermarkText`, o `includeLogo` y `compress`), los cuales son completamente invisibles para el compilador y solo provocan fallos sutiles en tiempo de ejecución. Además, obliga a pasar valores `null` o booleanos arbitrarios para parámetros opcionales que el usuario no desea alterar.
  2. *Constructores sobrecargados (Antipatrón del Constructor Telescópico):*  
     Con 8 parámetros opcionales independientes, el número de combinaciones potenciales posibles crece combinatoriamente ($2^8 = 256$ combinaciones). Mantener docenas de constructores sobrecargados genera código duplicado masivo, proliferación inmanejable de constructores y ambigüedad para el desarrollador que debe elegir cuál invocar.
  3. *Clase mutable con JavaBean Setters sueltos:*  
     Permite que el objeto exista en un **estado inconsistente o a medio configurar** durante su ciclo de vida mientras se invocan los setters uno a uno. No existe un punto de corte único y atómico para validar la coherencia entre parámetros dependientes (como `compress` y `outputPath`). Además, rompe la inmutabilidad: cualquier componente con una referencia a `ExportConfig` podría alterar su configuración en concurrencia o en capas intermedias, provocando efectos colaterales indeseados.

---

### Decisión 4 — ¿ReportFactoryRegistry necesita ser Singleton? (Parte 2)

* **Conclusión explícita:** **NO**, no conviene ni se justifica convertir `ReportFactoryRegistry` en un Singleton clásico.
* **Justificación basada en los 4 criterios objetivos de la Guía Teórica:**
  1. **Identidad de objeto y polimorfismo:**  
     *Criterio:* ¿El registro necesita ser manipulado como una instancia de objeto (ej. implementar una interfaz, inyectarse como dependencia polimórfica o sustituirse por un mock en pruebas unitarias)?  
     *Evaluación:* No. En este sistema, el registro no implementa ninguna interfaz de catálogo ni requiere ser sustituido en tiempo de ejecución. Ningún servicio lo recibe por constructor; su invocación es un punto de resolución estático de infraestructura (`ReportFactoryRegistry.resolve(format)`). Convertirlo en un objeto con `getInstance()` solo obligaría a escribir `ReportFactoryRegistry.getInstance().resolve(...)` sin aportar ninguna ventaja de polimorfismo ni abstracción.
  2. **Inicialización costosa (Eager vs. Lazy):**  
     *Criterio:* ¿La instanciación requiere operaciones pesadas (I/O de disco, sockets, base de datos) que justifiquen inicialización perezosa (*lazy loading*)?  
     *Evaluación:* No. El registro solo inicializa un `Map` en memoria con tres referencias a constructores (`Supplier` / method references). La sobrecarga es insignificante y ocurre una sola vez de forma atómica y segura para hilos durante la carga de clase (*classloading*) en el bloque `static`.
  3. **Garantía de fuente única de verdad:**  
     *Criterio:* ¿Se requiere la semántica de instancia de Singleton para garantizar un registro único compartido en la JVM?  
     *Evaluación:* No. La presencia del campo estático `private static final Map<...> REGISTRY` dentro de una clase final con constructor privado ya garantiza a nivel del ClassLoader que existe exactamente una única fuente de verdad compartida en toda la JVM. Agregar la maquinaria de Singleton (guardas de constructor, `getInstance()`, eventual sincronización o protección contra serialización/reflexión) agregaría complejidad innecesaria sin resolver ningún problema real (sobreingeniería).
  4. **Escenarios futuros razonables (Multi-tenant / Concurrencia):**  
     *Criterio:* ¿Singleton facilitaría o perjudicaría evoluciones realistas del sistema?  
     *Evaluación:* Singleton perjudicaría escenarios futuros como una arquitectura multi-institución (multi-tenant) donde cada universidad o cliente requiriera su propio catálogo aislado de formatos o políticas de exportación. En tal escenario, un Singleton global sería un obstáculo arquitectónico porque fuerza una única instancia global. Si el sistema evolucionara a ese nivel, se preferiría inyección de dependencias (IoC / Spring / CDI) con alcances configurables, no un Singleton GoF clásico.

---

## Estructura del Proyecto

```text
garcia-post1-u2/
├── .gitignore
├── README.md
└── exportador-reportes/
    ├── pom.xml
    └── src/
        ├── main/
        │   └── java/
        │       └── com/
        │           └── patrones/
        │               └── u2/
        │                   ├── GradeRecord.java            # Modelo inmutable de calificaciones
        │                   ├── ReportBody.java             # Producto abstracto 1 (Cuerpo)
        │                   ├── ReportHeaderFooter.java     # Producto abstracto 2 (Encabezado y Pie)
        │                   ├── PdfReportBody.java          # Producto concreto PDF
        │                   ├── PdfHeaderFooter.java        # Producto concreto PDF
        │                   ├── ExcelReportBody.java        # Producto concreto Excel
        │                   ├── ExcelHeaderFooter.java      # Producto concreto Excel
        │                   ├── HtmlReportBody.java         # Producto concreto HTML
        │                   ├── HtmlHeaderFooter.java       # Producto concreto HTML
        │                   ├── ReportFormatFactory.java    # Interfaz Abstract Factory
        │                   ├── PdfReportFactory.java       # Fabrica concreta PDF
        │                   ├── ExcelReportFactory.java     # Fabrica concreta Excel
        │                   ├── HtmlReportFactory.java      # Fabrica concreta HTML
        │                   ├── ReportFactoryRegistry.java  # Registro dinamico extensible (OCP)
        │                   ├── ExportConfig.java           # Configuracion compleja con Builder (Parte 2)
        │                   ├── ReportExportService.java    # Servicio orquestador cliente
        │                   └── Main.java                   # Demostracion ejecutable de ambas partes
        └── test/
            └── java/
                └── com/
                    └── patrones/
                        └── u2/
                            ├── ReportExportServiceTest.java # Pruebas unitarias de exportacion
                            ├── ReportFactoryRegistryTest.java # Pruebas de resolucion y extension OCP
                            └── ExportConfigTest.java        # Pruebas de Builder y validaciones de estado
```

---

## Herramientas utilizadas

* **Java JDK:** 17+ (Compilado y ejecutado con Java 21 LTS)
* **Apache Maven:** 3.8+ (Gestión de ciclo de vida del proyecto y dependencias)
* **JUnit Jupiter:** 5.10.2 (Suite de pruebas unitarias automatizadas)
* **VS Code:** Editor de código y entorno de desarrollo con Extension Pack for Java
* **Git & GitHub:** Control de versiones distribuido y repositorio remoto de entrega

---

## Conclusiones

El desarrollo de este post-contenido evidenció que la selección de un patrón creacional no debe fundamentarse en la intuición ni en la costumbre, sino en un análisis riguroso de las fuerzas y restricciones arquitectónicas del dominio. La elección de **Abstract Factory** sobre Factory Method se justificó en la necesidad intrínseca de garantizar la consistencia entre familias de productos dependientes (cuerpo y encabezado/pie), eliminando por diseño la posibilidad de inconsistencias entre formatos. Por su parte, el patrón **Builder** demostró ser la alternativa óptima para resolver la configuración compleja de `ExportConfig`, garantizando inmutabilidad y validando estados inconsistentes de forma atómica antes de la instanciación. Finalmente, la evaluación crítica de `ReportFactoryRegistry` demostró la importancia de evitar la sobreingeniería: resistir la tentación de convertir componentes estáticos en **Singleton** cuando una clase utilitaria final resuelve el problema de forma más limpia, eficiente y adaptable a futuras arquitecturas desacopladas.
