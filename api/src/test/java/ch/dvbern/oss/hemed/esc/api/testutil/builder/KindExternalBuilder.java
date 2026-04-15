/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternal;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KindExternalBuilder {

    private final KindExternal kindExternal;

    private KindExternalBuilder() {
        this.kindExternal = defaultKindExternal();
        TestUtil.setEntityInfo(kindExternal);
    }

    public static KindExternal defaultKindExternal() {
        return new KindExternal(
            null,
            "377780",
            "Mutter",
            "Caserta",
            "Maya",
            "2006-10-01T00:00:00",
            "2",
            "756.3147.8443.00",
            "Dornacherstrasse 39a",
            null,
            null,
            "4053",
            "Basel",
            "079 945 71 12",
            "maya.caserta@yahoo.com",
            "Schweiz",
            "CH",
            "Deutsch",
            "Türkisch",
            "Berufsfachschule Basel",
            "888",
            KlasseExternalBuilder.defaultKlasseExternal()
        );
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withEscadaKlasseId(@Nullable String escadaKlasseId) {
        kindExternal.setEscadaKlasseId(escadaKlasseId);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchulhausId(@Nullable String schulhausId) {
        kindExternal.setSchulhausId(schulhausId);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerErstsprache(@Nullable String schuelerErstsprache) {
        kindExternal.setSchuelerErstsprache(schuelerErstsprache);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerSprache(@Nullable String schuelerSprache) {
        kindExternal.setSchuelerSprache(schuelerSprache);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerStaatcodeISO(@Nullable String schuelerStaatcodeISO) {
        kindExternal.setSchuelerStaatcodeISO(schuelerStaatcodeISO);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerNationalitaet(@Nullable String schuelerNationalitaet) {
        kindExternal.setSchuelerNationalitaet(schuelerNationalitaet);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerEMail(@Nullable String schuelerEMail) {
        kindExternal.setSchuelerEMail(schuelerEMail);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerMobile(@Nullable String schuelerMobile) {
        kindExternal.setSchuelerMobile(schuelerMobile);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerOrt(@Nullable String schuelerOrt) {
        kindExternal.setSchuelerOrt(schuelerOrt);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerPLZ(@Nullable String schuelerPLZ) {
        kindExternal.setSchuelerPLZ(schuelerPLZ);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerAdresse2(@Nullable String schuelerAdresse2) {
        kindExternal.setSchuelerAdresse2(schuelerAdresse2);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerAdresse1(@Nullable String schuelerAdresse1) {
        kindExternal.setSchuelerAdresse1(schuelerAdresse1);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerAdresse(@Nullable String schuelerAdresse) {
        kindExternal.setSchuelerAdresse(schuelerAdresse);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerSozialversicherung(@Nullable String schuelerSozialversicherung) {
        kindExternal.setSchuelerSozialversicherung(schuelerSozialversicherung);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerGeschlecht(@Nullable String schuelerGeschlecht) {
        kindExternal.setSchuelerGeschlecht(schuelerGeschlecht);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerGeburtsdatum(@Nullable String schuelerGeburtsdatum) {
        kindExternal.setSchuelerGeburtsdatum(schuelerGeburtsdatum);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerVorname(@Nullable String schuelerVorname) {
        kindExternal.setSchuelerVorname(schuelerVorname);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerName(@Nullable String schuelerName) {
        kindExternal.setSchuelerName(schuelerName);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withWohntyp(@Nullable String wohntyp) {
        kindExternal.setWohntyp(wohntyp);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withSchuelerId(@NonNull String schuelerId) {
        kindExternal.setSchuelerId(schuelerId);
        return this;
    }

    @CanIgnoreReturnValue
    public KindExternalBuilder withKind(@Nullable Kind kind) {
        kindExternal.setKind(kind);
        return this;
    }

    @NonNull
    public static KindExternal create(@NonNull Consumer<KindExternalBuilder> block) {
        KindExternalBuilder builder = new KindExternalBuilder();
        block.accept(builder);
        return builder.kindExternal;
    }
}
