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
import java.time.OffsetDateTime;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungKrankheitImmunitaet;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungInternJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungImpfungInternJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungInternJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungImpfungInternBuilder {

    private UntersuchungImpfungIntern impfung;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private UntersuchungImpfungInternBuilder() {
        this.impfung = defaultUntersuchungImpfungIntern(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(impfung);
    }

    @NonNull
    public static UntersuchungImpfungIntern defaultUntersuchungImpfungIntern(@NonNull LocalDate now) {
        UntersuchungImpfungIntern entity = new UntersuchungImpfungIntern();
        entity.setImpfstoff(ImpfstoffBuilder.defaultImpfstoff());
        entity.setUntersuchungImpfungen(UntersuchungImpfungenBuilder.defaultUntersuchungImpfungen(now));
        entity.setTimestampErstellt(OffsetDateTime.now());
        entity.setAnlassImpfungDurchgefuehrt(null);
        return entity;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder forImpfungIntern(@NonNull UntersuchungImpfungIntern existingImpfung) {
        this.impfung = existingImpfung;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder withID(@NonNull ID<UntersuchungImpfungIntern> id) {
        this.impfung.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder forUntersuchungImpfungen(
        @NonNull UntersuchungImpfungen untersuchungImpfungen
    ) {
        this.impfung.setUntersuchungImpfungen(untersuchungImpfungen);
        this.impfung.setAnlassImpfungErfasst(untersuchungImpfungen.getUntersuchung().getEinladung().getAnlass());
        this.impfung.setAnlassImpfungDurchgefuehrt(untersuchungImpfungen.getUntersuchung().getEinladung().getAnlass());
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder withImpfstoff(@NonNull Impfstoff impfstoff) {
        this.impfung.setImpfstoff(impfstoff);
        for (Krankheit krankheit : impfstoff.getKrankheiten()) {
            UntersuchungImpfungKrankheitImmunitaet immunitaet =
                new UntersuchungImpfungKrankheitImmunitaet(this.impfung, krankheit, false);
            this.impfung.getKrankheitenImmunitaeten().add(immunitaet);
        }
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder withTimestampLastManualChange(@Nullable OffsetDateTime timestampLastManualChange) {
        this.impfung.setTimestampLastManualChange(timestampLastManualChange);
        return this;
    }


    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder withImmunitaetForKrankheit(
        @NonNull Krankheit krankheit,
        boolean immunitaetGenuegend) {
        // iterate over the immunities and set the immunity for the given disease
        this.impfung.getKrankheitenImmunitaeten()
            .stream()
            .filter(immunitaet -> immunitaet.getKrankheit() == krankheit)
            .forEach(immunitaet -> immunitaet.setImmunitaetGenuegend(immunitaetGenuegend));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder withCharge(@Nullable ImpfstoffCharge charge) {
        this.impfung.setCharge(charge);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder withDatumImpfung(@Nullable LocalDate datumDurchgefuehrt) {
        this.impfung.setDatumImpfung(datumDurchgefuehrt);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder withUserErfasst(@NonNull Benutzer user) {
        this.impfung.setUserErfasst(user);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder withUserDurchgefuehrt(@NonNull Benutzer user) {
        this.impfung.setUserDurchgefuehrt(user);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder withImpfStatus(@NonNull ImpfStatus impfStatus) {
        this.impfung.setImpfStatus(impfStatus);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder withAnlassImpfungErfasst(@NonNull Anlass anlass) {
        this.impfung.setAnlassImpfungErfasst(anlass);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungInternBuilder withAnlassImpfungDurchgefuehrt(@NonNull Anlass anlass) {
        this.impfung.setAnlassImpfungDurchgefuehrt(anlass);
        return this;
    }

    @NonNull
    public static UntersuchungImpfungIntern create(@NonNull Consumer<UntersuchungImpfungInternBuilder> block) {
        UntersuchungImpfungInternBuilder builder = new UntersuchungImpfungInternBuilder();
        block.accept(builder);
        return builder.impfung;
    }

    @NonNull
    public static UntersuchungImpfungInternJson createJax(@NonNull Consumer<UntersuchungImpfungInternBuilder> block) {
        UntersuchungImpfungIntern entity = create(block);
        return UntersuchungImpfungInternMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungImpfungInternJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungImpfungInternBuilder> block) {
        UntersuchungImpfungInternJson json = createJax(block);
        return UntersuchungImpfungInternJsonSpecMapper.INSTANCE.untersuchungImpfungInternJsonToUntersuchungImpfungInternJsonSpec(
            json);
    }
}
