package ch.dvbern.oss.hemed.esc.api.shared.auth;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum TenantEnum {

    DEFAULT(null),
    BS(Constants.REALM_NAME_BS),
    BE(Constants.REALM_NAME_BE),
    ;


    @Nullable
    private final String realmName; // im esc-keycloak

    TenantEnum(@Nullable String realmName) {
        this.realmName = realmName;
    }

    @NonNull
    public String getRealmName() {
        if(realmName == null) {
            throw new IllegalStateException("Realm name not set for tenant " + this);
        }
        return realmName;
    }

    @NonNull
    public String getHibernateMultiTenancyIdentifier() {
        return this.name().toUpperCase();
    }

    @UtilityClass
    private static class Constants {
        public static final String REALM_NAME_BS = "bs";
        public static final String REALM_NAME_BE = "be";
    }
}
