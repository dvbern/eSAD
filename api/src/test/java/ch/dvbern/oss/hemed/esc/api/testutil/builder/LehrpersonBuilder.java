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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.LehrpersonMapper;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.json.LehrpersonJson;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.LehrpersonJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LehrpersonJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LehrpersonSaveJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class LehrpersonBuilder {

    private Lehrperson lehrperson;

    private LehrpersonBuilder() {
        this.lehrperson = defaultLehrperson();
        TestUtil.setEntityInfo(lehrperson);
    }

    @NonNull
    public static Lehrperson defaultLehrperson() {
        return new Lehrperson(
            null,
            new HashSet<>(),
            Geschlecht.WEIBLICH,
            "Schulkontakterin",
            "Klara",
            EmailAddress.fromString("klara.schulkontakterin@mailbucket.dvbern.ch"),
            NormalizedPhoneNumber.parse("+41 79 123 45 67")
        );
    }

    @NonNull
    public static Lehrperson defaultLehrperson(@NonNull Klasse klasse) {
        return new Lehrperson(
            null,
            new HashSet<>(Set.of(klasse)),
            Geschlecht.WEIBLICH,
            "Schulkontakterin",
            "Klara",
            EmailAddress.fromString("klara.schulkontakterin@mailbucket.dvbern.ch"),
            NormalizedPhoneNumber.parse("+41 79 123 45 67")
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public LehrpersonBuilder forLehrperson(@NonNull Lehrperson person) {
        this.lehrperson = person;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public LehrpersonBuilder forKlasse(@NonNull Klasse... klassen) {
        this.lehrperson.getKlassen().clear();
        this.lehrperson.getKlassen().addAll(List.of(klassen));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public LehrpersonBuilder withID(@NonNull ID<Lehrperson> id) {
        this.lehrperson.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public LehrpersonBuilder withName(@NonNull String name, @NonNull String vorname) {
        this.lehrperson.setName(name);
        this.lehrperson.setVorname(vorname);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public LehrpersonBuilder withExternalImportInfo(
        @NonNull String externalId,
        boolean aktiv,
        @NonNull OffsetDateTime now,
        @NonNull ImportTyp importTyp) {
        ExternalImportInfo importInfo = new ExternalImportInfo(externalId, aktiv, now, now, importTyp);
        this.lehrperson.setExternalImportInfo(importInfo);
        return this;
    }

    @NonNull
    public static Lehrperson create(@NonNull Consumer<LehrpersonBuilder> block) {
        LehrpersonBuilder builder = new LehrpersonBuilder();
        block.accept(builder);
        return builder.lehrperson;
    }

    @NonNull
    public static LehrpersonJson createJax(@NonNull Consumer<LehrpersonBuilder> block) {
        Lehrperson entity = create(block);
        return LehrpersonMapper.toJson(entity);
    }

    @NonNull
    public static LehrpersonJsonSpec createJaxSpec(@NonNull Consumer<LehrpersonBuilder> block) {
        LehrpersonJson json = createJax(block);
        return LehrpersonJsonSpecMapper.INSTANCE.lehrpersonJsonToLehrpersonJsonSpec(json);
    }

    @NonNull
    public static LehrpersonSaveJsonSpec createSaveJsonSpec(
        @NonNull Consumer<LehrpersonBuilder> block) {
        LehrpersonJsonSpec json = createJaxSpec(block);
        return json.getData();
    }
}
