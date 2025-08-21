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
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindMapper;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json.KontaktPersonKindJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KontaktPersonKindBeziehung;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.KontaktPersonKindJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonKindJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class KontaktPersonKindBuilder {

    private KontaktPersonKind kontaktPerson;

    private KontaktPersonKindBuilder() {
        this.kontaktPerson = defaultKontaktPersonKind();
        TestUtil.setEntityInfo(kontaktPerson);
    }

    @NonNull
    public static KontaktPersonKind defaultKontaktPersonKind() {
        return new KontaktPersonKind(
            null,
            new HashSet<>(Set.of(KindBuilder.defaultKind())),
            "Gerber",
            "Klothilde",
            Geschlecht.WEIBLICH,
            KontaktPersonKindBeziehung.MUTTER,
            AdresseBuilder.defaultAdresse(),
            EmailAddress.fromString("klothilde.gerber@mailbucket.dvbern.ch"),
            NormalizedPhoneNumber.parse("+41 79 123 45 67"),
            true
        );
    }

    @NonNull
    public static KontaktPersonKind create(@NonNull Consumer<KontaktPersonKindBuilder> block) {
        KontaktPersonKindBuilder builder = new KontaktPersonKindBuilder();
        block.accept(builder);
        return builder.kontaktPerson;
    }

    @NonNull
    public static KontaktPersonKindJson createJax(@NonNull Consumer<KontaktPersonKindBuilder> block) {
        KontaktPersonKind entity = create(block);
        return KontaktPersonKindMapper.toJson(entity);
    }

    @NonNull
    public static KontaktPersonKindJsonSpec createJaxSpec(@NonNull Consumer<KontaktPersonKindBuilder> block) {
        KontaktPersonKindJson json = createJax(block);
        return KontaktPersonKindJsonSpecMapper.INSTANCE.kontaktPersonKindJsonToKontaktPersonKindJsonSpec(json);
    }

    @NonNull
    @CanIgnoreReturnValue
    public KontaktPersonKindBuilder forKontaktPersonKind(@NonNull KontaktPersonKind existingKontaktPersonKind) {
        this.kontaktPerson = existingKontaktPersonKind;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KontaktPersonKindBuilder forKind(@NonNull Kind... kinder) {
        this.kontaktPerson.getKinder().clear();
        this.kontaktPerson.getKinder().addAll(List.of(kinder));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KontaktPersonKindBuilder withID(@NonNull ID<KontaktPersonKind> id) {
        this.kontaktPerson.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KontaktPersonKindBuilder withName(@NonNull String name, @NonNull String vorname) {
        this.kontaktPerson.setName(name);
        this.kontaktPerson.setVorname(vorname);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KontaktPersonKindBuilder withBeziehung(@NonNull KontaktPersonKindBeziehung beziehung) {
        this.kontaktPerson.setBeziehung(beziehung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public KontaktPersonKindBuilder withExternalImportInfo(@NonNull String externalId, boolean aktiv, @NonNull OffsetDateTime now) {
        ExternalImportInfo importInfo = new ExternalImportInfo(externalId, aktiv, now, now);
        this.kontaktPerson.setExternalImportInfo(importInfo);
        return this;
    }
}
