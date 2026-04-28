package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.signatur.UntersuchungSignatur;
import ch.dvbern.oss.hemed.esc.api.features.anlass.signatur.UntersuchungSignaturMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.signatur.json.UntersuchungSignaturJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungSignaturJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSignaturJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;

public class UntersuchungSignaturBuilder {

    private UntersuchungSignatur untersuchungSignatur;

    private UntersuchungSignaturBuilder() {
        FixedClockProvider clockProvider = FixedClockProvider.now();
        AppClock appClock = new AppClock(clockProvider);
        this.untersuchungSignatur = defaultUntersuchungSignatur(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(untersuchungSignatur);
    }

    @NonNull
    public static UntersuchungSignatur defaultUntersuchungSignatur(@NonNull LocalDate now) {
        return UntersuchungSignatur.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSignaturBuilder forUntersuchungSignatur(@NonNull UntersuchungSignatur existing) {
        this.untersuchungSignatur = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSignaturBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.untersuchungSignatur.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSignaturBuilder withSignatur(@Nullable Benutzer person, @Nullable LocalDateTime timestampVisiert) {
        this.untersuchungSignatur.setPerson(person);
        this.untersuchungSignatur.setTimestampVisiert(timestampVisiert);
        return this;
    }

    @NonNull
    public static UntersuchungSignatur create(@NonNull Consumer<UntersuchungSignaturBuilder> block) {
        UntersuchungSignaturBuilder builder = new UntersuchungSignaturBuilder();
        block.accept(builder);
        return builder.untersuchungSignatur;
    }

    @NonNull
    public static UntersuchungSignaturJson createJax(@NonNull Consumer<UntersuchungSignaturBuilder> block) {
        UntersuchungSignatur entity = create(block);
        return UntersuchungSignaturMapper.toJson(entity, TenantEnum.BS);
    }

    @NonNull
    public static UntersuchungSignaturJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungSignaturBuilder> block) {
        UntersuchungSignaturJson json = createJax(block);
        return UntersuchungSignaturJsonSpecMapper.INSTANCE.jsonToJsonSpec(json);
    }
}
