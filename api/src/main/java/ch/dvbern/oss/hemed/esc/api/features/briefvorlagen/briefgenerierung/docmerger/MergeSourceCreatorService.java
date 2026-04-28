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

package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.math.GanzZahl;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Audiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammSimpel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenAugeEinzelnResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.kinder.NachkontrolleService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtilImpl;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.MergeSourceDummyDataCreator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
public class MergeSourceCreatorService {
    private final TL tl;
    private final DossierService dossierService;
    private final UntersuchungService untersuchungService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final NachkontrolleService nachkontrolleService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final AppClock appClock;
    private final BenutzerService benutzerService;
    private static final String BRIEF_PREFIX = "brief";
    private static final PrintUtilImpl PRINT_UTIL = new PrintUtilImpl("");
    private final KontaktPersonKindService kontaktPersonKindService;

    @NonNull
    public RootMergeSource fromAnlass(@NonNull Einladung einladung, @Nullable Arzt arzt) {
        Dossier dossier = einladung.getDossier();
        Nachkontrolle nachkontrolle = nachkontrolleService.getOrCreateByDossier(dossier.getId());

        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        List<UntersuchungImpfungIntern> impfungList =
            untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId());
        List<UntersuchungImpfstatusKrankheit> impfstatusList =
            untersuchungImpfstatusKrankheitService.findByUntersuchung(untersuchung.getId());

        Benutzer currentBenutzer = benutzerService.getCurrentBenutzerOrThrowException();

        KontaktPersonKind kontaktPersonKind = kontaktPersonKindService.findByKind(dossier.getKind().getId())
            .stream()
            .filter(KontaktPersonKind::isKorrespondenz)
            .findFirst()
            .orElse(null);

        return toRootMergeSourceWithAnlass(
            dossier,
            untersuchung.getEinladungRequired(),
            nachkontrolle,
            impfstatusList,
            impfungList,
            currentBenutzer,
            appClock.offsetDateTime().now(),
            tl,
            arzt,
            kontaktPersonKind);
    }

    @NonNull
    public RootMergeSource fromKind(@NonNull Kind kind, @Nullable Arzt arzt, @NonNull Benutzer benutzer) {

        Dossier dossier = dossierService.getOrCreateByKind(kind.getId());
        Nachkontrolle nachkontrolle = nachkontrolleService.getOrCreateByDossier(dossier.getId());
        KontaktPersonKind kontaktPersonKind = kontaktPersonKindService.findByKind(kind.getId())
            .stream()
            .filter(KontaktPersonKind::isKorrespondenz)
            .findFirst()
            .orElse(null);

        return toRootMergeSource(
            dossier,
            nachkontrolle,
            benutzer,
            appClock.offsetDateTime().now(),
            tl,
            arzt,
            kontaktPersonKind);
    }

    @NonNull
    public RootMergeSource testMergeSourceForKind(@NonNull Kind kind) {
        OffsetDateTime now = appClock.offsetDateTime().now();
        Dossier dossier = MergeSourceDummyDataCreator.getTestDossier(kind, now);
        Nachkontrolle nachkontrolle = MergeSourceDummyDataCreator.getTestNachkontrolle(dossier);
        Benutzer currentBenutzer = benutzerService.getCurrentBenutzerOrThrowException();
        Arzt arzt = MergeSourceDummyDataCreator.getTestArzt();

        return toRootMergeSource(
            dossier,
            nachkontrolle,
            currentBenutzer,
            now,
            tl,
            arzt,
            null);
    }

    @NonNull
    public RootMergeSource testMergeSourceForAnlass(@NonNull Kind kind) {
        OffsetDateTime now = appClock.offsetDateTime().now();
        Dossier dossier = MergeSourceDummyDataCreator.getTestDossier(kind, now);
        Nachkontrolle nachkontrolle = MergeSourceDummyDataCreator.getTestNachkontrolle(dossier);
        Anlass anlass = MergeSourceDummyDataCreator.getTestAnlass(now);
        List<UntersuchungImpfungIntern> impfungList = MergeSourceDummyDataCreator.getTestImpfungList(dossier, anlass);
        List<UntersuchungImpfstatusKrankheit> impfstatusList = MergeSourceDummyDataCreator.getTestImpfStatusList();
        Benutzer currentBenutzer = benutzerService.getCurrentBenutzerOrThrowException();
        Untersuchung untersuchung = MergeSourceDummyDataCreator.getTestUntersuchung();
        Arzt arzt = MergeSourceDummyDataCreator.getTestArzt();
        KontaktPersonKind kontaktPersonKind = MergeSourceDummyDataCreator.getTestKontaktPersonKind(kind);

        Einladung einladung = new Einladung();
        einladung.setAnlass(anlass);
        einladung.setUntersuchung(untersuchung);
        einladung.setDossier(dossier);

        return toRootMergeSourceWithAnlass(
            dossier,
            einladung,
            nachkontrolle,
            impfstatusList,
            impfungList,
            currentBenutzer,
            now,
            tl,
            arzt,
            kontaktPersonKind);
    }

    @NonNull
    public static RootMergeSource toRootMergeSource(
        @NonNull Dossier dossier,
        @NonNull Nachkontrolle nachkontrolle,
        @NonNull Benutzer currentBenutzer,
        @NonNull OffsetDateTime now,
        @NonNull TL tl,
        @Nullable Arzt arzt,
        @Nullable KontaktPersonKind kontaktPersonKind
    ) {
        return new RootMergeSource(
            toAllgemeinDTO(now, currentBenutzer, tl),
            toKindDTO(dossier, nachkontrolle, tl, kontaktPersonKind),
            null,
            toExternerArztDTO(arzt)
        );
    }

    @NonNull
    public static RootMergeSource toRootMergeSourceWithAnlass(
        @NonNull Dossier dossier,
        @NonNull Einladung einladung,
        @NonNull Nachkontrolle nachkontrolle,
        @NonNull List<UntersuchungImpfstatusKrankheit> impfstatusList,
        @NonNull List<UntersuchungImpfungIntern> impfungList,
        @NonNull Benutzer currentBenutzer,
        @NonNull OffsetDateTime now,
        @NonNull TL tl,
        @Nullable Arzt arzt,
        @Nullable KontaktPersonKind kontaktPersonKind
    ) {
        Untersuchung untersuchung = einladung.getUntersuchung();
        AnlassConfig anlassConfig = einladung.getAnlass().getAnlassConfig();
        Objects.requireNonNull(untersuchung);

        return new RootMergeSource(
            toAllgemeinDTO(now, currentBenutzer, tl),
            toKindDTO(dossier, nachkontrolle, tl, kontaktPersonKind),
            new AnlassMergeSource(
                toAnlassMetadata(einladung.getAnlass()),
                toImpfstatusListDTO(impfstatusList, tl),
                toImpfungDTO(impfungList, tl),
                toSehenDTO(untersuchung, anlassConfig, tl),
                toHoerenAudiogrammDTO(untersuchung, anlassConfig, tl),
                toHoerenOtoskopieDTO(untersuchung, anlassConfig, tl),
                toGroesseGewichtDTO(untersuchung.getUntersuchungGroesseGewicht(), tl),
                toUntersuchungArztDTO(untersuchung.getUntersuchungArztBeurteilung(), tl)
            ),
            toExternerArztDTO(arzt)
        );
    }

    @NonNull
    private static AllgemeinDocMergerDTO toAllgemeinDTO(
        @NonNull OffsetDateTime now,
        @NonNull Benutzer currentBenutzer,
        @NonNull TL tl
    ) {
        return new AllgemeinDocMergerDTO(
            PRINT_UTIL.formatDate(now),
            PRINT_UTIL.trimToPlaceholder(currentBenutzer.getTitel()),
            PRINT_UTIL.trimVornameNameToPlaceholder(currentBenutzer),
            PRINT_UTIL.trimToPlaceholder(currentBenutzer.getEmail().toString()),
            currentBenutzer.getTelefon() == null ?
                PRINT_UTIL.placeholder :
                PRINT_UTIL.trimToPlaceholder(currentBenutzer.getTelefon().getFormattedPhoneNumber()),
            currentBenutzer.getFachRollenCombined()
                .stream()
                .map(tl::translateValue)
                .collect(Collectors.joining(", "))
        );
    }

    @NonNull
    private static KindDocMergerDTO toKindDTO(
        @NonNull Dossier dossier,
        @NonNull Nachkontrolle nachkontrolle,
        @NonNull TL tl,
        @Nullable KontaktPersonKind kontaktPersonKind
    ) {
        Kind kind = dossier.getKind();
        return new KindDocMergerDTO(
            kind.getVorname(),
            kind.getName(),
            PRINT_UTIL.trimToPlaceholder(kind.getGeburtsdatum()),
            formatAddress(kind.getAdresse()),
            PRINT_UTIL.trimNameVornameToPlaceholder(dossier.getZustaendigArztFromDossierOrSchule()),
            KinderdossierPrintUtil.extractKindSchuleDaten(kind, Schule::getSchulhaus),
            KinderdossierPrintUtil.extractKindKlasseDaten(kind, klasse -> tl.translateValue(klasse.getKlassenstufe())),
            KinderdossierPrintUtil.extractKindKlasseDaten(kind, Klasse::getBezeichnung),
            getNachkontrolle(nachkontrolle, tl),
            kontaktPersonKind == null ? kind.getName() : kontaktPersonKind.getName(),
            kontaktPersonKind == null ? kind.getVorname() : kontaktPersonKind.getVorname());
    }

    @NonNull
    private static AnlassMetadatenDocMergerDTO toAnlassMetadata(@NonNull Anlass anlass) {
        return new AnlassMetadatenDocMergerDTO(
            PRINT_UTIL.formatDate(anlass.getDatum()),
            PRINT_UTIL.formatTime(anlass.getZeitVonBis() != null ? anlass.getZeitVonBis().getVon() : null),
            PRINT_UTIL.trimVornameNameToPlaceholder(anlass.getMpa()),
            PRINT_UTIL.trimVornameNameToPlaceholder(anlass.getArzt())
        );
    }

    @NonNull
    private static String getNachkontrolle(@NonNull Nachkontrolle nachkontrolle, @NonNull TL tl) {
        List<String> elements = new ArrayList<>();

        AnlassFlags anlassFlags = nachkontrolle.getAnlassFlags();
        if (anlassFlags.isNachkontrolleHoeren()) {
            elements.add(tl.translate("brief.nachkontrolle.nachkontrolleHoeren"));
        }
        if (anlassFlags.isNachkontrolleAnderes()) {
            elements.add(tl.translate("brief.nachkontrolle.nachkontrolleAnderes"));
        }
        if (anlassFlags.isNachkontrolleGroesseGewicht()) {
            elements.add(tl.translate("brief.nachkontrolle.nachkontrolleGroesseGewicht"));
        }
        if (anlassFlags.isNachkontrolleMotorik()) {
            elements.add(tl.translate("brief.nachkontrolle.nachkontrolleMotorik"));
        }
        if (anlassFlags.isNachkontrolleSehen()) {
            elements.add(tl.translate("brief.nachkontrolle.nachkontrolleSehen"));
        }
        if (anlassFlags.isNachkontrolleSprache()) {
            elements.add(tl.translate("brief.nachkontrolle.nachkontrolleSprache"));
        }
        return String.join(", ", elements);
    }

    @NonNull
    private static List<ImpfstatusDocMergerDTO> toImpfstatusListDTO(
        @NonNull List<UntersuchungImpfstatusKrankheit> impfstatusList,
        @NonNull TL tl
    ) {
        List<ImpfstatusDocMergerDTO> impfstatusDocMergerDTO = new ArrayList<>();

        for (Krankheit krankheit : Krankheit.values()) {
            impfstatusList.stream()
                .filter(impfstatus -> impfstatus.getKrankheit() == krankheit)
                .findFirst()
                .ifPresentOrElse(
                    impfstatus -> impfstatusDocMergerDTO.add(toImpfstatusDTO(krankheit, impfstatus, tl)),
                    () -> impfstatusDocMergerDTO.add(toImpfstatusDTO(krankheit, null, tl)));
        }

        return impfstatusDocMergerDTO;
    }

    @NonNull
    static String formatAddress(@Nullable Adresse adresse) {
        String adresseFormat = PRINT_UTIL.getPlaceholder();
        if (adresse != null) {
            // create whitespace free, newline separated address string
            adresseFormat = Stream.of(
                    StringUtils.trimToEmpty(adresse.getStrasseUndNr()),
                    StringUtils.trimToEmpty(adresse.getAdresseZusatz()),
                    String.join(
                        " ",
                        StringUtils.trimToEmpty(adresse.getPlz()),
                        StringUtils.trimToEmpty(adresse.getOrt())
                    ).trim()
                ).filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));

            if (adresseFormat.isBlank()) {
                adresseFormat = PRINT_UTIL.getPlaceholder();
            }
        }
        return adresseFormat;
    }

    @NonNull
    private static ImpfungDocMergerDTO toImpfungDTO(
        @NonNull List<UntersuchungImpfungIntern> impfungInternList,
        @NonNull TL tl
    ) {
        String impfungListString = impfungInternList
            .stream()
            .sorted(Comparator.comparing(
                UntersuchungImpfungIntern::getDatumImpfung,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .map(impfung -> String.format(
                "%s %s (%s)",
                PRINT_UTIL.trimToPlaceholder(impfung.getDatumImpfung()),
                impfung.getImpfstoff().getName(),
                impfung.getImpfstoff()
                    .getKrankheiten()
                    .stream()
                    .map(tl::translateValue)
                    .collect(Collectors.joining(", "))))
            .collect(Collectors.joining("\n"));

        return new ImpfungDocMergerDTO(impfungListString);
    }

    @NonNull
    private static ImpfstatusDocMergerDTO toImpfstatusDTO(
        @NonNull Krankheit krankheit,
        @Nullable UntersuchungImpfstatusKrankheit impfstatus,
        @NonNull TL tl
    ) {

        if (impfstatus == null) {
            return new ImpfstatusDocMergerDTO(krankheit.name(), null, null, null, null, null, null);
        }

        return new ImpfstatusDocMergerDTO(
            krankheit.name(),
            tl.translateValue(krankheit),
            PRINT_UTIL.trimToPlaceholder(impfstatus.getAnzahlImpfungen()),
            PRINT_UTIL.trimToPlaceholder(impfstatus.getDatumLetzteImpfung(), "MM/yyyy"),
            PRINT_UTIL.trimToPlaceholder(impfstatus.isImpfschutzGegeben(), tl),
            PRINT_UTIL.trimToPlaceholder(impfstatus.isImpfempfehlung(), tl),
            PRINT_UTIL.trimToPlaceholder(impfstatus.isImpfentscheid(), tl)
        );
    }

    @NonNull
    private static SehenDocMergerDTO toSehenDTO(
        @NonNull Untersuchung untersuchung,
        @NonNull AnlassConfig anlassConfig,
        @NonNull TL tl
    ) {
        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = untersuchung.getUntersuchungSehenBeurteilung();
        if (untersuchungSehenBeurteilung == null) {
            return new SehenDocMergerDTO(
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder());
        }

        return new SehenDocMergerDTO(
            getEnumValue(
                "brief.eltern",
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getBrilleLinsen(),
                    BrilleLinsenResultat.class),
                tl),
            getEnumValue(
                "brief.arzt",
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getBrilleLinsen(),
                    BrilleLinsenResultat.class),
                tl),
            getEnumValue(
                "brief.eltern",
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getStereotest(),
                    StereotestResultat.class),
                tl),
            getEnumValue(
                "brief.arzt",
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getStereotest(),
                    StereotestResultat.class),
                tl),
            getFarbeSehen(
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getFarbsehen(),
                    FarbsehenResultat.class), untersuchung.getUntersuchungMassnahmen(), tl),
            getEnumValue(
                BRIEF_PREFIX,
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getVerwendeteTafel(),
                    VerwendetTafelResultat.class),
                tl),
            getVisusString(
                ValueEnhancedUtil.getEnumSetOrEmpty(
                    untersuchungSehenBeurteilung.getFernvisus(),
                    DistanzSehenResultat.class), tl),
            PRINT_UTIL.trimToPlaceholder(untersuchungSehenBeurteilung.getFernvisusRechts()),
            PRINT_UTIL.trimToPlaceholder(untersuchungSehenBeurteilung.getFernvisusLinks()),
            getEnumValue(
                BRIEF_PREFIX,
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getFernvisusAugeLinks(),
                    DistanzSehenAugeEinzelnResultat.class),
                tl),
            getEnumValue(
                BRIEF_PREFIX,
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getFernvisusAugeRechts(),
                    DistanzSehenAugeEinzelnResultat.class),
                tl),
            getVisusString(
                ValueEnhancedUtil.getEnumSetOrEmpty(
                    untersuchungSehenBeurteilung.getNahvisus(),
                    DistanzSehenResultat.class), tl),
            PRINT_UTIL.trimToPlaceholder(untersuchungSehenBeurteilung.getNahvisusRechts()),
            PRINT_UTIL.trimToPlaceholder(untersuchungSehenBeurteilung.getNahvisusLinks()),
            ValueEnhancedUtil.getEnumSetOrEmpty(untersuchungSehenBeurteilung.getStrabismus(), StrabismusResultat.class)
                .stream().map(strabismus -> getEnumValue(BRIEF_PREFIX, strabismus, tl))
                .collect(Collectors.joining(", ")),
            getEnumValue(
                BRIEF_PREFIX, ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getStrabismusAuspraegung(),
                    StrabismusAuspraegungResultat.class), tl),
            getEnumValue(
                BRIEF_PREFIX, ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getStrabismusAuftreten(),
                    StrabismusAuftretenResultat.class), tl),
            getEnumValue(
                BRIEF_PREFIX,
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getInspektion(),
                    InspektionKonvergenzResultat.class),
                tl),
            getEnumValue(
                BRIEF_PREFIX,
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getKonvergenz(),
                    InspektionKonvergenzResultat.class),
                tl),
            getSehenBeurteilung(untersuchung, anlassConfig, tl),
            getEnumValue(
                BRIEF_PREFIX,
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehenBeurteilung.getMotilitaet(),
                    MotilitaetResultat.class),
                tl),
            ValueEnhancedUtil.getEnumSetOrEmpty(untersuchungSehenBeurteilung.getEmpfehlung(), SehenEmpfehlung.class)
                .stream().map(empfehlung -> getEnumValue(BRIEF_PREFIX, empfehlung, tl))
                .collect(Collectors.joining(", "))
        );
    }

    @NonNull
    private static String getFarbeSehen(
        @Nullable FarbsehenResultat farbeSehen,
        @Nullable UntersuchungMassnahmen untersuchungMassnahmen,
        @NonNull TL tl) {
        if (farbeSehen == FarbsehenResultat.UNSICHER
            && untersuchungMassnahmen != null
            && untersuchungMassnahmen.getNachkontrolle().isNachkontrolleSehen()) {
            return tl.translate(BRIEF_PREFIX + ".farbsehenUnsicherMitNachkontrolle");
        }

        return getEnumValue(BRIEF_PREFIX, farbeSehen, tl);
    }

    @NonNull
    private static String getSehenBeurteilung(
        @NonNull Untersuchung untersuchung,
        @NonNull AnlassConfig anlassConfig,
        @NonNull TL tl
    ) {
        UntersuchungMassnahmen untersuchungMassnahmen = untersuchung.getUntersuchungMassnahmen();
        if (untersuchungMassnahmen != null && untersuchungMassnahmen.isBereitsBehandeltSehen()) {
            return tl.translate(BRIEF_PREFIX + ".beurteilungSehen.bereitsInBehandlung");
        }

        ZusammenfassungUtilImpl helper = new ZusammenfassungUtilImpl(anlassConfig, untersuchung);
        if (helper.isFarbsehen(FarbsehenResultat.NORMAL)
            && helper.isStereotest(StereotestResultat.NORMAL)
            && helper.isBrilleLinsen(BrilleLinsenResultat.OHNE_KEINE)
            && helper.isFernvisus(DistanzSehenResultat.NORMAL)
            && helper.isNahvisus(DistanzSehenResultat.NORMAL)
            && helper.isStrabismus(StrabismusResultat.KEIN_STRABISMUS)
            && helper.isInspektion(InspektionKonvergenzResultat.UNAUFFAELLIG)
            && helper.isKonvergenz(InspektionKonvergenzResultat.UNAUFFAELLIG)
            && helper.isMotilitaet(MotilitaetResultat.NORMAL)
        ) {
            return tl.translate("brief.beurteilungSehen.normal");
        }

        if (helper.isFarbsehen(FarbsehenResultat.NICHT_GEPRUEFT)
            && helper.isStereotest(StereotestResultat.NICHT_GEPRUEFT)
            && helper.isBrilleLinsen(BrilleLinsenResultat.NICHT_GEPRUEFT)
            && helper.isFernvisus(DistanzSehenResultat.NICHT_GEPRUEFT)
            && helper.isNahvisus(DistanzSehenResultat.NICHT_GEPRUEFT)
            && helper.isStrabismus(StrabismusResultat.NICHT_GEPRUEFT)
            && helper.isInspektion(InspektionKonvergenzResultat.NICHT_GEPRUEFT)
            && helper.isKonvergenz(InspektionKonvergenzResultat.NICHT_GEPRUEFT)
            && helper.isMotilitaet(MotilitaetResultat.NICHT_GEPRUEFT)
        ) {
            return tl.translate("brief.beurteilungSehen.nichtGeprueft");
        }

        return tl.translate("brief.beurteilungSehen.sieheBegleitbrief");
    }

    @NonNull
    private static HoerenAudiogrammDocMergerDTO toHoerenAudiogrammDTO(
        @NonNull Untersuchung untersuchung,
        @NonNull AnlassConfig anlassConfig,
        @NonNull TL tl
    ) {
        UntersuchungHoerenAudiogramm untersuchungHoerenAudiogramm = untersuchung.getUntersuchungHoerenAudiogramm();

        if (untersuchungHoerenAudiogramm == null) {
            return new HoerenAudiogrammDocMergerDTO(
                PrintUtil.PLACEHOLDER,
                PrintUtil.PLACEHOLDER,
                new HashMap<>(),
                new HashMap<>(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder()
            );
        }

        return new HoerenAudiogrammDocMergerDTO(
            ValueEnhancedUtil.getEnumSetOrEmpty(
                untersuchungHoerenAudiogramm.getZusaetzlicheAngaben(),
                HoerenZusaetzlicheAngaben.class).contains(HoerenZusaetzlicheAngaben.ERKAELTUNG) ?
                getEnumValue(BRIEF_PREFIX, HoerenZusaetzlicheAngaben.ERKAELTUNG, tl) :
                "",
            ValueEnhancedUtil.getEnumSetOrEmpty(
                untersuchungHoerenAudiogramm.getZusaetzlicheAngaben(),
                HoerenZusaetzlicheAngaben.class).contains(HoerenZusaetzlicheAngaben.HOERHILFE) ?
                getEnumValue(BRIEF_PREFIX, HoerenZusaetzlicheAngaben.HOERHILFE, tl) :
                "",
            getAudiogrammMap(untersuchungHoerenAudiogramm.getAudiogrammRechts()),
            getAudiogrammMap(untersuchungHoerenAudiogramm.getAudiogrammLinks()),
            getZusammenfassungAudiogramm(untersuchung, anlassConfig, tl),
            getAudiogramBeurteilung(
                ValueEnhancedUtil.getEnumSetOrEmpty(
                    untersuchungHoerenAudiogramm.getAudiogrammBeurteilung(),
                    AudiogrammBeurteilung.class), tl),
            ValueEnhancedUtil.getEnumSetOrEmpty(untersuchungHoerenAudiogramm.getEmpfehlung(), HoerenEmpfehlung.class)
                .stream().map(empfehlung -> getEnumValue(BRIEF_PREFIX, empfehlung, tl))
                .collect(Collectors.joining(", ")),
            getEnumValue(
                BRIEF_PREFIX,
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungHoerenAudiogramm.getAudiogrammSimpel(),
                    AudiogrammSimpel.class),
                tl),
            getEnumValue(
                BRIEF_PREFIX,
                ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungHoerenAudiogramm.getHoerhilfe(),
                    HoerenHoerhilfe.class),
                tl)
        );
    }

    @NonNull
    private static HoerenOtoskopieDocMergerDTO toHoerenOtoskopieDTO(
        @NonNull Untersuchung untersuchung,
        @NonNull AnlassConfig anlassConfig,
        @NonNull TL tl
    ) {
        UntersuchungHoerenOtoskopie untersuchungHoerenOtoskopie = untersuchung.getUntersuchungHoerenOtoskopie();

        if (untersuchungHoerenOtoskopie == null) {
            return new HoerenOtoskopieDocMergerDTO(
                PrintUtil.PLACEHOLDER,
                PrintUtil.PLACEHOLDER,
                PrintUtil.PLACEHOLDER);
        }

        return new HoerenOtoskopieDocMergerDTO(
            otoskopieResultatToString(
                ValueEnhancedUtil.getEnumSetOrEmpty(
                    untersuchungHoerenOtoskopie.getOtoskopieRechts(),
                    OtoskopieResultat.class),
                untersuchungHoerenOtoskopie.getOtoskopieRechtsResultatAndere(),
                tl),
            otoskopieResultatToString(
                ValueEnhancedUtil.getEnumSetOrEmpty(
                    untersuchungHoerenOtoskopie.getOtoskopieLinks(),
                    OtoskopieResultat.class),
                untersuchungHoerenOtoskopie.getOtoskopieLinksResultatAndere(),
                tl),
            getZusammenfassungOtoskopie(untersuchung, anlassConfig, tl)
        );
    }

    private static String otoskopieResultatToString(
        @Nullable EnumSet<OtoskopieResultat> otoskopieResultate,
        @Nullable String resultatBemerkung,
        @NonNull TL tl
    ) {
        if (otoskopieResultate == null || otoskopieResultate.isEmpty()) {
            return PRINT_UTIL.getPlaceholder();
        }
        return otoskopieResultate.stream().map(beurteilung -> {
            String otoskopieValue = getEnumValue(BRIEF_PREFIX, beurteilung, tl);
            boolean isAnderesWithBemerkung =
                beurteilung == OtoskopieResultat.ANDERES && StringUtils.isNoneEmpty(resultatBemerkung);

            return isAnderesWithBemerkung ? otoskopieValue + ": " + resultatBemerkung : otoskopieValue;
        }).collect(Collectors.joining(", "));
    }

    @NonNull
    private static String getAudiogramBeurteilung(
        @NonNull EnumSet<AudiogrammBeurteilung> audiogrammBeurteilungList,
        @NonNull TL tl
    ) {
        List<AudiogrammBeurteilung> beurteilungenToPrint = new ArrayList<>(audiogrammBeurteilungList);
        Set<AudiogrammBeurteilung> sideReducedValues =
            Set.of(AudiogrammBeurteilung.LINKS_REDUZIERT, AudiogrammBeurteilung.RECHTS_REDUZIERT);
        boolean hasBothSidesReduced = new HashSet<>(beurteilungenToPrint).containsAll(sideReducedValues);

        List<String> results = new ArrayList<>();

        if (hasBothSidesReduced) {
            results.add(tl.translate("brief.AudiogrammBeurteilungBeidseitigReduziert"));
            beurteilungenToPrint.removeAll(sideReducedValues);
        }
        for (AudiogrammBeurteilung audiogrammBeurteilung : beurteilungenToPrint) {
            results.add(getEnumValue(BRIEF_PREFIX, audiogrammBeurteilung, tl));
        }

        return String.join(", ", results);

    }

    @NonNull
    private static String getZusammenfassungAudiogramm(
        @NonNull Untersuchung untersuchung,
        @NonNull AnlassConfig anlassConfig,
        @NonNull TL tl
    ) {
        UntersuchungMassnahmen untersuchungMassnahmen = untersuchung.getUntersuchungMassnahmen();
        if (untersuchungMassnahmen != null && untersuchungMassnahmen.isBereitsBehandeltHoeren()) {
            return tl.translate("brief.zusammenfassungOhren.bereitsInBehandlung");
        }

        UntersuchungHoerenAudiogramm untersuchungHoeren =
            Objects.requireNonNull(untersuchung.getUntersuchungHoerenAudiogramm());
        ZusammenfassungUtilImpl helper = new ZusammenfassungUtilImpl(anlassConfig, untersuchung);
        if (helper.isAudiogramBeurteilung(AudiogrammBeurteilung.NORMAL)
            && helper.isHoerenZusaetzlicheAngaben(HoerenZusaetzlicheAngaben.KEINE)
            && helper.isNormalAudiogramm(untersuchungHoeren.getAudiogrammRechts())
            && helper.isNormalAudiogramm(untersuchungHoeren.getAudiogrammLinks())
        ) {
            return tl.translate("brief.zusammenfassungOhren.normal");
        }
        return tl.translate("brief.zusammenfassungOhren.sieheBegleitbrief");
    }

    @NonNull
    private static String getZusammenfassungOtoskopie(
        @NonNull Untersuchung untersuchung,
        @NonNull AnlassConfig anlassConfig,
        @NonNull TL tl
    ) {
        UntersuchungMassnahmen untersuchungMassnahmen = untersuchung.getUntersuchungMassnahmen();
        if (untersuchungMassnahmen != null && untersuchungMassnahmen.isBereitsBehandeltHoeren()) {
            return tl.translate("brief.zusammenfassungOhren.bereitsInBehandlung");
        }

        ZusammenfassungUtilImpl helper = new ZusammenfassungUtilImpl(anlassConfig, untersuchung);
        if (helper.isOtoskopieRechts(OtoskopieResultat.NORMAL)
            && helper.isOtoskopieLinks(OtoskopieResultat.NORMAL)
        ) {
            return tl.translate("brief.zusammenfassungOhren.normal");
        }
        return tl.translate("brief.zusammenfassungOhren.sieheBegleitbrief");
    }

    @NonNull
    private static GroesseGewichtDocMergerDTO toGroesseGewichtDTO(
        @Nullable UntersuchungGroesseGewicht groesseGewicht,
        @NonNull TL tl
    ) {
        if (groesseGewicht == null) {
            return new GroesseGewichtDocMergerDTO(
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder()
            );
        }

        return new GroesseGewichtDocMergerDTO(
            numberWithSuffix(
                groesseGewicht.getGroesse() != null ? GanzZahl.valueOf(groesseGewicht.getGroesse()).getValue() : null,
                "brief.centimeter.suffix", tl),
            getEnumValue(
                BRIEF_PREFIX, ValueEnhancedUtil.getEnumValueOrNull(
                    groesseGewicht.getGroessePerzentile(),
                    PerzentileGroesse.class), tl),
            numberWithSuffix(groesseGewicht.getGewicht(), "brief.kilogram.suffix", tl),
            getEnumValue(
                BRIEF_PREFIX, ValueEnhancedUtil.getEnumValueOrNull(
                    groesseGewicht.getGewichtPerzentile(),
                    PerzentileGewicht.class), tl),
            PRINT_UTIL.trimToPlaceholder(groesseGewicht.getBmi()),
            getEnumValue(
                BRIEF_PREFIX, ValueEnhancedUtil.getEnumValueOrNull(
                    groesseGewicht.getBmiPerzentile(),
                    PerzentileBmi.class), tl)
        );
    }

    @NonNull
    private static UntersuchungArztDocMergerDTO toUntersuchungArztDTO(
        @Nullable UntersuchungArztBeurteilung untersuchungArztBeurteilung,
        @NonNull TL tl
    ) {
        if (untersuchungArztBeurteilung == null) {
            return new UntersuchungArztDocMergerDTO(
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder(),
                PRINT_UTIL.getPlaceholder()
            );
        }
        return new UntersuchungArztDocMergerDTO(
            getEnumValue(BRIEF_PREFIX, untersuchungArztBeurteilung.getBlutdruck(), tl),
            PRINT_UTIL.trimToPlaceholder(untersuchungArztBeurteilung.getBlutdruckSystolisch()),
            PRINT_UTIL.trimToPlaceholder(untersuchungArztBeurteilung.getBlutdruckDiastolisch())
        );
    }

    @NonNull
    private static Map<String, String> getAudiogrammMap(@Nullable Audiogramm audiogramm) {
        Map<String, String> audiogrammMap = new HashMap<>();
        if (audiogramm != null) {
            audiogrammMap.put("250hz", getDezibelValue(audiogramm.getDbHz250()));
            audiogrammMap.put("500hz", getDezibelValue(audiogramm.getDbHz500()));
            audiogrammMap.put("1000hz", getDezibelValue(audiogramm.getDbHz1000()));
            audiogrammMap.put("2000hz", getDezibelValue(audiogramm.getDbHz2000()));
            audiogrammMap.put("4000hz", getDezibelValue(audiogramm.getDbHz4000()));
            audiogrammMap.put("6000hz", getDezibelValue(audiogramm.getDbHz6000()));
            audiogrammMap.put("8000hz", getDezibelValue(audiogramm.getDbHz8000()));
        }
        return audiogrammMap;
    }

    @NonNull
    private static String getDezibelValue(@Nullable Dezibel dezibel) {
        return dezibel != null ? dezibel.name() : PRINT_UTIL.getPlaceholder();
    }

    @NonNull
    private static String numberWithSuffix(@Nullable BigDecimal number, @NonNull String suffix, @NonNull TL tl) {
        if (number == null) {
            return PRINT_UTIL.getPlaceholder();
        }

        return number + tl.translate(suffix);
    }

    @NonNull
    private static String getVisusString(@NonNull EnumSet<DistanzSehenResultat> visus, @NonNull TL tl) {
        boolean isBeidseitig = new HashSet<>(visus).containsAll(
            List.of(DistanzSehenResultat.LINKS_REDUZIERT, DistanzSehenResultat.RECHTS_REDUZIERT));

        if (isBeidseitig) {
            return tl.translate("brief.DistanzSehenResultat.beidseitig");
        }

        return visus
            .stream()
            .map(resultat -> getEnumValue(BRIEF_PREFIX, resultat, tl))
            .collect(Collectors.joining("/"));
    }

    @NonNull
    private static String getEnumValue(@NonNull String prefix, @Nullable Enum<?> value, @NonNull TL tl) {
        if (value == null) {
            return PRINT_UTIL.getPlaceholder();
        }
        return tl.translate(getTranslationKey(prefix, value));
    }

    @NonNull
    private static String getTranslationKey(@NonNull String prefix, @NonNull Enum<?> value) {
        return prefix + '.' + value.getDeclaringClass().getSimpleName() + '.' + value.name();
    }

    @Nullable
    private static ExternerArztDocMergerDTO toExternerArztDTO(@Nullable Arzt arzt) {
        if (arzt == null) {
            return null;
        }

        return new ExternerArztDocMergerDTO(
            arzt.getAnrede(),
            arzt.getNameVorname(),
            formatAddress(arzt.getAdresse()),
            arzt.getTelefon() != null ? arzt.getTelefon().getFormattedPhoneNumber() : PRINT_UTIL.getPlaceholder(),
            arzt.getEmail() != null ? arzt.getEmail().getAddress() : PRINT_UTIL.getPlaceholder()
        );
    }
}
