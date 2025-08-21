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

import java.util.UUID;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.Infomaterial;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.InfomaterialMapper;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.json.InfomaterialJson;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedString;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.InfomaterialJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InfomaterialJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InfomaterialSaveJsonSpec;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public class InfomaterialBuilder {

    private final Infomaterial infomaterial;

    private InfomaterialBuilder() {
        this.infomaterial = defaultInfomaterial();
        TestUtil.setEntityInfo(infomaterial);
    }

    @NonNull
    public static Infomaterial defaultInfomaterial() {
        return new Infomaterial(
            new TranslatedString(
                "text_de",
                "text_fr"),
            true);
    }

    @NonNull
    @CanIgnoreReturnValue
    public InfomaterialBuilder withID(@NonNull ID<Infomaterial> id) {
        this.infomaterial.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public InfomaterialBuilder isActive(boolean isActive) {
        this.infomaterial.setAktiv(isActive);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public InfomaterialBuilder withRandomName(@NonNull String name) {
        this.infomaterial.getName().setNameDe(name + '_' + UUID.randomUUID());
        this.infomaterial.getName().setNameFr(infomaterial.getName().getNameDe());
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public InfomaterialBuilder withName(@NonNull String name) {
        this.infomaterial.getName().setNameDe(name);
        this.infomaterial.getName().setNameFr(name);
        return this;
    }

    @NonNull
    public static Infomaterial create(@NonNull Consumer<InfomaterialBuilder> block) {
        InfomaterialBuilder builder = new InfomaterialBuilder();
        block.accept(builder);
        return builder.infomaterial;
    }

    @NonNull
    public static InfomaterialJson createJax(@NonNull Consumer<InfomaterialBuilder> block) {
        Infomaterial entity = create(block);
        return InfomaterialMapper.toJson(entity);
    }

    @NonNull
    public static InfomaterialJsonSpec createJaxSpec(@NonNull Consumer<InfomaterialBuilder> block) {
        InfomaterialJson json = createJax(block);
        return InfomaterialJsonSpecMapper.INSTANCE.infomaterialJsonToInfomaterialJsonSpec(json);
    }

    @NonNull
    public static InfomaterialSaveJsonSpec createSaveJaxSpec(
        @NonNull Consumer<InfomaterialBuilder> block
    ) {
        InfomaterialJsonSpec json = createJaxSpec(block);
        return json.getData();
    }
}
