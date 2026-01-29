# Virtual MDC

**Virtual Threads Friendly MDC Implementation for Java 21+**

## Features
- Full compatibility with SLF4J MDC API (drop-in via SPI).
- Automatic propagation in virtual threads (via wrappers).
- Safe clear to prevent leaks.
- Works on JDK 21+ (stable API, no preview).

## Why?
Standard SLF4J MDC uses `ThreadLocal`, which breaks when using Virtual Threads (context is lost when threads are unmounted/remounted).

This library provides a drop-in replacement that works correctly with Virtual Threads by propagating context via decorated Executors.

## 📦 Installation

Add dependency from Maven Central:
```xml
<dependency>
    <groupId>com.github.kzhunmax</groupId>
    <artifactId>virtual-mdc</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🛠 Usage

#### 1. Basic Logging (Nothing changes!)
   Just use standard SLF4J:

```java
MDC.put("requestId", "req-123");
log.info("Hello World"); // Logback will see "req-123"
```

#### 2. Using with Virtual Threads

Instead of `Executors.newVirtualThreadPerTaskExecutor()`, use:

```java
import com.github.kzhunmax.virtualmdc.wrapper.VirtualThreadExecutors;

// Create a propagating executor
ExecutorService executor = VirtualThreadExecutors.propagatingVirtualExecutor();

MDC.put("userId", "user-555");

executor.submit(() -> {
    // Context is automatically propagated!
    log.info("I am inside a virtual thread"); // logs userId=user-555
});
```

#### 3. Web Applications (Spring Boot / Tomcat)

Register the Servlet Filter to handle incoming requests:

```java
@Bean
public FilterRegistrationBean<MdcPropagatingServletFilter> loggingFilter() {
    FilterRegistrationBean<MdcPropagatingServletFilter> bean = new FilterRegistrationBean<>();
    bean.setFilter(new MdcPropagatingServletFilter());
    bean.addUrlPatterns("/*");
    return bean;
}
```

