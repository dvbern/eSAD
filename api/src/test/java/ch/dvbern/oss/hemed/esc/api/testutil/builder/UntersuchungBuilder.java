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

import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolle;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.signatur.UntersuchungSignatur;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class UntersuchungBuilder {

    private final Untersuchung untersuchung;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private UntersuchungBuilder() {
        this.untersuchung = defaultUntersuchung(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(untersuchung);
    }

    @NonNull
    public static Untersuchung defaultUntersuchung(@NonNull LocalDate now) {
        Untersuchung entity = new Untersuchung();
        entity.setEinladung(EinladungBuilder.defaultEinladung(now));
        return entity;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withID(@NonNull ID<Untersuchung> id) {
        this.untersuchung.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder forEinladung(@NonNull Einladung einladung) {
        this.untersuchung.setEinladung(einladung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withUntersuchungTriage(@NonNull UntersuchungTriage untersuchungTriage) {
        this.untersuchung.setUntersuchungTriage(untersuchungTriage);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withUntersuchungSehen(@NonNull UntersuchungSehenBeurteilung untersuchungSehenBeurteilung) {
        this.untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withUntersuchungHoerenAudiogramm(
        @NonNull UntersuchungHoerenAudiogramm untersuchungHoeren) {
        this.untersuchung.setUntersuchungHoerenAudiogramm(untersuchungHoeren);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withUntersuchungHoerenOtoskopie(
        @NonNull UntersuchungHoerenOtoskopie untersuchungHoeren) {
        this.untersuchung.setUntersuchungHoerenOtoskopie(untersuchungHoeren);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withUntersuchungGroesseGewicht(
        @NonNull UntersuchungGroesseGewicht untersuchungGroesseGewicht) {
        this.untersuchung.setUntersuchungGroesseGewicht(untersuchungGroesseGewicht);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withUntersuchungArztBeurteilung(@NonNull UntersuchungArztBeurteilung untersuchungArztBrt) {
        this.untersuchung.setUntersuchungArztBeurteilung(untersuchungArztBrt);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withUntersuchungLaeusekontrolle(@NonNull UntersuchungLaeusekontrolle untersuchungLaeusekontrolle) {
        this.untersuchung.setUntersuchungLaeusekontrolle(untersuchungLaeusekontrolle);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withUntersuchungArztBemerkung(@NonNull UntersuchungArztBemerkung uaBem) {
        this.untersuchung.setUntersuchungArztBemerkung(uaBem);
        return this;
    }


    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withUntersuchungMassnahmen(@NonNull UntersuchungMassnahmen untersuchungMassnahmen) {
        this.untersuchung.setUntersuchungMassnahmen(untersuchungMassnahmen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withDurchgefuehrt(@NonNull UntersuchungDurchfuehrung untersuchungDurchfuehrung) {
        this.untersuchung.setUntersuchungDurchfuehrung(untersuchungDurchfuehrung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withUntersuchungImpfungen(@NonNull UntersuchungImpfungen untersuchungImpfungen) {
        this.untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBuilder withUntersuchungSignatur(@NonNull UntersuchungSignatur untersuchungSignatur) {
        this.untersuchung.setUntersuchungSignatur(untersuchungSignatur);
        return this;
    }

    @NonNull
    public static Untersuchung create(@NonNull Consumer<UntersuchungBuilder> block) {
        UntersuchungBuilder builder = new UntersuchungBuilder();
        block.accept(builder);
        return builder.untersuchung;
    }
}
