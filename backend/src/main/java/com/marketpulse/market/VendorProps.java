package com.marketpulse.market;
import java.util.Map; import org.springframework.boot.context.properties.ConfigurationProperties;
/** Settings for a licensed market-data vendor that exposes JSON over HTTP. See config/application.example.yml. */
@ConfigurationProperties("app.market.vendor")
public record VendorProps(boolean enabled, String name, String apiKeyHeader, String apiKey, boolean delayed, String timeFormat,
    String quoteUrl, String quotePath, String moversUrl, String listPath, String searchUrl, String historyUrl, String candlesPath,
    Map<String,String> symbols, Map<String,String> fields, Map<String,String> candleFields) {}
