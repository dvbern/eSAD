package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.csv;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.KlasseExternal;
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
            null,
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
            null,
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
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            pupilCsvDTO.klasseId(),
            PupilCSVMapperService.toExternalKlasse(pupilCsvDTO).orElse(null)
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

    @SafeVarargs
    public static void warnIfDataIsNotEmpty(
        @NonNull String explain,
        @NonNull Pair<String, String>... fieldAndNameParis) {
        for (var fieldAndName : fieldAndNameParis) {
            if (!StringUtils.isEmpty(fieldAndName.getValue())) {
                LOG.warn(
                    "{}.Field '{}' was set to value '{}' but no id was present, ignoring item",
                    explain,
                    fieldAndName.getKey(),
                    fieldAndName.getValue());
            }
        }
    }

}
