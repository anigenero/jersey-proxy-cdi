package com.anigenero.jersey.example;

import java.io.Serializable;

public class APIResult<T extends Serializable> implements Serializable {

    private String status;
    private T message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

}
