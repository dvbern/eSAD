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

import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierMapper;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierJson;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.DossierJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DossierJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DossierBuilder {

    private Dossier dossier;

    private DossierBuilder() {
        this.dossier = defaultDossier();
        TestUtil.setEntityInfo(dossier);
    }

    @NonNull
    public static Dossier defaultDossier() {
        Dossier dossier = new Dossier();
        dossier.setKind(KindBuilder.defaultKind());
        return dossier;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DossierBuilder forDossier(@NonNull Dossier existingDossier) {
        this.dossier = existingDossier;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DossierBuilder forKind(@NonNull Kind kind) {
        this.dossier.setKind(kind);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DossierBuilder withID(@NonNull ID<Dossier> id) {
        this.dossier.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DossierBuilder withAndereZusatzinformation(boolean hasAndereZusatzinformation, @Nullable String zusatzinformation) {
        this.dossier.setAndereZusatzinformation(hasAndereZusatzinformation);
        this.dossier.setAndereZusatzInformationText(zusatzinformation);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DossierBuilder withZustaendigArzt(@Nullable Benutzer zustaendigArzt) {
        this.dossier.setZustaendigArzt(zustaendigArzt);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DossierBuilder withZustaendigMPA(@Nullable Benutzer zustaendigMPA) {
        this.dossier.setZustaendigMPA(zustaendigMPA);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DossierBuilder withImmunitaetVarizellen(@NonNull Boolean immunitaetVarizellen) {
        this.dossier.setImmunitaetVarizellen(immunitaetVarizellen);
        return this;
    }
    @NonNull
    @CanIgnoreReturnValue
    public DossierBuilder withImmunitaetMasern(@NonNull Boolean immunitaetMasern) {
        this.dossier.setImmunitaetMasern(immunitaetMasern);
        return this;
    }

    @NonNull
    public static Dossier create(@NonNull Consumer<DossierBuilder> block) {
        DossierBuilder builder = new DossierBuilder();
        block.accept(builder);
        return builder.dossier;
    }

    @NonNull
    public static DossierJson createJax(@NonNull Consumer<DossierBuilder> block,
        @NonNull LocalDate now) {
        Dossier entity = create(block);
        return DossierMapper.toJson(entity, null, now, true);
    }

    @NonNull
    public static DossierJsonSpec createJaxSpec(@NonNull Consumer<DossierBuilder> block,
        @NonNull LocalDate now) {
        DossierJson json = createJax(block, now);
        return DossierJsonSpecMapper.INSTANCE.dossierJsonToDossierJsonSpec(json);
    }
}
