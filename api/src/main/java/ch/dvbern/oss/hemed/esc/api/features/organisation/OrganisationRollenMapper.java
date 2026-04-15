package ch.dvbern.oss.hemed.esc.api.features.organisation;

import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationRollenJson;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@JaxMapper
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrganisationRollenMapper {

    @NonNull
    public static List<OrganisationRollenJson> toJsonList(@NonNull Set<OrganisationRollen> organisationRollen) {
        return organisationRollen.stream().map(OrganisationRollenMapper::toJson).toList();
    }

    @Nullable
    public static OrganisationRollenJson toJson(@Nullable OrganisationRollen organisationRollen) {
        if (organisationRollen == null) {
            return null;
        }
        return new OrganisationRollenJson(
            OrganisationMapper.toJson(organisationRollen.getOrganisation()),
            EnumSetUtil.toList(organisationRollen.getFachRollen())
        );
    }
}
