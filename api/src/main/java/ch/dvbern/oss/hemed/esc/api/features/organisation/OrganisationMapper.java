package ch.dvbern.oss.hemed.esc.api.features.organisation;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationJson;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationSaveJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@JaxMapper
@RequiredArgsConstructor
public final class OrganisationMapper {

    private final OrganisationService organisationService;

    @Nullable
    public static OrganisationJson toJson(@Nullable Organisation organisation) {
        if (organisation == null) {
            return null;
        }
        return new OrganisationJson(
            organisation.getId(),
            EntityInfoJson.fromEntity(organisation),
            new OrganisationSaveJson(
                organisation.getVerantwortlicherArzt().getId(),
                organisation.getVersion()
            ),
            organisation.getVerantwortlicherArzt().getVornameName()
        );
    }

    @NonNull
    public List<OrganisationJson> findAll() {
        return organisationService.findAll().stream().map(OrganisationMapper::toJson).toList();
    }

    @NonNull
    public OrganisationJson getById(@NonNull ID<Organisation> id) {
        return toJson(organisationService.getById(id));
    }
}
