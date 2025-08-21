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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.KontaktPersonSchule;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.KontaktPersonSchuleMapper;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.json.KontaktPersonSchuleJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.KontaktPersonSchuleFunktion;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.KontaktPersonSchuleJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonSchuleJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonSchuleSaveJsonSpec;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public class KontaktPersonSchuleBuilder {

    private final KontaktPersonSchule kontaktPerson;

    private KontaktPersonSchuleBuilder() {
        this.kontaktPerson = defaultKontaktPersonSchule();
        TestUtil.setEntityInfo(kontaktPerson);
    }

    @NonNull
    public static KontaktPersonSchule defaultKontaktPersonSchule() {
        return defaultKontaktPersonSchule(SchuleBuilder.defaultSchule());
    }

    @NonNull
    public static KontaktPersonSchule defaultKontaktPersonSchule(Schule schule) {
        return new KontaktPersonSchule(
            new HashSet<>(Set.of(schule)),
            Geschlecht.WEIBLICH,
            KontaktPersonSchuleFunktion.SEKRETARIAT,
            "Schulkontakterin",
            "Klara",
            EmailAddress.fromString("klara.schulkontakterin@mailbucket.dvbern.ch"),
            NormalizedPhoneNumber.parse("+41 79 123 45 67"),
            true
        );
    }

    @NonNull
    public static KontaktPersonSchule create(@NonNull Consumer<KontaktPersonSchuleBuilder> block) {
        KontaktPersonSchuleBuilder builder = new KontaktPersonSchuleBuilder();
        block.accept(builder);
        return builder.kontaktPerson;
    }

    @NonNull
    public static KontaktPersonSchuleJson createJax(@NonNull Consumer<KontaktPersonSchuleBuilder> block) {
        KontaktPersonSchule entity = create(block);
        return KontaktPersonSchuleMapper.toJson(entity);
    }

    @NonNull
    public static KontaktPersonSchuleJsonSpec createJaxSpec(@NonNull Consumer<KontaktPersonSchuleBuilder> block) {
        KontaktPersonSchuleJson json = createJax(block);
        return KontaktPersonSchuleJsonSpecMapper.INSTANCE.kontaktPersonSchuleJsonToKontaktPersonSchuleJsonSpec(json);
    }

    @NonNull
    public static KontaktPersonSchuleSaveJsonSpec createSaveJsonSpec(
        @NonNull Consumer<KontaktPersonSchuleBuilder> block) {
        KontaktPersonSchuleJsonSpec json = createJaxSpec(block);
        return json.getData();
    }

    @NonNull
    @CanIgnoreReturnValue
    public KontaktPersonSchuleBuilder forSchule(@NonNull Schule... schule) {
        this.kontaktPerson.getSchulen().clear();
        this.kontaktPerson.getSchulen().addAll(List.of(schule));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KontaktPersonSchuleBuilder withID(@NonNull ID<KontaktPersonSchule> id) {
        this.kontaktPerson.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KontaktPersonSchuleBuilder withName(@NonNull String name, @NonNull String vorname) {
        this.kontaktPerson.setName(name);
        this.kontaktPerson.setVorname(vorname);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KontaktPersonSchuleBuilder withFunktion(@NonNull KontaktPersonSchuleFunktion funktion) {
        this.kontaktPerson.setFunktion(funktion);
        return this;
    }
}
