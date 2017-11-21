package com.anigenero.jersey.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

public class Main {

    private final static Logger logger = LogManager.getLogger("main");

    public static void main(String[] args) {

        try (final SeContainer container = SeContainerInitializer.newInstance().initialize()) {

            final DogHandler dogAPI = container.select(DogHandler.class).get();

            dogAPI.getAllBreeds().forEach((key, value) -> logger.info(key));

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

}
