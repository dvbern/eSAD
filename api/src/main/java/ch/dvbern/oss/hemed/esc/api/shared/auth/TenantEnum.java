package ch.dvbern.oss.hemed.esc.api.shared.auth;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public enum TenantEnum {

    DEFAULT(false, null),
    BS(true, Constants.REALM_NAME_BS),
    WIN(true, Constants.REALM_NAME_WIN),
    BE(true, Constants.REALM_NAME_BE),
    ;

    @Getter
    private final boolean aktiv;

    @Nullable
    private final String realmName; // im esc-keycloak

    TenantEnum(boolean aktiv, @Nullable String realmName) {
        this.aktiv = aktiv;
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
        return this.name().toLowerCase();
    }

    @UtilityClass
    private static class Constants {
        public static final String REALM_NAME_BS = "bs";
        public static final String REALM_NAME_WIN = "win";
        public static final String REALM_NAME_BE = "be";
    }

    @Nullable
    public static TenantEnum fromString(@NonNull String tenant) {
        try {
            return TenantEnum.valueOf(tenant);
        } catch (IllegalArgumentException e) {
            LOG.warn("Illegal Tenant Name {}", tenant);
            return null;
        }
    }
}
