package ch.dvbern.dvbstarter.infrastructure.ratelimit;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RateLimitConst {

    public static final int DEFAULT_LIMIT = 30;

    /**
     * Requests per second (the {@code @RateLimit} default window is one second), so this only
     * guards against a client hammering the endpoint - not against long-running exports.
     * <p>
     * The meaningful limits for the HelloData export are elsewhere: the {@code @Bulkhead(1)} on
     * {@code HelloDataExportService.writeSchulen} allows exactly one export at a time, and
     * {@code ClientFilter} lets only the HelloData service account reach the endpoint at all. This value is therefore kept in line with
     * {@link #DEFAULT_LIMIT}
     */
    public static final int HELLODATA_EXPORT_LIMIT = DEFAULT_LIMIT;
}
