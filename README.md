[![Build Status](https://travis-ci.org/anigenero/resteasy-proxy-cdi.svg?branch=master)](https://travis-ci.org/anigenero/resteasy-proxy-cdi)

resteasy-proxy-cdi
==================
This library provides a quick, easy way to inject RESTeasy proxies into your project, with minimal setup.

**Note:** this project is still in very early stages of development. Do not use in production.

Java Implementation
-------------------

Create the java interface for the proxy, marked with the `com.anigenero.resteasy.cdi.proxy.ResteasyProxy` annotation.

```java
@RestProxy(name = "myproxy", url = "https://example.com:8080/api/v1")
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
| **url** | `java.lang.String` | *(optional)* the url of the proxy |
| **requestFilter** | `javax.ws.rs.client.ClientRequestFilter` | *(optional)* a request filter |
| **responseFilter** | `javax.ws.rs.client.ClientResponseFilter` | *(optional)* a response filter |
| **credentialsProvider** | `org.apache.http.client.CredentialsProvider` | *(optional)* |

It's then as simple as creating the injection point for the proxy in your class.
```java
public class BarHandler {

    @Inject
    private FooProxy fooProxy;
    
    // ... handler code
    
}
```