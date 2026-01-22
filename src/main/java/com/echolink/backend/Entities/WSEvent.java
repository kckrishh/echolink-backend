package com.echolink.backend.Entities;

public class WSEvent<T> {
    public String eventType;
    public T data;

    public WSEvent(String eventType, T data) {
        this.eventType = eventType;
        this.data = data;
    }
}
