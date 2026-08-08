package com.talaatharb.leadmanager.scraper;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for a single scraper, supporting both guest (anonymous) and
 * authenticated (logged-in) modes.
 * <p>
 * For guest mode only a {@code userAgent} and optional proxy are needed.
 * For authenticated scraping, supply {@code username}/{@code password} and/or
 * pre-captured session {@code cookies}.  Each scraper reads only the fields it
 * needs; unused fields are silently ignored.
 *
 * <pre>{@code
 * ScraperConfig cfg = ScraperConfig.builder()
 *         .userAgent("Mozilla/5.0 ...")
 *         .username("alice@example.com")
 *         .password("s3cr3t")
 *         .cookie("li_at", "<linkedin-session-token>")
 *         .timeoutMs(15_000)
 *         .build();
 * }</pre>
 */
@Getter
public final class ScraperConfig {

    /** Default browser-like user-agent string used when none is specified. */
    public static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) "
            + "Chrome/124.0.0.0 Safari/537.36";

    public static final int DEFAULT_TIMEOUT_MS = 10_000;

    private final String userAgent;
    private final int timeoutMs;

    // Authenticated scraping
    private final String username;
    private final String password;
    private final Map<String, String> cookies;

    // Optional proxy (host:port)
    private final String proxyHost;
    private final int proxyPort;

    private ScraperConfig(Builder b) {
        this.userAgent = b.userAgent;
        this.timeoutMs = b.timeoutMs;
        this.username  = b.username;
        this.password  = b.password;
        this.cookies   = Collections.unmodifiableMap(new HashMap<>(b.cookies));
        this.proxyHost = b.proxyHost;
        this.proxyPort = b.proxyPort;
    }

    /** {@code true} when credentials or session cookies have been supplied. */
    public boolean isAuthenticated() {
        return (!cookies.isEmpty())
                || (username != null && !username.isBlank()
                        && password != null && !password.isBlank());
    }

    /** Create a default guest-only configuration. */
    public static ScraperConfig defaultConfig() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    // ---- Builder ----

    public static final class Builder {
        private String userAgent = DEFAULT_USER_AGENT;
        private int    timeoutMs = DEFAULT_TIMEOUT_MS;
        private String username;
        private String password;
        private final Map<String, String> cookies = new HashMap<>();
        private String proxyHost;
        private int    proxyPort;

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /** Add a single session cookie (e.g. {@code "li_at"} for LinkedIn). */
        public Builder cookie(String name, String value) {
            this.cookies.put(name, value);
            return this;
        }

        /** Bulk-add cookies from an existing map. */
        public Builder cookies(Map<String, String> cookies) {
            this.cookies.putAll(cookies);
            return this;
        }

        public Builder proxy(String host, int port) {
            this.proxyHost = host;
            this.proxyPort = port;
            return this;
        }

        public ScraperConfig build() {
            return new ScraperConfig(this);
        }
    }
}
