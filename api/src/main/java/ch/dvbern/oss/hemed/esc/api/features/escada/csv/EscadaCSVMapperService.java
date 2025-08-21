package ch.dvbern.oss.hemed.esc.api.features.escada.csv;

import java.util.Optional;

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

import static ch.dvbern.oss.hemed.esc.api.features.escada.csv.EscadaCsvDTO.SCHULANLAGE_BEZEICHNUNG_COL_NAME;
import static ch.dvbern.oss.hemed.esc.api.features.escada.csv.EscadaCsvDTO.SCHULANLAGE_ID_COL_NAME;
import static ch.dvbern.oss.hemed.esc.api.features.escada.csv.EscadaCsvDTO.SCHULKREIS_BEZEICHNUNG_COL_NAME;

@Service
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class EscadaCSVMapperService {

    @NonNull
    public static Optional<SchuleExternal> toExternalSchule(
        @NonNull EscadaCsvDTO escadaCsvDTO
    ) {
        if (StringUtils.isEmpty(escadaCsvDTO.schulhausId())) {
            validateAllDataIsEmpty(
                "All Schule data MUST be null if no id is present",
                Pair.of(SCHULANLAGE_BEZEICHNUNG_COL_NAME, escadaCsvDTO.schulhausBezeichnung()),
                Pair.of(SCHULANLAGE_ID_COL_NAME, escadaCsvDTO.schulanlageId()),
                Pair.of(SCHULANLAGE_BEZEICHNUNG_COL_NAME, escadaCsvDTO.schulanlageBezeichnung()));

            warnIfDataIsNotEmpty(
                "All Schule data SHOULD be null if no id is present",
                Pair.of(SCHULKREIS_BEZEICHNUNG_COL_NAME, escadaCsvDTO.schulkreisBezeichnung()));

            return Optional.empty();
        }
        return Optional.of(new SchuleExternal(
            null,
            escadaCsvDTO.schulhausId(),
            escadaCsvDTO.schulhausBezeichnung(),
            escadaCsvDTO.schulkreisBezeichnung(),
            escadaCsvDTO.schulanlageId(),
            escadaCsvDTO.schulanlageBezeichnung()
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

    @NonNull
    public static Optional<KlasseExternal> toExternalKlasse(@NonNull EscadaCsvDTO escadaCsvDTO) {
        if (StringUtils.isEmpty(escadaCsvDTO.klasseId())) {
            validateAllDataIsEmpty(
                "All Klasse data MUST be null if no id is present",
                Pair.of(EscadaCsvDTO.KLASSE_BEZEICHNUNG_COL_NAME, escadaCsvDTO.klasseBezeichnung()),
                Pair.of(EscadaCsvDTO.KLASSE_STUFE_COL_NAME, escadaCsvDTO.klasseStufe()),
                Pair.of(EscadaCsvDTO.KLASSE_SCHULART_COL_NAME, escadaCsvDTO.klasseSchulart()));

            return Optional.empty();
        }
        return Optional.of(new KlasseExternal(
            null,
            escadaCsvDTO.klasseId(),
            escadaCsvDTO.klasseBezeichnung(),
            escadaCsvDTO.klasseStufe(),
            escadaCsvDTO.klasseSchulart(),
            escadaCsvDTO.klasseProgrammjahr(),
            escadaCsvDTO.schulhausId(),
            escadaCsvDTO.schulkreisBezeichnung()
        ));
    }

    @NonNull
    public static Optional<LehrpersonExternal> toExternalLehrperson(
        @NonNull EscadaCsvDTO escadaCsvDTO
    ) {
        if (StringUtils.isEmpty(escadaCsvDTO.klasseLehrpersonId())) {
            validateAllDataIsEmpty(
                "All KontaktPersonSchule data MUST be null if no id is present",
                Pair.of(EscadaCsvDTO.KLASSE_LEHRPERSON_ANREDE_COL_NAME, escadaCsvDTO.klasseLehrpersonAnrede()),
                Pair.of(EscadaCsvDTO.KLASSE_LEHRPERSON_NAME_COL_NAME, escadaCsvDTO.klasseLehrpersonName()),
                Pair.of(EscadaCsvDTO.KLASSE_LEHRPERSON_VORNAME_COL_NAME, escadaCsvDTO.klasseLehrpersonVorname()));

            return Optional.empty();
        }
        return Optional.of(new LehrpersonExternal(
            null,
            escadaCsvDTO.klasseLehrpersonId(),
            escadaCsvDTO.klasseLehrpersonAnrede(),
            escadaCsvDTO.klasseLehrpersonName(),
            escadaCsvDTO.klasseLehrpersonVorname(),
            1,
            escadaCsvDTO.klasseId(),
            escadaCsvDTO.klasseId()
        ));
    }

    @NonNull
    public static Optional<LehrpersonExternal> toExternalLehrperson2(
        @NonNull EscadaCsvDTO escadaCsvDTO
    ) {
        if (StringUtils.isEmpty(escadaCsvDTO.klasseLehrperson2Id())) {
            validateAllDataIsEmpty(
                "All KontaktPersonSchule2 data MUST be null if no id is present",
                Pair.of(EscadaCsvDTO.KLASSE_LEHRPERSON2_ANREDE_COL_NAME, escadaCsvDTO.klasseLehrperson2Anrede()),
                Pair.of(EscadaCsvDTO.KLASSE_LEHRPERSON2_NAME_COL_NAME, escadaCsvDTO.klasseLehrperson2Name()),
                Pair.of(EscadaCsvDTO.KLASSE_LEHRPERSON2_VORNAME_COL_NAME, escadaCsvDTO.klasseLehrperson2Vorname()));

            return Optional.empty();
        }
        return Optional.of(new LehrpersonExternal(
            null,
            escadaCsvDTO.klasseLehrperson2Id(),
            escadaCsvDTO.klasseLehrperson2Anrede(),
            escadaCsvDTO.klasseLehrperson2Name(),
            escadaCsvDTO.klasseLehrperson2Vorname(),
            2,
            escadaCsvDTO.klasseId(),
            escadaCsvDTO.klasseId()
        ));
    }

    @NonNull
    public static Optional<LehrpersonExternal> toExternalLehrperson3(
        @NonNull EscadaCsvDTO escadaCsvDTO
    ) {
        if (StringUtils.isEmpty(escadaCsvDTO.klasseLehrperson3Id())) {
            validateAllDataIsEmpty(
                "All KontaktPersonSchule3 data MUST be null if no id is present",
                Pair.of(EscadaCsvDTO.KLASSE_LEHRPERSON3_ANREDE_COL_NAME, escadaCsvDTO.klasseLehrperson3Anrede()),
                Pair.of(EscadaCsvDTO.KLASSE_LEHRPERSON3_NAME_COL_NAME, escadaCsvDTO.klasseLehrperson3Name()),
                Pair.of(EscadaCsvDTO.KLASSE_LEHRPERSON3_VORNAME_COL_NAME, escadaCsvDTO.klasseLehrperson3Vorname()));

            return Optional.empty();
        }
        return Optional.of(new LehrpersonExternal(
            null,
            escadaCsvDTO.klasseLehrperson3Id(),
            escadaCsvDTO.klasseLehrperson3Anrede(),
            escadaCsvDTO.klasseLehrperson3Name(),
            escadaCsvDTO.klasseLehrperson3Vorname(),
            3,
            escadaCsvDTO.klasseId(), // can only have 1 here
            escadaCsvDTO.klasseId()
        ));
    }

    @NonNull
    public static Optional<LehrpersonExternal> toExternalLehrperson4(
        @NonNull EscadaCsvDTO escadaCsvDTO
    ) {
        if (StringUtils.isEmpty(escadaCsvDTO.klasseLehrperson4Id())) {
            validateAllDataIsEmpty(
                "All KontaktPersonSchule4 data MUST be null if no id is present",
                Pair.of(EscadaCsvDTO.KLASSE_LEHRPERSON4_ANREDE_COL_NAME, escadaCsvDTO.klasseLehrperson4Anrede()),
                Pair.of(EscadaCsvDTO.KLASSE_LEHRPERSON4_NAME_COL_NAME, escadaCsvDTO.klasseLehrperson4Name()),
                Pair.of(EscadaCsvDTO.KLASSE_LEHRPERSON4_VORNAME_COL_NAME, escadaCsvDTO.klasseLehrperson4Vorname()));

            return Optional.empty();
        }
        return Optional.of(new LehrpersonExternal(
            null,
            escadaCsvDTO.klasseLehrperson4Id(),
            escadaCsvDTO.klasseLehrperson4Anrede(),
            escadaCsvDTO.klasseLehrperson4Name(),
            escadaCsvDTO.klasseLehrperson4Vorname(),
            4,
            escadaCsvDTO.klasseId(),// can only have 1 here
            escadaCsvDTO.klasseId()
        ));
    }

    @NonNull
    public static Optional<KindExternal> toExternalKind(@NonNull EscadaCsvDTO escadaCsvDTO) {
        if (StringUtils.isEmpty(escadaCsvDTO.schuelerId())) {
            validateAllDataIsEmpty(
                "All Kind data MUST be null if no id is present",
                Pair.of(EscadaCsvDTO.SCHUELER_NAME_COL_NAME, escadaCsvDTO.schuelerName()),
                Pair.of(EscadaCsvDTO.SCHUELER_VORNAME_COL_NAME, escadaCsvDTO.schuelerVorname()));
            return Optional.empty();
        }

        return Optional.of(new KindExternal(
            null,
            escadaCsvDTO.schuelerId(),
            escadaCsvDTO.wohntyp(),
            escadaCsvDTO.schuelerName(),
            escadaCsvDTO.schuelerVorname(),
            escadaCsvDTO.schuelerGeburtsdatum(),
            escadaCsvDTO.schuelerGeschlecht(),
            escadaCsvDTO.schuelerSozialversicheurng(),
            escadaCsvDTO.schuelerAdresse(),
            escadaCsvDTO.schuelerAdresse1(),
            escadaCsvDTO.schuelerAdresse2(),
            escadaCsvDTO.schuelerPlz(),
            escadaCsvDTO.schuelerOrt(),
            escadaCsvDTO.schuelerMobile(),
            escadaCsvDTO.schuelerEmail(),
            escadaCsvDTO.schuelerNationalitaet(),
            escadaCsvDTO.schuelerStaatcodeIso(),
            escadaCsvDTO.schuelerSprache(),
            escadaCsvDTO.schuelerErstsprache(),
            escadaCsvDTO.schulhausId(),
            escadaCsvDTO.klasseId(),
            EscadaCSVMapperService.toExternalKlasse(escadaCsvDTO).orElse(null)
        ));
    }

    @NonNull
    public static Optional<KontaktPersonKindExternal> toExternalKontaktPersonKind(
        @NonNull EscadaCsvDTO escadaCsvDTO
    ) {
        if (StringUtils.isEmpty(escadaCsvDTO.adressierungNr())) {
            validateAllDataIsEmpty(
                "All KontaktPersonKind data MUST be null if no id is present",
                Pair.of(EscadaCsvDTO.ADRESSIERUNG_ANREDE_COL_NAME, escadaCsvDTO.adressierungAnrede()),
                Pair.of(EscadaCsvDTO.ADRESSIERUNG_NAME_COL_NAME, escadaCsvDTO.adressierungName()),
                Pair.of(EscadaCsvDTO.ADRESSIERUNG_VORNAME_COL_NAME, escadaCsvDTO.adressierungVorname())
            );

            return Optional.empty();
        }

        if (escadaCsvDTO.schuelerId() == null) {
            LOG.warn("ESCADA IMPORT: KontaktpersonKind with id '{}' has no SchuelerId, ignoring", escadaCsvDTO.adressierungNr());
            return Optional.empty();
        }

        return Optional.of(new KontaktPersonKindExternal(
            null,
            escadaCsvDTO.schuelerId(), // this will later be used to collect all connected Kinder
            escadaCsvDTO.schuelerId(),
            escadaCsvDTO.adressierungNr(),
            escadaCsvDTO.adressierungAnrede(),
            escadaCsvDTO.adressierungName(),
            escadaCsvDTO.adressierungVorname(),
            escadaCsvDTO.adressierungStrasse(),
            escadaCsvDTO.adressierungHausnummer(),
            escadaCsvDTO.adressierungPlz(),
            escadaCsvDTO.adressierungOrt(),
            escadaCsvDTO.adressierungTelefonPrivat(),
            escadaCsvDTO.adressierungTelefonGeschaeft(),
            escadaCsvDTO.adressierungNatel(),
            escadaCsvDTO.adressierungEmail()
        ));
    }

    @NonNull
    public static Optional<KontaktPersonKindExternal> toExternalKontaktPersonKind2(
        @NonNull EscadaCsvDTO escadaCsvDTO
    ) {
        if (StringUtils.isEmpty(escadaCsvDTO.adressierungNr2())) {
            validateAllDataIsEmpty(
                "All KontaktPersonKind data MUST be null if no id is present",
                Pair.of(EscadaCsvDTO.ADRESSIERUNG_ANREDE2_COL_NAME, escadaCsvDTO.adressierungAnrede2()),
                Pair.of(EscadaCsvDTO.ADRESSIERUNG_NAME2_COL_NAME, escadaCsvDTO.adressierungName2()),
                Pair.of(EscadaCsvDTO.ADRESSIERUNG_VORNAME2_COL_NAME, escadaCsvDTO.adressierungVorname2())
            );

            return Optional.empty();
        }
        return Optional.of(new KontaktPersonKindExternal(
            null,
            escadaCsvDTO.schuelerId(), // this will later be used to collect all connected Kinder
            escadaCsvDTO.schuelerId(),
            escadaCsvDTO.adressierungNr2(),
            escadaCsvDTO.adressierungAnrede2(),
            escadaCsvDTO.adressierungName2(),
            escadaCsvDTO.adressierungVorname2(),
            escadaCsvDTO.adressierungStrasse2(),
            escadaCsvDTO.adressierungHausnummer2(),
            escadaCsvDTO.adressierungPlz2(),
            escadaCsvDTO.adressierungOrt2(),
            escadaCsvDTO.adressierungTelefonPrivat2(),
            escadaCsvDTO.adressierungTelefonGeschaeft2(),
            escadaCsvDTO.adressierungNatel2(),
            escadaCsvDTO.adressierungEmail2()
        ));
    }
}
