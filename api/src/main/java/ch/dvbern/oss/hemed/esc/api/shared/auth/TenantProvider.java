package ch.dvbern.oss.hemed.esc.api.shared.auth;

@FunctionalInterface
public interface TenantProvider {
    TenantEnum produceTenant();
}
