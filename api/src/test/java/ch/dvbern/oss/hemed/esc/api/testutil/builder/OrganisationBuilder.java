package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.util.Set;
import java.util.function.Consumer;

import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class OrganisationBuilder {

    private Organisation organisation;

    private OrganisationBuilder() {
        this.organisation = defaultOrganisation();
        TestUtil.setEntityInfo(organisation);
    }

    @NonNull
    public static Organisation defaultOrganisation() {
        Organisation defaultOrganisation = new Organisation(
            BenutzerBuilder.defaultBenutzer(),
            Set.of()
        );
        defaultOrganisation.setVersion(1L);
        return defaultOrganisation;
    }

    @NonNull
    @CanIgnoreReturnValue
    public OrganisationBuilder forOrganisation(@NonNull Organisation organisation) {
        this.organisation = organisation;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public OrganisationBuilder withVerantwortlicherArzt(@NonNull Benutzer verantwortlicherArzt) {
        this.organisation.setVerantwortlicherArzt(verantwortlicherArzt);
        return this;
    }

    @NonNull
    public static Organisation create(@NonNull Consumer<OrganisationBuilder> block) {
        OrganisationBuilder builder = new OrganisationBuilder();
        block.accept(builder);
        return builder.organisation;
    }
}
