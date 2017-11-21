package com.anigenero.jersey.example;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class DogHandler {

    private final DogAPI dogAPI;

    @Inject
    public DogHandler(DogAPI dogAPI) {
        this.dogAPI = dogAPI;
    }

    public Map<String, List<String>> getAllBreeds() {
        return this.dogAPI.getAllBreeds().getMessage();
    }

}
