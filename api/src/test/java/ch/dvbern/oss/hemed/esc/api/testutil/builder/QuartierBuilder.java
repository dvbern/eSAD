package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.Quartier;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public class QuartierBuilder {

    private final Quartier quartier;

    private QuartierBuilder() {
        this.quartier = defaultQuartier();
        TestUtil.setEntityInfo(quartier);
    }

    public static Quartier defaultQuartier() {
        return new Quartier("GBW");
    }

    @NonNull
    @CanIgnoreReturnValue
    public QuartierBuilder withID(@NonNull ID<Quartier> id) {
        this.quartier.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public QuartierBuilder withQuartier(@NonNull String quartierName) {
        this.quartier.setQuartierName(quartierName);
        return this;
    }

    @NonNull
    public static Quartier create(@NonNull Consumer<QuartierBuilder> block) {
        QuartierBuilder builder = new QuartierBuilder();
        block.accept(builder);
        return builder.quartier;
    }
}
