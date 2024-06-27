package com.sharecampus.utils;

import java.time.format.DateTimeFormatter;

public class RedisConstants {

    public static final String Register_CODE_KEY = "register:code:";
    public static final Long Register_CODE_TTL = 2L;
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final String CACHE_Venue_KEY = "cache:venue:";

    public static final String LOCK_Venue_KEY = "lock:venue:";
    public static final Long LOCK_Venue_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String SECKILL_BRGIN_Time_KEY = "seckill:begin_time:";
    public static final String SECKILL_END_Time_KEY = "seckill:end_time:";

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String CACHE_Voucher_KEY = "cache:voucher:";
    public static final Long CACHE_Voucher_TTL = 30L;

    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String Venue_GEO_KEY = "venue:geo:";
    public static final String USER_SIGN_KEY = "sign:";
}
