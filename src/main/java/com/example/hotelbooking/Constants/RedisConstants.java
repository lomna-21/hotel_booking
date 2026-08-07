package com.example.hotelbooking.Constants;

public class RedisConstants {

    private RedisConstants() {}

    public static final String HOTEL_KEY_PREFIX="hotel:";

    public static final String HOTEL_ROOMS_KEY_PREFIX = "hotel:rooms:";
    public static final long TTL_MINUTES = 5;
}
