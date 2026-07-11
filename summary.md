# Java 17 Migration – Build Fix Summary

## Status
**BUILD SUCCESS** — 81 tests run, 0 failures, 0 errors.

---

## Changes Made

### pom.xml

| Change | Reason |
|--------|--------|
| Added `log4j-version` property (`2.23.1`) | Replaced `LATEST` version specifier (deprecated by Maven, was resolving to unstable `3.0.0-beta3`) |
| Pinned `log4j-core` and `log4j-slf4j-impl` to `${log4j-version}` (2.23.1) | Stable Log4j 2.x; compatible with SLF4J 1.7.x used in the project |
| Added `org.aspectj:aspectjrt:${org.aspectj-version}` dependency | **Root cause of original build failure.** The AspectJ Maven plugin (AJC) requires `aspectjrt.jar` on the *project* classpath but it was only a plugin-level dependency. |
| Upgraded `maven-surefire-plugin` from `2.20.1` to `3.2.5` | Version 2.20.1 uses Apache Commons Lang3 `JavaVersion` enum which does not recognise Java 17, causing a `NullPointerException` at test startup. |
| Added explicit version `3.4.0` for `maven-war-plugin` | Suppresses Maven "missing version" warning; best practice. |

### src/main/java/.../config/WebMvcConfig.java

| Change | Reason |
|--------|--------|
| Added import for `ContentNegotiationConfigurer` | Required for the new override below. |
| Added `setUseSuffixPatternMatch(true)` inside `configurePathMatch` | Spring 5.3 disables suffix pattern matching by default; re-enabled to support `/mapping/produces.json` → `/mapping/produces` routing tested by the showcase. |
| Added `configureContentNegotiation` override with `favorPathExtension(true)` | Spring 5.3 disables path-extension content negotiation by default; re-enabled so `.json`/`.xml` extensions drive the response `Content-Type`. Both settings are deprecated in 5.3 but fully functional and will only be removed in Spring 6.0. |

### src/test/…/data/StandardArgumentsControllerTests.java

| Change | Reason |
|--------|--------|
| Relaxed `response` test assertion from `startsWith("response = org.springframework.mock.web.MockHttpServletResponse@")` to `startsWith("response = ")` | Spring 5.3 wraps the response in `StandardServletAsyncWebRequest$LifecycleHttpServletResponse`; the concrete class is an internal detail. The test intent (verify `HttpServletResponse` injection) is preserved. |

### src/test/…/data/DataControllerTests.java

| Change | Reason |
|--------|--------|
| Updated expected string in `requestBodyAndHeaders` from `{Content-Type=[text/plain], Content-Length=[3]}` to `[Content-Type:"text/plain", Content-Length:"3"]` | `HttpHeaders.toString()` format changed in Spring 5.x from `Map`-style to bracket/quoted style. |

### src/test/…/messageconverters/MessageConvertersControllerTests.java

| Change | Reason |
|--------|--------|
| Changed `content().contentType(MediaType.APPLICATION_FORM_URLENCODED)` to `content().contentTypeCompatibleWith(...)` in `writeForm` | Spring 5.3 now appends `;charset=UTF-8` to form-urlencoded responses. `contentTypeCompatibleWith` performs a structural match (ignoring extra parameters) rather than a strict string equality check. |

---

## Next Steps / Notes

- **Deprecated APIs**: `PathMatchConfigurer.setUseSuffixPatternMatch()` and `ContentNegotiationConfigurer.favorPathExtension()` are deprecated in Spring 5.3 and **will be removed in Spring 6.0**. If the project is later migrated to Spring Boot 3 / Spring 6, these two calls must be removed and the extension-based showcase tests must either be deleted or rewritten to use `Accept` headers.
- **jackson-databind 2.9.3** — very old with known CVEs. Consider upgrading to 2.17.x when convenient.
- **commons-fileupload 1.3.3** — has known CVEs. Consider upgrading to 1.5+ or migrating to `spring-webmvc`'s `StandardServletMultipartResolver`.
- **commons-io 2.5** — relatively old; consider upgrading to 2.16.x.
- **Async TaskExecutor warning** — `CallableControllerTests` logs a warning about `SimpleAsyncTaskExecutor`; this is a runtime configuration note, not a test failure.
