package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.csv;

import java.util.Optional;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternal;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.external.KontaktPersonKindExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.KlasseExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.external.LehrpersonExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.schulenexternal.SchuleExternal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PupilCSVMapperService {

    @NonNull
    public static Optional<LehrpersonExternal> toExternalLehrperson1(
        @NonNull PupilCsvDTO pupilCsvDTO
    ) {
        if (StringUtils.isEmpty(pupilCsvDTO.lehrperson1Id())) {
            validateAllDataIsEmpty(
                "All Lehrperson data MUST be null if no id is present",
                Pair.of(PupilCsvDTO.KLASSE_LEHRPERSON_1_ANREDE_COL_NAME, pupilCsvDTO.lehrperson1Anrede()),
                Pair.of(PupilCsvDTO.KLASSE_LEHRPERSON_1_NAME_COL_NAME, pupilCsvDTO.lehrperson1Name()),
                Pair.of(PupilCsvDTO.KLASSE_LEHRPERSON_1_VORNAME_COL_NAME, pupilCsvDTO.lehrperson1Vorname())
            );
            return Optional.empty();
        }
        return Optional.of(new LehrpersonExternal(
            null,
            pupilCsvDTO.lehrperson1Id(),
            pupilCsvDTO.lehrperson1Anrede(),
            pupilCsvDTO.lehrperson1Name(),
            pupilCsvDTO.lehrperson1Vorname(),
            1,
            pupilCsvDTO.klasseId(),
            pupilCsvDTO.klasseId()
        ));
    }

    @NonNull
    public static Optional<LehrpersonExternal> toExternalLehrperson2(
        @NonNull PupilCsvDTO pupilCsvDTO
    ) {
        if (StringUtils.isEmpty(pupilCsvDTO.lehrperson2Id())) {
            validateAllDataIsEmpty(
                "All Lehrperson data MUST be null if no id is present",
                Pair.of(PupilCsvDTO.KLASSE_LEHRPERSON_2_ANREDE_COL_NAME, pupilCsvDTO.lehrperson2Anrede()),
                Pair.of(PupilCsvDTO.KLASSE_LEHRPERSON_2_NAME_COL_NAME, pupilCsvDTO.lehrperson2Name()),
                Pair.of(PupilCsvDTO.KLASSE_LEHRPERSON_2_VORNAME_COL_NAME, pupilCsvDTO.lehrperson2Vorname())
            );
            return Optional.empty();
        }
        return Optional.of(new LehrpersonExternal(
            null,
            pupilCsvDTO.lehrperson2Id(),
            pupilCsvDTO.lehrperson2Anrede(),
            pupilCsvDTO.lehrperson2Name(),
            pupilCsvDTO.lehrperson2Vorname(),
            2,
            pupilCsvDTO.klasseId(),
            pupilCsvDTO.klasseId()
        ));
    }

    @NonNull
    public static Optional<SchuleExternal> toExternalSchule(
        @NonNull PupilCsvDTO pupilCsvDTO
    ) {
        if (StringUtils.isEmpty(pupilCsvDTO.schuleId())) {
            validateAllDataIsEmpty(
                "All Schule data MUST be null if no id is present",
                Pair.of(PupilCsvDTO.SCHULE_BEZEICHNUNG_COL_NAME, pupilCsvDTO.schuleBezeichnung())
            );

            return Optional.empty();
        }
        return Optional.of(new SchuleExternal(
            null,
            pupilCsvDTO.schuleId(),
            pupilCsvDTO.schuleBezeichnung(),
            pupilCsvDTO.schulkreis(),
            null,
            null
        ));
    }

    @NonNull
    public static Optional<KlasseExternal> toExternalKlasse(@NonNull PupilCsvDTO pupilCsvDTO) {
        if (StringUtils.isEmpty(pupilCsvDTO.klasseId())) {
            validateAllDataIsEmpty(
                "All Klasse data MUST be null if no id is present",
                Pair.of(PupilCsvDTO.KLASSE_BEZEICHNUNG_COL_NAME, pupilCsvDTO.klasseBezeichnung()),
                Pair.of(PupilCsvDTO.KLASSE_STUFE_COL_NAME, pupilCsvDTO.klasseStufe()));

            return Optional.empty();
        }
        return Optional.of(new KlasseExternal(
            null,
            pupilCsvDTO.klasseId(),
            pupilCsvDTO.klasseBezeichnung(),
            pupilCsvDTO.klasseStufe(),
            pupilCsvDTO.klasseSchulart(),
            pupilCsvDTO.klasseProgrammjahr(),
            pupilCsvDTO.schuleId(),
            null
        ));
    }

    @NonNull
    public static Optional<KindExternal> toExternalKind(@NonNull PupilCsvDTO pupilCsvDTO) {
        if (StringUtils.isEmpty(pupilCsvDTO.schuelerId())) {
            validateAllDataIsEmpty(
                "All Kind data MUST be null if no id is present",
                Pair.of(PupilCsvDTO.SCHUELER_NAME_COL_NAME, pupilCsvDTO.schuelerName()),
                Pair.of(PupilCsvDTO.SCHUELER_VORNAME_COL_NAME, pupilCsvDTO.schuelerVorname()));
            return Optional.empty();
        }

        return Optional.of(new KindExternal(
            null,
            pupilCsvDTO.schuelerId(),
            null,
            pupilCsvDTO.schuelerName(),
            pupilCsvDTO.schuelerVorname(),
            pupilCsvDTO.schuelerGeburtsdatum(),
            pupilCsvDTO.schuelerGeschlecht(),
            null,
            pupilCsvDTO.schuelerAdresseStrasse(),
            null,
            null,
            pupilCsvDTO.schuelerAdressePlz(),
            pupilCsvDTO.schuelerAdresseOrt(),
            pupilCsvDTO.schuelerTelefon(),
            pupilCsvDTO.schuelerEmail(),
            pupilCsvDTO.schuelerNationalitaet(),
            pupilCsvDTO.schuelerStaatcodeIso(),
            null,
            pupilCsvDTO.schuelerErstsprache(),
            pupilCsvDTO.schuleId(),
            pupilCsvDTO.klasseId(),
            PupilCSVMapperService.toExternalKlasse(pupilCsvDTO).orElse(null)
        ));
    }

    @NonNull
    public static Optional<KontaktPersonKindExternal> toExternalKontaktPersonKind1(
        @NonNull PupilCsvDTO pupilCsvDTO
    ) {
        // Default callback logs immediately; only used for one-off mappings (e.g. tests).
        // Bulk imports MUST use the overload below with an aggregating callback to avoid flooding the log
        // with one line per CSV row (see ESC-... log volume incident).
        return toExternalKontaktPersonKind1(pupilCsvDTO, id -> LOG.warn(
            "PUPIL IMPORT: KontaktpersonKind with id '{}' has no SchuelerId, ignoring",
            id));
    }

    @NonNull
    public static Optional<KontaktPersonKindExternal> toExternalKontaktPersonKind1(
        @NonNull PupilCsvDTO pupilCsvDTO,
        @NonNull Consumer<String> missingSchuelerIdHandler
    ) {
        if (StringUtils.isEmpty(pupilCsvDTO.kontaktperson1Id())) {
            validateAllDataIsEmpty(
                "All KontaktPersonKind data MUST be null if no id is present",
                Pair.of(PupilCsvDTO.KONTAKTPERSON_1_ANREDE_COL_NAME, pupilCsvDTO.kontaktperson1Anrede()),
                Pair.of(PupilCsvDTO.KONTAKTPERSON_1_NAME_COL_NAME, pupilCsvDTO.kontaktperson1Name()),
                Pair.of(PupilCsvDTO.KONTAKTPERSON_1_VORNAME_COL_NAME, pupilCsvDTO.kontaktperson1Vorname())
            );

            return Optional.empty();
        }

        if (pupilCsvDTO.schuelerId() == null) {
            missingSchuelerIdHandler.accept(pupilCsvDTO.kontaktperson1Id());
            return Optional.empty();
        }

        return Optional.of(new KontaktPersonKindExternal(
            null,
            pupilCsvDTO.schuelerId(), // this will later be used to collect all connected Kinder
            pupilCsvDTO.schuelerId(),
            pupilCsvDTO.kontaktperson1Id(),
            pupilCsvDTO.kontaktperson1Anrede(),
            pupilCsvDTO.kontaktperson1Beziehung(),
            pupilCsvDTO.kontaktperson1Name(),
            pupilCsvDTO.kontaktperson1Vorname(),
            pupilCsvDTO.kontaktperson1Strasse(),
            pupilCsvDTO.kontaktperson1Hausnummer(),
            pupilCsvDTO.kontaktperson1Plz(),
            pupilCsvDTO.kontaktperson1Ort(),
            pupilCsvDTO.kontaktperson1TelefonPrivat(),
            pupilCsvDTO.kontaktperson1TelefonGeschaeft(),
            pupilCsvDTO.kontaktperson1TelefonMobile(),
            pupilCsvDTO.kontaktperson1Email()
        ));
    }

    @NonNull
    public static Optional<KontaktPersonKindExternal> toExternalKontaktPersonKind2(
        @NonNull PupilCsvDTO pupilCsvDTO
    ) {
        // Default callback logs immediately; only used for one-off mappings (e.g. tests).
        // Bulk imports MUST use the overload below with an aggregating callback to avoid flooding the log
        // with one line per CSV row (see ESC-... log volume incident).
        return toExternalKontaktPersonKind2(pupilCsvDTO, id -> LOG.warn(
            "PUPIL IMPORT: KontaktpersonKind with id '{}' has no SchuelerId, ignoring",
            id));
    }

    @NonNull
    public static Optional<KontaktPersonKindExternal> toExternalKontaktPersonKind2(
        @NonNull PupilCsvDTO pupilCsvDTO,
        @NonNull Consumer<String> missingSchuelerIdHandler
    ) {
        if (StringUtils.isEmpty(pupilCsvDTO.kontaktperson2Id())) {
            validateAllDataIsEmpty(
                "All KontaktPersonKind data MUST be null if no id is present",
                Pair.of(PupilCsvDTO.KONTAKTPERSON_2_ANREDE_COL_NAME, pupilCsvDTO.kontaktperson2Anrede()),
                Pair.of(PupilCsvDTO.KONTAKTPERSON_2_NAME_COL_NAME, pupilCsvDTO.kontaktperson2Name()),
                Pair.of(PupilCsvDTO.KONTAKTPERSON_2_VORNAME_COL_NAME, pupilCsvDTO.kontaktperson2Vorname())
            );

            return Optional.empty();
        }

        if (pupilCsvDTO.schuelerId() == null) {
            missingSchuelerIdHandler.accept(pupilCsvDTO.kontaktperson2Id());
            return Optional.empty();
        }

        return Optional.of(new KontaktPersonKindExternal(
            null,
            pupilCsvDTO.schuelerId(), // this will later be used to collect all connected Kinder
            pupilCsvDTO.schuelerId(),
            pupilCsvDTO.kontaktperson2Id(),
            pupilCsvDTO.kontaktperson2Anrede(),
            pupilCsvDTO.kontaktperson2Beziehung(),
            pupilCsvDTO.kontaktperson2Name(),
            pupilCsvDTO.kontaktperson2Vorname(),
            pupilCsvDTO.kontaktperson2Strasse(),
            pupilCsvDTO.kontaktperson2Hausnummer(),
            pupilCsvDTO.kontaktperson2Plz(),
            pupilCsvDTO.kontaktperson2Ort(),
            pupilCsvDTO.kontaktperson2TelefonPrivat(),
            pupilCsvDTO.kontaktperson2TelefonGeschaeft(),
            pupilCsvDTO.kontaktperson2TelefonMobile(),
            pupilCsvDTO.kontaktperson2Email()
        ));
    }

    @SafeVarargs
    public static void validateAllDataIsEmpty(
        @NonNull String explain,
        @NonNull Pair<String, String>... fieldAndNameParis) {
        for (var fieldAndName : fieldAndNameParis) {
            Validate.isTrue(
                StringUtils.isEmpty(fieldAndName.getValue()),
                explain + " but there was a value '" + fieldAndName.getKey() + "' that is not null");
        }
    }
}
