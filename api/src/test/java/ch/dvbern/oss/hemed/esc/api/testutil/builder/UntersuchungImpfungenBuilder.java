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
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.Visum;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungImpfungenJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungImpfungenBuilder {

    private UntersuchungImpfungen impfungen;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);


    private UntersuchungImpfungenBuilder() {
        this.impfungen = defaultUntersuchungImpfungen(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(impfungen);
    }

    @NonNull
    public static UntersuchungImpfungen defaultUntersuchungImpfungen(@NonNull LocalDate now) {
        UntersuchungImpfungen entity = new UntersuchungImpfungen();
        entity.setVersion(1L); // Weil bei der Validierung erst ab Version 1 geprüft wird
        entity.setUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
        return entity;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder forUntersuchungImpfungen(@NonNull UntersuchungImpfungen existing) {
        this.impfungen = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.impfungen.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder withID(@NonNull ID<UntersuchungImpfungen> id) {
        this.impfungen.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder withImpfausweisUnvollstaendig(boolean unvollstaendig) {
        this.impfungen.setImpfausweisUnvollstaendig(unvollstaendig);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder withImpfausweisNichtVorhanden(boolean nichtVorhanden) {
        this.impfungen.setImpfausweisNichtVorhanden(nichtVorhanden);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder withImpfausweisZweiPlusEinsSchema(boolean zweiPlusEinsSchema) {
        this.impfungen.setImpfausweisZweiPlusEinsSchema(zweiPlusEinsSchema);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder withCheckEinwilligungVorhanden(boolean check) {
        this.impfungen.setCheckEinwilligungVorhanden(check);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder withCheckKontraindikationenAbgeklaert(boolean check) {
        this.impfungen.setCheckKontraindikationenAbgeklaert(check);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder withCheckDoppelkontrolleImpfentscheidDurchgefuehrt(boolean check) {
        this.impfungen.setCheckDoppelkontrolleImpfentscheidDurchgefuehrt(check);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder withBemerkungen(@Nullable String bemerkungen) {
        this.impfungen.setBemerkungen(bemerkungen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder withUntersuchungImpfungenMassnahmen(@Nullable
    UntersuchungImpfungenMassnahmen untersuchungImpfungenMassnahmen) {
        this.impfungen.setUntersuchungImpfungenMassnahmen(untersuchungImpfungenMassnahmen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder withVisum1(@Nullable Benutzer person, @Nullable LocalDate datumVisiert) {
        if (person == null || datumVisiert == null) {
            this.impfungen.setVisum1(null);
        } else {
            this.impfungen.setVisum1(new Visum(person, datumVisiert));
        }
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenBuilder withVisum2(@Nullable Benutzer person, @Nullable LocalDate datumVisiert) {
        if (person == null || datumVisiert == null) {
            this.impfungen.setVisum2(null);
        } else {
            this.impfungen.setVisum2(new Visum(person, datumVisiert));
        }
        return this;
    }

    @NonNull
    public static UntersuchungImpfungen create(@NonNull Consumer<UntersuchungImpfungenBuilder> block) {
        UntersuchungImpfungenBuilder builder = new UntersuchungImpfungenBuilder();
        block.accept(builder);
        return builder.impfungen;
    }

    @NonNull
    public static UntersuchungImpfungenJson createJax(@NonNull Consumer<UntersuchungImpfungenBuilder> block) {
        UntersuchungImpfungen entity = create(block);
        return UntersuchungImpfungenMapper.toJson(entity);
    }


    @NonNull
    public static UntersuchungImpfungenJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungImpfungenBuilder> block) {
        UntersuchungImpfungenJson json = createJax(block);
        return UntersuchungImpfungenJsonSpecMapper.INSTANCE.untersuchungImpfungenJsonToUntersuchungImpfungenJsonSpec(json);
    }
}
