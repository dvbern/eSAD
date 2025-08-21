/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
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
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungDurchfuehrungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungDurchfuehrungJson;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungDurchfuehrungJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungDurchfuehrungJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungDurchfuehrungBuilder {

    private UntersuchungDurchfuehrung durchfuehrung;

    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private UntersuchungDurchfuehrungBuilder() {
        this.durchfuehrung = defaultUntersuchungDurchfuehrung(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(durchfuehrung);
    }

    @NonNull
    public static UntersuchungDurchfuehrung defaultUntersuchungDurchfuehrung(@NonNull LocalDate now) {
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(now);
        return UntersuchungDurchfuehrung.forUntersuchung(untersuchung);
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungDurchfuehrungBuilder forUntersuchungDurchfuehrung(@NonNull UntersuchungDurchfuehrung existing) {
        this.durchfuehrung = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungDurchfuehrungBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.durchfuehrung.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungDurchfuehrungBuilder withID(@NonNull ID<UntersuchungDurchfuehrung> id) {
        this.durchfuehrung.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungDurchfuehrungBuilder withArzt(@Nullable Benutzer benutzer) {
        this.durchfuehrung.setArzt(benutzer);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungDurchfuehrungBuilder withMpa(@Nullable Benutzer benutzer) {
        this.durchfuehrung.setMpa(benutzer);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungDurchfuehrungBuilder isNachzuegler(boolean isNachzuegler) {
        this.durchfuehrung.setNachzuegler(isNachzuegler);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungDurchfuehrungBuilder isNeueintritt(boolean isNeueintritt) {
        this.durchfuehrung.setNeueintritt(isNeueintritt);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungDurchfuehrungBuilder isNachkontrolle(boolean isNachkontrolle) {
        this.durchfuehrung.setNachkontrolle(isNachkontrolle);
        return this;
    }

    @NonNull
    public static UntersuchungDurchfuehrung create(@NonNull Consumer<UntersuchungDurchfuehrungBuilder> block) {
        UntersuchungDurchfuehrungBuilder builder = new UntersuchungDurchfuehrungBuilder();
        block.accept(builder);
        return builder.durchfuehrung;
    }

    @NonNull
    public static UntersuchungDurchfuehrungJson createJax(@NonNull Consumer<UntersuchungDurchfuehrungBuilder> block) {
        UntersuchungDurchfuehrung entity = create(block);
        return UntersuchungDurchfuehrungMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungDurchfuehrungJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungDurchfuehrungBuilder> block) {
        UntersuchungDurchfuehrungJson json = createJax(block);
        return UntersuchungDurchfuehrungJsonSpecMapper.INSTANCE.untersuchungDurchfuehrungJsonToUntersuchungDurchfuehrungJsonSpec(
            json);
    }
}
