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

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseMapper;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.KlasseJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KlasseJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KlasseSaveJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class KlasseBuilder {

    public static final String KLASSENTYP_SEKE = "SEKE";
    public static final String KLASSENTYP_KG = "KG";
    public static final String KLASSENTYP_PRIM = "RK";

    private Klasse klasse;

    private KlasseBuilder() {
        this.klasse = defaultKlasse();
        TestUtil.setEntityInfo(klasse);
    }

    @NonNull
    public static Klasse defaultKlasse() {
        return new Klasse(
            null,
            SchuleBuilder.defaultSchule(),
            Collections.emptyList(),
            "Klasse 4c",
            Klassenstufe.PRIMAR_KLASSE_4,
            KlasseBuilder.KLASSENTYP_PRIM,
            EntityAktivStatus.AKTIV
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public KlasseBuilder forKlasse(@NonNull Klasse existingKlasse) {
        this.klasse = existingKlasse;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KlasseBuilder forSchule(@NonNull Schule schule) {
        this.klasse.setSchule(schule);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KlasseBuilder withID(@NonNull ID<Klasse> id) {
        this.klasse.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KlasseBuilder withBezeichnung(@NonNull String bezeichnung) {
        this.klasse.setBezeichnung(bezeichnung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KlasseBuilder withLehrperson(@NonNull Lehrperson lehrperson) {
        this.klasse.setLehrpersonen(List.of(lehrperson));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KlasseBuilder withLehrperson(@NonNull List<Lehrperson> lehrperson) {
        this.klasse.setLehrpersonen(lehrperson);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KlasseBuilder withKlassenstufe(@NonNull Klassenstufe klassenstufe) {
        this.klasse.setKlassenstufe(klassenstufe);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KlasseBuilder withKlasseSchulart(@NonNull String klasseSchulart) {
        this.klasse.setKlasseSchulart(klasseSchulart);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KlasseBuilder withExternalImportInfo(
        @NonNull String externalId,
        boolean aktiv,
        @NonNull OffsetDateTime now) {
        ExternalImportInfo importInfo = new ExternalImportInfo(externalId, aktiv, now, now);
        this.klasse.setExternalImportInfo(importInfo);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KlasseBuilder withEntityAktivStatus(@NonNull EntityAktivStatus entityAktivStatus) {
        this.klasse.setKlasseAktivStatus(entityAktivStatus);
        return this;
    }

    @NonNull
    public static Klasse create(@NonNull Consumer<KlasseBuilder> block) {
        KlasseBuilder builder = new KlasseBuilder();
        block.accept(builder);
        return builder.klasse;
    }

    @NonNull
    public static KlasseJson createJax(@NonNull Consumer<KlasseBuilder> block) {
        Klasse entity = create(block);
        return KlasseMapper.toJson(entity);
    }

    @NonNull
    public static KlasseJsonSpec createJaxSpec(@NonNull Consumer<KlasseBuilder> block) {
        KlasseJson json = createJax(block);
        return KlasseJsonSpecMapper.INSTANCE.klasseJsonToKlasseJsonSpec(json);
    }

    @NonNull
    public static KlasseSaveJsonSpec createSaveJaxSpec(@NonNull Consumer<KlasseBuilder> block) {
        KlasseJsonSpec entity = createJaxSpec(block);
        return entity.getData();
    }

}
