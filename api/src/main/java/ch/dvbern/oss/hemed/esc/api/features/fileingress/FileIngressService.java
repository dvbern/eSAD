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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.json.AppValidationErrorJson;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.json.ViolationJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.util.AnlassUtil;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.KindAnlaesseService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidId;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizStatus;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.features.spss.SpssReportType;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
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
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.GERMAN);

    private final FileStoreService fileStoreService;
    private final DossierService dossierService;
    private final KindAnlaesseService kindAnlaesseService;
    private final AutoNotizService autoNotizService;
    private final BenutzerService benutzerService;
    private final NotizService notizService;
    private final LabelService labelService;
    private final EinladungService einladungService;
    private final TL tl;
    private final AppClock appClock;

    @NonNull
    public FileIngressResultJson processUploadFragebogen(@NonNull @IgnoreForIdClassTest String esadId, @NonNull FileUpload fileUpload) {
        try {
            // Parse Filename
            Pair<LocalDate, EdokidId> filenameParts = parseFilename(esadId, fileUpload.fileName());
            LocalDate geburtsdatum = filenameParts.first;
            EdokidId edokidId = filenameParts.second;
            // Find Dossier
            Dossier dossier = findDossier(edokidId, geburtsdatum);
            // Find Einladung
            Einladung einladung = findEinladung(dossier.getKind());
            // Notiz ermitteln
            Notiz fragebogenNotiz = null;
            Optional<Notiz> notizOptional = einladung.getFragebogenNotiz();
            if (notizOptional.isEmpty()) {
                // Wir muessen zuerst die Fragebogen-Notiz erstellen
                fragebogenNotiz = createNewFragebogenNotiz(einladung, fileUpload);
                autoNotizService.createEinladungNotiz(einladung, fragebogenNotiz, List.of(fileUpload));
                einladung.disableOptimisticLockingCheck();
                einladungService.persist(einladung);
                LOG.info("FILE_INGRESS: Created new Fragebogen-Notiz for Kind with Esad-ID={}", esadId);
            } else {
                // Wir haben bereits eine Fragebogen-Notiz, wir fügen nur die Datei hinzu
                fragebogenNotiz = notizOptional.get();
                // Bestehende Dateien entfernen, falls sie nicht mehr in S3 vorhanden sind
                removeFileMetadataIfNotPresentInS3(fragebogenNotiz);
                // Das Label "Datenverlust" entfernen, falls vorhanden
                removeLabelDatenverlustIfPresent(fragebogenNotiz);
                // Speichern und das neue File hochladen
                notizService.persist(fragebogenNotiz);
                notizService.addUploadedFile(fragebogenNotiz.getId(), fileUpload);
                LOG.info("FILE_INGRESS: Added File to existing Fragebogen-Notiz for Kind with Esad-ID={}", esadId);
            }
            // Bemerkung anhaengen
            addBemerkungToNotiz(fragebogenNotiz);
            notizService.persist(fragebogenNotiz);

            return getSuccessResult(fileUpload, einladung);
        } catch (AppValidationException ex) {
            return getErrorResult(ex);
        }
    }

    private void addBemerkungToNotiz(@NonNull Notiz fragebogenNotiz) {
        FileData fileData = fragebogenNotiz.getFiles()
            .stream()
            .max(Comparator.comparing(FileData::getTimestampErstellt))
            .orElseThrow();
        String bemerkung = getBemerkung(fileData);
        String existingBemerkung = fragebogenNotiz.getBemerkungen();
        if (existingBemerkung != null && !existingBemerkung.isBlank()) {
            bemerkung = existingBemerkung + '\n' + bemerkung;
        }
        fragebogenNotiz.setBemerkungen(bemerkung);
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
    private FileIngressResultJson getSuccessResult(@NonNull FileUpload fileUpload, @NonNull Einladung einladung) {
        return new FileIngressResultJson(
            FileIngressStatus.OK,
            getSuccessMessage(fileUpload.fileName(), einladung),
            List.of()
        );
    }

    @NonNull
    static Pair<LocalDate, EdokidId> parseFilename(@NonNull @IgnoreForIdClassTest String esadId, @NonNull String filename) {
        String[] filenameparts = PATTERN.split(filename);
        if (filenameparts.length != 3) {
            LOG.warn("FILE_INGRESS: Filename invalid: {}. Must match Pattern yyyy-MM-dd__esadID.pdf", filename);
            throw new AppValidationException(AppValidationMessage.fileIngressInvalidFilenameForFragebogenIngress(filename));
        }
        LocalDate geburtsdatum = null;
        EdokidId edokidId = null;
        try {
            geburtsdatum = LocalDate.parse(filenameparts[0], DATE_TIME_FORMATTER);
        } catch (Exception e) {
            LOG.warn("FILE_INGRESS: Filename invalid: {}. Could not parse Geburtsdatum", filename);
            throw new AppValidationException(AppValidationMessage.fileIngressInvalidFilenameForFragebogenIngress(filename), e);
        }
        try {
            edokidId = EdokidId.parse(filenameparts[1], true);
        } catch (Exception e) {
            LOG.warn("FILE_INGRESS: Filename invalid: {}. Could not parse Edokid-ID", filename);
            throw new AppValidationException(AppValidationMessage.fileIngressInvalidFilenameForFragebogenIngress(filename), e);
        }
        if (edokidId == null) {
            LOG.warn("FILE_INGRESS: Filename invalid: {}. Could not parse Edokid-ID", filename);
            throw new AppValidationException(AppValidationMessage.fileIngressInvalidFilenameForFragebogenIngress(filename));
        }
        if (!edokidId.toString().equals(esadId)) {
            LOG.warn("FILE_INGRESS: Filename invalid: {}. Edokid-ID from Filename does not match Edokid-ID from Path {}", filename, esadId);
            throw new AppValidationException(AppValidationMessage.fileIngressInvalidFilenameForFragebogenIngress(filename));
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
            LOG.warn("FILE_INGRESS: Geburtsdatum from Dossier does not match Geburtsdatum in Filename for Kind with EsadID={}", edokidId.toFormattedString());
            throw new AppValidationException(AppValidationMessage.fileIngressGeburtsdatumMissmatch(edokidId.toFormattedString()));
        }
        return dossier;
    }

    @NonNull
    private Einladung findEinladung(@NonNull Kind kind) {
        Objects.requireNonNull(kind.getEsadId());
        List<Einladung> allEinladungen = kindAnlaesseService.findAllEinladungForKind(kind.getId());
        List<Einladung> potentialEinladungen = allEinladungen.stream()
            .filter(e -> !SpssReportType.IMPF_ANLAESSE.getAnlassStatusList().contains(e.getAnlass().getAnlassConfig().getAnlassTyp()))
            .toList();
        if (potentialEinladungen.isEmpty()) {
            LOG.warn("FILE_INGRESS: No Einladungen found for Kind with EsadID={}", kind.getEsadId());
            throw new AppValidationException(AppValidationMessage.fileIngressNoSuitableAnlass(kind.getEsadId()));
        }
        if (potentialEinladungen.size() == 1) {
            LOG.info("FILE_INGRESS: Exactly one AnlassTyp found for Kind with EsadID={}", kind.getEsadId());
            return potentialEinladungen.getFirst();
        }
        // Es hat mehrere Einladungen. Diese dürfen NICHT zu verschiedenen AnlassTypen gehören
        Set<AnlassTyp> distinctTypes = potentialEinladungen.stream()
            .map(Einladung::getAnlass)
            .map(anlass -> anlass.getAnlassConfig().getAnlassTyp())
            .collect(Collectors.toSet());
        if (distinctTypes.size() > 1) {
            LOG.warn("FILE_INGRESS: More than one AnlassTyp found for Kind with EsadID={}", kind.getEsadId());
            throw new AppValidationException(AppValidationMessage.fileIngressNoSuitableAnlass(kind.getEsadId()));
        }
        // Es hat also mehrere Einladungen desselben Typs. Wir nehmen die neueste Einladung (gemäss Anlass Datum)
        LOG.info("FILE_INGRESS: Multiple Einladungen of one AnlassTyp found for Kind with EsadID={}. Choosing the latest", kind.getEsadId());
        return potentialEinladungen.stream()
            .max(Comparator.comparing(e -> e.getAnlass().getDatum()))
            .orElseThrow();
    }

    @NonNull
    private String getBemerkung(@NonNull FileData fileData) {
        return tl.translate(
            "fileingress.upload.bemerkung",
            "dateiname", fileData.getCalculatedFileName(),
            "datum", appClock.localDateTime().now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN)));
    }

    public String getSuccessMessage(@NonNull String originalFilename, @NonNull Einladung einladung) {
        return tl.translate(
            "fileingress.upload.success",
            "originalFilename", originalFilename,
            "calculatedFilename", einladung.getFragebogenNotiz().orElseThrow().getFiles().getFirst().getCalculatedFileName(),
            "esadId", Objects.requireNonNull(einladung.getDossier().getKind().getEsadId()),
            "anlass", einladung.getAnlass().getFullAnlassTitel(tl));
    }

    @NonNull
    private Notiz createNewFragebogenNotiz(@NonNull Einladung einladung, @NonNull FileUpload fileUpload) {
        Benutzer admin = benutzerService.getById(Benutzer.SYSTEM_ADMIN_BS_ID);
        String notizTitel = tl.translate(
            "einladung.fragebogen.notiz.title",
            AutoNotizService.PARAM_ANLASSNAME, einladung.getAnlass().getFullAnlassTitel(tl));
        return new Notiz(
            notizTitel,
            null,
            NotizStatus.ABGESCHLOSSEN,
            NotizTyp.FRAGEBOGEN,
            einladung.getDossier(),
            admin,
            AnlassUtil.createAnlassLink(einladung),
            Set.of(labelService.getById(Label.ID_LABEL_FRAGEBOGEN)),
            new ArrayList<>() // Files werden separat hinzugefügt
        );
    }

    private void removeFileMetadataIfNotPresentInS3(@NonNull Notiz fragebogenNotiz) {
        List<FileData> files = fragebogenNotiz.getFiles();
        Map<String, ID<FileData>> fileMap = new HashMap<>();
        for (FileData file : files) {
            fileMap.put(file.getFileInfoBucket().getFileIdInBucket(), file.getId());
        }
        for (Entry<String, ID<FileData>> entry : fileMap.entrySet()) {
            String fileIdInBucket = entry.getKey();
            if (!fileStoreService.isFilePresent(fileIdInBucket)) {
                notizService.removeUploadedFile(fragebogenNotiz, entry.getValue());
                LOG.info("FILE_INGRESS: File with id {} not found in S3, removing from Notiz {}", fileIdInBucket, fragebogenNotiz.getId());
            }
        }
    }

    private void removeLabelDatenverlustIfPresent(@NonNull Notiz fragebogenNotiz) {
        Set<Label> labels = fragebogenNotiz.getLabels();
        labels.removeIf(label -> label.getId().equals(Label.ID_LABEL_DATENVERLUST_NOTIZ));
        fragebogenNotiz.setLabels(labels);
    }
}


