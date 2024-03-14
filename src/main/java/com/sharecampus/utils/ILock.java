package com.sharecampus.utils;

public interface ILock {
    boolean tryLock(long timeoutSec);

    void unlock();
}
