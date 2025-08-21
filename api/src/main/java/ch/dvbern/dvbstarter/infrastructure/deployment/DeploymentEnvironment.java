package ch.dvbern.dvbstarter.infrastructure.deployment;

import lombok.RequiredArgsConstructor;

// FIXME: starter: init: remove unused environments
@RequiredArgsConstructor
public enum DeploymentEnvironment {
    LOCAL,
    UNIT_TEST,
    DEV,
    REVIEW,
    IAT,
    UAT,
    DEMO,
    PROD,
    ;

    public boolean isVersionFilterEnabled() {
        return !(this == LOCAL || this == UNIT_TEST);
    }

    public boolean isProd(){
        return this == PROD;
    }
}
