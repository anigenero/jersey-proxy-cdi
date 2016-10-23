package com.anigenero.resteasy.cdi.proxy;

import org.apache.http.client.CredentialsProvider;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;

class ProxyConfiguration {

    private ClientRequestFilter requestFilter;
    private ClientResponseFilter responseFilter;
    private Class<? extends CredentialsProvider> credentialsProvider;

    private String host;
    private String urlPrefix;
    private String username;
    private String password;

    private int port;
    private int timeout;

    public ClientRequestFilter getRequestFilter() {
        return requestFilter;
    }

    public void setRequestFilter(ClientRequestFilter requestFilter) {
        this.requestFilter = requestFilter;
    }

    public ClientResponseFilter getResponseFilter() {
        return responseFilter;
    }

    public void setResponseFilter(ClientResponseFilter responseFilter) {
        this.responseFilter = responseFilter;
    }

    public Class<? extends CredentialsProvider> getCredentialsProvider() {
        return credentialsProvider;
    }

    public void setCredentialsProvider(Class<? extends CredentialsProvider> credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
