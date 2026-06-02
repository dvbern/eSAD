package ch.dvbern.oss.hemed.esc.api.shared.fileio.healthcheck;

import java.util.Set;

record FileStoreDiffReport(
    Set<String> dbOnly,
    Set<String> s3Only,
    int dbOnlyTotal,
    int s3OnlyTotal
) {
}
