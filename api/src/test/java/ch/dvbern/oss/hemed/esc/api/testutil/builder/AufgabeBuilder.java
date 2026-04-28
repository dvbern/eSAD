package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeMapper;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.json.AufgabeJson;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.types.AufgabeStatus;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.AufgabeJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AufgabeJsonSpec;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;

public class AufgabeBuilder {
    private Aufgabe aufgabe;

    private AufgabeBuilder() {
        this.aufgabe = defaultAufgabe();
        TestUtil.setEntityInfo(aufgabe);
    }

    @NonNull
    public static Aufgabe defaultAufgabe(
    ) {
        @NotNull @NonNull @Valid Benutzer defaultEmpfaenger = BenutzerBuilder.defaultBenutzer();
        @NotNull @NonNull Set<Label> defaultLabels = Collections.emptySet();
        @NotNull @NonNull List<FileData> defaultFiles = Collections.emptyList();

        return new Aufgabe(
            TestUtil.getRandomString("Aufgabe"),
            "",
            AufgabeStatus.OFFEN,
            LocalDate.of(2025, 1, 3),
            defaultEmpfaenger,
            defaultEmpfaenger,
            defaultLabels,
            null,
            null,
            defaultFiles,
            null
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public AufgabeBuilder forAufgabe(@NonNull Aufgabe initAufgabe) {
        this.aufgabe = initAufgabe;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AufgabeBuilder withID(@NonNull ID<Aufgabe> id) {
        this.aufgabe.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AufgabeBuilder withTitle(@NonNull String title) {
        this.aufgabe.setTitel(title);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AufgabeBuilder withStatus(@NonNull AufgabeStatus status) {
        this.aufgabe.setAufgabeStatus(status);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AufgabeBuilder withFaelligDatum(@NonNull LocalDate faelligDatum) {
        this.aufgabe.setFaelligBis(faelligDatum);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AufgabeBuilder withEmpfaenger(@NonNull Benutzer empfaenger) {
        this.aufgabe.setEmpfaenger(empfaenger);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AufgabeBuilder withUserErfasst(@NonNull Benutzer userErfasst) {
        this.aufgabe.setUserErfasst(userErfasst);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AufgabeBuilder withKind(@Nullable Kind kind) {
        this.aufgabe.setKind(kind);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AufgabeBuilder withBemerkung(@NonNull String bemerkung) {
        this.aufgabe.setBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AufgabeBuilder withLabel(@NonNull Set<Label> labels) {
        this.aufgabe.setLabels(labels);
        return this;
    }

    @NonNull
    public static Aufgabe create(@NonNull Consumer<AufgabeBuilder> block) {
        AufgabeBuilder builder = new AufgabeBuilder();
        block.accept(builder);
        return builder.aufgabe;
    }

    @NonNull
    public static AufgabeJson createJax(@NonNull Consumer<AufgabeBuilder> block) {
        Aufgabe entity = create(block);
        return AufgabeMapper.toJson(entity, TenantEnum.BS);
    }

    @NonNull
    public static AufgabeJsonSpec createJaxSpec(@NonNull Consumer<AufgabeBuilder> block) {
        AufgabeJson json = createJax(block);
        return AufgabeJsonSpecMapper.INSTANCE.aufgabeJsonToAufgabeJsonSpec(json);
    }
}
