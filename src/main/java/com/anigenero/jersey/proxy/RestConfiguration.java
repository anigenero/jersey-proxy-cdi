package com.anigenero.jersey.proxy;

import org.apache.http.client.CredentialsProvider;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;

@SuppressWarnings("WeakerAccess")
public class RestConfiguration {

    private final Class proxyClass;

    private ClientRequestFilter requestFilter;
    private ClientResponseFilter responseFilter;
    private Class<? extends CredentialsProvider> credentialsProvider;

    private String url;
    private String username;
    private String password;

    private int port;
    private int timeout;

    private <T> RestConfiguration(final Class<T> proxyClass) {
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

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }

    public Class getProxyClass() {
        return this.proxyClass;
    }

    public static final class Builder {

        private RestConfiguration restConfiguration;

        public <T> Builder(Class<T> clazz) {
            this.restConfiguration = new RestConfiguration(clazz);
        }

        public Builder setRequestFilter(ClientRequestFilter requestFilter) {
            this.restConfiguration.requestFilter = requestFilter;
            return this;
        }

        public Builder setResponseFilter(ClientResponseFilter responseFilter) {
            this.restConfiguration.responseFilter = responseFilter;
            return this;
        }

        public Builder setUrl(String url) {
            this.restConfiguration.url = url;
            return this;
        }

        public Builder setTimeout(int timeout) {
            this.restConfiguration.timeout = timeout;
            return this;
        }

        public Builder setPort(int port) {
            this.restConfiguration.port = port;
            return this;
        }

        public Builder setUsername(String username) {
            this.restConfiguration.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.restConfiguration.password = password;
            return this;
        }

        public Builder setCredentialsProvider(Class<? extends CredentialsProvider> credentialsProvider) {
            this.restConfiguration.credentialsProvider = credentialsProvider;
            return this;
        }

        public RestConfiguration build() {
            return this.restConfiguration;
        }

    }

}
