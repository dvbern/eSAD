package ch.dvbern.oss.hemed.esc.api.shared.fileio.healthcheck;

import java.util.Set;

/**
 * Full (untruncated) diff between DB and S3. Truncation for the mail body happens in
 * {@link FileStoreHealthCheckMailService}; the attachment contains all entries.
 */
record FileStoreDiffReport(
    Set<String> dbOnly,
    Set<String> s3Only,
    int dbOnlyIgnored,
    int s3OnlyIgnored
) {

    int dbOnlyTotal() {
        return dbOnly.size();
    }

    int s3OnlyTotal() {
        return s3Only.size();
    }
}
