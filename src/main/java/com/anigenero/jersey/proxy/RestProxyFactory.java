package com.anigenero.jersey.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("WeakerAccess")
public final class RestProxyFactory {

    private static final Logger log = LogManager.getLogger(RestProxyFactory.class);

    private RestConfiguration restConfiguration;

    public RestProxyFactory(RestConfiguration restConfiguration) {
        this.restConfiguration = restConfiguration;
    }

    @SuppressWarnings("unchecked")
    public Object build() {

        try {

            // configure the client builder
            createClient(restConfiguration);

            Client client = createClient(restConfiguration);
            return WebResourceFactory.newResource(restConfiguration.getProxyClass(), client.target(restConfiguration.getHost()));

        } catch (Exception e) {
            log.error("Unable to create proxy {}: {}", restConfiguration.getProxyClass().getSimpleName(), e.getMessage(), e);
            return null;
        }

    }

    /**
     * Creates the client builder
     *
     * @param configuration {@link RestConfiguration}
     * @return {@link Client}
     */
    private Client createClient(RestConfiguration configuration) {

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(configuration.getProxyClass());

        JerseyClientBuilder clientBuilder = new JerseyClientBuilder();
        clientBuilder.withConfig(clientConfig);

        if (configuration.getRequestFilter() != null) {
            clientBuilder.register(configuration.getRequestFilter());
        }

        if (configuration.getResponseFilter() != null) {
            clientBuilder.register(configuration.getResponseFilter());
        }

        JerseyClient client = clientBuilder.build();

        client.property(ClientProperties.CONNECT_TIMEOUT, configuration.getTimeout() / 2);
        client.property(ClientProperties.READ_TIMEOUT, configuration.getTimeout() / 2);

        try {
            client.target(this.buildUri(configuration));
        } catch (URISyntaxException e) {
            log.error("Invalid URI for proxy: '{}'", restConfiguration.getProxyClass().getName(), e);
        }

        return client;

    }

    /**
     * Builds the base URI from the configuration
     *
     * @param configuration {@link RestConfiguration}
     * @return {@link URI}
     * @throws URISyntaxException if the URI is invalid
     */
    private URI buildUri(RestConfiguration configuration) throws URISyntaxException {

        final String host = configuration.getHost();
        final String scheme = configuration.getScheme();
        final String prefix = this.generateUrlPrefix(configuration.getUrlPrefix());

        final int port = configuration.getPort();

        return new URI(scheme + "://" + host + ":" + port + "/" + prefix);

    }

    /**
     * Generates the URL prefix
     *
     * @param urlPrefix {@link String}
     * @return {@link String}
     */
    private String generateUrlPrefix(String urlPrefix) {

        if (urlPrefix != null && urlPrefix.startsWith("/")) {
            urlPrefix = urlPrefix.substring(1);
        }

        return urlPrefix;

    }

}
