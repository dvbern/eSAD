package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.Briefvorlage;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageType;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BriefvorlageBuilder {
    private final Briefvorlage briefvorlage;

    private BriefvorlageBuilder() {
        this.briefvorlage = defaultBriefvorlage();
        TestUtil.setEntityInfo(briefvorlage);
    }

    @NonNull
    public static Briefvorlage defaultBriefvorlage() {
        return new Briefvorlage(
            TestUtil.getRandomString("Briefvorlage"),
            BriefvorlageType.KIND,
            AppLanguages.DE,
            FileDataBuilder.defaultWordFileData()
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public BriefvorlageBuilder withId(@NonNull ID<Briefvorlage> id) {
        this.briefvorlage.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BriefvorlageBuilder withName(@NonNull String name) {
        this.briefvorlage.setName(name);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BriefvorlageBuilder withType(@NonNull BriefvorlageType type) {
        this.briefvorlage.setType(type);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public BriefvorlageBuilder withFile(@NonNull FileData fileData) {
        this.briefvorlage.setFile(fileData);
        return this;
    }

    @NonNull
    public static Briefvorlage create(@NonNull Consumer<BriefvorlageBuilder> block) {
        BriefvorlageBuilder builder = new BriefvorlageBuilder();
        block.accept(builder);
        return builder.briefvorlage;
    }
}
