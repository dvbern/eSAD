package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.util.UUID;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelMapper;
import ch.dvbern.oss.hemed.esc.api.features.labels.json.LabelJson;
import ch.dvbern.oss.hemed.esc.api.features.labels.json.LabelSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedString;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.LabelJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelSaveJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class LabelBuilder {

    private Label label;

    private LabelBuilder() {
        this.label = defaultLabel();
        TestUtil.setEntityInfo(label);
    }

    @NonNull
    public static Label defaultLabel() {
        return new Label(
            new TranslatedString(
                TestUtil.getRandomString("Test Label"),
                TestUtil.getRandomString("Label de test")
            ),
            LabelTyp.NOTIZEN,
            false,
            false,
            true
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public LabelBuilder forLabel(@NonNull Label existingLabel) {
        this.label = existingLabel;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public LabelBuilder withID(@NonNull ID<Label> id) {
        this.label.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public LabelBuilder withRandomName(@NonNull String name) {
        this.label.getName().setNameDe(name + '_' + UUID.randomUUID());
        this.label.getName().setNameFr(label.getName().getNameDe());
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public LabelBuilder withName(@NonNull String name) {
        this.label.getName().setNameDe(name);
        this.label.getName().setNameFr(name);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public LabelBuilder withTyp(@NonNull LabelTyp typ) {
        this.label.setLabelTyp(typ);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public LabelBuilder isAutoLabel(@NonNull Boolean autoLabel) {
        this.label.setAutoLabel(autoLabel);
        return this;
    }

    @NonNull
    public static Label create(@NonNull Consumer<LabelBuilder> block) {
        LabelBuilder builder = new LabelBuilder();
        block.accept(builder);
        return builder.label;
    }

    @NonNull
    public static LabelJson createJax(@NonNull Consumer<LabelBuilder> block) {
        Label entity = create(block);
        return LabelMapper.toJson(entity);
    }

    @NonNull
    public static LabelJsonSpec createJaxSpec(@NonNull Consumer<LabelBuilder> block) {
        LabelJson json = createJax(block);
        return LabelJsonSpecMapper.INSTANCE.labelJsonToLabelJsonSpec(json);
    }

    @NonNull
    public static LabelSaveJson createSaveJax(@NonNull Consumer<LabelBuilder> block) {
        LabelJson json = createJax(block);
        return json.getData();
    }

    @NonNull
    public static LabelSaveJsonSpec createSaveJaxSpec(@NonNull Consumer<LabelBuilder> block) {
        LabelJsonSpec entity = createJaxSpec(block);
        return entity.getData();
    }
}
