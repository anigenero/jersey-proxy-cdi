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
public final class ResourceProxyFactory {

    private static final Logger log = LogManager.getLogger(ResourceProxyFactory.class);

    private ResourceProxyConfiguration resourceProxyConfiguration;

    public ResourceProxyFactory(ResourceProxyConfiguration resourceProxyConfiguration) {
        this.resourceProxyConfiguration = resourceProxyConfiguration;
    }

    @SuppressWarnings("unchecked")
    public Object build() {

        try {

            // configure the client builder
            createClient(resourceProxyConfiguration);

            Client client = createClient(resourceProxyConfiguration);

            return WebResourceFactory.newResource(resourceProxyConfiguration.getProxyClass(),
                    client.target(resourceProxyConfiguration.getUrl()));

        } catch (Exception e) {
            log.error("Unable to create proxy {}: {}",
                    resourceProxyConfiguration.getProxyClass().getSimpleName(), e.getMessage(), e);
            return null;
        }

    }

    /**
     * Creates the client builder
     *
     * @param configuration {@link ResourceProxyConfiguration}
     * @return {@link Client}
     */
    private Client createClient(ResourceProxyConfiguration configuration) {

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
            client.target(new URI(configuration.getUrl()));
        } catch (URISyntaxException e) {
            log.error("Invalid URI for proxy: '{}'", resourceProxyConfiguration.getProxyClass().getName(), e);
        }

        return client;

    }

}
