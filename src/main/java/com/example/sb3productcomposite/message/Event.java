package com.example.sb3productcomposite.message;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.*;

import java.time.ZonedDateTime;

@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Event<K,D>{

    public enum Type{CREATE,DELETE;}
    private  K key;
    private  D data;
    private  Type eventType;
    private  ZonedDateTime eventCreatedAt;


    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    public ZonedDateTime getEventCreatedAt() {
        return eventCreatedAt;
    }

    public K getKey() {
        return key;
    }

    public D getData() {
        return data;
    }

    public Type getEventType() {
        return eventType;
    }
}
