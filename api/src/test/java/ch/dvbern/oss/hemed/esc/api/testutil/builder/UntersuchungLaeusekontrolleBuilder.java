package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.types.LaeusekontrolleBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolle;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.json.UntersuchungLaeusekontrolleJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolleMapper;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungLaeusekontrolleJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungLaeusekontrolleJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungLaeusekontrolleBuilder {

    private UntersuchungLaeusekontrolle laeusekontrolle;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private UntersuchungLaeusekontrolleBuilder() {
        this.laeusekontrolle = defaultUntersuchungLaeusekontrolle(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(laeusekontrolle);
    }

    @NonNull
    public static UntersuchungLaeusekontrolle defaultUntersuchungLaeusekontrolle(@NonNull LocalDate now) {
        return UntersuchungLaeusekontrolle.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungLaeusekontrolleBuilder forUntersuchungLaeusekontrolle(@NonNull UntersuchungLaeusekontrolle existing) {
        this.laeusekontrolle = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungLaeusekontrolleBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.laeusekontrolle.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungLaeusekontrolleBuilder withBeurteilung(
        @Nullable Collection<LaeusekontrolleBeurteilung> beurteilungList
    ) {
        this.laeusekontrolle.setBeurteilung(EnumSetUtil.toEnumSet(beurteilungList, LaeusekontrolleBeurteilung.class));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungLaeusekontrolleBuilder withBemerkung(@Nullable String bemerkung) {
        this.laeusekontrolle.setBemerkungen(bemerkung);
        return this;
    }

    @NonNull
    public static UntersuchungLaeusekontrolle create(@NonNull Consumer<UntersuchungLaeusekontrolleBuilder> block) {
        UntersuchungLaeusekontrolleBuilder builder = new UntersuchungLaeusekontrolleBuilder();
        block.accept(builder);
        return builder.laeusekontrolle;
    }

    @NonNull
    public static UntersuchungLaeusekontrolleJson createJax(
        @NonNull Consumer<UntersuchungLaeusekontrolleBuilder> block) {
        UntersuchungLaeusekontrolle entity = create(block);
        return UntersuchungLaeusekontrolleMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungLaeusekontrolleJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungLaeusekontrolleBuilder> block) {
        UntersuchungLaeusekontrolleJson json = createJax(block);
        return UntersuchungLaeusekontrolleJsonSpecMapper.INSTANCE.toUntersuchungLaeusekontrolleJsonSpec(json);
    }
}
