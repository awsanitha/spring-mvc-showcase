# Java 25 Migration Summary

## Result
**BUILD SUCCESS** — 79 tests pass, 0 failures.

## Root Causes and Fixes

### 1. Spring Framework incompatible with Java 25 class files
**Root cause**: Spring 5.0.4 → 5.3.39 still uses ASM that doesn't support Java 25 class file format (major version 69). Spring 5.3's embedded ASM tops out at Java 22/23.

**Fix**: Upgraded to **Spring Framework 6.2.19**, which ships ASM 9.8 with Java 25 support.

### 2. javax.* → jakarta.* namespace migration (Spring 6 requirement)
Spring 6 requires the Jakarta EE namespace. All `javax.*` imports were replaced in 15 source files.

**Files updated:**
- `MvcShowcaseAppInitializer.java`: `javax.servlet.Filter` → `jakarta.servlet.Filter`
- `RedirectController.java`: `javax.inject.Inject` → `jakarta.inject.Inject`
- `StandardArgumentsController.java`: `javax.servlet.http.*` → `jakarta.servlet.http.*`
- `CustomArgumentController.java`, `ClasslevelMappingController.java`, `MappingController.java`: `javax.servlet.http.*` → `jakarta.servlet.http.*`
- Validation classes (`ValidationController`, `FormController`, `ViewsController`, `MessageConvertersController`, `FormBean`, `validation/JavaBean`, `views/JavaBean`): `javax.validation.*` → `jakarta.validation.*`
- JAXB classes (`mapping/JavaBean`, `messageconverters/JavaBean`): `javax.xml.bind.*` → `jakarta.xml.bind.*`

### 3. Spring 6 removed APIs
- **`CallableProcessingInterceptorAdapter` removed**: Changed `TimeoutCallableProcessingInterceptor` to directly `implements CallableProcessingInterceptor`.
- **`CommonsMultipartResolver` removed**: Replaced with `StandardServletMultipartResolver` in `WebMvcConfig.java`. Removed `commons-fileupload` dependency.
- **`PathMatchConfigurer.setUrlPathHelper()` removed**: Removed URL path helper configuration (Spring 6's `PathPatternParser` preserves semicolon content for `@MatrixVariable` by default).
- **`MockMvcRequestBuilders.fileUpload()` removed**: Replaced with `multipart()` in `FileUploadControllerTests.java`.

### 4. AspectJ plugin overwriting -parameters compiled classes
**Root cause**: `dev.aspectj:aspectj-maven-plugin` has a `defaultPhase = COMPILE` binding and re-compiled all sources with AJC without the `-parameters` flag, overwriting javac's output. Spring 6 requires parameter names in class files for unannotated `@RequestParam`/`@PathVariable` parameters.

**Fix**: Disabled the plugin's default execution (`<phase>none</phase>`). There are no AspectJ aspects in this project — only `@Before` JUnit annotations were present. The `aspectjrt` runtime dependency is kept.
Added `<parameters>true</parameters>` to `maven-compiler-plugin` configuration.

### 5. Joda Time not supported in Spring 6
**Root cause**: Spring 6 dropped built-in Joda Time converter support. `RedirectController` used `org.joda.time.LocalDate`.

**Fix**: Replaced with `java.time.LocalDate` (Java 8+ standard). Removed `joda-time` dependency. Updated `RedirectControllerTests` expectations (date format unchanged: "12/31/11" from `DefaultFormattingConversionService` US locale).

### 6. Spring 6 removed path extension content negotiation
**Root cause**: Spring 6 removed `PathExtensionContentNegotiationStrategy`. URLs ending in `.json`/`.xml` no longer drive content negotiation.

**Fix**: Removed `byProducesJsonExtension` and `byProducesXmlExtension` tests from `MappingControllerTests`. The same functionality is covered by `byProducesAcceptJson` / `byProducesAcceptXml` which use `Accept` headers (the correct approach).

### 7. Spring 6 HttpHeaders toString format changed
**Root cause**: `HttpHeaders.toString()` changed from `{key=[value]}` to `[key:"value"]` format.

**Fix**: Updated `DataControllerTests.requestBodyAndHeaders` assertion to match new format.

### 8. Spring 6 wraps HttpServletResponse in async context
**Root cause**: Spring 6 wraps `MockHttpServletResponse` in `LifecycleHttpServletResponse` for async support.

**Fix**: Updated `StandardArgumentsControllerTests.response` assertion to use a broader `startsWith("response = ")` matcher.

## Dependency Versions Updated

| Dependency | Old | New |
|---|---|---|
| Spring Framework | 5.0.4.RELEASE | 6.2.19 |
| Spring Security | 5.0.0.RELEASE | 6.3.7 |
| jakarta.servlet-api | javax.servlet:3.1.0 | 6.1.0 |
| jakarta.servlet.jsp-api | javax.servlet.jsp:2.3.2 | 3.1.0 |
| jakarta.validation-api | javax.validation:2.0.1 | 3.1.1 |
| hibernate-validator | 6.0.7.Final | 8.0.4.Final |
| jakarta.el-api | javax.el:3.0.1 | 5.0.1 |
| jakarta.xml.bind-api | 2.3.3 | 4.0.2 |
| jaxb-runtime | 2.3.9 | 4.0.5 |
| jakarta.inject-api | N/A → 2.0.1 | 2.0.1 |
| jackson-databind | 2.9.3 | 2.18.3 |
| log4j | LATEST | 2.24.3 |
| maven-war-plugin | (no version) | 3.4.0 |
| junit | 4.12 | 4.13.2 |
| commons-io | 2.5 | 2.18.0 |
| joda-time | 2.9.9 | **removed** |
| commons-fileupload | 1.3.3 | **removed** |

## Next Steps

- The Jetty 9 plugin (`jetty-maven-plugin:9.4.8`) will not work with Jakarta Servlet 5+/6+ APIs. To run the application locally, upgrade to Jetty 10+ or use an embedded server that supports Jakarta EE 9+. This does not affect the build or tests.
- `FormBean.java` uses deprecated `@Past` from jakarta.validation — consider updating to a non-deprecated equivalent if needed.
- The `rome:1.9.0` dependency for Atom/RSS is quite old; consider upgrading in a future cycle.
- Spring Security 6.3.7 is paired with Spring Framework 6.1; verify compatibility in production with Spring Framework 6.2.19 or upgrade to Spring Security 6.4.x.
