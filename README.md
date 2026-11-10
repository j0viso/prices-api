# Prices

Servicio Spring Boot que responde a una pregunta: **¿qué precio tiene un producto de una cadena en una fecha y hora concretas?** Los datos salen de la tabla `PRICES` del enunciado. Si varias tarifas cubren el mismo instante, gana la de mayor prioridad.

Java 21 · Spring Boot 3.5 · Spring Data JPA · H2 en memoria · OpenAPI Generator · Swagger UI · JaCoCo · Karate 2 · Docker Compose

## 1. API

### 1.1 Petición y respuesta

La API tiene un único endpoint:

```
GET /prices?brandId=1&productId=35455&applicationDate=2020-06-14T16:00:00
```

Recibe tres parámetros, los tres obligatorios:

| Parámetro | Qué es | Formato |
|---|---|---|
| `brandId` | la cadena (1 = ZARA) | entero positivo |
| `productId` | el producto | entero positivo |
| `applicationDate` | el momento para el que se pregunta el precio | fecha y hora ISO-8601, por ejemplo `2020-06-14T16:00:00` |

Y responde de tres maneras:

| Código | Cuándo | Cuerpo |
|---|---|---|
| 200 | hay una tarifa de ese producto y esa cadena que cubre la fecha | `brandId`, `productId`, `priceList`, `startDate`, `endDate`, `price`, `currency` |
| 404 | ninguna tarifa cubre la fecha, o el producto o la cadena no existen | un *problem detail* con título `Price not found` y, en `detail`, el producto, la cadena y la fecha consultados |
| 400 | falta un parámetro, viene mal formado o no es positivo | un *problem detail* (RFC 9457) en `application/problem+json`: `type`, `title`, `status`, `detail`, `instance` |

Tres ejemplos con el servicio arrancado:

```
$ curl "http://localhost:8080/prices?brandId=1&productId=35455&applicationDate=2020-06-14T16:00:00"
{"brandId":1,"productId":35455,"priceList":2,"startDate":"2020-06-14T15:00:00","endDate":"2020-06-14T18:30:00","price":25.45,"currency":"EUR"}

$ curl "http://localhost:8080/prices?brandId=1&productId=35455&applicationDate=2020-06-13T10:00:00"
{"type":"about:blank","title":"Price not found","status":404,"detail":"no price for product 35455 of brand 1 at 2020-06-13T10:00:00","instance":"/prices"}

$ curl "http://localhost:8080/prices?brandId=0&productId=35455&applicationDate=2020-06-14T16:00:00"
{"type":"about:blank","title":"Invalid query","status":400,"detail":"brandId must be positive","instance":"/prices"}
```

### 1.2 Contrato y Swagger UI

El contrato de la API es el fichero [`openapi.yaml`](openapi.yaml) de la raíz, y se escribió antes que el código (API first). En cada build, el plugin `openapi-generator-maven-plugin` lee ese fichero y genera la interfaz `PricingApi` (la ruta, los parámetros y sus tipos) y la clase `PriceResponse` (el cuerpo de la respuesta). El controlador implementa esa interfaz, así que si alguien cambia el contrato y el código no lo sigue, el proyecto no compila. Es la manera más barata de que la documentación y la implementación no se separen nunca.

El servicio no genera documentación en tiempo de ejecución: sirve ese mismo fichero en `/openapi.yaml`, y Swagger UI lo lee de ahí.

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Contrato servido:** http://localhost:8080/openapi.yaml

## 2. Ejecución

### 2.1 Arrancar el servicio

Hacen falta JDK 21 y Maven 3.6.3 o superior; o solo Docker, si se prefiere no instalar nada de Java.

| Cómo | Comando |
|---|---|
| Con Maven | `mvn spring-boot:run` |
| Con el jar | `mvn package` y después `java -jar target/prices-0.0.1-SNAPSHOT.jar` |
| Con Docker | `docker compose up --build` |

El servicio arranca en http://localhost:8080. La base de datos H2 vive en memoria: se crea y se carga sola al arrancar, con `schema.sql` y `data.sql` (las cuatro filas del enunciado). La tabla `prices` conserva las columnas del enunciado con sus nombres (`brand_id`, `start_date`, `end_date`, `price_list`, `product_id`, `priority`, `price`, `curr`) y añade un `id` técnico como clave primaria, algo que el enunciado permite. No hay que instalar ni configurar nada más.

### 2.2 URLs útiles

| Qué | URL |
|---|---|
| El endpoint, `GET /prices` | http://localhost:8080/prices?brandId=1&productId=35455&applicationDate=2020-06-14T16:00:00 |
| Swagger UI, para probar el endpoint desde el navegador | http://localhost:8080/swagger-ui/index.html |
| El contrato OpenAPI que Swagger UI lee | http://localhost:8080/openapi.yaml |
| Salud, lo que consulta el healthcheck de Docker | http://localhost:8080/actuator/health |

Con Docker Compose el puerto es el mismo, 8080. El stack de la suite end-to-end no publica ningún puerto: Karate le habla por la red interna de Compose (ver [3.4](#34-ejecutar-los-end-to-end)).

## 3. Tests

### 3.1 Qué tests hay

Hay tres niveles, del más rápido y pequeño al más lento y completo:

| Suite | Qué comprueba | Dónde está |
|---|---|---|
| Unitarios (34 tests) | la regla de negocio, el caso de uso y la validación de la query. No arrancan Spring: cada clase se construye con `new`, y el puerto de salida es un mock de Mockito | `src/test/java`, junto a la clase que prueban |
| Integración (16 tests) | el endpoint real, `GET /prices`, sobre el contexto completo de Spring, el adaptador JPA y los datos del enunciado en H2: las cinco peticiones del enunciado, los 404 y todos los 400, también los que genera Spring cuando un parámetro falta o viene mal. Y el adaptador JPA contra `schema.sql` | `src/test/java`: `GetApplicablePriceIT` y `JpaPriceRepositoryIT` |
| End-to-end (10 escenarios) | el servicio como caja negra: la imagen Docker arrancada y Karate pidiendo por HTTP. Las cinco peticiones, tres casos sin precio y dos consultas rechazadas | `e2e/src/test/java/prices/pricing/get-applicable-price.feature` |

### 3.2 Las cinco peticiones del enunciado

Las cinco peticiones envían siempre `brandId=1` (ZARA) y `productId=35455`; lo único que cambia es `applicationDate`. Las tres últimas columnas son la respuesta que se espera: qué `priceList` aplica (la tarifa, en palabras del enunciado), entre qué fechas y a qué precio.

| # | `applicationDate` (petición) | `priceList` (respuesta) | `startDate` → `endDate` (respuesta) | `price` (respuesta) |
|---|---|---|---|---|
| 1 | 2020-06-14T10:00:00 | 1 | 2020-06-14T00:00:00 → 2020-12-31T23:59:59 | 35.50 EUR |
| 2 | 2020-06-14T16:00:00 | 2 | 2020-06-14T15:00:00 → 2020-06-14T18:30:00 | 25.45 EUR |
| 3 | 2020-06-14T21:00:00 | 1 | 2020-06-14T00:00:00 → 2020-12-31T23:59:59 | 35.50 EUR |
| 4 | 2020-06-15T10:00:00 | 3 | 2020-06-15T00:00:00 → 2020-06-15T11:00:00 | 30.50 EUR |
| 5 | 2020-06-16T21:00:00 | 4 | 2020-06-15T16:00:00 → 2020-12-31T23:59:59 | 38.95 EUR |

Están probadas dos veces, por motivos distintos. En `GetApplicablePriceIT` se ejecutan dentro de `mvn verify`, con MockMvc sobre el contexto de Spring y H2: así cualquiera puede verlas pasar sin tener Docker. Y en el primer `Scenario Outline` de la feature de Karate se ejecutan contra la imagen Docker, que es lo más parecido a producción.

### 3.3 Ejecutar los tests unitarios y de integración

Los lanza Maven. Con JDK 21 y Maven instalados, valen los comandos de la columna "Con Maven". Sin instalar nada de Java, los de la columna "Con Docker": ejecutan el mismo Maven dentro de un contenedor, el servicio `maven` de `compose.yaml`, que monta el proyecto y guarda las dependencias descargadas en un volumen, así solo la primera vez tarda. En ningún caso hace falta tener el servicio arrancado: H2 va en memoria.

| Qué | Con Maven | Con Docker |
|---|---|---|
| Unitarios | `mvn test` | `docker compose run --rm maven mvn test` |
| Unitarios e integración, con la comprobación de code coverage: lo mismo que ejecuta el build | `mvn verify` | `docker compose run --rm maven mvn verify` |
| Los funcionales del endpoint, `GetApplicablePriceIT` (las cinco peticiones, los 404 y los 400); Maven ejecuta antes los unitarios, siempre | `mvn verify -Dit.test=GetApplicablePriceIT` | `docker compose run --rm maven mvn verify -Dit.test=GetApplicablePriceIT` |
| Una clase unitaria concreta | `mvn test -Dtest=MoneyTest` | `docker compose run --rm maven mvn test -Dtest=MoneyTest` |

La imagen Docker del servicio no ejecuta los tests al construirse (`-DskipTests` en el `Dockerfile`), y es a propósito. La imagen es lo que se despliega; los tests pertenecen al build que va antes, `mvn verify`. Si fueran dentro del `Dockerfile`, cada `docker compose up --build` tardaría uno o dos minutos más para repetir lo que Maven ya comprobó. La prueba que sí le corresponde a la imagen es la suite end-to-end.

### 3.4 Ejecutar los end-to-end

Esta suite es un proyecto Maven aparte (`e2e/`) que arranca el servicio con Docker y le pide por HTTP, como lo haría un cliente real. Solo hace falta Docker: Karate corre en su propio contenedor, el servicio `e2e` de `compose.e2e.yaml`, en la misma red que el servicio. Un comando lo hace todo, desde la raíz del repositorio: construye la imagen, arranca el servicio, espera a que su healthcheck responda, ejecuta los 10 escenarios y termina con el resultado de Karate:

```
docker compose -f compose.yaml -f compose.e2e.yaml run --build --rm e2e
```

El informe queda en `e2e/target/karate-reports/karate-summary.html`. Compose deja el contenedor del servicio arrancado; este otro comando lo para y lo borra:

```
docker compose -f compose.yaml -f compose.e2e.yaml down
```

El stack de pruebas tiene su propio proyecto Compose (`prices-e2e`) y no publica ningún puerto, así que no interfiere con un `docker compose up` de desarrollo en el 8080. Si hiciera falta apuntar la suite a otra dirección desde una máquina con JDK y Maven: `mvn -f e2e/pom.xml test -DbaseUrl=http://host:puerto`.

### 3.5 Code coverage con JaCoCo

[JaCoCo](https://www.jacoco.org) mide el code coverage: qué líneas y qué ramas del código ejecutan los tests. No hay que lanzarlo aparte: va dentro de `mvn verify`.

| Comando | Qué pasa |
|---|---|
| `mvn verify` | mide los tests unitarios, genera el informe y falla el build si alguna clase del ámbito baja del 100 % de líneas o de ramas |
| `mvn test jacoco:report` | solo el informe, sin tests de integración ni comprobación del mínimo |

El informe está en `target/site/jacoco/index.html`, para abrirlo en el navegador. Muestra todas las clases escritas a mano, también las que quedan fuera de la comprobación. No muestra dos cosas: el código generado desde `openapi.yaml` (`PricingApi`, `PriceResponse`), excluido en el `pom.xml` para que no ensucie el informe, y los getters y el constructor que Lombok genera en `PriceEntity`, porque Lombok los marca como generados y JaCoCo ignora lo marcado como auto generado.

Qué exige: 100 % de líneas y ramas, por clase, contando solo tests unitarios, en las clases que se prueban sin Spring: `Price`, `Money`, `ApplicablePrice`, `GetApplicablePriceUseCase`, `GetApplicablePriceQuery` y `PriceView` (un record sin lógica, que cubren los tests del caso de uso). Quedan fuera de la comprobación, por patrón en el `pom.xml`: las tres excepciones (`InvalidPriceException`, `InvalidQueryException`, `PriceNotFoundException`), que cubren los tests unitarios del código que las lanza; y la infraestructura y el código generado (`PricesApplication`, `PricingConfig`, controlador, manejador de errores, adaptador JPA, entidad, mapper, repositorio de Spring Data, `PricingApi` y `PriceResponse`), que cubren los tests de integración, los cuales no cuentan para JaCoCo.

## 4. Diseño

### 4.1 Arquitectura hexagonal

Hay una sola feature, `pricing`, organizada en tres anillos: el dominio en el centro (la regla de negocio), la aplicación alrededor (el caso de uso y sus puertos) y los adaptadores por fuera (HTTP de entrada, JPA de salida). Las dependencias apuntan siempre hacia dentro: los adaptadores conocen la aplicación, la aplicación conoce el dominio, y el dominio no conoce a nadie.

```
com.gfttest.prices
├── PricesApplication                          arranque de Spring Boot
└── pricing
    ├── domain
    │   ├── model/Price                        una tarifa: cadena, producto, lista, prioridad, [startDate, endDate], importe; appliesAt(fecha)
    │   ├── model/Money                        importe con la escala de su moneda ISO 4217 (35.5 EUR → 35.50)
    │   ├── service/ApplicablePrice            select(fecha, precios): la de mayor prioridad entre las que cubren la fecha
    │   └── exception/InvalidPriceException
    ├── application
    │   ├── port/in/GetApplicablePrice         find(query) → PriceView; lanza PriceNotFoundException si ninguna tarifa aplica
    │   ├── port/out/PriceRepository           findByBrandAndProduct(brandId, productId) → List<Price>
    │   ├── usecase/GetApplicablePriceUseCase  pide las tarifas al repositorio, aplica la regla, devuelve la vista
    │   ├── query/GetApplicablePriceQuery      valida: ids positivos y una fecha
    │   ├── model/PriceView                    lo que responde la API, en tipos planos
    │   ├── exception/InvalidQueryException
    │   └── exception/PriceNotFoundException
    ├── adapter
    │   ├── in/web/api/PricingApi              generada desde openapi.yaml: GET /prices, parámetros, tipos
    │   ├── in/web/dto/PriceResponse           generada desde openapi.yaml: el cuerpo de la respuesta
    │   ├── in/web/controller/PriceController  implementa PricingApi; llama al puerto y construye la respuesta
    │   ├── in/web/error/PricingExceptionHandler   InvalidQueryException → 400, PriceNotFoundException → 404; ambos ProblemDetail
    │   └── out/persistence                    JpaPriceRepository, entity/PriceEntity, mapper/PriceEntityMapper, repository/PriceEntityRepository
    └── config/PricingConfig                   crea el bean del caso de uso
```

### 4.2 Decisiones

- **Spring está en los adaptadores, no en el núcleo.** Ni `domain` ni `application` importan nada de Spring, de Jakarta Persistence ni de Hibernate: solo `java.*` y el propio proyecto. ¿Por qué? Porque la regla de negocio y el caso de uso son la parte del servicio que más vale y la que menos debería cambiar. Si no dependen de ningún framework, se leen sin saber Spring, se prueban con `new` en milisegundos y una subida de versión o un cambio de framework no les toca. El precio es pequeño: el caso de uso se construye a mano en `PricingConfig`, y Spring le pasa el adaptador JPA porque este implementa el puerto de salida. Todo lo de Spring queda en `adapter/in/web`, `adapter/out/persistence`, `config` y `PricesApplication`.
- **El contrato manda sobre el código.** `PricingApi` y `PriceResponse` se generan en `target/generated-sources/openapi` en cada build y no se versionan. Para cambiar la API se edita `openapi.yaml`, y el compilador señala lo que el controlador tiene que seguir. Así la documentación nunca va por detrás del código.
- **La librería que genera es OpenAPI Generator.** `openapi-generator-maven-plugin` es el proyecto comunitario [OpenAPI Generator](https://openapi-generator.tech) (`org.openapitools`, bifurcado de Swagger Codegen en 2018). No es de Spring: Spring no ofrece ningún generador, y springdoc, también de terceros, hace el camino contrario (del código al documento), que es justo lo que no queremos. Se usa su generador `spring` en modo `interfaceOnly` (solo la interfaz y los modelos, sin controladores ni ficheros de apoyo), con `useTags` para que la interfaz se llame como el tag (`PricingApi`), sin Bean Validation ni `jackson-databind-nullable` para no arrastrar dependencias, con el esquema `ProblemDetail` mapeado a la clase de Spring y `date-time` mapeado a `LocalDateTime`.
- **Docker Compose aunque la aplicación sea un solo contenedor.** H2 vive dentro de la JVM, así que no hay contenedor de base de datos y el `Dockerfile` bastaría por sí solo (`docker build -t prices . && docker run -p 8080:8080 prices`). Compose se mantiene porque hace tres cosas que el `Dockerfile` no puede: deja el arranque en un solo comando, `docker compose up --build`; aloja el servicio `maven`, que permite lanzar los tests sin instalar Java (3.3); y, con el overlay `compose.e2e.yaml`, monta el stack de pruebas con su propio proyecto, sin puerto publicado y con el contenedor de Karate esperando al healthcheck del servicio antes de empezar (3.4).
- **No encontrar precio es un error, y se explica.** Si ninguna tarifa del producto y la cadena cubre la fecha, el caso de uso lanza `PriceNotFoundException` con el producto, la cadena y la fecha consultados, y el manejador la convierte en un 404 `application/problem+json` con título `Price not found`. Nunca se inventa un precio por defecto ni se devuelve un cuerpo vacío: quien llama sabe qué pidió y por qué no hay respuesta. La misma pieza traduce `InvalidQueryException` (ids no positivos) a un 400 con título `Invalid query`, y Spring produce sus propios problem details para parámetros ausentes o mal formados (`spring.mvc.problemdetails.enabled`). Dentro del dominio, en cambio, que ninguna tarifa aplique no es un error: `ApplicablePrice.select` devuelve `Optional` vacío, y es el caso de uso quien decide que eso, para la API, es un 404.
- **Los mappers están escritos a mano.** Hay tres conversiones, una por frontera entre layers: de la entidad JPA al dominio (`PriceEntityMapper`), del dominio a `PriceView` (dentro del caso de uso) y de `PriceView` a la respuesta generada (dentro del controlador). Cada una es un método estático que llama a un constructor con siete argumentos. Con este volumen, siete campos y tres conversiones de ida, no compensa traer nada más; y los records ya dan la red de seguridad que importa: su constructor obliga a pasar todos los campos, así que añadir uno a `Price` o a `PriceView` rompe la compilación hasta que cada mapper lo incluye. En un proyecto más grande, con decenas de campos y muchas clases que convertir, los `@Builder` de Lombok harían esas llamadas mucho más legibles, con cada campo por su nombre en vez de por posición. El precio sería perder esa comprobación: un builder compila igual aunque se olvide un campo, y lo deja a `null`. Aquí la claridad extra no vale esa pérdida.

### 4.3 Dependencias

| Dependencia | Para qué |
|---|---|
| `spring-boot-starter-web` | el endpoint REST y los problem details |
| `spring-boot-starter-data-jpa` | el acceso a la tabla con JPA e Hibernate |
| `h2` | la base de datos en memoria |
| `spring-boot-starter-actuator` | `/actuator/health`, el que consulta el healthcheck de Docker |
| `swagger-ui` y `webjars-locator-lite` | la página de Swagger UI y las rutas sin número de versión a sus ficheros |
| `lombok` | solo en compilación, para `PriceEntity`: `@Getter` y `@NoArgsConstructor` sustituyen a los getters y al constructor escritos a mano. Es la única clase que no puede ser un record, porque JPA exige una clase mutable con constructor sin argumentos. Va como `optional` y `spring-boot-maven-plugin` la excluye del jar |
| `spring-boot-starter-test` | JUnit 5, AssertJ, Mockito y MockMvc |
| `openapi-generator-maven-plugin`, `jacoco-maven-plugin` (build) | generar la API desde el contrato; medir y exigir el code coverage |

## 5. Estructura del repositorio

```
.
├── openapi.yaml                 el contrato de la API: entrada del generador y fichero servido en /openapi.yaml
├── pom.xml                      build del servicio: OpenAPI Generator, Surefire (unitarios), Failsafe (integración), code coverage con JaCoCo
├── Dockerfile                   multi-stage: build con Maven → eclipse-temurin:21-jre
├── compose.yaml                 stack de desarrollo (puerto 8080, healthcheck en /actuator/health) y el servicio maven para lanzar los tests sin JDK
├── compose.e2e.yaml             overlay end-to-end: proyecto prices-e2e, sin puerto publicado, más el servicio e2e que ejecuta Karate
├── src/main/java                el servicio (árbol de arriba)
├── src/main/resources           application.yml, schema.sql, data.sql, static/swagger-ui/index.html
├── src/test/java                tests unitarios y de integración
└── e2e                          suite de Karate: pom.xml, karate-config.js, prices/PricesTest.java y la feature
```
