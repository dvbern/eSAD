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
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungTriageMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungTriageJson;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungTriageJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungTriageJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class UntersuchungTriageBuilder {

    private UntersuchungTriage triage;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);


    private UntersuchungTriageBuilder() {
        this.triage = defaultUntersuchungTriage(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(triage);
    }

    @NonNull
    public static UntersuchungTriage defaultUntersuchungTriage(@NonNull LocalDate now) {
        return UntersuchungTriage.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder forUntersuchungTriage(@NonNull UntersuchungTriage existing) {
        this.triage = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.triage.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder withID(@NonNull ID<UntersuchungTriage> id) {
        this.triage.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder withTriageSu(@NonNull Boolean triage) {
        this.triage.setTriageSu(triage);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder withTriageSu(@NonNull Boolean triage, @NonNull String bemerkungen) {
        this.triage.setTriageSu(triage);
        this.triage.setTriageSuBemerkungen(bemerkungen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder withTriageMpa(@NonNull Boolean triage) {
        this.triage.setTriageMpa(triage);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder withTriageMpa(@NonNull Boolean triage, @NonNull String bemerkungen) {
        this.triage.setTriageMpa(triage);
        this.triage.setTriageMpaBemerkungen(bemerkungen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder withBefundSu(@NonNull Boolean triage) {
        this.triage.setBefundSu(triage);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder withBefundSu(@NonNull Boolean triage, @NonNull String bemerkungen) {
        this.triage.setBefundSu(triage);
        this.triage.setBefundSuBemerkungen(bemerkungen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder withWunschSchueler(@NonNull Boolean wunschSchueler) {
        this.triage.setWunschSchueler(wunschSchueler);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder withWunschSchueler(@NonNull Boolean triage, @NonNull String bemerkungen) {
        this.triage.setWunschSchueler(triage);
        this.triage.setWunschSchuelerBemerkungen(bemerkungen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder withVorinformationLehrperson(@NonNull Boolean info) {
        this.triage.setVorinformationLehrperson(info);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungTriageBuilder withVorinformationLehrperson(@NonNull Boolean triage, @NonNull String bemerkungen) {
        this.triage.setVorinformationLehrperson(triage);
        this.triage.setVorinformationLehrpersonBemerkungen(bemerkungen);
        return this;
    }

    @NonNull
    public static UntersuchungTriage create(@NonNull Consumer<UntersuchungTriageBuilder> block) {
        UntersuchungTriageBuilder builder = new UntersuchungTriageBuilder();
        block.accept(builder);
        return builder.triage;
    }

    @NonNull
    public static UntersuchungTriageJson createJax(@NonNull Consumer<UntersuchungTriageBuilder> block) {
        UntersuchungTriage entity = create(block);
        return UntersuchungTriageMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungTriageJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungTriageBuilder> block) {
        UntersuchungTriageJson json = createJax(block);
        return UntersuchungTriageJsonSpecMapper.INSTANCE.untersuchungTriageJsonToUntersuchungTriageJsonSpec(json);
    }
}
