package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResourcePlain;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerResource;
import ch.dvbern.oss.hemed.esc.api.features.example.developer.DeveloperResource;
import ch.dvbern.oss.hemed.esc.api.features.pub.clientconfig.ClientConfigResource;
import ch.dvbern.oss.hemed.esc.api.shared.plz.PLZResource;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsResource;
import ch.dvbern.oss.hemed.esc.api.shared.timetravel.TimeTravelResource;
import lombok.experimental.UtilityClass;

@UtilityClass
@JaxResourcePlain
public class PermitAllEndpointConstants {

    public static final Map<String, Class<?>> PUBLIC_PATHS_WITHOUT_TENANT_VALIDATION = Map.of(
        "/pub/", ClientConfigResource.class,
        "/benutzer/self", BenutzerResource.class,
        "/tenant-settings/self", TenantSettingsResource.class
    );

    public static final Map<String, Class<?>> PUBLIC_PATHS_WITH_TENANT_VALIDATION = Map.of(
        "/plz", PLZResource.class,
        "/example/developer", DeveloperResource.class,
        "/time-travel", TimeTravelResource.class
    );

    @SuppressWarnings("java:S2386" /*we want this to be visible*/)
    public static final Map<String, Class<?>> ALL_PUBLIC_RESOURCES = Stream.concat(
            PermitAllEndpointConstants.PUBLIC_PATHS_WITHOUT_TENANT_VALIDATION.entrySet().stream(),
            PermitAllEndpointConstants.PUBLIC_PATHS_WITH_TENANT_VALIDATION.entrySet().stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
}
