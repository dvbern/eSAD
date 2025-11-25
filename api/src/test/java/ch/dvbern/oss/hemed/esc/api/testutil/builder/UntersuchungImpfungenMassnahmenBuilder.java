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

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungImpfungenMassnahmenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungImpfungenMassnahmenJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenMassnahmenJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungImpfungenMassnahmenBuilder {

    private UntersuchungImpfungenMassnahmen massnahmen;

    private UntersuchungImpfungenMassnahmenBuilder() {
        this.massnahmen = defaultUntersuchungImpfungenMassnahmen();
        TestUtil.setEntityInfo(massnahmen);
    }

    @NonNull
    public static UntersuchungImpfungenMassnahmen defaultUntersuchungImpfungenMassnahmen() {
        return new UntersuchungImpfungenMassnahmen();
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenMassnahmenBuilder forUntersuchungImpfungen(@NonNull UntersuchungImpfungen impfungen) {
        this.massnahmen.setUntersuchungImpfungen(impfungen);
        if (impfungen.getUntersuchungImpfungenMassnahmen() != null) {
            this.massnahmen.setVersion(impfungen.getUntersuchungImpfungenMassnahmen().getVersion());
        } else {
            this.massnahmen.setVersion(0L);
        }
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenMassnahmenBuilder withID(@NonNull ID<UntersuchungImpfungenMassnahmen> id) {
        this.massnahmen.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenMassnahmenBuilder withInformationEltern(@NonNull InformationEltern info) {
        this.massnahmen.setInformationEltern(info);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenMassnahmenBuilder withInformationHausarzt(boolean info) {
        this.massnahmen.setInformationHausarzt(info);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenMassnahmenBuilder withRueckfrageHausarzt(boolean info) {
        this.massnahmen.setRueckfrageHausarzt(info);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenMassnahmenBuilder withAchtung(@Nullable String achtung) {
        this.massnahmen.setAchtung(achtung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenMassnahmenBuilder withBemerkungen(@NonNull String bemerkungen) {
        this.massnahmen.setBemerkungen(bemerkungen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenMassnahmenBuilder withVormerkenFuerGruppenFolgetermin(boolean vormerken) {
        this.massnahmen.setVormerkenFuerGruppenFolgetermin(vormerken);
        return this;
    }

    @NonNull
    public static UntersuchungImpfungenMassnahmen create(
        @NonNull Consumer<UntersuchungImpfungenMassnahmenBuilder> block) {
        UntersuchungImpfungenMassnahmenBuilder builder = new UntersuchungImpfungenMassnahmenBuilder();
        block.accept(builder);
        return builder.massnahmen;
    }

    @NonNull
    public static UntersuchungImpfungenMassnahmenJson createJax(
        @NonNull Consumer<UntersuchungImpfungenMassnahmenBuilder> block) {
        UntersuchungImpfungenMassnahmen entity = create(block);
        return UntersuchungImpfungenMassnahmenMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungImpfungenMassnahmenJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungImpfungenMassnahmenBuilder> block) {
        UntersuchungImpfungenMassnahmenJson json = createJax(block);
        return UntersuchungImpfungenMassnahmenJsonSpecMapper.INSTANCE.jsonToJsonSpec(json);
    }
}
