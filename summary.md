# Migration Summary: Java 21 + Spring 6.2.9 + Jakarta EE 10

## Status: BUILD SUCCESS
- **81 tests run, 0 failures, 0 errors, 2 skipped**
- Date: 2026-09-10

---

## Problem

After the OpenRewrite Java 21 upgrade cycle, the application compiled cleanly but all Spring integration tests failed at runtime with:

```
java.lang.reflect.InaccessibleObjectException: Unable to make protected final
java.lang.Class java.lang.ClassLoader.defineClass(...) accessible: module java.base
does not "opens java.lang" to unnamed module
```

Root cause: Spring 5.0.4.RELEASE (and even Spring 5.3.x) bundles an old CGLIB that uses illegal reflective access to `ClassLoader.defineClass()`, which Java 9+ strong encapsulation blocks at runtime — even though the code compiles cleanly. Spring 6.x is the first line whose bundled ASM/CGLIB works on Java 21 bytecode without illegal access.

---

## Changes Made

### pom.xml

| Dependency | Before | After |
|---|---|---|
| Spring Framework | 5.0.4.RELEASE | **6.2.9** |
| Spring Security | 5.0.0.RELEASE | **6.4.3** |
| javax.servlet-api | javax.servlet:3.1.0 | **jakarta.servlet:6.0.0** |
| javax.servlet.jsp-api | javax.servlet.jsp:2.3.2 | **jakarta.servlet.jsp:3.1.1** |
| jstl | javax.servlet:jstl:1.2 | **jakarta.servlet.jsp.jstl-api:3.0.0** + impl |
| jakarta.xml.bind-api | 2.3.3 | **4.0.2** |
| jaxb-runtime | org.glassfish.jaxb:2.3.9 | **org.glassfish.jaxb:4.0.5** |
| jakarta.validation-api | javax.validation:2.0.1.Final | **jakarta.validation:3.0.2** |
| hibernate-validator | 6.0.7.Final | **8.0.2.Final** |
| EL impl | javax.el 3.0.x | **org.glassfish.expressly:5.0.0** |
| jackson-databind | 2.9.3 | **2.17.2** |
| log4j2 | LATEST (unstable) | **2.24.3** (pinned) |
| commons-fileupload | 1.3.3 | **removed** (CommonsMultipartResolver gone in Spring 6) |
| jakarta.inject-api | 1.0.3 | **2.0.1** |
| rome | 1.9.0 | **2.1.0** |
| hamcrest | hamcrest-library:1.3 | **hamcrest:2.2** (consolidated artifact) |
| junit | 4.12 | **4.13.2** |
| maven-surefire-plugin | 2.20.1 (no Java 21 support) | **3.2.5** |
| maven-war-plugin | (no version) | **3.4.0** |
| compiler `-parameters` flag | absent | added (required by Spring 6) |

### Source Code (javax → jakarta namespace migration)

| File | Change |
|---|---|
| MvcShowcaseAppInitializer.java | `javax.servlet.Filter` → `jakarta.servlet.Filter` |
| StandardArgumentsController.java | `javax.servlet.http.*` → `jakarta.servlet.http.*` |
| ClasslevelMappingController.java | `javax.servlet.http.HttpServletRequest` → jakarta |
| MappingController.java | `javax.servlet.http.HttpServletRequest` → jakarta |
| CustomArgumentController.java | `javax.servlet.http.HttpServletRequest` → jakarta |
| validation/JavaBean.java | `javax.validation.constraints.*` → `jakarta.validation.constraints.*` |
| form/FormBean.java | `javax.validation.*` + `org.hibernate.validator.constraints.NotEmpty` → `jakarta.validation.*` |
| FormController.java, ValidationController.java, ViewsController.java, MessageConvertersController.java | `javax.validation.Valid` → `jakarta.validation.Valid` |
| views/JavaBean.java, messageconverters/JavaBean.java | `javax.validation.constraints.*` → jakarta |
| messageconverters/JavaBean.java, mapping/JavaBean.java | `javax.xml.bind.annotation.XmlRootElement` → `jakarta.xml.bind.annotation.XmlRootElement` |
| RedirectController.java | `javax.inject.Inject` → `jakarta.inject.Inject` + Joda-Time `LocalDate` → `java.time.LocalDate` |

### Removed API replacements (Spring 6 breaking changes)

- **`CallableProcessingInterceptorAdapter`** (removed in Spring 6): `TimeoutCallableProcessingInterceptor` now directly `implements CallableProcessingInterceptor`
- **`CommonsMultipartResolver`** (removed in Spring 6): replaced with `StandardServletMultipartResolver` in `WebMvcConfig`

### Test updates

| Test | Change |
|---|---|
| FileUploadControllerTests.java | `fileUpload()` → `multipart()` (API renamed in Spring 5.x) |
| DataControllerTests.java | Updated `HttpHeaders.toString()` expected format (Spring 6 changed from `{K=[V]}` to `[K:"V"]`) |
| StandardArgumentsControllerTests.java | Updated `startsWith` for response object — Spring 6 wraps in `LifecycleHttpServletResponse` |
| MappingControllerTests.java | `@Ignore` on `byProducesJsonExtension` and `byProducesXmlExtension` — path extension content negotiation removed in Spring 6 |

---

## Next Steps

- The 2 skipped tests (`byProducesJsonExtension`, `byProducesXmlExtension`) test path extension content negotiation which was intentionally removed in Spring 6 due to security concerns. These can be deleted if not needed, or the JSP views can be tested via `Accept` header instead.
- The Jetty Maven plugin version (9.4.8.v20171121) is very old and does not support the Jakarta EE 10 servlet spec. For local development, replace with an embedded Tomcat 10.1+ or update Jetty to 12.x (which supports both Jakarta EE 10 and 11).
- `commons-io:2.15.1` is included but may not be actively used in application code after the `CommonsMultipartResolver` removal — can be removed if confirmed unused.
- Joda-Time dependency was removed from source code (RedirectController migrated to `java.time.LocalDate`). The `joda-time:2.9.9` dependency can be removed from pom.xml if no other code references it.
