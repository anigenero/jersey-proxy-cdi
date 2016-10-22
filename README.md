[![Build Status](https://travis-ci.org/anigenero/resteasy-proxy-cdi.svg?branch=master)](https://travis-ci.org/anigenero/resteasy-proxy-cdi)

resteasy-proxy-cdi
==================
This library provides a quick, easy way to inject RESTeasy proxies into your project, with minimal setup.

**Note:** this project is still in very early stages of development. Do not use in production.

Java Implementation
-------------------

Create the java interface for the proxy, marked with the `com.anigenero.resteasy.cdi.proxy.ResteasyProxy` annotation.

```java
@ResteasyProxy(name = "myproxy", urlPrefix = "/rs")
public interface FooProxy {

    @Path("foo")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ResponseBean getBar();

}
```

| Property | Type | Description |
| -------- | -------- | -------- |
| **name** | `java.lang.String` | refrences the proxy name you will use in the configuration. You may have multiple instances for the same name (e.g. if you want to implement separate proxy classes for different functionality) |
| **urlPrefix** | `java.lang.String` | *(optional)* the path prefix (e.g. if you declare several proxy classes for the same name, you might need each one to access a different root path) |
| **requestFilter** | `javax.ws.rs.client.ClientRequestFilter` | *(optional)* a request filter |
| **responseFilter** | `javax.ws.rs.client.ClientResponseFilter` | *(optional)* a response filter |

It's then as simple as creating the injection point for the proxy in your class.
```java
public class BarHandler {

    @Proxy
    @Inject
    private FooProxy fooProxy;
    
    // ... handler code
    
}
```

Configuration
-------------
The configuration parameter schema is restproxy.*{ proxy name }*.*{ property }*

```ini
restproxy.myproxy.host=localhost
restproxy.myproxy.url=/FooBar

restproxy.myproxy.port=8080           ## default: 80
restproxy.myproxy.timeout=10000       ## default: 5000

restproxy.myproxy.username=system     ## optional
restproxy.myproxy.password=12345      ## optional
```