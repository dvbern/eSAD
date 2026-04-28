package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizMapper;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizStatus;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizJson;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.NotizJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NotizJsonSpec;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import lombok.RequiredArgsConstructor;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public class NotizBuilder {

    private final Notiz notiz;

    private NotizBuilder() {
        this.notiz = defaultNotiz();
        TestUtil.setEntityInfo(notiz);
    }

    @NonNull
    public static Notiz defaultNotiz() {
        @NotNull @NonNull Dossier defaultDossier = new Dossier();
        @NotNull @NonNull @Valid Benutzer defaultVerantwortlicher = BenutzerBuilder.defaultBenutzer();
        @NotNull @NonNull Set<Label> defaultLabels = new HashSet<>();
        @NotNull @NonNull List<FileData> defaultFiles = new ArrayList<>();
        Notiz notiz = new Notiz(
            "My Default Notiz",
            "My Default Notiz Bemerkungen",
            NotizStatus.AKTIV,
            NotizTyp.DEFAULT,
            defaultDossier,
            defaultVerantwortlicher,
            null,
            defaultLabels,
            defaultFiles
        );
        notiz.setLabels(new LinkedHashSet<>());
        notiz.setTimestampErstellt(OffsetDateTime.now());
        return notiz;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NotizBuilder withID(@NonNull ID<Notiz> id) {
        this.notiz.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NotizBuilder withTitel(@NonNull String titel) {
        this.notiz.setTitel(titel);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NotizBuilder withBemerkungen(@NonNull String bemerkungen) {
        this.notiz.setBemerkungen(bemerkungen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NotizBuilder withStatus(@NonNull NotizStatus status) {
        this.notiz.setStatus(status);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NotizBuilder withDossier(@NonNull Dossier dossier) {
        this.notiz.setDossier(dossier);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NotizBuilder withLabels(@NonNull Set<Label> labels) {
        this.notiz.setLabels(labels);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NotizBuilder withLabel(@NonNull Label label) {
        this.notiz.setLabels(Collections.singleton(label));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NotizBuilder withVerantwortlicher(@NonNull Benutzer benutzer) {
        this.notiz.setVerantwortlicher(benutzer);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NotizBuilder withFiles(@NonNull List<FileData> files) {
        this.notiz.setFiles(files);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NotizBuilder withTimestampErstellt(@NonNull OffsetDateTime dateTime) {
        this.notiz.setTimestampErstellt(dateTime);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NotizBuilder withNotizTyp(@NonNull NotizTyp typ) {
        this.notiz.setTyp(typ);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NotizBuilder withAnlassLink(@NonNull String link) {
        this.notiz.setAnlassLink(link);
        return this;
    }

    @NonNull
    public static Notiz create(@NonNull Consumer<NotizBuilder> block) {
        NotizBuilder builder = new NotizBuilder();
        block.accept(builder);
        return builder.notiz;
    }

    @NonNull
    public static NotizJson createJax(@NonNull Consumer<NotizBuilder> block) {
        Notiz entity = create(block);
        return NotizMapper.toJson(entity, TenantEnum.BS);
    }

    @NonNull
    public static NotizJsonSpec createJaxSpec(@NonNull Consumer<NotizBuilder> block) {
        NotizJson json = createJax(block);
        return NotizJsonSpecMapper.INSTANCE.toSpec(json);
    }
}
