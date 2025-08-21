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

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.EinladungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.ArtAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.GrundAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.EinladungJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinladungJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EinladungBuilder {

    private Einladung einladung;

    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private EinladungBuilder() {
        this.einladung = defaultEinladung(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(einladung);
    }

    @NonNull
    public static Einladung defaultEinladung(@NonNull LocalDate now) {
        Einladung einl = new Einladung();
        einl.setDossier(DossierBuilder.defaultDossier());
        einl.setAnlass(AnlassBuilder.defaultAnlass(now));
        return einl;
    }

    @NonNull
    @CanIgnoreReturnValue
    public EinladungBuilder forEinladung(@NonNull Einladung existingEinladung) {
        this.einladung = existingEinladung;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public EinladungBuilder withID(@NonNull ID<Einladung> id) {
        this.einladung.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public EinladungBuilder withDossier(@NonNull Dossier dossier) {
        this.einladung.setDossier(dossier);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public EinladungBuilder withAnlass(@NonNull Anlass anlass) {
        this.einladung.setAnlass(anlass);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public EinladungBuilder withUntersuchung(@Nullable Untersuchung untersuchung) {
        this.einladung.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public EinladungBuilder withStatus(@NonNull EinladungStatus status) {
        this.einladung.setEinladungStatus(status);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public EinladungBuilder withGrundAbmeldung(@NonNull GrundAbmeldung grundAbmeldung) {
        this.einladung.setGrundAbmeldung(grundAbmeldung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public EinladungBuilder withAbmeldungsNotiz(@NonNull Notiz abmeldungsNotiz) {
        this.einladung.setAbmeldungsNotiz(abmeldungsNotiz);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public EinladungBuilder withArtAbmeldung(@NonNull ArtAbmeldung artAbmeldung) {
        this.einladung.setArtAbmeldung(artAbmeldung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public EinladungBuilder withImpfausweis(@NonNull Notiz impfausweis) {
        this.einladung.setImpfausweis(impfausweis);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public EinladungBuilder withFrageBogen(@NonNull Notiz frageBogen) {
        this.einladung.setFragebogen(frageBogen);
        return this;
    }

    @NonNull
    public static Einladung create(@NonNull Consumer<EinladungBuilder> block) {
        EinladungBuilder builder = new EinladungBuilder();
        block.accept(builder);
        return builder.einladung;
    }

    @NonNull
    public static EinladungJson createJax(
        @NonNull Consumer<EinladungBuilder> block,
        @NonNull LocalDate now) {
        Einladung entity = create(block);
        return EinladungMapper.toJson(entity, null, now);
    }

    @NonNull
    public static EinladungJsonSpec createJaxSpec(
        @NonNull Consumer<EinladungBuilder> block,
        @NonNull LocalDate now) {
        EinladungJson json = createJax(block, now);
        return EinladungJsonSpecMapper.INSTANCE.einladungJsonToEinladungJsonSpec(json);
    }
}
