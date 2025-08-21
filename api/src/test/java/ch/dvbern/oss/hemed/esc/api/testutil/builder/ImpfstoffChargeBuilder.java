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

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffChargeJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffChargeMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.ImpfstoffChargeJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfstoffChargeJsonSpec;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public class ImpfstoffChargeBuilder {

    private final ImpfstoffCharge charge;


	private ImpfstoffChargeBuilder() {
        this.charge = defaultImpfstoffCharge();
        TestUtil.setEntityInfo(charge);
    }

    @NonNull
    public static ImpfstoffCharge defaultImpfstoffCharge()  {
        return new ImpfstoffCharge(
            ImpfstoffBuilder.defaultImpfstoff(),
            TestUtil.getRandomString("123456"),
            true
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public ImpfstoffChargeBuilder withID(@NonNull ID<ImpfstoffCharge> id) {
        this.charge.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ImpfstoffChargeBuilder withName(@NonNull String name) {
        this.charge.setName(name);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ImpfstoffChargeBuilder forImpfstoff(@NonNull Impfstoff impfstoff) {
        this.charge.setImpfstoff(impfstoff);
        return this;
    }

    @NonNull
    public static ImpfstoffCharge create(@NonNull Consumer<ImpfstoffChargeBuilder> block) {
        ImpfstoffChargeBuilder builder = new ImpfstoffChargeBuilder();
        block.accept(builder);
        return builder.charge;
    }

	@NonNull
	public static ImpfstoffChargeJson createJax(@NonNull Consumer<ImpfstoffChargeBuilder> block) {
        ImpfstoffCharge entity = create(block);
        return ImpfstoffChargeMapper.toJson(entity);
	}

	@NonNull
	public static ImpfstoffChargeJsonSpec createJaxSpec(@NonNull Consumer<ImpfstoffChargeBuilder> block) {
        ImpfstoffChargeJson json = createJax(block);
        return ImpfstoffChargeJsonSpecMapper.INSTANCE.impfstoffChargeJsonToImpfstoffChargeJsonSpec(json);
	}
}
