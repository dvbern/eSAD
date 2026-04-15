package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.util.function.Consumer;

import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.kinder.NachkontrolleMapper;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.NachkontrolleJson;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.NachkontrolleJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NachkontrolleJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NachkontrolleBuilder {

    private Nachkontrolle nachkontrolle;

    private NachkontrolleBuilder() {
        this.nachkontrolle = defaultNachkontrolle();
        TestUtil.setEntityInfo(nachkontrolle);
    }

    @NonNull
    public static Nachkontrolle defaultNachkontrolle() {
        return Nachkontrolle.forDossier(DossierBuilder.defaultDossier());
    }

    @NonNull
    @CanIgnoreReturnValue
    public NachkontrolleBuilder forDossier(@NonNull Dossier dossier) {
        this.nachkontrolle.setDossier(dossier);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NachkontrolleBuilder withNachkontrolleSehen(boolean nachkontrolleNeeded) {
        this.nachkontrolle.getAnlassFlags().setNachkontrolleSehen(nachkontrolleNeeded);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public NachkontrolleBuilder withNachkontrolleHoeren(boolean nachkontrolleNeeded) {
        this.nachkontrolle.getAnlassFlags().setNachkontrolleHoeren(nachkontrolleNeeded);
        return this;
    }

    @NonNull
    public static Nachkontrolle create(@NonNull Consumer<NachkontrolleBuilder> block) {
        NachkontrolleBuilder builder = new NachkontrolleBuilder();
        block.accept(builder);
        return builder.nachkontrolle;
    }

    @NonNull
    public static NachkontrolleJson createJax(@NonNull Consumer<NachkontrolleBuilder> block) {
        Nachkontrolle entity = create(block);
        return NachkontrolleMapper.toJson(entity);
    }

    @NonNull
    public static NachkontrolleJsonSpec createJaxSpec(@NonNull Consumer<NachkontrolleBuilder> block) {
        NachkontrolleJson json = createJax(block);
        return NachkontrolleJsonSpecMapper.INSTANCE.nachkontrolleJsonToNachkontrolleJsonSpec(json);
    }
}
