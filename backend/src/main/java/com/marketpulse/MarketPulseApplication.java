package com.marketpulse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication @EnableScheduling @EnableAsync
public class MarketPulseApplication { public static void main(String[] a){ SpringApplication.run(MarketPulseApplication.class,a);} }
