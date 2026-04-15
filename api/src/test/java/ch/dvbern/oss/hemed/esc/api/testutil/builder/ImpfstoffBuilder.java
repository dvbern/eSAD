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

import java.util.List;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffMapper;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.ImpfstoffJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfstoffJsonSpec;
import lombok.RequiredArgsConstructor;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public class ImpfstoffBuilder {

    private final Impfstoff impfstoff;

    private ImpfstoffBuilder() {
        this.impfstoff = defaultImpfstoff();
        TestUtil.setEntityInfo(impfstoff);
    }

    @NonNull
    public static Impfstoff defaultImpfstoff() {
        Impfstoff priorix = new Impfstoff(
            TestUtil.getRandomString("Priorix"),
            "", // will be set later
            EntityAktivStatus.AKTIV
        );
        priorix.setKrankheiten(List.of(
            Krankheit.MASERN,
            Krankheit.MUMPS,
            Krankheit.ROETELN
        ));
        return priorix;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ImpfstoffBuilder withID(@NonNull ID<Impfstoff> id) {
        this.impfstoff.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ImpfstoffBuilder withName(@NonNull String name) {
        this.impfstoff.setName(name);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ImpfstoffBuilder forKrankheiten(@NonNull Krankheit... krankheiten) {
        this.impfstoff.setKrankheiten(List.of(krankheiten));
        return this;
    }

    @NonNull
    public static Impfstoff create(@NonNull Consumer<ImpfstoffBuilder> block) {
        ImpfstoffBuilder builder = new ImpfstoffBuilder();
        block.accept(builder);
        return builder.impfstoff;
    }

    @NonNull
    public static ImpfstoffJson createJax(@NonNull Consumer<ImpfstoffBuilder> block) {
        Impfstoff entity = create(block);
        return ImpfstoffMapper.toJson(entity);
    }

    @NonNull
    public static ImpfstoffJsonSpec createJaxSpec(@NonNull Consumer<ImpfstoffBuilder> block) {
        ImpfstoffJson json = createJax(block);
        return ImpfstoffJsonSpecMapper.INSTANCE.impfstoffJsonToImpfstoffJsonSpec(json);
    }
}
