/*
 *
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.time.Month;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassArt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateTimeRange;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.AnlassJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AnlassBuilder {

    private Anlass anlass;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private AnlassBuilder() {
        this.anlass = defaultAnlass(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(anlass);
    }

    @NonNull
    public static Anlass defaultAnlass(
        @NonNull LocalDate now
    ) {
        LocalDate anlassDate = now.plusMonths(1);
        int schuljahr = anlassDate.isAfter(LocalDate.of(anlassDate.getYear(), Month.AUGUST, 1))
            ? anlassDate.getYear() : anlassDate.getYear() - 1;
        return new Anlass(
            AnlassConfigBuilder.defaultAnlassConfig(),
            TestUtil.getRandomString("Test-Anlass"),
            AnlassStatus.IN_PLANUNG,
            anlassDate,
            schuljahr,
            null,
            AnlassArt.GRUPPEN_ANLASS,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder forAnlass(@NonNull Anlass initAnlass) {
        this.anlass = initAnlass;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder withID(@NonNull ID<Anlass> id) {
        this.anlass.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder withTitle(@Nullable String title) {
        this.anlass.setTitel(title);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder withAnlassStatus(@NonNull AnlassStatus status) {
        this.anlass.setAnlassStatus(status);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder withAnlassTyp(@NonNull AnlassTyp anlassTyp) {
        this.anlass.getAnlassConfig().setAnlassTyp(anlassTyp);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder withSchuljahr(int schuljahr) {
        this.anlass.setSchuljahr(schuljahr);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder withDatumSetSchuljahr(@NonNull LocalDate datum) {
        int schuljahr = datum.isAfter(LocalDate.of(datum.getYear(), Month.AUGUST, 1))
            ? datum.getYear() : datum.getYear() - 1;
        this.anlass.setDatum(datum);
        this.anlass.setSchuljahr(schuljahr);
        this.anlass.getAnlassConfig().setGueltigkeitsZeitraum(DateRange.ofYearContaining(datum));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder withArzt(@Nullable Benutzer arzt) {
        this.anlass.setArzt(arzt);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder withMPA(@Nullable Benutzer mpa) {
        this.anlass.setMpa(mpa);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder withWeiterePerson(@NonNull Benutzer weiterePerson) {
        this.anlass.setWeiterePerson(weiterePerson);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder withAnlassConfig(@NonNull AnlassConfig config) {
        this.anlass.setAnlassConfig(config);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder withZeitVonBis(@NonNull DateTimeRange dateTimeRange) {
        this.anlass.setZeitVonBis(dateTimeRange);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassBuilder withAnlassArt(@NonNull AnlassArt anlassArt) {
        this.anlass.setAnlassArt(anlassArt);
        return this;
    }

    @NonNull
    public static Anlass create(@NonNull Consumer<AnlassBuilder> block) {
        AnlassBuilder builder = new AnlassBuilder();
        block.accept(builder);
        return builder.anlass;
    }

    @NonNull
    public static AnlassJson createJax(@NonNull Consumer<AnlassBuilder> block) {
        Anlass entity = create(block);
        return AnlassMapper.toJson(entity, TenantEnum.BS);
    }

    @NonNull
    public static AnlassJsonSpec createJaxSpec(@NonNull Consumer<AnlassBuilder> block) {
        AnlassJson json = createJax(block);
        return AnlassJsonSpecMapper.INSTANCE.anlassJsonToAnlassJsonSpec(json);
    }
}
