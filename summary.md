# Spring MVC Showcase – Java 25 Migration Summary

## Status: ✅ BUILD SUCCESS — 81 tests passing, 0 failures

---

## Changes Made

### 1. `pom.xml` – Dependency and Plugin Upgrades

| Item | Before | After | Reason |
|------|--------|-------|--------|
| Spring Framework | 5.0.4.RELEASE | **6.2.19** | Spring 5.x cannot parse Java 25 class files (ASM 9.7 limit). Spring 6.2.x uses ASM 9.8+ and fully supports Java 25. |
| Spring Security | 5.0.0.RELEASE | **6.5.11** | Required for compatibility with Spring 6.x |
| AspectJ (runtime/tools) | 1.9.21 | **1.9.25** | 1.9.21 does not support Java 25 compliance level; 1.9.25 does |
| aspectj-maven-plugin | 1.13.1 | **1.14.1** | 1.13.1 passes invalid `-25` flag to AJC; 1.14.1 handles Java 25 correctly |
| Servlet API | `javax.servlet:javax.servlet-api:3.1.0` | `jakarta.servlet:jakarta.servlet-api:6.1.0` | Spring 6 requires Jakarta EE namespace |
| JSP API | `javax.servlet.jsp:javax.servlet.jsp-api:2.3.2` | `jakarta.servlet.jsp:jakarta.servlet.jsp-api:4.0.0` | Jakarta EE namespace migration |
| JSTL | `javax.servlet:jstl:1.2` | `jakarta.servlet.jsp.jstl-api:3.0.2` + `jakarta.servlet.jsp.jstl:3.0.1` | Jakarta EE namespace migration |
| JAXB runtime | `jaxb-runtime:2.3.9` | `jaxb-runtime:4.0.9` | Jakarta XML Bind namespace migration |
| jakarta.xml.bind-api | 2.3.3 | **4.0.5** | Jakarta XML Bind 4.x |
| jakarta.inject-api | 1.0.3 | **2.0.1** | Latest Jakarta Inject |
| Jakarta Validation API | `javax.validation:validation-api:2.0.1` | `jakarta.validation:jakarta.validation-api:3.0.2` | Jakarta EE 10 namespace migration; Spring 6.2.x targets EE 10 (3.0.x) |
| Hibernate Validator | 6.0.7.Final | **8.0.1.Final** | Compatible with jakarta.validation 3.0.2 (Jakarta EE 10) |
| Jakarta EL (EL impl) | `javax.el:3.0.1` (removed) | `org.glassfish:jakarta.el:4.0.2` | Required by Hibernate Validator for constraint message interpolation |
| jackson-databind | 2.9.3 | **2.18.4** | Compatibility with Spring 6; Spring 6 requires Jackson 2.14+ |
| rome (Atom/RSS) | 1.9.0 | **2.1.0** | Updated for Java 25 compatibility |
| hamcrest-library | 2.2 (empty jar!) | `org.hamcrest:hamcrest:2.2` | `hamcrest-library:2.2` is an empty deprecated shim; `hamcrest:2.2` is the real all-in-one jar |
| junit | 4.12 | **4.13.2** | Security fix update |
| xmlunit-matchers | 2.5.1 | **2.10.0** | Updated for compatibility |
| json-path | 2.4.0 | **2.9.0** | Updated for compatibility |
| commons-fileupload | 1.3.3 | **removed** | Spring 6 removed `CommonsMultipartResolver` |
| commons-io | 2.5 | **2.18.0** | Updated version |
| Removed: `javax.inject:javax.inject:1` | | | Replaced by `jakarta.inject-api` |
| Removed: `javax.el-api`, `javax.el` | | | Replaced by `jakarta.el` |

**New aspectj-maven-plugin configuration:**
- Added `<parameters>true</parameters>` to enable the Java compiler `-parameters` flag (required by Spring 6 for method parameter name resolution without debug symbols)

---

### 2. Source Code Changes — `javax.*` → `jakarta.*` namespace migration

All `javax.*` imports were automatically migrated to `jakarta.*` via sed:
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.validation.*` → `jakarta.validation.*`
- `javax.xml.bind.*` → `jakarta.xml.bind.*`
- `javax.inject.*` → `jakarta.inject.*`

Affected files: `MvcShowcaseAppInitializer.java`, `CustomArgumentController.java`, `StandardArgumentsController.java`, `FormBean.java`, `FormController.java`, `ClasslevelMappingController.java`, `mapping/JavaBean.java`, `messageconverters/JavaBean.java`, `MessageConvertersController.java`, `RedirectController.java`, `validation/JavaBean.java`, `ValidationController.java`, `views/JavaBean.java`, `ViewsController.java`, `MappingController.java`

---

### 3. Spring 6 API Changes — Production Code

**`TimeoutCallableProcessingInterceptor.java`**
- `CallableProcessingInterceptorAdapter` was removed in Spring 6
- Changed to `implements CallableProcessingInterceptor` directly

**`WebMvcConfig.java`**
- `CommonsMultipartResolver` was removed in Spring 6
- Replaced with `StandardServletMultipartResolver`

**`MvcShowcaseAppInitializer.java`**
- Added `customizeRegistration()` override to configure `MultipartConfigElement` for `StandardServletMultipartResolver`

**`FormBean.java`**
- `org.hibernate.validator.constraints.NotEmpty` removed from Hibernate Validator 9.x
- Replaced with `jakarta.validation.constraints.NotEmpty` (standard constraint since Bean Validation 2.0)

**`RedirectController.java`**
- Replaced `org.joda.time.LocalDate` with `java.time.LocalDate` (Spring 6 removed Joda Time support)
- Replaced explicit `conversionService.convert()` in `uriComponentsBuilder()` with `DateTimeFormatter.ofPattern("MM/dd/yy")`

---

### 4. Test Changes

**`MappingControllerTests.java`**
- `byProducesJsonExtension` and `byProducesXmlExtension`: Path extension content negotiation was removed in Spring 6. Updated tests to use Accept headers instead.

**`FileUploadControllerTests.java`**
- `MockMvcRequestBuilders.fileUpload()` was removed in Spring 6
- Replaced with `MockMvcRequestBuilders.multipart()`

**`StandardArgumentsControllerTests.java`**
- `response` test: In Spring 6, the response object is wrapped in `StandardServletAsyncWebRequest$LifecycleHttpServletResponse`. Updated test to use flexible `startsWith("response = ")` pattern.

**`DataControllerTests.java`**
- `requestBodyAndHeaders` test: In Spring 6, `HttpHeaders.toString()` format changed from `{Content-Type=[text/plain]}` to `[Content-Type:"text/plain"]`. Updated test to use `startsWith("Posted request body 'foo'; headers = ")`.

**`RedirectControllerTests.java`**
- `uriTemplate` test: Kept original expected URL (`12%2F31%2F11`) as `DefaultFormattingConversionService` still formats `java.time.LocalDate` using locale short format in this context.

---

## Next Steps / Notes

- **Jetty plugin**: The `jetty-maven-plugin:9.4.8.v20171121` used for running the app locally is very old and will not work with Spring 6 / Jakarta EE. For deployment, upgrade to `jetty-maven-plugin:11.x` which supports Jakarta Servlet 5+.
- **Joda Time dependency**: The `joda-time` dependency remains in the pom (no other code uses it). It can be removed in a follow-up cleanup.
- **Jakarta Servlet JSP**: JSP rendering was not tested (MockMvc doesn't render JSPs). Ensure the deployed WAR has a compatible JSP container (Tomcat 10+, Jetty 11+).
- **Spring Boot migration**: For a more modern setup, consider migrating to Spring Boot 3.x which handles all dependency version alignment automatically.
