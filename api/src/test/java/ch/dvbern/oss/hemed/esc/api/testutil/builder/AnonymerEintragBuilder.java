package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AnonymerEintragBuilder {

    private AnonymerEintrag anonymerEintrag;


    private AnonymerEintragBuilder() {
        this.anonymerEintrag = defaultAnonymerEintrag();
        TestUtil.setEntityInfo(anonymerEintrag);
    }

    @NonNull
    public static AnonymerEintrag defaultAnonymerEintrag() {
        AnonymerEintrag eintrag = new AnonymerEintrag();
        eintrag.setHash("default-hash");
        eintrag.setAnlass(AnlassBuilder.defaultAnlass(LocalDate.now()));
        return eintrag;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnonymerEintragBuilder forAnonymerEintrag(@NonNull AnonymerEintrag initAnonymerEintrag) {
        this.anonymerEintrag = initAnonymerEintrag;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnonymerEintragBuilder withID(@NonNull ID<AnonymerEintrag> id) {
        this.anonymerEintrag.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnonymerEintragBuilder withAnlass(@NonNull Anlass anlass) {
        this.anonymerEintrag.setAnlass(anlass);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnonymerEintragBuilder withHash(@NonNull String hash) {
        this.anonymerEintrag.setHash(hash);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnonymerEintragBuilder withUntersuchung(@Nullable Untersuchung untersuchung) {
        this.anonymerEintrag.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    public static AnonymerEintrag create(@NonNull Consumer<AnonymerEintragBuilder> block) {
        AnonymerEintragBuilder builder = new AnonymerEintragBuilder();
        block.accept(builder);
        return builder.anonymerEintrag;
    }
}
