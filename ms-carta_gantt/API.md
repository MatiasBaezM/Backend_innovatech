# ms-carta_gantt — Documentación de API REST y pruebas

Microservicio que arma la **carta Gantt** de un proyecto. No tiene base de datos
propia: consume proyecto y tareas desde `ms-gestion_proyectos` vía HTTP,
propagando el JWT del usuario, y devuelve la estructura que el frontend usa para
dibujar el eje de tiempo (calendario) y las barras de tareas.

- **Puerto:** `8085`
- **Contexto raíz:** `/api/gantt`
- **Autenticación:** JWT (Bearer) obligatorio en `Authorization`
- **Acceso público a través del API Gateway:** `Path=/api/gantt/**` → `:8085`

---

## 1. Endpoints expuestos

### 1.1 `GET /api/gantt/proyectos/{id}`

Devuelve la carta Gantt completa del proyecto: nombre, estado, **rango de
entrega (fecha de inicio y término definidas por el administrador)** y la lista
de tareas con fecha de inicio/fin para posicionar las barras.

El calendario del frontend usa `fechaInicioProyecto` y `fechaFinProyecto` como
extremos del eje X; el mes se dibuja sobre los días. Como la respuesta se calcula
en vivo, **cualquier cambio de fechas del proyecto se refleja en el siguiente
request** sin necesidad de regenerar nada.

#### Parámetros

| Ubicación | Nombre | Tipo | Descripción |
|-----------|--------|------|-------------|
| Path | `id` | `Long` | ID del proyecto |
| Header | `Authorization` | `String` | `Bearer <jwt>` (obligatorio) |

#### Petición de ejemplo

```bash
curl -X GET http://localhost:8080/api/gantt/proyectos/4 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

> A través del API Gateway: `http://localhost:8080/api/gantt/proyectos/{id}`.
> Directo al microservicio: `http://localhost:8085/api/gantt/proyectos/{id}`.

#### Respuesta `200 OK` — `GanttProyectoDTO`

```json
{
  "proyectoId": 4,
  "nombreProyecto": "Proyecto Gantt Demo",
  "estado": "EN_PROGRESO",
  "fechaInicioProyecto": "2026-07-05",
  "fechaFinProyecto": "2026-08-20",
  "tareas": [
    {
      "id": 1,
      "titulo": "Diseño UI",
      "estado": "EN_PROGRESO",
      "prioridad": "ALTA",
      "asignadoNombre": "Ana López",
      "fechaInicio": "2026-07-01",
      "fechaFin": "2026-07-10",
      "horasEstimadas": 16,
      "orden": 1
    }
  ]
}
```

#### Códigos de estado

| Código | Cuándo |
|--------|--------|
| `200 OK` | Siempre que el JWT sea válido. Si el proyecto no responde o no existe, se devuelve una carta *degradada* (nombre `"Proyecto {id}"`, estado `EN_PROGRESO`, fechas `null`, `tareas: []`) en lugar de un error — el servicio es tolerante a fallos del backend. |
| `401 Unauthorized` | Falta el header `Authorization`, o el JWT es inválido/expiró. |

#### Reglas de negocio aplicadas por el servicio

- **Fechas del proyecto:** `fechaInicioProyecto`/`fechaFinProyecto` provienen de
  `fechaInicio`/`fechaFin` del proyecto (las fija el administrador al crear o
  editar). Si el proyecto no está disponible, quedan en `null`.
- **Filtrado de tareas:** solo se incluyen tareas con **ambas** fechas presentes
  (`fechaCreacion` y `fechaLimite`); las demás se descartan.
- **Orden:** las tareas se ordenan por `fechaInicio` ascendente y se numeran
  correlativamente en el campo `orden` (1, 2, 3, …).
- **Mapeo de fechas de tarea:** `fechaInicio` = `fechaCreacion` y
  `fechaFin` = `fechaLimite` del modelo de `ms-gestion_proyectos`.
- **Asignado:** si la tarea no tiene `asignadoNombre`, se devuelve `"Sin asignar"`.

---

## 2. Endpoints consumidos (internos)

El servicio llama a `ms-gestion_proyectos` (`GESTION_PROYECTOS_SERVICE_URL`,
por defecto `http://localhost:8083`), propagando el mismo JWT entrante:

| Método | Ruta | Uso |
|--------|------|-----|
| `GET` | `/api/proyectos/{id}` | Nombre, estado y fechas (`fechaInicio`, `fechaFin`) del proyecto |
| `GET` | `/api/proyectos/{id}/tareas` | Lista de tareas con fechas, prioridad y colaborador asignado |

Si cualquiera de las dos llamadas lanza `RestClientException`, el servicio la
absorbe: el proyecto se trata como `null` y las tareas como lista vacía.

---

## 3. Modelos (DTOs)

### `GanttProyectoDTO` (respuesta principal)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `proyectoId` | `Long` | ID del proyecto |
| `nombreProyecto` | `String` | Nombre del proyecto |
| `estado` | `String` | Estado del proyecto |
| `fechaInicioProyecto` | `String` (ISO `YYYY-MM-DD`) | Día 0 del eje X; `null` si no está definido |
| `fechaFinProyecto` | `String` (ISO `YYYY-MM-DD`) | Último día del eje X; `null` si no está definido |
| `tareas` | `List<GanttTareaDTO>` | Tareas a dibujar |

### `GanttTareaDTO` (cada barra)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | `Long` | ID de la tarea |
| `titulo` | `String` | Título de la tarea |
| `estado` | `String` | `POR_HACER` \| `EN_PROGRESO` \| `COMPLETADO` \| `REVISADO` |
| `prioridad` | `String` | `ALTA` \| `MEDIA` \| `BAJA` |
| `asignadoNombre` | `String` | Colaborador asignado (`"Sin asignar"` si no hay) |
| `fechaInicio` | `String` (ISO) | Inicio de la barra (= `fechaCreacion`) |
| `fechaFin` | `String` (ISO) | Fin de la barra (= `fechaLimite`) |
| `horasEstimadas` | `Integer` | Horas estimadas (puede ser `null`) |
| `orden` | `Integer` | Posición correlativa por `fechaInicio` |

### DTOs de entrada (mapeo desde `ms-gestion_proyectos`)

- **`ProyectoExternoDTO`**: `id`, `nombre`, `estado`, `fechaInicio`, `fechaFin`.
- **`TareaExternaDTO`**: `id`, `proyectoId`, `titulo`, `estado`, `prioridad`,
  `asignadoNombre`, `fechaCreacion`, `fechaLimite`, `horasEstimadas`.

Ambos usan `@JsonIgnoreProperties(ignoreUnknown = true)`: campos extra del
origen se ignoran sin romper la deserialización.

---

## 4. Pruebas del servicio

Archivo: `src/test/java/Innovatech/ms_carta_gantt/GanttServiceTest.java`
Estilo: unit test con **JUnit 5 + Mockito** (`@ExtendWith(MockitoExtension.class)`),
mockeando `BackendClient` e inyectándolo en `GanttService` (`@InjectMocks`).
Aserciones con **AssertJ**.

| # | Test | Qué verifica |
|---|------|--------------|
| 1 | `getGanttProyecto_cuandoBackendFalla_devuelveListaVacia` | Ante fallo de ambas llamadas HTTP, la carta se devuelve degradada: `tareas` vacía y `nombreProyecto = "Proyecto 1"`. Tolerancia a fallos. |
| 2 | `getGanttProyecto_filtraTareasSinFechas` | Solo entra la tarea con `fechaCreacion` y `fechaLimite`; la tarea sin fechas se descarta (`hasSize(1)`). |
| 3 | `getGanttProyecto_propagaFechasDeEntregaDelProyecto` **(nuevo)** | Las fechas del proyecto (`2026-07-05` / `2026-08-20`) se reflejan en `fechaInicioProyecto` / `fechaFinProyecto`. Sustenta el rango del calendario definido por el administrador. |
| 4 | `getGanttProyecto_sinProyecto_dejaFechasEnNull` **(nuevo)** | Si el proyecto no está disponible, `fechaInicioProyecto` y `fechaFinProyecto` quedan en `null` (sin NPE). |
| 5 | `tareasConFechas_tienenOrdenCorrelativo` | El campo `orden` de las tareas es creciente según `fechaInicio`. |

### Cómo ejecutar

> **JDK 26:** el Byte Buddy que trae Mockito soporta oficialmente hasta Java 23.
> En una JVM 24+ hay que habilitar el modo experimental, de lo contrario los
> mocks de clases concretas fallan con `MockitoException`.

```bash
# Solo pruebas
./mvnw test -Dnet.bytebuddy.experimental=true

# Pruebas + reporte de cobertura JaCoCo (target/site/jacoco/index.html)
./mvnw test jacoco:report -Dnet.bytebuddy.experimental=true
```

### Resultado y cobertura (JaCoCo)

`Tests run: 5, Failures: 0, Errors: 0` · **BUILD SUCCESS**

| Clase | Instr. | Branch | Línea |
|-------|--------|--------|-------|
| `GanttService` | 97.0% | 75.0% | 100% |
| `GanttProyectoDTO` / `GanttTareaDTO` | 100% | 100% | 100% |

> El resto de clases del módulo (`GanttController`, `BackendClient`, seguridad
> JWT, clase `main`) no tienen pruebas unitarias — se cubren por el smoke test
> end-to-end vía API Gateway.

---

## 5. Configuración relevante

`src/main/resources/application.properties`:

```properties
server.port=8085
spring.application.name=ms-carta_gantt
jwt.secret=${JWT_SECRET:...}
# GESTION_PROYECTOS_SERVICE_URL=http://localhost:8083   # inyectada por entorno
```

Seguridad (`SecurityConfig`): stateless, `/actuator/**` público, **todo `/api/**`
requiere JWT válido**; sin token → `401 Unauthorized`.

Docker Compose (`ms-carta-gantt`): depende de `ms-gestion-proyectos`, expone
`8085`, recibe `GESTION_PROYECTOS_SERVICE_URL` y `JWT_SECRET` por entorno.
