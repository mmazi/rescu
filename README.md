ResCU - a lightweight Rest client utility for Java (JAX-RS)
========================================================

ResCU enables the user to create a proxy Rest client in run-time directly from a JAX-RS annotated interface.
ResCU is mostly focused on json-based services.

Several other libraries do this (eg. Jersey and RESTEasy); the benefit of ResCU is that it is very lightweight with
minimal dependencies. This makes it useful for quickly creating REST clients in Android apps etc.


Dependencies
---------------

- jackson (json parser)
- slf4j (logging interface)
- jsr-311 (JAX-RS) (a set of REST-service specific annotations)
- jsr-305 (a set of annotations)


Benefits
---------------

- lightweight, minimal dependencies.
- JAX-RS-annotated server-side interfaces may be reused to create clients.
- Support for basic HTTP authentication and some common request signing paradigms.


Limitations
---------------

- This is meant mostly for json-based REST services, ie. the response body is always interpreted as json. No XML.
- JAX-RS: basic support is provided for @GET, @POST, @PUT, @DELETE, @HEAD, @OPTIONS, @Path, @QueryParam, @FormParam,
   @HeaderParam, @PathParam. No support for @MatrixParam.


Logging
---------------

ResCU uses slf4j for logging. For best results, a supported logging implementation (eg. log4j, JUL, logback, ...)
should be provided in runtime. See slf4j's documentation for more info.

Usage
---------------

# Create a JAX-RS-annotated interface (or get it from the REST service developer), eg. `ExampleService`.
# Call `ExampleService service = RestProxyFactory.createProxy(ExampleService.class, "http://www.example.com/")`.
# That's it! Just use the `service` object you just got.

For several working examples, see [XChange](https://github.com/timmolter/XChange), eg. [BitcoinCentralPollingTradeService.java](https://github.com/timmolter/XChange/blob/develop/xchange-bitcoincentral/src/main/java/com/xeiam/xchange/bitcoincentral/service/trade/polling/BitcoinCentralPollingTradeService.java).


Important note:
---------------

I am very open to new suggestions, change requests etc. If ResCU seems almost right for you, but not quite,
do write me a note.
