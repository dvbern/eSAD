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

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungKindDatenMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungKindDatenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungKindDatenSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungDatenJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungDatenKindSaveJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungKindDatenJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungKindDatenSaveJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class UntersuchungKindDatenBuilder {

    private UntersuchungKindDaten daten;

    private UntersuchungKindDatenBuilder() {
        this.daten = defaultUntersuchungDaten();
        TestUtil.setEntityInfo(daten);
    }

    @NonNull
    public static UntersuchungKindDaten defaultUntersuchungDaten() {
        return new UntersuchungKindDaten(
            "Gerber",
            "Emma",
            Geschlecht.WEIBLICH,
            LocalDate.of(2010, 1, 1),
            null,
            null,
            null,
            null,
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
    public UntersuchungKindDatenBuilder forKind(@NonNull UntersuchungKindDaten kind) {
        this.daten = kind;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungKindDatenBuilder withID(@NonNull ID<UntersuchungKindDaten> id) {
        this.daten.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungKindDatenBuilder withVorname(@NonNull String vorname) {
        this.daten.setVorname(vorname);
        return this;
    }

    @NonNull
    public static UntersuchungKindDaten create(@NonNull Consumer<UntersuchungKindDatenBuilder> block) {
        UntersuchungKindDatenBuilder builder = new UntersuchungKindDatenBuilder();
        block.accept(builder);
        return builder.daten;
    }

    @NonNull
    public static UntersuchungKindDatenJson createJax(
        @NonNull Consumer<UntersuchungKindDatenBuilder> block,
        @NonNull LocalDate now
    ) {
        UntersuchungKindDaten entity = create(block);
        Untersuchung untersuchung = new Untersuchung();
        untersuchung.setUntersuchungKindDaten(entity);
        return UntersuchungKindDatenMapper.toJson(untersuchung, now);
    }

    @NonNull
    public static UntersuchungKindDatenJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungKindDatenBuilder> block,
        @NonNull LocalDate now
    ) {
        UntersuchungKindDatenJson json = createJax(block, now);
        return UntersuchungDatenJsonSpecMapper.INSTANCE.untersuchungDatenJsonToUntersuchungDatenJsonSpec(json);
    }

    @NonNull
    public static UntersuchungKindDatenSaveJson createSaveJax(
        @NonNull Consumer<UntersuchungKindDatenBuilder> block
    ) {
        UntersuchungKindDaten entity = create(block);
        return new UntersuchungKindDatenSaveJson(
            entity.getName(),
            entity.getVorname(),
            entity.getGeschlecht(),
            entity.getGeburtsdatum(),
            entity.getNationalitaet(),
            entity.getSchulhaus(),
            entity.getQuartier(),
            entity.getSchulkreis(),
            entity.getSchulhausnummer(),
            entity.getKlassenstufe(),
            entity.getKlassentyp(),
            entity.getKlasse(),
            entity.getVersion()
        );
    }

    @NonNull
    public static UntersuchungKindDatenSaveJsonSpec createSaveJaxSpec(
        @NonNull Consumer<UntersuchungKindDatenBuilder> block
    ) {
        UntersuchungKindDatenSaveJson json = createSaveJax(block);
        return UntersuchungDatenKindSaveJsonSpecMapper.INSTANCE.untersuchungKindSaveJsonToUntersuchungKindDatenSaveJsonSpec(
            json);
    }
}
