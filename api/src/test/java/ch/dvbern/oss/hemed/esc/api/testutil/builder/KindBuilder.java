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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindMapper;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.util.HashValueGeneratorUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.KindJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KindBuilder {

    private Kind kind;

    private KindBuilder() {
        this.kind = defaultKind();
    }

    @NonNull
    public static Kind defaultKind() {
        String name = "Gerber";
        String vorname = "Emma";
        Geschlecht geschlecht = Geschlecht.WEIBLICH;
        LocalDate geburtsdatum = LocalDate.of(2010, 1, 1);
        Kind kind = new Kind(
            null,
            null,
            HashValueGeneratorUtil.generateHashValueForKindValues(
                name, vorname, geschlecht, geburtsdatum
            ),
            null,
            name,
            vorname,
            "gerber",
            "emma",
            geschlecht,
            geburtsdatum,
            Land.SCHWEIZ,
            "Deutsch/Chinesisch",
            "Eltern",
            AdresseBuilder.defaultAdresse(),
            EmailAddress.fromString("emma.gerber@mailbucket.dvbern.ch"),
            NormalizedPhoneNumber.parse("+41 79 123 45 67"),
            null,
            null,
            EntityAktivStatus.AKTIV
        );
        TestUtil.setEntityInfo(kind);
        return kind;
    }

    @NonNull
    public static Kind defaultKindWithNullValues() {
        Kind kind = new Kind(
            null,
            null,
            "dummyHash",
            null,
            "Gerber",
            "Emma",
            "gerber",
            "emma",
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
            EntityAktivStatus.AKTIV
        );
        TestUtil.setEntityInfo(kind);
        return kind;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder forKind(@NonNull Kind existingKind) {
        this.kind = existingKind;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withID(@NonNull ID<Kind> id) {
        this.kind.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withEsadId(@NonNull String esadId) {
        this.kind.setEsadId(esadId);
        return this;
    }



    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withName(@NonNull String name, @NonNull String vorname) {
        this.kind.setName(name);
        this.kind.setVorname(vorname);

        String hashValue = HashValueGeneratorUtil.generateHashValueForKindValues(
            name,
            vorname,
            this.kind.getGeschlecht(),
            this.kind.getGeburtsdatum()
        );

        this.kind.setHashValue(hashValue);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withUniqueName(@NonNull String name, @NonNull String vorname) {
        this.kind.setName(TestUtil.getRandomString(name));
        this.kind.setVorname(TestUtil.getRandomString(vorname));

        String hashValue = HashValueGeneratorUtil.generateHashValueForKindValues(
            this.kind.getName(),
            this.kind.getVorname(),
            this.kind.getGeschlecht(),
            this.kind.getGeburtsdatum()
        );

        this.kind.setHashValue(hashValue);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withSozialversicherungsNummer(@Nullable SozialversicherungsNummer sozNummer) {
        this.kind.setSozialversicherungsNummer(sozNummer);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withSchule(@Nullable Schule schule) {
        this.kind.setSchule(schule);

        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withKlasse(@Nullable Klasse klasse) {
        this.kind.setKlasse(klasse);
        if (klasse != null) {
            this.kind.setSchule(klasse.getSchule());
        }
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withWohntyp(@NonNull String wohntyp) {
        this.kind.setWohntyp(wohntyp);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withGeburtsdatum(@NonNull LocalDate geburtsdatum) {
        this.kind.setGeburtsdatum(geburtsdatum);

        String hashValue = HashValueGeneratorUtil.generateHashValueForKindValues(
            this.kind.getName(),
            this.kind.getVorname(),
            this.kind.getGeschlecht(),
            geburtsdatum
        );

        this.kind.setHashValue(hashValue);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withGeschlecht(@NonNull Geschlecht geschlecht) {
        this.kind.setGeschlecht(geschlecht);
        String hashValue = HashValueGeneratorUtil.generateHashValueForKindValues(
            this.kind.getName(),
            this.kind.getVorname(),
            geschlecht,
            this.kind.getGeburtsdatum()
        );

        this.kind.setHashValue(hashValue);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withExternalImportInfo(
        @NonNull String externalId,
        boolean aktiv,
        @NonNull OffsetDateTime now,
        @NonNull ImportTyp importTyp
    ) {
        return withExternalImportInfo(externalId, aktiv, now, now, importTyp);
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withExternalImportInfo(
        @NonNull String externalId,
        boolean aktiv,
        @NonNull OffsetDateTime now,
        @Nullable OffsetDateTime lastImport,
        @NonNull ImportTyp importTyp
    ) {
        ExternalImportInfo importInfo = new ExternalImportInfo(externalId, aktiv, now, lastImport, importTyp);
        this.kind.setExternalImportInfo(importInfo);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KindBuilder withKindAktivStatus(EntityAktivStatus status) {
        this.kind.setKindAktivStatus(status);
        return this;
    }

    @NonNull
    public static Kind create(@NonNull Consumer<KindBuilder> block) {
        KindBuilder builder = new KindBuilder();
        block.accept(builder);
        return builder.kind;
    }

    @NonNull
    public static KindJson createJax(
        @NonNull Consumer<KindBuilder> block,
        @NonNull LocalDate now) {
        Kind entity = create(block);
        return KindMapper.toJson(entity, now);
    }

    @NonNull
    public static KindJsonSpec createJaxSpec(
        @NonNull Consumer<KindBuilder> block,
        @NonNull LocalDate now) {
        KindJson json = createJax(block, now);
        return KindJsonSpecMapper.INSTANCE.kindJsonToKindJsonSpec(json);
    }
}
