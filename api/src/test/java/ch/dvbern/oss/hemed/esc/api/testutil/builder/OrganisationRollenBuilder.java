package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.util.EnumSet;
import java.util.function.Consumer;

import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollen;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class OrganisationRollenBuilder {

    private OrganisationRollen organisationRollen;

    private OrganisationRollenBuilder() {
        this.organisationRollen = defaultOrganisationRollen();
        TestUtil.setEntityInfo(organisationRollen);
    }

    @NonNull
    public static OrganisationRollen defaultOrganisationRollen() {
        return new OrganisationRollen(
            OrganisationBuilder.defaultOrganisation(),
            BenutzerBuilder.defaultBenutzer(),
            EnumSetUtil.toString(EnumSet.of(FachRolle.MPA))
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public OrganisationRollenBuilder forOrganisationRolle(@NonNull OrganisationRollen existingOrganisationRolle) {
        this.organisationRollen = existingOrganisationRolle;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public OrganisationRollenBuilder withOrganisation(@NonNull Organisation organisation) {
        this.organisationRollen.setOrganisation(organisation);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public OrganisationRollenBuilder withBenutzer(@NonNull Benutzer benutzer) {
        this.organisationRollen.setBenutzer(benutzer);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public OrganisationRollenBuilder withFachrollen(@NonNull EnumSet<FachRolle> fachrollen) {
        this.organisationRollen.setFachRollen(fachrollen);
        return this;
    }

    @NonNull
    public static OrganisationRollen create(@NonNull Consumer<OrganisationRollenBuilder> block) {
        OrganisationRollenBuilder builder = new OrganisationRollenBuilder();
        block.accept(builder);
        return builder.organisationRollen;
    }
}
