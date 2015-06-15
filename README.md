ResCU - a lightweight Rest client utility for Java (JAX-RS)
========================================================

ResCU enables the user to create a proxy Rest client in run-time directly from a JAX-RS annotated interface.
ResCU is mostly focused on json-based services, and uses Jackson for json-to-object mapping.

Several other libraries do this (eg. Jersey and RESTEasy); the benefit of ResCU is that it is very lightweight with
minimal dependencies. This makes it useful for quickly creating REST clients in Android apps etc.


Dependencies
---------------

- jackson (json parser)
- slf4j (logging interface)
- jsr-311 (JAX-RS) (a set of REST-service specific annotations)
- jsr-305 (a set of annotations)


Features and benefits
---------------

- Lightweight, minimal dependencies.
- JAX-RS-annotated server-side interfaces may be reused to create clients; basic support 
 is provided for @GET, @POST, @PUT, @DELETE, @HEAD, @OPTIONS, @Path, @QueryParam, @FormParam, @HeaderParam, @PathParam,
 @Consumes and @Produces (`application/json` and `text/plain` only).
- Support for basic HTTP authentication and some common request signing paradigms. See the [Basic HTTP Authentication wiki](https://github.com/mmazi/rescu/wiki/Basic-HTTP-Authentication).
- Support for custom exceptions on API methods: on an exceptional HTTP response, rescu can deserialize the response body as an exception. See [Exception handling wiki](https://github.com/mmazi/rescu/wiki/Exception-handling).
- Suppoft for custom interceptors.


Limitations
---------------

- Rescu is meant mostly for json-based REST services. The response body is always interpreted as json or plain text. No XML, and no plans to add it.
- JAX-RS: No support yet for the following annotations: @MatrixParam, @CookieParam, @ApplicationPath, @HttpMethod, @Encoded, @DefaultValue.


Logging
---------------

ResCU uses slf4j for logging. For best results, a supported logging implementation (eg. log4j, JUL, logback, ...)
should be provided in runtime, though this is not required. See slf4j's documentation for more info.

See [logback.xml](/src/test/resources/logback.xml) in test sources for example configuration.

Set the logging level for rescu to `debug` or `trace` in `logback.xml` for debugging:

    <logger name="si.mazi.rescu" level="trace"/>

Usage
---------------

#### Maven

Rescu is hosted in Maven Central so all you need to do is add this dependency to your pom:

    <dependency>
        <groupId>com.github.mmazi</groupId>
        <artifactId>rescu</artifactId>
        <version>1.6.0</version>
    </dependency>

#### Usage in code

1. Create a JAX-RS-annotated interface (or get it from the REST service developer), eg. `ExampleService.java`.
2. Call `ExampleService service = RestProxyFactory.createProxy(ExampleService.class, "http://www.example.com/")`.
3. That's it! Just use the `service` object you just got.

#### Examples

See the [tests](/src/test) for some examples. [ExampleService](/src/test/java/si/mazi/rescu/ExampleService.java)
is an example of a JAX-RS-annotated interface.

For more working examples, see [XChange](https://github.com/timmolter/XChange), eg. [BitstampTradeServiceRaw.java](https://github.com/timmolter/XChange/blob/develop/xchange-bitstamp/src/main/java/com/xeiam/xchange/bitstamp/service/polling/BitstampTradeServiceRaw.java).

#### Settings

Rescu can be configured by adding a `rescu.properties` file in your classpath.

Supported settings with example values (copy this into your `rescu.properties` file):

    rescu.http.readTimeoutMillis = 5000             # Read timeout in milliseconds when performing HTTP requests. The default is 30000 (30 seconds).
    rescu.http.readProxyHost = www.example.com      # HTTP proxy host. Both host and port must be set in order to use a proxy.
    rescu.http.readProxyPort = 80                   # HTTP proxy port. Both host and port must be set in order to use a proxy.
    rescu.http.ignoreErrorCodes = true              # If set to true, the HTTP response body never be parsed as Exception but always as the method response type. Defaults to false.

License
---------------

Rescu is released under the MIT License. Please see [LINCESE.txt](LICENSE.txt)FIXED  for the full text.

An important note
---------------

I am very open to new suggestions, change requests etc. If ResCU seems almost right for you, but not quite,
do write me a note, eg. by [opening an issue](https://github.com/mmazi/rescu/issues/new). Documentation and
clarification requests are welcome too!
