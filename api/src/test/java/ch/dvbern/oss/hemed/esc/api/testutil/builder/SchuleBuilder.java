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
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleMapper;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.Quartier;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.SchuleJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchuleJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchuleSaveJsonSpec;
import lombok.RequiredArgsConstructor;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public class SchuleBuilder {

    private final Schule schule;

    private SchuleBuilder() {
        this.schule = defaultSchule();
        TestUtil.setEntityInfo(schule);
    }

    @NonNull
    public static Schule defaultSchule() {
        return new Schule(
            null,
            "Leonhard (SEK) Theodor",
            "Sekundarschule Leonhard",
            "Volksschulen",
            null,
            "123",
            AdresseBuilder.defaultAdresse(),
            EmailAddress.fromString("emma.gerber@mailbucket.dvbern.ch"),
            NormalizedPhoneNumber.parse("+41 79 123 45 67"),
            "Bemerkungen",
            null,
            null,
            EntityAktivStatus.AKTIV
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public SchuleBuilder withID(@NonNull ID<Schule> id) {
        this.schule.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public SchuleBuilder withSchulhaus(@NonNull String name) {
        this.schule.setSchulhaus(name);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public SchuleBuilder withSchulstandort(@NonNull String schulstandort) {
        this.schule.setSchulstandort(schulstandort);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public SchuleBuilder withQuartier(@NonNull Quartier quartier) {
        this.schule.setQuartier(quartier);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public SchuleBuilder withSchulhausnummer(@NonNull String schulhausnummer) {
        this.schule.setSchulhausnummer(schulhausnummer);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public SchuleBuilder withEmail(@NonNull EmailAddress emailAddress) {
        this.schule.setEmail(emailAddress);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public SchuleBuilder withZustaendigArzt(@NonNull Benutzer benutzer) {
        this.schule.setZustaendigArzt(benutzer);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public SchuleBuilder withZustaendigMPA(@NonNull Benutzer benutzer) {
        this.schule.setZustaendigMPA(benutzer);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public SchuleBuilder withExternalImportInfo(
        @NonNull String externalId,
        boolean aktiv,
        @NonNull OffsetDateTime now,
        @NonNull ImportTyp importTyp
    ) {
        ExternalImportInfo importInfo = new ExternalImportInfo(externalId, aktiv, now, now, importTyp);
        this.schule.setExternalImportInfo(importInfo);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public SchuleBuilder withEntityAktivStatus(@NonNull EntityAktivStatus entityAktivStatus) {
        this.schule.setSchuleAktivStatus(entityAktivStatus);
        return this;
    }

    @NonNull
    public static Schule create(@NonNull Consumer<SchuleBuilder> block) {
        SchuleBuilder builder = new SchuleBuilder();
        block.accept(builder);
        return builder.schule;
    }

    @NonNull
    public static SchuleJson createJax(@NonNull Consumer<SchuleBuilder> block) {
        Schule entity = create(block);
        return SchuleMapper.toJson(entity, Collections.emptyList());
    }

    @NonNull
    public static SchuleJsonSpec createJaxSpec(@NonNull Consumer<SchuleBuilder> block) {
        SchuleJson json = createJax(block);
        return SchuleJsonSpecMapper.INSTANCE.schuleJsonToSchuleJsonSpec(json);
    }

    @NonNull
    public static SchuleSaveJsonSpec createSaveJaxSpec(@NonNull Consumer<SchuleBuilder> block) {
        SchuleJsonSpec json = createJaxSpec(block);
        return json.getData();
    }

    @NonNull
    public static SchuleSaveJsonSpec createSaveJaxForExistingSchule(@NonNull Schule schule) {
        return SchuleJsonSpecMapper.INSTANCE.schuleJsonToSchuleJsonSpec(SchuleMapper.toJson(
            schule,
            Collections.emptyList()
        )).getData();
    }
}
