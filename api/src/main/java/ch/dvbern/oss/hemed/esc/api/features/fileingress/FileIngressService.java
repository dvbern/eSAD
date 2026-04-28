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

package ch.dvbern.oss.hemed.esc.api.features.fileingress;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.json.AppValidationErrorJson;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.json.ViolationJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.util.AnlassUtil;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidId;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizStatus;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.notizen.AutoNotizService;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import com.ibm.icu.impl.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileIngressService {

    // Pattern "__" or ".": yyyy-MM-dd__esadID.pdf -> yyyy-MM-dd, esadID, pdf
    private static final Pattern PATTERN = Pattern.compile("__|\\.");
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.GERMAN);

    private final DossierService dossierService;
    private final AutoNotizService autoNotizService;
    private final BenutzerService benutzerService;
    private final NotizService notizService;
    private final EinladungService einladungService;
    private final TL tl;
    private final AppClock appClock;

    @NonNull
    public FileIngressResultJson processUploadNotiz(
        @NonNull @IgnoreForIdClassTest String esadId,
        @NonNull ID<Anlass> anlassId,
        @NonNull NotizTyp notizTyp,
        @NonNull FileUpload fileUpload) {
        try {
            assertValidNotizTyp(notizTyp);

            Pair<LocalDate, EdokidId> filenameParts = parseFilename(esadId, fileUpload.fileName());
            Dossier dossier = findDossier(filenameParts.second, filenameParts.first);
            Einladung einladung = findEinladung(dossier.getKind(), anlassId);

            assertNotizTypAllowedForAnlass(notizTyp, einladung.getAnlass().getAnlassConfig());

            handleNotiz(einladung, notizTyp, fileUpload);

            return getSuccessResult(fileUpload, einladung, notizTyp);
        } catch (AppValidationException ex) {
            return getErrorResult(ex);
        }
    }

    private void handleNotiz(
        @NonNull Einladung einladung,
        @NonNull NotizTyp notizTyp,
        @NonNull FileUpload fileUpload
    ) {
        Notiz notiz;
        Optional<Notiz> notizOptional = getExistingNotiz(einladung, notizTyp);

        if (notizOptional.isEmpty()) {
            // Wir muessen zuerst die Notiz erstellen
            notiz = prepareNewNotiz(einladung, notizTyp);
            autoNotizService.createEinladungNotiz(einladung, notiz, List.of(fileUpload));
            einladung.disableOptimisticLockingCheck();
            einladungService.persist(einladung);
            LOG.info("FILE_INGRESS: Created new Notiz for Kind with Esad-ID={} and AnlassID={}",
                einladung.getDossier().getKind().getEsadId(), einladung.getAnlass().getId());
        } else {
            // Wir haben bereits eine Notiz desselben Typs, wir fügen nur die Datei hinzu
            notiz = notizOptional.get();
            // Speichern und das neue File hochladen
            notizService.persist(notiz);
            notizService.addUploadedFile(notiz.getId(), fileUpload);
            LOG.info(
                "FILE_INGRESS: Added File to existing Einladungs Notiz for Kind with Esad-ID={} and Anlass-ID={}",
                einladung.getDossier().getKind().getEsadId(), einladung.getAnlass().getId());
        }
        // Bemerkung anhaengen
        addBemerkungToNotiz(notiz);
        notizService.persist(notiz);
    }

    private void addBemerkungToNotiz(@NonNull Notiz notiz) {
        FileData fileData = notiz.getFiles()
            .stream()
            .max(Comparator.comparing(FileData::getTimestampErstellt))
            .orElseThrow();
        String bemerkung = getBemerkung(fileData);
        String existingBemerkung = notiz.getBemerkungen();
        if (existingBemerkung != null && !existingBemerkung.isBlank()) {
            bemerkung = existingBemerkung + '\n' + bemerkung;
        }
        notiz.setBemerkungen(bemerkung);
    }

    @NonNull
    private FileIngressResultJson getErrorResult(@NonNull AppValidationException ex) {
        String key = ex.getI18nMessage().args().keySet().stream().findFirst().orElseThrow();
        Serializable arg1 = ex.getI18nMessage().args().values().stream().findFirst().orElseThrow();
        String translatedMessage = tl.translate(
            ex.getI18nMessage().key().value(),
            key,
            arg1);
        AppValidationErrorJson error = new AppValidationErrorJson(List.of(
            ViolationJson.fromReferenceList(
                translatedMessage,
                ex.getClientKey(),
                List.of()
            )
        ));
        return new FileIngressResultJson(
            FileIngressStatus.ERROR,
            null,
            List.of(error)
        );
    }

    @NonNull
    private FileIngressResultJson getSuccessResult(@NonNull FileUpload fileUpload, @NonNull Einladung einladung, @NonNull NotizTyp notizTyp) {
        return new FileIngressResultJson(
            FileIngressStatus.OK,
            getSuccessMessage(fileUpload.fileName(), einladung, notizTyp),
            List.of()
        );
    }

    @NonNull
    static Pair<LocalDate, EdokidId> parseFilename(
        @NonNull @IgnoreForIdClassTest String esadId,
        @NonNull String filename) {
        String[] filenameparts = PATTERN.split(filename);
        if (filenameparts.length != 3) {
            LOG.warn("FILE_INGRESS: Filename invalid: {}. Must match Pattern yyyy-MM-dd__esadID.pdf", filename);
            throw new AppValidationException(AppValidationMessage.fileIngressInvalidFilenameForNotizIngress(
                filename));
        }
        LocalDate geburtsdatum = null;
        EdokidId edokidId = null;
        try {
            geburtsdatum = LocalDate.parse(filenameparts[0], DATE_TIME_FORMATTER);
        } catch (Exception e) {
            LOG.warn("FILE_INGRESS: Filename invalid: {}. Could not parse Geburtsdatum", filename);
            throw new AppValidationException(
                AppValidationMessage.fileIngressInvalidFilenameForNotizIngress(filename), e);
        }
        try {
            edokidId = EdokidId.parse(filenameparts[1], true);
        } catch (Exception e) {
            LOG.warn("FILE_INGRESS: Filename invalid: {}. Could not parse Edokid-ID", filename);
            throw new AppValidationException(
                AppValidationMessage.fileIngressInvalidFilenameForNotizIngress(filename), e);
        }
        if (edokidId == null) {
            LOG.warn("FILE_INGRESS: Filename invalid: {}. Could not parse Edokid-ID", filename);
            throw new AppValidationException(AppValidationMessage.fileIngressInvalidFilenameForNotizIngress(
                filename));
        }
        if (!edokidId.toString().equals(esadId)) {
            LOG.warn(
                "FILE_INGRESS: Filename invalid: {}. Edokid-ID from Filename does not match Edokid-ID from Path {}",
                filename,
                esadId);
            throw new AppValidationException(AppValidationMessage.fileIngressInvalidFilenameForNotizIngress(
                filename));
        }
        return Pair.of(geburtsdatum, edokidId);
    }

    @NonNull
    private Dossier findDossier(@NonNull @IgnoreForIdClassTest EdokidId edokidId, @NonNull LocalDate geburtsdatum) {
        Optional<Dossier> dossierOptional = dossierService.findByEsadId(edokidId.toFormattedString());
        if (dossierOptional.isEmpty()) {
            LOG.warn("FILE_INGRESS: No Dossier found for Kind with EsadID={}", edokidId.toFormattedString());
            throw new AppValidationException(AppValidationMessage.fileIngressNoDossierForEsadId(edokidId.toFormattedString()));
        }
        Dossier dossier = dossierOptional.get();
        if (!dossier.getKind().getGeburtsdatum().equals(geburtsdatum)) {
            LOG.warn(
                "FILE_INGRESS: Geburtsdatum from Dossier does not match Geburtsdatum in Filename for Kind with "
                    + "EsadID={}",
                edokidId.toFormattedString());
            throw new AppValidationException(AppValidationMessage.fileIngressGeburtsdatumMissmatch(edokidId.toFormattedString()));
        }
        return dossier;
    }

    @NonNull
    private Einladung findEinladung(@NonNull Kind kind, @NonNull ID<Anlass> anlassId) {
        Objects.requireNonNull(kind.getEsadId());

        Optional<Einladung> potentialEinladung = einladungService.findByAnlassUndKind(anlassId, kind.getId());
        if (potentialEinladung.isEmpty()) {
            LOG.warn(
                "FILE_INGRESS: No Einladung found for Kind with EsadID={} and AnlassID={}",
                kind.getEsadId(),
                anlassId);
            throw new AppValidationException(AppValidationMessage.fileIngressNoSuitableAnlass(kind.getEsadId()));
        }
        return potentialEinladung.get();
    }

    @NonNull
    private String getBemerkung(@NonNull FileData fileData) {
        return tl.translate(
            "fileingress.upload.bemerkung",
            "dateiname", fileData.getCalculatedFileName(),
            "datum", appClock.localDateTime().now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN)));
    }

    public String getSuccessMessage(@NonNull String originalFilename, @NonNull Einladung einladung, @NonNull NotizTyp notizTyp) {
        return tl.translate(
            "fileingress.upload.success",
            "originalFilename",
            originalFilename,
            "calculatedFilename",
            getExistingNotiz(einladung, notizTyp).orElseThrow().getFiles().getFirst().getCalculatedFileName(),
            "esadId",
            Objects.requireNonNull(einladung.getDossier().getKind().getEsadId()),
            "anlass",
            einladung.getAnlass().getFullAnlassTitel(tl));
    }

    @NonNull
    private Notiz prepareNewNotiz(
        @NonNull Einladung einladung,
        @NonNull NotizTyp notizTyp) {
        Benutzer admin = benutzerService.getById(Benutzer.SYSTEM_ADMIN_BS_ID);

        String notizTitel = tl.translate(
            getNotizTypTitelString(notizTyp),
            AutoNotizService.PARAM_ANLASSNAME, einladung.getAnlass().getFullAnlassTitel(tl));

        return new Notiz(
            notizTitel,
            null,
            NotizStatus.ABGESCHLOSSEN,
            notizTyp,
            einladung.getDossier(),
            admin,
            AnlassUtil.createAnlassLink(einladung),
            Set.of(), // AutoLabel wird im AutoNotizService gesetzt.
            new ArrayList<>() // Files werden separat hinzugefügt.
        );
    }

    private String getNotizTypTitelString(@NonNull NotizTyp notizTyp) {
        return switch (notizTyp) {
            case FRAGEBOGEN -> "einladung.fragebogen.notiz.title";
            case IMPFAUSWEIS -> "einladung.impfausweis.notiz.title";
            case ANMELDUNG_BESTAETIGUNG -> "einladung.anmeldungBestaetigung.notiz.title";
            case IMPFANMELDUNG -> "einladung.impfanmeldung.notiz.title";
            default -> throw new IllegalArgumentException("Unknown NotizTyp: " + notizTyp);
        };
    }

    private static void assertValidNotizTyp(@NonNull NotizTyp notizTyp) {
        if (!List.of(
            NotizTyp.IMPFAUSWEIS,
            NotizTyp.FRAGEBOGEN,
            NotizTyp.ANMELDUNG_BESTAETIGUNG,
            NotizTyp.IMPFANMELDUNG).contains(notizTyp)) {
            LOG.warn("FILE_INGRESS: Notiz Typ invalid: {}. ", notizTyp);
            throw new AppValidationException(AppValidationMessage.fileIngressInvalidNotizTypIngress(
                notizTyp.name()));
        }
    }

    private void assertNotizTypAllowedForAnlass(@NonNull NotizTyp notizTyp, @NonNull AnlassConfig anlassConfig) {
        boolean resultat = switch(notizTyp) {
                case IMPFANMELDUNG -> anlassConfig.isShowImpfanmeldung();
                case FRAGEBOGEN -> anlassConfig.isShowFragebogen();
                case IMPFAUSWEIS -> anlassConfig.isShowImpfausweis();
                case ANMELDUNG_BESTAETIGUNG -> anlassConfig.isShowAnmeldungBestaetigung();
                case ABMELDUNG, DEFAULT -> false;
            };
        if (!resultat) {
            LOG.warn("FILE_INGRESS: Notiz Typ not allowed for Anlass: {} {}. anlassConfigId= {}", notizTyp, anlassConfig.getAnlassTyp(), anlassConfig.getId().getId());
            throw new AppValidationException(AppValidationMessage.fileIngressInvalidNotizTypForAnlassIngress(
                notizTyp.name(), anlassConfig.getAnlassTyp().name()));
        }
    }

    private Optional<Notiz> getExistingNotiz(@NonNull Einladung einladung, @NonNull NotizTyp notizTyp) {
        return switch (notizTyp) {
            case NotizTyp.FRAGEBOGEN -> einladung.getFragebogenNotiz();
            case NotizTyp.ANMELDUNG_BESTAETIGUNG -> einladung.getAnmeldungBestaetigungNotiz();
            case NotizTyp.IMPFAUSWEIS -> einladung.getImpfausweisNotiz();
            case NotizTyp.IMPFANMELDUNG -> einladung.getImpfanmeldungsNotiz();
            default -> throw new IllegalArgumentException("Unknown NotizTyp: " + notizTyp);
        };
    }
}


