/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.AdresseMassenimportDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.KindMassenimportDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.KlasseMassenimportDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.KontaktPersonKindMassenimportDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportReportDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportResultDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportRowDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.SchuleMassenimportDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.ValueErrorObject;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KontaktPersonKindBeziehung;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Row;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportUtil.isAnyKontaktPersonKindFieldFilledOut;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportUtil.mapBooleanStringToValueErrorObject;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportUtil.mapEmailStringToValueErrorObject;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportUtil.mapEnumStringToValueErrorObject;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportUtil.mapKlasseStringToValueErrorObject;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportUtil.mapLocalDateStringToValueErrorObject;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportUtil.mapStringToValueErrorObject;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportUtil.mapSvnrStringToValueErrorObject;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportUtil.mapTelefonStringToValueErrorObject;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_LAND;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_LAND_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_ORT;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_ORT_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_PLZ;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_PLZ_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_STRASSE_NR;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_STRASSE_NR_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_ZUSATZ;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_ZUSATZ_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_BEZIEHUNG_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_EMAIL;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_EMAIL_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ERSTSPRACHE;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_GEBURTSDATUM;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_GESCHLECHT;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_GESCHLECHT_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_KLASSE_BEZEICHUNG;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_KORRESPONDENZ_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_NACHNAME;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_NACHNAME_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_NATIONALITAET;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_SVNR;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_TELEFON;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_TELEFON_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_VORNAME;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_VORNAME_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_WOHNTYP;

@Slf4j
@Service
@RequiredArgsConstructor
public class MassenimportDTOHelperService {

    private final KlasseService klasseService;

    @NonNull
    public MassenimportRowDTO mapRowToRowDTO(
        @NonNull Integer rowIndex,
        @NonNull Row row,
        @NonNull ID<Schule> schuleID,
        boolean doNotImportKlasse
    ) {
        ValueErrorObject<SozialversicherungsNummer> kindSvnr =
            mapSvnrStringToValueErrorObject(rowIndex, COL_INDEX_SVNR.getColIndex(), row);
        ValueErrorObject<String> kindNachname =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_NACHNAME.getColIndex(), row, true);
        ValueErrorObject<String> kindVorname =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_VORNAME.getColIndex(), row, true);
        ValueErrorObject<Geschlecht> kindGeschlecht =
            mapEnumStringToValueErrorObject(rowIndex, COL_INDEX_GESCHLECHT.getColIndex(), row, Geschlecht.class, true);
        ValueErrorObject<LocalDate> kindGeburtsdatum =
            mapLocalDateStringToValueErrorObject(rowIndex, COL_INDEX_GEBURTSDATUM.getColIndex(), row, true);
        ValueErrorObject<Land> kindNationalitaet =
            mapEnumStringToValueErrorObject(rowIndex, COL_INDEX_NATIONALITAET.getColIndex(), row, Land.class, false);
        ValueErrorObject<String> kindErstsprache =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_ERSTSPRACHE.getColIndex(), row, false);
        ValueErrorObject<EmailAddress> kindEmai =
            mapEmailStringToValueErrorObject(rowIndex, COL_INDEX_EMAIL.getColIndex(), row);
        ValueErrorObject<NormalizedPhoneNumber> kindTelefon =
            mapTelefonStringToValueErrorObject(rowIndex, COL_INDEX_TELEFON.getColIndex(), row);
        ValueErrorObject<String> kindWohntyp =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_WOHNTYP.getColIndex(), row, false);
        ValueErrorObject<Land> kindLand =
            mapEnumStringToValueErrorObject(rowIndex, COL_INDEX_ADRESSE_LAND.getColIndex(), row, Land.class, false);
        ValueErrorObject<String> kindStrasseNr =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_ADRESSE_STRASSE_NR.getColIndex(), row, false);
        ValueErrorObject<String> kindAdressZusatz =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_ADRESSE_ZUSATZ.getColIndex(), row, false);
        ValueErrorObject<String> kindPlz =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_ADRESSE_PLZ.getColIndex(), row, false);
        ValueErrorObject<String> kindOrt =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_ADRESSE_ORT.getColIndex(), row, false);

        boolean kpFilled = isAnyKontaktPersonKindFieldFilledOut(row);
        ValueErrorObject<Geschlecht> kpkGeschlecht =
            mapEnumStringToValueErrorObject(
                rowIndex,
                COL_INDEX_GESCHLECHT_KP.getColIndex(),
                row,
                Geschlecht.class,
                kpFilled);
        ValueErrorObject<String> kpkNachname =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_NACHNAME_KP.getColIndex(), row, kpFilled);
        ValueErrorObject<String> kpkVorname =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_VORNAME_KP.getColIndex(), row, false);
        ValueErrorObject<KontaktPersonKindBeziehung> kpkBeziehung = mapEnumStringToValueErrorObject(
            rowIndex,
            COL_INDEX_BEZIEHUNG_KP.getColIndex(),
            row,
            KontaktPersonKindBeziehung.class,
            false);
        ValueErrorObject<EmailAddress> kpkEmail =
            mapEmailStringToValueErrorObject(rowIndex, COL_INDEX_EMAIL_KP.getColIndex(), row);
        ValueErrorObject<NormalizedPhoneNumber> kpkTelefon =
            mapTelefonStringToValueErrorObject(rowIndex, COL_INDEX_TELEFON_KP.getColIndex(), row);
        ValueErrorObject<Boolean> kpkKorrespondenz =
            mapBooleanStringToValueErrorObject(rowIndex, COL_INDEX_KORRESPONDENZ_KP.getColIndex(), row);
        ValueErrorObject<Land> kpkLand =
            mapEnumStringToValueErrorObject(rowIndex, COL_INDEX_ADRESSE_LAND_KP.getColIndex(), row, Land.class, false);
        ValueErrorObject<String> kpkStrasseNr =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_ADRESSE_STRASSE_NR_KP.getColIndex(), row, false);
        ValueErrorObject<String> kpkAdressZusatz =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_ADRESSE_ZUSATZ_KP.getColIndex(), row, false);
        ValueErrorObject<String> kpkPlz =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_ADRESSE_PLZ_KP.getColIndex(), row, false);
        ValueErrorObject<String> kpkOrt =
            mapStringToValueErrorObject(rowIndex, COL_INDEX_ADRESSE_ORT_KP.getColIndex(), row, false);
        ValueErrorObject<Klasse> klasse = mapKlasseStringToValueErrorObject(
            rowIndex,
            COL_INDEX_KLASSE_BEZEICHUNG.getColIndex(),
            row,
            schuleID,
            klasseService,
            doNotImportKlasse);

        return new MassenimportRowDTO(
            rowIndex,
            kindSvnr,
            kindNachname,
            kindVorname,
            kindGeschlecht,
            kindGeburtsdatum,
            kindNationalitaet,
            kindErstsprache,
            kindEmai,
            kindTelefon,
            kindWohntyp,
            kindLand,
            kindStrasseNr,
            kindAdressZusatz,
            kindPlz,
            kindOrt,
            kpkGeschlecht,
            kpkNachname,
            kpkVorname,
            kpkBeziehung,
            kpkEmail,
            kpkTelefon,
            kpkKorrespondenz,
            kpkLand,
            kpkStrasseNr,
            kpkAdressZusatz,
            kpkPlz,
            kpkOrt,
            klasse
        );
    }

    @NonNull
    public MassenimportReportDTO mapResultToReportDTO(@NonNull MassenimportResultDTO resultDTO) {
        List<KindMassenimportDTO> importedKinder = resultDTO.getImportedKinder()
            .stream()
            .map(this::toKindMassenimportDTO)
            .filter(Objects::nonNull).toList();

        List<KontaktPersonKindMassenimportDTO> importedKontaktPersonenKind = resultDTO.getImportedKontaktPersonenKind()
            .stream()
            .map(this::toKontaktPersonKindMassenimportDTO)
            .filter(Objects::nonNull)
            .toList();

        List<String> exceptionInfos = resultDTO.getExceptionsWhilePersisting().stream()
            .map(this::toExceptionInfo).toList();

        return new MassenimportReportDTO(
            resultDTO.couldImportAllRows(),
            resultDTO.getTotalRows(),
            resultDTO.getSkippedRows(),
            importedKinder,
            importedKontaktPersonenKind,
            resultDTO.getErrorsWhilePreparing(),
            exceptionInfos
        );
    }

    @Nullable
    private KindMassenimportDTO toKindMassenimportDTO(@Nullable Kind kind) {
        if (kind == null) {
            return null;
        }
        return new KindMassenimportDTO(
            kind.getName(),
            kind.getVorname(),
            kind.getGeschlecht(),
            kind.getGeburtsdatum(),
            kind.getSozialversicherungsNummer(),
            kind.getNationalitaet(),
            kind.getErstsprache(),
            kind.getEmail(),
            kind.getTelefon(),
            kind.getWohntyp(),
            toAdresseMassenimportDTO(kind.getAdresse()),
            toSchuleMassenimportDTO(kind.getSchule()),
            toKlasseMassenimportDTO(kind.getKlasse())
        );
    }

    @Nullable
    private KontaktPersonKindMassenimportDTO toKontaktPersonKindMassenimportDTO(
        @Nullable KontaktPersonKind kontaktPersonKind) {
        if (kontaktPersonKind == null) {
            return null;
        }
        return new KontaktPersonKindMassenimportDTO(
            kontaktPersonKind.getGeschlecht(),
            kontaktPersonKind.getName(),
            kontaktPersonKind.getVorname(),
            kontaktPersonKind.getBeziehung(),
            kontaktPersonKind.getEmail(),
            kontaktPersonKind.getTelefon(),
            kontaktPersonKind.isKorrespondenz(),
            toAdresseMassenimportDTO(kontaktPersonKind.getAdresse())
        );
    }

    @Nullable
    private AdresseMassenimportDTO toAdresseMassenimportDTO(@Nullable Adresse adresse) {
        if (adresse == null) {
            return null;
        }
        return new AdresseMassenimportDTO(
            adresse.getOrt(),
            adresse.getStrasseUndNr(),
            adresse.getAdresseZusatz(),
            adresse.getPlz(),
            adresse.getLand()
        );
    }

    @Nullable
    private SchuleMassenimportDTO toSchuleMassenimportDTO(@Nullable Schule schule) {
        if (schule == null) {
            return null;
        }
        return new SchuleMassenimportDTO(
            schule.getId(),
            schule.getSchulhaus()
        );
    }

    @Nullable
    private KlasseMassenimportDTO toKlasseMassenimportDTO(@Nullable Klasse klasse) {
        if (klasse == null) {
            return null;
        }
        return new KlasseMassenimportDTO(
            klasse.getId(),
            klasse.getBezeichnung()
        );
    }

    @NonNull
    private String toExceptionInfo(@NonNull Exception ex) {
        // e.g. "ConstraintViolationException: ..." (root cause if nested)
        String rootMsg = ExceptionUtils.getRootCauseMessage(ex);

        // fallback in case some exception returns null/blank messages
        if (rootMsg.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return rootMsg;
    }
}
