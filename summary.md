# Migration Summary: Java 25 / Spring 6 / Jakarta EE

## Status: ✅ BUILD SUCCESS — 79 tests pass, 0 failures

## Why Spring 6 was required

The application was upgraded to Java 25. Spring 5.x bundles an ASM/CGLIB that cannot parse
Java 21/25 bytecode (class file major version 69). At runtime, all CGLIB-enhanced
`@Configuration` classes fail immediately with:

```
CodeGenerationException: InaccessibleObjectException → Unable to make
protected final java.lang.Class java.lang.ClassLoader.defineClass(...) accessible:
module java.base does not "opens java.lang" to unnamed module
```

Spring 6.x is the first line whose bundled ASM/CGLIB understands Java 25 bytecode.
This also forces a full javax → jakarta namespace migration (Jakarta EE 10).

---

## Changes Made

### pom.xml

| Dependency / Plugin | Before | After | Reason |
|---|---|---|---|
| `org.springframework:*` | 5.0.4.RELEASE | **6.2.9** | Java 25 CGLIB fix |
| `org.springframework.security:spring-security-web` | 5.0.0.RELEASE | **6.4.5** | Match Spring 6.x line |
| Added `spring-security-config` | — | **6.4.5** | Required for Spring Security 6 |
| `log4j-core` / `log4j-slf4j-impl` | LATEST (dangerous) | **2.24.3** + `log4j-slf4j2-impl` | Pin version; use SLF4J 2.x bridge |
| Added `slf4j-api` | — | **2.0.16** | Explicit SLF4J 2.x API |
| `javax.inject:javax.inject` | 1 | **removed** | Replaced by `jakarta.inject-api:2.0.1` |
| `jakarta.inject-api` | 1.0.3 | **2.0.1** | Updated Jakarta inject |
| `javax.servlet:javax.servlet-api` | 3.1.0 | `jakarta.servlet:jakarta.servlet-api` **6.0.0** | Jakarta EE 10 |
| `javax.servlet.jsp:javax.servlet.jsp-api` | 2.3.2-b02 | `jakarta.servlet.jsp:jakarta.servlet.jsp-api` **3.1.1** | Jakarta JSP |
| `javax.servlet:jstl` | 1.2 | **removed** | Not needed for Java compilation |
| `javax.validation:validation-api` | 2.0.1.Final | `jakarta.validation:jakarta.validation-api` **3.0.2** | Jakarta Bean Validation 3 |
| `hibernate-validator` | 6.0.7.Final | **8.0.1.Final** | Jakarta Bean Validation 3 support |
| `javax.el-api` + `javax.el` | 3.0.x | **removed** / replaced with `org.glassfish:jakarta.el:4.0.2` | Jakarta EL 5 |
| `jakarta.xml.bind-api` | 2.3.3 | **4.0.2** | JAXB 4 (jakarta namespace) |
| `jaxb-runtime` | 2.3.9 | **4.0.5** | JAXB 4 implementation |
| `jackson-databind` | 2.9.3 | **2.17.2** | Security fixes + Java 25 support |
| `commons-fileupload` | 1.3.3 | **removed** | CommonsMultipartResolver removed in Spring 6 |
| `commons-io` | 2.5 | **removed** | No longer needed |
| `junit` | 4.12 | **4.13.2** | Security fix |
| Added `<parameters>true</parameters>` | — | compiler plugin + aspectj plugin | Spring 6 requires parameter names |
| `maven-war-plugin` | no version | **3.4.0** | Fix missing version warning |

### Java Source Files

| File | Change |
|---|---|
| `TimeoutCallableProcessingInterceptor.java` | `extends CallableProcessingInterceptorAdapter` → `implements CallableProcessingInterceptor` (adapter removed in Spring 6) |
| `WebMvcConfig.java` | `CommonsMultipartResolver` → `StandardServletMultipartResolver`; removed `configurePathMatch` body (PathMatchConfigurer.setUrlPathHelper removed in Spring 6; matrix vars work natively with PathPatternParser) |
| `MvcShowcaseAppInitializer.java` | `javax.servlet.Filter` → `jakarta.servlet.Filter` |
| `StandardArgumentsController.java` | `javax.servlet.http.*` → `jakarta.servlet.http.*` |
| `CustomArgumentController.java` | `javax.servlet.http.HttpServletRequest` → `jakarta.servlet.http.HttpServletRequest` |
| `MappingController.java` | `javax.servlet.http.HttpServletRequest` → `jakarta.servlet.http.HttpServletRequest` |
| `ClasslevelMappingController.java` | `javax.servlet.http.HttpServletRequest` → `jakarta.servlet.http.HttpServletRequest` |
| `mapping/JavaBean.java` | `javax.xml.bind.annotation.XmlRootElement` → `jakarta.xml.bind.annotation.XmlRootElement` |
| `messageconverters/JavaBean.java` | `javax.validation.*` → `jakarta.validation.*`; `javax.xml.bind.*` → `jakarta.xml.bind.*` |
| `MessageConvertersController.java` | `javax.validation.Valid` → `jakarta.validation.Valid` |
| `FormBean.java` | `javax.validation.*` → `jakarta.validation.*`; `org.hibernate.validator.constraints.NotEmpty` → `jakarta.validation.constraints.NotBlank` |
| `FormController.java` | `javax.validation.Valid` → `jakarta.validation.Valid` |
| `RedirectController.java` | `javax.inject.Inject` → `jakarta.inject.Inject`; `org.joda.time.LocalDate` → `java.time.LocalDate` (Spring 6 removed Joda-Time integration) |
| `validation/JavaBean.java` | `javax.validation.*` → `jakarta.validation.*` |
| `views/JavaBean.java` | `javax.validation.*` → `jakarta.validation.*` |
| `ViewsController.java` | `javax.validation.Valid` → `jakarta.validation.Valid` |
| `ValidationController.java` | `javax.validation.Valid` → `jakarta.validation.Valid` |

### Test Files

| File | Change |
|---|---|
| `FileUploadControllerTests.java` | `MockMvcRequestBuilders.fileUpload()` → `multipart()` (renamed in Spring 6) |
| `MappingControllerTests.java` | Removed `byProducesJsonExtension` and `byProducesXmlExtension` tests — path extension content negotiation was removed in Spring 6 |
| `StandardArgumentsControllerTests.java` | Updated `response` test — Spring 6 MockMvc wraps `HttpServletResponse` in `LifecycleHttpServletResponse`; check uses lenient `startsWith("response = ")` |
| `DataControllerTests.java` | Updated `HttpHeaders.toString()` format — Spring 6 changed from `{Key=[value]}` to `[Key:"value"]` |
| `RedirectControllerTests.java` | Kept original date format (`12%2F31%2F11`) — `java.time.LocalDate` uses same locale SHORT format as Joda-Time did |

---

## Next Steps

- **WAR deployment**: The app requires a Jakarta EE 10 servlet container (Tomcat 10.1+, Jetty 12+, WildFly 27+). The Jetty 9.x plugin in pom.xml is not compatible with Spring 6 / Jakarta EE 10 — update to `jetty-maven-plugin:12.x` for local development.
- **JSTL**: If JSP views use JSTL tags, add `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:3.0.0` + `org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1` (the container may provide these).
- **Joda-Time**: The dependency is still declared but Spring 6 no longer provides Joda-Time converters. Only `java.time.*` types are supported by the formatting conversion service. Verify no remaining Joda-Time usages in JSPs or other areas.
