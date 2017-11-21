package com.anigenero.jersey.proxy;

import org.apache.http.client.CredentialsProvider;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ResourceProxyConfiguration {

    private final Class proxyClass;

    private ClientRequestFilter requestFilter;
    private ClientResponseFilter responseFilter;
    private Class<? extends CredentialsProvider> credentialsProvider;

    private String url;

    private int timeout;

    private <T> ResourceProxyConfiguration(final Class<T> proxyClass) {
        this.proxyClass = proxyClass;
    }

    public ClientRequestFilter getRequestFilter() {
        return requestFilter;
    }

    public ClientResponseFilter getResponseFilter() {
        return responseFilter;
    }

    public Class<? extends CredentialsProvider> getCredentialsProvider() {
        return credentialsProvider;
    }

    public String getUrl() {
        return url;
    }

    public int getTimeout() {
        return timeout;
    }

    public Class getProxyClass() {
        return this.proxyClass;
    }

    public static final class Builder {

        private ResourceProxyConfiguration configuration;

        public <T> Builder(Class<T> clazz) {
            this.configuration = new ResourceProxyConfiguration(clazz);
        }

        public Builder setRequestFilter(ClientRequestFilter requestFilter) {
            this.configuration.requestFilter = requestFilter;
            return this;
        }

        public Builder setResponseFilter(ClientResponseFilter responseFilter) {
            this.configuration.responseFilter = responseFilter;
            return this;
        }

        public Builder setUrl(String url) {
            this.configuration.url = url;
            return this;
        }

        public Builder setTimeout(int timeout) {
            this.configuration.timeout = timeout;
            return this;
        }

        public Builder setCredentialsProvider(Class<? extends CredentialsProvider> credentialsProvider) {
            this.configuration.credentialsProvider = credentialsProvider;
            return this;
        }

        public ResourceProxyConfiguration build() {
            return this.configuration;
        }

    }

}
