package ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface EntityWithOptimisticLocking {

    long getVersion();

    void setVersionFromFrontend(@NonNull Long version);
}
