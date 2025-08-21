package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.features.arzt.ArztMapper;
import ch.dvbern.oss.hemed.esc.api.features.arzt.json.ArztJson;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.Fachrichtung;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.HpvTeilnahme;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.ArztJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ArztJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ArztSaveJsonSpec;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.bouncycastle.math.ec.AbstractECLookupTable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequiredArgsConstructor
public class ArztBuilder {

    private final Arzt arzt;

    private ArztBuilder() {
        this.arzt = defaultArzt();
        TestUtil.setEntityInfo(arzt);
    }

    @NonNull
    public static Arzt defaultArzt() {
        return new Arzt(
            null,
            Fachrichtung.ANDERES,
            Organisation.NATUERLICHE_PERSON,
            null,
            "NameArztDefault",
            AdresseBuilder.defaultAdresse(),
            NormalizedPhoneNumber.parse("+41 79 123 45 67"),
            EmailAddress.fromString("arzt.default@mailbucket.dvbern.ch"),
            null,
            HpvTeilnahme.NEIN);
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArztBuilder withName(@NonNull String name) {
        this.arzt.setName(name);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArztBuilder withAnrede(@Nullable String anrede) {
        this.arzt.setAnrede(anrede);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArztBuilder withVorame(@Nullable String vorname) {
        this.arzt.setVorname(vorname);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArztBuilder withHpvTeilnahme(@NonNull HpvTeilnahme hpvTeilnahme) {
        this.arzt.setHpvTeilnahme(hpvTeilnahme);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArztBuilder withTelefon(@Nullable NormalizedPhoneNumber telefon) {
        this.arzt.setTelefon(telefon);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArztBuilder withBemerkungen(@Nullable String bemerkungen) {
        this.arzt.setBemerkungen(bemerkungen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArztBuilder withAdresse(@Nullable Adresse adresse) {
        this.arzt.setAdresse(adresse);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArztBuilder withEmail(@Nullable String email) {
        this.arzt.setEmail(email != null ? EmailAddress.fromString(email) : null);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArztBuilder withFachrichtung(@NonNull Fachrichtung fachrichtung) {
        this.arzt.setFachrichtung(fachrichtung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArztBuilder withOrganisation(@NonNull Organisation organisation) {
        this.arzt.setOrganisation(organisation);
        return this;
    }

    @NonNull
    public static Arzt create(@NonNull Consumer<ArztBuilder> block) {
        ArztBuilder builder = new ArztBuilder();
        block.accept(builder);
        return builder.arzt;
    }

    @NonNull
    public static ArztJson createJax(@NonNull Consumer<ArztBuilder> block) {
        Arzt entity = create(block);
        return ArztMapper.toJson(entity);
    }

    @NonNull
    public static ArztJsonSpec createJaxSpec(@NonNull Consumer<ArztBuilder> block) {
        ArztJson json = createJax(block);
        return ArztJsonSpecMapper.INSTANCE.arztJsonToArztJsonSpec(json);
    }

    @NonNull
    public static ArztSaveJsonSpec createSaveJaxSpec(@NonNull Consumer<ArztBuilder> block) {
        ArztJsonSpec entity = createJaxSpec(block);
        return entity.getData();
    }
}
