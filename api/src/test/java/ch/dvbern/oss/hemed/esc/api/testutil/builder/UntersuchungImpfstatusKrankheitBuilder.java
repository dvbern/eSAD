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

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfstatusKrankheitSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungImpfstatusKrankheitSaveJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfstatusKrankheitSaveJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungImpfstatusKrankheitBuilder {

    private final UntersuchungImpfstatusKrankheit impfstatus;

    private UntersuchungImpfstatusKrankheitBuilder() {
        this.impfstatus = defaultUntersuchungImpfungen();
        TestUtil.setEntityInfo(impfstatus);
    }

    @NonNull
    public static UntersuchungImpfstatusKrankheit defaultUntersuchungImpfungen() {
        UntersuchungImpfstatusKrankheit entity = new UntersuchungImpfstatusKrankheit();
        entity.setKrankheit(Krankheit.MASERN);
        return entity;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfstatusKrankheitBuilder withID(@NonNull ID<UntersuchungImpfstatusKrankheit> id) {
        this.impfstatus.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfstatusKrankheitBuilder forUntersuchungImpfungen(
        @NonNull UntersuchungImpfungen untersuchungImpfungen) {
        this.impfstatus.setUntersuchungImpfungen(untersuchungImpfungen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfstatusKrankheitBuilder forKrankheit(@NonNull Krankheit krankheit) {
        this.impfstatus.setKrankheit(krankheit);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfstatusKrankheitBuilder withAnzahlImpfungen(@Nullable Integer anzahlImpfungen) {
        this.impfstatus.setAnzahlImpfungen(anzahlImpfungen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfstatusKrankheitBuilder withDatumLetzteImpfung(@Nullable LocalDate date) {
        this.impfstatus.setDatumLetzteImpfung(date);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfstatusKrankheitBuilder withTimestampLastManualChange(@Nullable OffsetDateTime timestamp) {
        this.impfstatus.setTimestampLastManualChange(timestamp);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfstatusKrankheitBuilder withTimestampMutiert(@NonNull OffsetDateTime date) {
        this.impfstatus.setTimestampMutiert(date);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfstatusKrankheitBuilder withImpfschutzGegeben(boolean impfschutzGegeben) {
        this.impfstatus.setImpfschutzGegeben(impfschutzGegeben);
        return this;
    }

    @NonNull
    public static UntersuchungImpfstatusKrankheit create(
        @NonNull Consumer<UntersuchungImpfstatusKrankheitBuilder> block) {
        UntersuchungImpfstatusKrankheitBuilder builder = new UntersuchungImpfstatusKrankheitBuilder();
        block.accept(builder);
        return builder.impfstatus;
    }

    @NonNull
    public static UntersuchungImpfstatusKrankheitSaveJson createSaveJax(
        @NonNull Consumer<UntersuchungImpfstatusKrankheitBuilder> block) {
        UntersuchungImpfstatusKrankheit entity = create(block);
        return UntersuchungImpfstatusKrankheitMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungImpfstatusKrankheitSaveJsonSpec createSaveJaxSpec(
        @NonNull Consumer<UntersuchungImpfstatusKrankheitBuilder> block) {
        UntersuchungImpfstatusKrankheitSaveJson json = createSaveJax(block);
        return UntersuchungImpfstatusKrankheitSaveJsonSpecMapper.INSTANCE.untersuchungImpfstatusJsonToUntersuchungImpfstatusJsonSpec(
            json);
    }
}
