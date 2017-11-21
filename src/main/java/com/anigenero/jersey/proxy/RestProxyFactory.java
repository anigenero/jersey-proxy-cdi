package com.anigenero.jersey.proxy;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
public final class RestProxyFactory {

    private static final Logger log = LogManager.getLogManager().getLogger(RestProxyFactory.class.getName());

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
            return WebResourceFactory.newResource(restConfiguration.getProxyClass(), client.target(restConfiguration.getUrl()));

        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to create proxy " + restConfiguration.getProxyClass().getSimpleName() +
                    ": " + e.getMessage(), e);
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
            client.target(new URI(configuration.getUrl()));
        } catch (URISyntaxException e) {
            log.log(Level.SEVERE, "Invalid URI for proxy: '" + restConfiguration.getProxyClass().getName() + "'", e);
        }

        return client;

    }

}
