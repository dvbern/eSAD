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

import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.shared.person.AdresseJson;
import ch.dvbern.oss.hemed.esc.api.shared.person.AdresseMapper;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.AdresseJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AdresseJsonSpec;
import lombok.RequiredArgsConstructor;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequiredArgsConstructor
public class AdresseBuilder {

    private final Adresse adresse;

    private AdresseBuilder() {
        this.adresse = defaultAdresse();
    }

    @NonNull
    public static Adresse defaultAdresse() {
        return new Adresse("Bahnhofstrasse 10", "c/o Muster", "3000", "Bern", Land.SCHWEIZ);
    }

    @NonNull
    @CanIgnoreReturnValue
    public AdresseBuilder withStrasse(@NonNull String strasseUndNr) {
        this.adresse.setStrasseUndNr(strasseUndNr);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AdresseBuilder withOrt(@Nullable String ort) {
        this.adresse.setOrt(ort);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AdresseBuilder withPlz(@Nullable String plz) {
        this.adresse.setPlz(plz);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AdresseBuilder withAdresseZusatz(@Nullable String adresseZusatz) {
        this.adresse.setAdresseZusatz(adresseZusatz);
        return this;
    }

    @NonNull
    public static Adresse create(@NonNull Consumer<AdresseBuilder> block) {
        AdresseBuilder builder = new AdresseBuilder();
        block.accept(builder);
        return builder.adresse;
    }

    @NonNull
    public static AdresseJson createJax(@NonNull Consumer<AdresseBuilder> block) {
        Adresse entity = create(block);
        return AdresseMapper.toJson(entity);
    }

    @NonNull
    public static AdresseJsonSpec createJaxSpec(@NonNull Consumer<AdresseBuilder> block) {
        AdresseJson json = createJax(block);
        return AdresseJsonSpecMapper.INSTANCE.adresseJsonToAdresseJsonSpec(json);
    }
}
