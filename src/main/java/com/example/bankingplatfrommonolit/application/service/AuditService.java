package com.example.bankingplatfrommonolit.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditService {
    public void security(String msg, Object... args) {
        log.warn("AUDIT {} {}", msg, args);
    }

    public void business(String msg, Object... args) {
        log.info("AUDIT {} {}", msg, args);
    }
}