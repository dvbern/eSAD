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

package ch.dvbern.oss.hemed.esc.api.features.anlass.einladung;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json.EinladungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.util.AnlassUtil;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizMapper;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizStatus;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.notizen.AutoNotizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@JaxMapper
@RequiredArgsConstructor
@Slf4j
public class EinladungNotizMapper {

    private final NotizMapper notizMapper;
    private final NotizService notizService;
    private final AutoNotizService autoNotizService;
    private final EinladungService einladungService;
    private final CurrentUserService currentUserService;
    private final TL tl;


    @NonNull
    public Notiz createEinladungNotiz(
        @NonNull ID<Einladung> einladungId,
        @NonNull NotizSaveJson createJson,
        @NonNull List<FileUpload> fileUploads
    ) {
        Einladung einladung = einladungService.getById(einladungId);
        einladung.disableOptimisticLockingCheck();
        Notiz notiz = notizMapper.toEntityForCreate(createJson, einladung.getDossier().getId());
        return autoNotizService.createEinladungNotiz(einladung, notiz, fileUploads);
    }

    public void createOrUpdateAbmeldungsNotiz(
        @NonNull Einladung einladung,
        @NonNull EinladungSaveJson einladungSaveJson
    ) {
        Notiz abmeldungsNotiz = einladung.getAbmeldungsNotiz().orElse(null);

        // ABGESAGT -> NICHT-ABGESAGT: delete
        if (einladung.getEinladungStatus() == EinladungStatus.TEILNAHME_ABGESAGT
            && einladungSaveJson.getEinladungStatus() != EinladungStatus.TEILNAHME_ABGESAGT
            && abmeldungsNotiz != null
        ) {
            einladung.getNotizen().remove(abmeldungsNotiz);
            notizService.remove(abmeldungsNotiz.getId(), true);
            return;
        }

        // Bleibt NICHT-ABGESAGT -> noop
        if (einladungSaveJson.getEinladungStatus() != EinladungStatus.TEILNAHME_ABGESAGT) {
            return;
        }

        // Bleibt ABGESAGT -> update
        if (abmeldungsNotiz != null) {
            Notiz existingNotiz = notizService.getById(abmeldungsNotiz.getId());

            String notizTitel = getAbmeldungsNotizTitel(einladung);
            existingNotiz.setTitel(notizTitel);
            existingNotiz.setBemerkungen(getAbmeldungsnotizBemerkung(einladungSaveJson));
            notizService.persistWithOptimisticLocking(existingNotiz, einladungSaveJson.getVersionAbmeldungsNotiz());
            return;
        }
        // Neu ABGESAGT -> create
        String notizTitel = getAbmeldungsNotizTitel(einladung);
        NotizSaveJson notizSaveJson = new NotizSaveJson(
            notizTitel,
            getAbmeldungsnotizBemerkung(einladungSaveJson),
            NotizStatus.ABGESCHLOSSEN,
            NotizTyp.ABMELDUNG,
            BenutzerMapper.toOptionJson(currentUserService.currentUserRef()),
            AnlassUtil.createAnlassLink(einladung),
            new ArrayList<>(),
            new ArrayList<>(),
            0L // version
        );
        Notiz entityForCreate = notizMapper.toEntityForCreate(notizSaveJson, einladung.getDossier().getId());
        Notiz notiz = autoNotizService.createNotizWithLabel(entityForCreate, Label.ID_LABEL_ABMELDUNGSGRUND);
        einladung.getNotizen().add(notiz);
    }

    @NonNull
    private String getAbmeldungsnotizBemerkung(@NonNull EinladungSaveJson saveJson) {

        String grundTxt = "";
        if (saveJson.getGrundAbmeldung() != null) {
            String enumText = tl.translateValue(saveJson.getGrundAbmeldung());
            I18nMessage grundMsg = I18nMessage.of("notiz.abmeldungsgrund.bemerkung.grund", "grund", enumText);
            grundTxt += tl.translate(grundMsg);
            grundTxt += "\n";
        }

        String artText = "";
        if (saveJson.getArtAbmeldung() != null) {
            String enumText = tl.translateValue(saveJson.getArtAbmeldung());
            I18nMessage artMsg = I18nMessage.of("notiz.abmeldungsgrund.bemerkung.art", "art", enumText);
            artText += tl.translate(artMsg);
            artText += "\n";
        }

        return grundTxt
            + artText
            + (saveJson.getBemerkungAbmeldung() == null ? "" : '\n' + saveJson.getBemerkungAbmeldung());
    }

    @NonNull
    private String getAbmeldungsNotizTitel(@NonNull Einladung einladung) {
        return tl.translate(
            I18nMessage.of(
                "notiz.abmeldungsgrund.titel",
                "anlasstitel", einladung.getAnlass().getFullAnlassTitel(tl)));
    }
}
