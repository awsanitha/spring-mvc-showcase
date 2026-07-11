# Java 17 Upgrade — Build Fix Summary

## Status: ✅ BUILD SUCCESS — 81 tests pass, 0 failures, 0 errors

---

## Changes Made

### pom.xml

| Change | Before | After | Reason |
|--------|--------|-------|--------|
| `java-version` property | `1.8` | `17` | Target Java 17 source/bytecode |
| `org.aspectj-version` property | `1.8.1` | `1.9.21` | AspectJ 1.9.21 supports Java 17+ |
| AspectJ Maven plugin groupId | `org.codehaus.mojo` | `dev.aspectj` | `org.codehaus.mojo:aspectj-maven-plugin:1.11` relies on `tools.jar` which was removed in Java 9. `dev.aspectj:aspectj-maven-plugin:1.13.1` is the supported fork for Java 9+ |
| AspectJ Maven plugin version | `1.11` | `1.13.1` | Same as above |
| Spring Framework version | `5.0.4.RELEASE` | `5.3.39` | Spring 5.0.x uses a CGLIB that requires `ClassLoader.defineClass()` via reflection — blocked by the Java module system since Java 9. Spring 5.3 ships an updated CGLIB using `MethodHandles.Lookup.defineClass()` which is fully compatible with Java 17 |
| Spring Security version | `5.0.0.RELEASE` | `5.8.14` | Updated to be compatible with Spring Framework 5.3.x |
| log4j-core version | `LATEST` | `2.23.1` | Pinned to a stable 2.x release to avoid resolving unstable beta/3.x versions |
| log4j-slf4j-impl version | `LATEST` | `2.23.1` | Same as above |
| maven-surefire-plugin version | `2.20.1` | `3.1.2` | Version 2.20.1 uses `commons-lang3` bundled with no support for Java version strings `"17"` or higher, causing `NullPointerException` at test runner startup |
| maven-war-plugin version | *(missing)* | `3.3.2` | Fixed missing version warning |

### src/main/java/.../config/WebMvcConfig.java

- Added `configureContentNegotiation` override: re-enables `favorPathExtension(true)` (deprecated but still functional in Spring 5.3) to restore URL extension–based content type negotiation (e.g. `/mapping/produces.json` → `Accept: application/json`).
- Updated `configurePathMatch` override: added `setUseSuffixPatternMatch(true)` (deprecated but still functional in Spring 5.3) so that `/mapping/produces.json` continues to match the `/mapping/produces` handler mapping.
- Added `ContentNegotiationConfigurer` import.

### src/test/java/.../data/DataControllerTests.java

- Updated `requestBodyAndHeaders` assertion: `HttpHeaders.toString()` format changed in Spring 5.3 from `{Key=[value]}` to `[Key:"value"]`.

### src/test/java/.../data/StandardArgumentsControllerTests.java

- Updated `response` assertion: Spring 5.3 wraps `MockHttpServletResponse` in `StandardServletAsyncWebRequest$LifecycleHttpServletResponse` for async setup. Changed assertion to check only for `"response = "` prefix (still verifies injection works, without coupling to an implementation class name).

### src/test/java/.../messageconverters/MessageConvertersControllerTests.java

- Updated `writeForm` assertion from `contentType(MediaType.APPLICATION_FORM_URLENCODED)` to `contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)`: Spring 5.3 appends `;charset=UTF-8` to the content type in the response header.

---

## Root Causes Summary

| Issue | Root Cause |
|-------|-----------|
| `aspectj-maven-plugin` failure | Plugin version 1.11 looks for `com.sun:tools:jar` (`tools.jar`) which was removed from JDK 9+ |
| `maven-surefire-plugin` failure | Version 2.20.1 bundled `commons-lang3` does not recognize Java version strings above `"1.8"` |
| Spring `cglib.proxy.Enhancer` failure | Spring 5.0.x CGLIB uses `ClassLoader.defineClass()` via setAccessible, which is denied by Java 9+ module system |
| Test assertion failures | Spring 5.3 behavioral changes: `HttpHeaders.toString()` format, response wrapping, suffix pattern matching default=false, content type charset |

---

## Next Steps

- **Deprecation warnings**: `setUseSuffixPatternMatch` and `favorPathExtension` are deprecated in Spring 5.3. For a full modernization, replace URL-extension content negotiation tests (`byProducesJsonExtension`, `byProducesXmlExtension`) with `Accept`-header–based tests and remove these two deprecated configurations from `WebMvcConfig`.
- **Async TaskExecutor warning**: Tests log a warning about `SimpleAsyncTaskExecutor` not being suitable under load. Consider configuring a proper `ThreadPoolTaskExecutor` in `WebMvcConfig.configureAsyncSupport()`.
- **Spring 6 / Jakarta EE migration**: The next upgrade step would be Spring 6 + Spring Security 6, which requires migrating all `javax.*` imports to `jakarta.*` (the project currently uses `javax.servlet`, `javax.inject`, etc.).
- **JUnit 5 migration**: Tests still use JUnit 4. Migrating to JUnit 5 (`junit-jupiter`) is recommended for long-term support.
- **commons-fileupload**: Version 1.3.3 has known CVEs. Should be updated to 1.5+ or replaced with the Spring 6 `StandardServletMultipartResolver`.
