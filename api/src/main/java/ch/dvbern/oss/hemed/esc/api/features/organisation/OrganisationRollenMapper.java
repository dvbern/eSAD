package ch.dvbern.oss.hemed.esc.api.features.organisation;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationJson;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationRollenJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.FachrollenService;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class OrganisationRollenMapper {

    private final OrganisationService organisationService;
    private final FachrollenService fachrollenService;

    @NonNull
    public static List<OrganisationRollenJson> toJsonList(
        @NonNull Set<OrganisationRollen> organisationRollen,
        @NonNull TenantEnum tenant
    ) {
        return organisationRollen.stream().map(or -> toJson(or, tenant)).toList();
    }

    @Nullable
    public static OrganisationRollenJson toJson(
        @Nullable OrganisationRollen organisationRollen,
        @NonNull TenantEnum tenant
    ) {
        if (organisationRollen == null) {
            return null;
        }
        List<Berechtigung> berechtigungen = organisationRollen.getBerechtigungen(tenant).stream().toList();
        return new OrganisationRollenJson(
            OrganisationMapper.toJson(organisationRollen.getOrganisation()),
            EnumSetUtil.toList(organisationRollen.getFachRollen()),
            berechtigungen
        );
    }

    @NonNull
    public Set<OrganisationRollen> toEntitySet(
        @NonNull Benutzer existingBenutzer,
        @NonNull List<OrganisationRollenJson> organisationRollenToSet,
        @NonNull Benutzer currentBenutzer
        // Benutzer gets passed instead of called inside this method to avoid circular dependency
    ) {
        for (OrganisationRollenJson organisationRollenJson : organisationRollenToSet) {
            Organisation organisation = null;
            OrganisationJson organisationJson = organisationRollenJson.getOrganisation();
            if (organisationJson != null) {
                organisation = organisationService.getById(organisationJson.getId());
            }
            EnumSet<FachRolle> enumSet = EnumSetUtil.toEnumSet(organisationRollenJson.getFachRollen(), FachRolle.class);
            fachrollenService.handleOrganisationsRollen(enumSet, existingBenutzer, organisation, currentBenutzer);
        }
        // Eventuell vorher vorhandene, die neu fehlen, entfernen
        Set<OrganisationRollen> existingRollenList = existingBenutzer.getOrganisationRollen();
        for (OrganisationRollen existingRollen : existingRollenList) {
            if (organisationRollenToSet.stream()
                .noneMatch(organisationRollenJson -> organisationRollenJson.getOrganisation() != null
                    && organisationRollenJson.getOrganisation()
                    .getId()
                    .equals(existingRollen.getOrganisation().getId()))) {
                existingBenutzer.getOrganisationRollen().remove(existingRollen);
            }
        }
        return existingBenutzer.getOrganisationRollen();
    }
}
