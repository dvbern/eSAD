package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.CreateBenutzerDTO;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.BenutzerLoginInfos;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.BenutzerJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BenutzerBuilder {

    private Benutzer benutzer;

    private BenutzerBuilder() {
        this.benutzer = defaultBenutzer();
        TestUtil.setEntityInfo(benutzer);
    }

    @NonNull
    public static Benutzer defaultBenutzer() {
        return new Benutzer(
            null,
            EmailAddress.fromString(UUID.randomUUID() + "@example.com"),
            null,
            "TesterNachname",
            "TesterVorname",
            null,
            null,
            EnumSetUtil.toString(EnumSet.of(FachRolle.MPA)),
            Collections.emptySet(),
            true,
            null
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder forBenutzer(@NonNull Benutzer existingBenutzer) {
        this.benutzer = existingBenutzer;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withID(@NonNull ID<Benutzer> id) {
        this.benutzer.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withRandomNachname(@NonNull String name) {
        this.benutzer.setNachname(name + '_' + UUID.randomUUID());
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withTitel(@NonNull String titel) {
        this.benutzer.setTitel(titel);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withNachname(@NonNull String name) {
        this.benutzer.setNachname(name);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withVorname(@NonNull String name) {
        this.benutzer.setVorname(name);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withRandomVorname(@NonNull String name) {
        this.benutzer.setVorname(name + '_' + UUID.randomUUID());
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withTelefon(@NonNull NormalizedPhoneNumber telefon) {
        this.benutzer.setTelefon(telefon);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withEmail(@NonNull EmailAddress email) {
        this.benutzer.setEmail(email);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withRandomEmail(@NonNull String email) {
        this.benutzer.setEmail(EmailAddress.fromString(UUID.randomUUID() + email));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withFachRolle(@NonNull Collection<FachRolle> fachRollen) {
        this.benutzer.setFachRollen(EnumSetUtil.toEnumSet(fachRollen, FachRolle.class));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withKeycloakId(@NonNull String string) {
        this.benutzer.setKeycloakId(string);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withLastUsedSessionTimestamp(@Nullable OffsetDateTime timestamp) {
        if (timestamp != null) {
            this.benutzer.setLoginInfos(new BenutzerLoginInfos(null, null, timestamp, null));
        } else {
            this.benutzer.setLoginInfos(null);
        }
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withLastManuallyActivatedTimestamp(@Nullable OffsetDateTime timestamp) {
        if (timestamp != null) {
            this.benutzer.setTimestampLastManuallyActivated(timestamp);
            this.benutzer.setAktiv(true);
        } else {
            this.benutzer.setTimestampLastManuallyActivated(null);
            this.benutzer.setAktiv(false);
        }

        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BenutzerBuilder withActive(boolean active) {
        benutzer.setAktiv(active);
        return this;
    }

    @NonNull
    public static Benutzer create(@NonNull Consumer<BenutzerBuilder> block) {
        BenutzerBuilder builder = new BenutzerBuilder();
        block.accept(builder);
        return builder.benutzer;
    }

    @NonNull
    public static BenutzerJson createJax(@NonNull Consumer<BenutzerBuilder> block) {
        Benutzer entity = create(block);
        return BenutzerMapper.toJson(entity);
    }

    @NonNull
    public static BenutzerJsonSpec createJaxSpec(@NonNull Consumer<BenutzerBuilder> block) {
        BenutzerJson benutzerJson = createJax(block);
        return BenutzerJsonSpecMapper.INSTANCE.benutzerJsonToBenutzerJsonSpec(benutzerJson);
    }

    @NonNull
    public static CreateBenutzerDTO createCreateDTO(@NonNull Consumer<BenutzerBuilder> block) {
        Benutzer entity = create(block);
        return new CreateBenutzerDTO(
            entity.getEmail(),
            entity.getTitel(),
            entity.getNachname(),
            entity.getVorname(),
            entity.getTelefon(),
            entity.getKeycloakId()
        );
    }
}
