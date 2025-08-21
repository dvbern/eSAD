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

package ch.dvbern.oss.hemed.esc.api.features.escada.csv;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.escada.SozialversicherungsnummerFixPreprocessor;
import ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.KlasseExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.external.LehrpersonExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.schulenexternal.SchuleExternal;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import com.cronutils.utils.StringUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil.createReaderFromPath;

class EscadaCsvDataAnalysisServiceTest {

    private final EscadaCsvService escadaCsvService = new EscadaCsvService();
    // to test the 'real' file replace the path "/EscadaTest/csv_pseudonymisiert.csv"
    private static final String FILE_IMPORT = "/EscadaTest/analysis/csv_analysis.csv";

    private final File escadaFile = TestUtil.loadTestfile(FILE_IMPORT);

    @EqualsAndHashCode(of = "schulanlageId")
    @Value
    private static class KpSchulanlageAnalysisRecord {
        String schulanlageId;
        String fullName;
        int lehrerNumbr;

        public KpSchulanlageAnalysisRecord(String schulanlageId, LehrpersonExternal kpSchule, int num) {
            this.schulanlageId = schulanlageId;
            this.fullName = kpSchule.getKlasseLehrpersonName() + ' ' + kpSchule.getKlasseLehrpersonVorname();
            this.lehrerNumbr = num;
        }
    }

    @EqualsAndHashCode(of = "schulhausId")
    @Value
    private static class KpSchuhausAnalysisRecord {
        String schulhausId;
        String fullName;
        int lehrerNumbr;

        public KpSchuhausAnalysisRecord(String schulhausId, LehrpersonExternal kpSchule, int num) {
            this.schulhausId = schulhausId;
            this.fullName = kpSchule.getKlasseLehrpersonName() + ' ' + kpSchule.getKlasseLehrpersonVorname();
            this.lehrerNumbr = num;
        }
    }

    @EqualsAndHashCode(of = "klasseId")
    @Value
    private static class KpSchuleToKlasseAnalysisRecord {
        String klasseId;
        String fullName;
        int lehrerNumbr;

        public KpSchuleToKlasseAnalysisRecord(String klasseId, LehrpersonExternal kpSchule, int num) {
            this.klasseId = klasseId;
            this.fullName = kpSchule.getKlasseLehrpersonName() + ' ' + kpSchule.getKlasseLehrpersonVorname();
            this.lehrerNumbr = num;
        }
    }

    @EqualsAndHashCode(of = "klasseId")
    @Value
    private static class KindAnalysisRecord {
        String klasseId;
        String fullName;
        String klaseBezeichnung;

        public KindAnalysisRecord(String klasseId, String klasseBez, KindExternal kindExternal) {
            this.klasseId = klasseId;
            this.fullName = kindExternal.getSchuelerName() + ' ' + kindExternal.getSchuelerVorname();
            this.klaseBezeichnung = klasseBez;
        }
    }

    @Test
    void check_each_kontaktperson_schule_belongs_to_only_one_schuleanlage() {
        // ARRANGE
        // transform file to reader
        Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
        // ACT
        List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);
        // ASSERT
        Multimap<String, KpSchulanlageAnalysisRecord> kpIdToSchuleIdMultimap = ArrayListMultimap.create();
        for (EscadaCsvDTO escadaCsvDTO : escadaCsvDTOS) {

            if (escadaCsvDTO.klasseLehrpersonId() != null) {

                kpIdToSchuleIdMultimap.put(
                    escadaCsvDTO.klasseLehrpersonId(),
                    new KpSchulanlageAnalysisRecord(
                        escadaCsvDTO.schulanlageId(),
                        EscadaCSVMapperService.toExternalLehrperson(escadaCsvDTO).orElseThrow(), 1));
            }
            if (escadaCsvDTO.klasseLehrperson2Id() != null) {
                kpIdToSchuleIdMultimap.put(
                    escadaCsvDTO.klasseLehrperson2Id(),
                    new KpSchulanlageAnalysisRecord(
                        escadaCsvDTO.schulanlageId(),
                        EscadaCSVMapperService.toExternalLehrperson2(escadaCsvDTO).orElseThrow(), 2));
            }
            if (escadaCsvDTO.klasseLehrperson3Id() != null) {
                kpIdToSchuleIdMultimap.put(
                    escadaCsvDTO.klasseLehrperson3Id(),
                    new KpSchulanlageAnalysisRecord(
                        escadaCsvDTO.schulanlageId(),
                        EscadaCSVMapperService.toExternalLehrperson3(escadaCsvDTO).orElseThrow(), 3));
            }
        }

        var lehrerInMultipleAnlagen = kpIdToSchuleIdMultimap.asMap().entrySet().stream()
            .filter(entry -> new HashSet<>(entry.getValue()).size() > 1)
            .flatMap(e -> e.getValue().stream().map(
                kp -> "klasseLehrpersonId %s with name %s belongs to multiple schulanlageId: %s, lehrer number %s"
                    .formatted(e.getKey(), kp.getFullName(), kp.getSchulanlageId(), kp.getLehrerNumbr())))
            .distinct()
            .toList();

        Assertions.assertThat(lehrerInMultipleAnlagen)
            .describedAs("Lehrer in multiple Anlagen are unexpected")
            .isEmpty();

    }

    @Test
    void check_each_kontaktperson_schule_belongs_to_only_one_schulhaus() {
        // ARRANGE
        // transform file to reader
        Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
        // ACT
        List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);
        // ASSERT
        Multimap<String, KpSchuhausAnalysisRecord> kpIdToSchuleIdMultimap = ArrayListMultimap.create();
        for (EscadaCsvDTO escadaCsvDTO : escadaCsvDTOS) {

            if (escadaCsvDTO.klasseLehrpersonId() != null) {

                kpIdToSchuleIdMultimap.put(
                    escadaCsvDTO.klasseLehrpersonId(),
                    new KpSchuhausAnalysisRecord(
                        escadaCsvDTO.schulhausId(),
                        EscadaCSVMapperService.toExternalLehrperson(escadaCsvDTO).orElseThrow(), 1));
            }
            if (escadaCsvDTO.klasseLehrperson2Id() != null) {
                kpIdToSchuleIdMultimap.put(
                    escadaCsvDTO.klasseLehrperson2Id(),
                    new KpSchuhausAnalysisRecord(
                        escadaCsvDTO.schulhausId(),
                        EscadaCSVMapperService.toExternalLehrperson2(escadaCsvDTO).orElseThrow(), 2));
            }
            if (escadaCsvDTO.klasseLehrperson3Id() != null) {
                kpIdToSchuleIdMultimap.put(
                    escadaCsvDTO.klasseLehrperson3Id(),
                    new KpSchuhausAnalysisRecord(
                        escadaCsvDTO.schulhausId(),
                        EscadaCSVMapperService.toExternalLehrperson3(escadaCsvDTO).orElseThrow(), 3));
            }
            if (escadaCsvDTO.klasseLehrperson4Id() != null) {
                kpIdToSchuleIdMultimap.put(
                    escadaCsvDTO.klasseLehrperson4Id(),
                    new KpSchuhausAnalysisRecord(
                        escadaCsvDTO.schulhausId(),
                        EscadaCSVMapperService.toExternalLehrperson4(escadaCsvDTO).orElseThrow(), 4));
            }
        }

        var lehrerInMultipleAnlagen = kpIdToSchuleIdMultimap.asMap().entrySet().stream()
            .filter(entry -> new HashSet<>(entry.getValue()).size() > 1)
            .flatMap(e -> e.getValue().stream().map(
                kp -> "klasseLehrpersonId %s with name %s belongs to multiple schulhaus: %s lehrer number %s"
                    .formatted(e.getKey(), kp.getFullName(), kp.getSchulhausId(), kp.getLehrerNumbr())))
            .distinct()
            .toList();

        Assertions.assertThat(lehrerInMultipleAnlagen)
            .describedAs("Lehrer in multiple Schulhäusern are unexpected")
            .isEmpty();
    }

    @Test
    void checkKindOnlyBelongsToOneKlasse() {
        // ARRANGE
        // transform file to reader
        Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
        // ACT
        List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);
        // ASSERT
        Multimap<String, KindAnalysisRecord> kindToKlasseMultimap = ArrayListMultimap.create();
        for (EscadaCsvDTO escadaCsvDTO : escadaCsvDTOS) {
            if (escadaCsvDTO.schuelerId() != null) {
                kindToKlasseMultimap.put(
                    escadaCsvDTO.schuelerId(),
                    new KindAnalysisRecord(
                        escadaCsvDTO.klasseId(), escadaCsvDTO.klasseBezeichnung() + ' ' + escadaCsvDTO.schulkreisBezeichnung(),
                        EscadaCSVMapperService.toExternalKind(escadaCsvDTO).orElseThrow()));
            }
        }
        var kindInMultipleKlassen = kindToKlasseMultimap.asMap().entrySet().stream()
            .filter(entry -> new HashSet<>(entry.getValue()).size() > 1)
            // since the csv we got is broken and has same schuelerId for multiple schueler let us filter them out
            .filter(entry -> entry.getValue().stream().map(KindAnalysisRecord::getFullName).collect(Collectors.toSet()).size() == 1)
            .flatMap(e -> e.getValue().stream().map(
                klasse -> "schuelerId %s belongs to multiple klasseId: %s"
                    .formatted(e.getKey(), klasse)))
            .distinct()
            .toList();



        if (!kindInMultipleKlassen.isEmpty()) {
            writeToFile("kind_in_multiple_klassen.txt", kindInMultipleKlassen);
        }

        Assertions.assertThat(kindInMultipleKlassen)
            .describedAs("Kind in multiple Klassen are unexpected")
            .isEmpty();
    }

    @Test
    void checkNoKindOnlyBelongsToSchuleFuerGestatlutungKlasse(){
        Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
        // ACT
        List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);
        Multimap<String, String> kindToKlasseMultimap = ArrayListMultimap.create();
        for (EscadaCsvDTO escadaCsvDTO : escadaCsvDTOS) {
            if (escadaCsvDTO.schuelerId() != null && escadaCsvDTO.schulkreisBezeichnung() != null) {
                kindToKlasseMultimap.put(
                    escadaCsvDTO.schuelerId(),
                    escadaCsvDTO.schulkreisBezeichnung()
                );
            }
        }
        var kinderInSchuleFuerGestaltung =
            kindToKlasseMultimap.asMap().entrySet().stream()
                .filter(entry -> new HashSet<>(entry.getValue()).size() == 1)
                .filter(entry -> entry.getValue().stream().anyMatch(v -> v.equals("Schule für Gestaltung")))
                    .toList();

        Assertions.assertThat(kinderInSchuleFuerGestaltung).isEmpty();

    }

    @Test
    void check_each_kontaktperson_schule_belongs_to_only_one_klasse() {
        // ARRANGE
        // transform file to reader
        Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
        // ACT
        List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);
        // ASSERT
        Multimap<String, KpSchuleToKlasseAnalysisRecord> kpIdToKlasseIdMultimap = ArrayListMultimap.create();
        for (EscadaCsvDTO escadaCsvDTO : escadaCsvDTOS) {

            if (escadaCsvDTO.klasseLehrpersonId() != null) {

                kpIdToKlasseIdMultimap.put(
                    escadaCsvDTO.klasseLehrpersonId(),
                    new KpSchuleToKlasseAnalysisRecord(
                        escadaCsvDTO.klasseId(),
                        EscadaCSVMapperService.toExternalLehrperson(escadaCsvDTO).orElseThrow(), 1));
            }
            if (escadaCsvDTO.klasseLehrperson2Id() != null) {
                kpIdToKlasseIdMultimap.put(
                    escadaCsvDTO.klasseLehrperson2Id(),
                    new KpSchuleToKlasseAnalysisRecord(
                        escadaCsvDTO.klasseId(),
                        EscadaCSVMapperService.toExternalLehrperson2(escadaCsvDTO).orElseThrow(), 2));
            }
            if (escadaCsvDTO.klasseLehrperson3Id() != null) {
                kpIdToKlasseIdMultimap.put(
                    escadaCsvDTO.klasseLehrperson3Id(),
                    new KpSchuleToKlasseAnalysisRecord(
                        escadaCsvDTO.klasseId(),
                        EscadaCSVMapperService.toExternalLehrperson3(escadaCsvDTO).orElseThrow(), 3));
            }
            if (escadaCsvDTO.klasseLehrperson4Id() != null) {
                kpIdToKlasseIdMultimap.put(
                    escadaCsvDTO.klasseLehrperson4Id(),
                    new KpSchuleToKlasseAnalysisRecord(
                        escadaCsvDTO.klasseId(),
                        EscadaCSVMapperService.toExternalLehrperson4(escadaCsvDTO).orElseThrow(), 4));
            }
        }

        var lehrerInMultilenKlassen = kpIdToKlasseIdMultimap.asMap().entrySet().stream()
            .filter(entry -> new HashSet<>(entry.getValue()).size() > 1)
            .flatMap(e -> e.getValue().stream().map(
                kp -> "klasseLehrpersonId %s with name %s belongs to multiple klasseId: %s, lehrer number %s"
                    .formatted(e.getKey(), kp.getFullName(), kp.getKlasseId(), kp.getLehrerNumbr())))
            .distinct()
            .toList();

        if (!lehrerInMultilenKlassen.isEmpty()) {
            writeToFile("lehrer_in_multiple_klassen_probably_no_problem.txt", lehrerInMultilenKlassen);
        }
        Assertions.assertThat(lehrerInMultilenKlassen)
            .describedAs("Lehrer in multiple Klassen are unexpected")
            .isEmpty();

    }

    record LehrerData(@Nullable String anrede, @Nullable String name, @Nullable String vorname) {
    }

    @Test
    @Disabled("This test is disabled, i guess the data is valid")
    void check_if_lehrer_with_same_data_exists_mutiple_times_with_different_lehrernumber() {
        // ARRANGE
        // transform file to reader
        Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
        // ACT
        List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);
        List<LehrpersonExternal> rows = escadaCsvDTOS.stream()
            .flatMap(dto -> Stream.of(
                        EscadaCSVMapperService.toExternalLehrperson(dto),
                        EscadaCSVMapperService.toExternalLehrperson2(dto),
                        EscadaCSVMapperService.toExternalLehrperson3(dto),
                        EscadaCSVMapperService.toExternalLehrperson4(dto)
                    )
                    .flatMap(Optional::stream) // Remove empty Optionals
            )
            .toList();
        // ASSERT
        Map<LehrerData, List<LehrpersonExternal>> lehrerdataToKlassenlehrer =
            rows.stream().collect(Collectors.groupingBy(e -> new LehrerData(
                e.getKlasseLehrpersonAnrede(),
                e.getKlasseLehrpersonName(),
                e.getKlasseLehrpersonVorname())));

        Multimap<LehrerData, String> namegoup = ArrayListMultimap.create();
        for (Entry<LehrerData, List<LehrpersonExternal>> entry : lehrerdataToKlassenlehrer.entrySet()) {
            for (LehrpersonExternal kp : entry.getValue()) {
                namegoup.put(entry.getKey(), kp.getLehrpersonNumber() + " " + kp.getEscadaKlasseId());
            }
        }

        var lines = namegoup.asMap().entrySet().stream()
            .map(e -> {
                Set<String> uniqueLehrernummernPerKlasse = new HashSet<>(e.getValue());
                if (uniqueLehrernummernPerKlasse.size() > 1) {
                    return e.getKey() + " => " + uniqueLehrernummernPerKlasse;
                }
                return "";
            }).filter(org.apache.commons.lang3.StringUtils::isNotBlank)
            .toList();

        if (!lines.isEmpty()) {
            writeToFile("lehrer_with_same_data_exists_mutiple_times_with_different_lehrernumber_no_problem.txt", lines);
        }

        Assertions.assertThat(lines)
            .describedAs("Lehrer with same data exists multiple times with different lehrernumber")
            .isEmpty();

    }

    @Test
    void klasse_belongs_to_only_one_schule() {
        // ARRANGE
        // transform file to reader
        Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
        // ACT
        List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);
        // ASSERT
        Multimap<String, String> klasseToSchuleMultimap = ArrayListMultimap.create();
        for (EscadaCsvDTO escadaCsvDTO : escadaCsvDTOS) {
            if (escadaCsvDTO.klasseId() != null) {
                klasseToSchuleMultimap.put(escadaCsvDTO.klasseId(), escadaCsvDTO.schulanlageId());
            }
        }
        var klasseInMultipleSchulen = klasseToSchuleMultimap.asMap().entrySet().stream()
            .filter(entry -> new HashSet<>(entry.getValue()).size() > 1)
            .flatMap(e -> e.getValue().stream().map(
                schule -> "klasseId %s belongs to multiple schuleId: %s"
                    .formatted(e.getKey(), schule)))
            .distinct()
            .toList();

        Assertions.assertThat(klasseInMultipleSchulen)
            .describedAs("Klasse in multiple Schulen are unexpected")
            .isEmpty();
    }

    @Test
    void all_schueler_should_belong_to_a_klasse() {
        // ARRANGE
        // transform file to reader
        Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
        // ACT
        List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);
        // ASSERT
        var schuelerWithoutKlasse = escadaCsvDTOS.stream()
            .filter(escadaCsvDTO -> escadaCsvDTO.klasseId() == null && escadaCsvDTO.schuelerId() != null)
            .map(escadaCsvDTO -> "schuelerId %s (%s %s) does not belong to a klasse"
                .formatted(escadaCsvDTO.schuelerId(), escadaCsvDTO.schuelerName(), escadaCsvDTO.schuelerVorname()))
            .toList();

        Assertions.assertThat(schuelerWithoutKlasse)
            .describedAs("Schueler without klasse are unexpected")
            .isEmpty();
    }

    @Test
    void all_klassen_should_belong_to_schulhaus() {
        // ARRANGE
        // transform file to reader
        Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
        // ACT
        List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);
        // ASSERT
        var klasseWithoutSchule = escadaCsvDTOS.stream()
            .filter(escadaCsvDTO -> escadaCsvDTO.klasseId() != null && escadaCsvDTO.schulhausId() == null)
            .map(escadaCsvDTO -> "klasseId %s (%s) does not belong to a schulhausid (schulanlage is %s)"
                .formatted(escadaCsvDTO.klasseId(), escadaCsvDTO.klasseBezeichnung(), escadaCsvDTO.schulanlageId()))
            .toList();

        Assertions.assertThat(klasseWithoutSchule)
            .describedAs("Klassen without schulhaus are unexpected and will require dummy schule (violated"
                + klasseWithoutSchule.size()
                + " times)")
            .isEmpty();
    }

    @Test
    void all_klassen_should_belong_to_a_schulanlage() {
        // ARRANGE
        // transform file to reader
        Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
        // ACT
        List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);
        // ASSERT
        var klasseWithoutSchulanlage = escadaCsvDTOS.stream()
            .filter(escadaCsvDTO -> escadaCsvDTO.klasseId() != null && escadaCsvDTO.schulanlageId() == null)
            .map(escadaCsvDTO -> "klasseId %s (%s) does not belong to a schulanlage"
                .formatted(escadaCsvDTO.klasseId(), escadaCsvDTO.klasseBezeichnung()))
            .toList();

        Assertions.assertThat(klasseWithoutSchulanlage)
            .describedAs("Klassen without schulanlage are unexpected and will require "
                + "dummy schulanlage (violated " + klasseWithoutSchulanlage.size() + " times)")
            .isEmpty();
    }

    @Nested
    @DisplayName("when external id is fixed")
    class EscadaIdSanityCheck {

        @Test
        void schueler_data_should_be_unique_for_schuler_id() {
            // ARRANGE
            // transform file to reader
            Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
            // ACT
            List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);

            List<KindExternal> kinderRows = escadaCsvDTOS.stream()
                .map(EscadaCSVMapperService::toExternalKind)
                .flatMap(Optional::stream)
                .toList();
            // Group by schuelerId
            Map<String, List<KindExternal>> groupedBySchuelerId = kinderRows.stream()
                .collect(Collectors.groupingBy(KindExternal::getSchuelerId));

            // Check if all objects in each group are equal
            Map<String, Boolean> validationResults = new HashMap<>();

            for (Map.Entry<String, List<KindExternal>> entry : groupedBySchuelerId.entrySet()) {
                List<KindExternal> items = entry.getValue();
                boolean isValid = items.stream()
                    .allMatch(item -> KindExternal.KIND_EXTERNAL_DATA_COMPARATOR.compare(item, items.getFirst())
                        == 0);
                validationResults.put(entry.getKey(), isValid);
            }

            // find offending
            Map<String, TreeSet<KindExternal>> offendingResult = validationResults.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .collect(Collectors.toMap(
                    Entry::getKey, (entry -> {
                        var uniqueOffending = new TreeSet<>(KindExternal.KIND_EXTERNAL_DATA_COMPARATOR);
                        uniqueOffending.addAll(groupedBySchuelerId.get(entry.getKey()));
                        return uniqueOffending;
                    })));
            // ASSERT

            if (!offendingResult.isEmpty()) {
                List<String> lines = offendingResult.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + " => " + entry.getValue())
                    .collect(Collectors.toList());
                writeToFile("escada_duplicate_schuler_id.txt", lines);
            }

            Assertions.assertThat(offendingResult)
                .describedAs("data is not unique for schuelerId found %s offending entries"
                    .formatted(offendingResult.size()))
                .isEmpty();
        }

        @Test
        void schule_data_schould_be_uniquer_for_schulhaus_id() {
            // ARRANGE
            // transform file to reader
            Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
            // ACT
            List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);

            var rows = escadaCsvDTOS.stream()
                .map(EscadaCSVMapperService::toExternalSchule)
                .flatMap(Optional::stream)
                .toList();
            // Group by externalId
            Map<String, List<SchuleExternal>> groupByExternalId = rows.stream()
                .collect(Collectors.groupingBy(SchuleExternal::getSchulhausId));

            // Check if all objects in each group are equal
            Map<String, Boolean> validationResults = new HashMap<>();

            for (Map.Entry<String, List<SchuleExternal>> entry : groupByExternalId.entrySet()) {
                List<SchuleExternal> items = entry.getValue();
                boolean isValid = items.stream()
                    .allMatch(item -> SchuleExternal.SCHULE_EXTERNAL_DATA_COMPARATOR.compare(
                        item,
                        items.getFirst())
                        == 0);
                validationResults.put(entry.getKey(), isValid);
            }

            // find offending
            Map<String, TreeSet<SchuleExternal>> offendingResult = validationResults.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .collect(Collectors.toMap(
                    Entry::getKey
                    , (entry -> {
                        var uniqueOffending = new TreeSet<>(SchuleExternal.SCHULE_EXTERNAL_DATA_COMPARATOR);
                        uniqueOffending.addAll(groupByExternalId.get(entry.getKey()));
                        return uniqueOffending;
                    })));

            if (!offendingResult.isEmpty()) {
                List<String> lines = offendingResult.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + " => " + entry.getValue())
                    .collect(Collectors.toList());
                writeToFile("escada_non_unique_schuledata_by_id.txt", lines);
            }

            // ASSERT
            Assertions.assertThat(offendingResult)
                .describedAs("data is not unique for schulhausId")
                .isEmpty();
        }

        @Test
        void klasse_data_should_be_unique_by_klasse_id() {
            // ARRANGE
            // transform file to reader
            Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
            // ACT
            List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);

            var rows = escadaCsvDTOS.stream()
                .map(EscadaCSVMapperService::toExternalKlasse)
                .flatMap(Optional::stream)
                .toList();
            // Group by externalId
            Map<String, List<KlasseExternal>> groupByExternalId = rows.stream()
                .collect(Collectors.groupingBy(KlasseExternal::getKlasseId));

            // Check if all objects in each group are equal
            Map<String, Boolean> validationResults = new HashMap<>();

            for (Map.Entry<String, List<KlasseExternal>> entry : groupByExternalId.entrySet()) {
                List<KlasseExternal> items = entry.getValue();
                boolean isValid = items.stream()
                    .allMatch(item -> KlasseExternal.KLASSE_EXTERNAL_DATA_COMPARATOR.compare(
                        item,
                        items.getFirst())
                        == 0);
                validationResults.put(entry.getKey(), isValid);
            }

            // find offending
            Map<String, TreeSet<KlasseExternal>> offendingResult = validationResults.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .collect(Collectors.toMap(
                    Entry::getKey
                    , (entry -> {
                        var uniqueOffending = new TreeSet<>(KlasseExternal.KLASSE_EXTERNAL_DATA_COMPARATOR);
                        uniqueOffending.addAll(groupByExternalId.get(entry.getKey()));
                        return uniqueOffending;
                    })));

            if (!offendingResult.isEmpty()) {
                List<String> lines = offendingResult.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + " => " + entry.getValue())
                    .toList();
                writeToFile("escada_non_unique_klassedata_by_id.txt", lines);
            }

            // ASSERT
            Assertions.assertThat(offendingResult)
                .describedAs("data is not unique for klasseId")
                .isEmpty();
        }

        @Test
        void klassenstufe_should_be_unique_by_klasse_id() {
            // ARRANGE
            // transform file to reader
            Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
            // ACT
            List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);

            var rows = escadaCsvDTOS.stream()
                .map(EscadaCSVMapperService::toExternalKlasse)
                .flatMap(Optional::stream)
                .toList();
            // Group by externalId
            Map<String, List<KlasseExternal>> groupByExternalId = rows.stream()
                .collect(Collectors.groupingBy(KlasseExternal::getKlasseId));

            Map<String, TreeSet<KlasseExternal>>
                offendingResult = doesKlasseIdIdentifyUniqueKlassenstufe(groupByExternalId);

            if(!offendingResult.isEmpty()) {
                List<String> lines = offendingResult.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + " => " + entry.getValue())
                    .toList();
                writeToFile("escada_non_unique_klassenstufe_by_id.txt", lines);
            }

            // ASSERT
            Assertions.assertThat(offendingResult)
                .describedAs("klasseId has multiple stufen")
                .isEmpty();
        }

        @NonNull
        private static Map<String, TreeSet<KlasseExternal>> doesKlasseIdIdentifyUniqueKlassenstufe(@NonNull Map<String, List<KlasseExternal>> groupByExternalId) {
            Map<String, TreeSet<KlasseExternal>> offendingResult = new HashMap<>();

            for (Entry<String, List<KlasseExternal>> entry : groupByExternalId.entrySet()) {
                List<KlasseExternal> items = entry.getValue();
                TreeSet<KlasseExternal> stufeList = new TreeSet<>(KlasseExternal.KLASSE_EXTERNAL_STUFE);
                stufeList.addAll(items);
                if(stufeList.size() > 1) {
                    offendingResult.put(entry.getKey(), stufeList);
                }
            }
            return offendingResult;
        }

        @Test
        void lehrer_data_should_be_unique_by_klasseLehrpersonId_id() {
            // ARRANGE
            // transform file to reader
            Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
            // ACT
            List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);

            var rows = escadaCsvDTOS.stream()
                .flatMap(dto -> Stream.of(
                            EscadaCSVMapperService.toExternalLehrperson(dto),
                            EscadaCSVMapperService.toExternalLehrperson2(dto),
                            EscadaCSVMapperService.toExternalLehrperson3(dto),
                            EscadaCSVMapperService.toExternalLehrperson4(dto)
                        )
                        .flatMap(Optional::stream) // Remove empty Optionals
                )
                .toList();
            // Group by externalId
            Map<String, List<LehrpersonExternal>> groupByExternalId = rows.stream()
                .collect(Collectors.groupingBy(LehrpersonExternal::getKlasseLehrpersonId));

            // Check if all objects in each group are equal
            Map<String, Boolean> validationResults = new HashMap<>();

            for (Map.Entry<String, List<LehrpersonExternal>> entry : groupByExternalId.entrySet()) {
                List<LehrpersonExternal> items = entry.getValue();
                boolean isValid = items.stream()
                    .allMatch(item -> LehrpersonExternal.KP_SCHULE_EXTERNAL_DATA_COMPARATOR.compare(
                        item,
                        items.getFirst())
                        == 0);
                validationResults.put(entry.getKey(), isValid);
            }

            // find offending
            Map<String, TreeSet<LehrpersonExternal>> offendingResult =
                validationResults.entrySet().stream()
                    .filter(entry -> !entry.getValue())
                    .collect(Collectors.toMap(
                        Entry::getKey
                        , (entry -> {
                            var uniqueOffending =
                                new TreeSet<>(LehrpersonExternal.KP_SCHULE_EXTERNAL_DATA_COMPARATOR);
                            uniqueOffending.addAll(groupByExternalId.get(entry.getKey()));
                            return uniqueOffending;
                        })));

            if (!offendingResult.isEmpty()) {
                List<String> lines = offendingResult.entrySet()
                    .stream()
                    .map(entry -> {
                        String valueToPrint = entry.getValue().stream().map(LehrpersonExternal::toString)
                            .collect(Collectors.joining("\n\t\t "));
                        return entry.getKey() + " =>\n\t\t " + valueToPrint;
                    })
                    .toList();
                writeToFile("escada_non_unique_lehrerdata_by_klasselehrperson_id.txt", lines);
            }

            // ASSERT
            Assertions.assertThat(offendingResult)
                .describedAs("data is not unique for klasseId")
                .isEmpty();
        }
    }

    @Nested
    @DisplayName("when data is invalid")
    class ValidationCheck {

        @Test
        void validation_for_ahvnr_fails() {
            // ARRANGE
            // transform file to reader
            Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
            // ACT
            List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);
            // ASSERT
            Set<String> invalidAhvNummern =
                SozialversicherungsnummerFixPreprocessor.extractSetOfInvalidAhvNummern(escadaCsvDTOS);
            if (!invalidAhvNummern.isEmpty()) {
                writeToFile("escada_invalid_ahvnr.txt", invalidAhvNummern.stream().toList());
            }

            Assertions.assertThat(invalidAhvNummern)
                .describedAs("Invalid AHV Nummern found")
                .isEmpty();

        }

        @Test
        void validation_for_phone_numbers_fails() {
            // Arrange
            // transform file to reader
            Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
            // ACT
            List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);

            Set<String> invalidNrList = escadaCsvDTOS.stream().map(EscadaCsvDTO::schuelerMobile)
                .filter(mobile -> mobile != null && !mobile.isBlank())
                .map(nr -> {
                    try {
                        NormalizedPhoneNumber.parse(nr);
                        return Optional.<String>empty();
                    } catch (Exception e) {
                        return Optional.of(nr);
                    }
                })
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
            if (!invalidNrList.isEmpty()) {
                writeToFile("escada_invalid_mobile_numbers.txt", invalidNrList);
            }

            // ASSERT
            Assertions.assertThat(invalidNrList)
                .describedAs("Invalid phone numbers found")
                .isEmpty();

        }

        @Test
        void validation_for_email_fails() {
            // Arrange
            // transform file to reader
            Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
            // ACT
            List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);

            Set<String> invalidNrList = escadaCsvDTOS.stream().map(EscadaCsvDTO::schuelerEmail)
                .filter(mobile -> mobile != null && !mobile.isBlank())
                .map(nr -> {
                    try {
                        EmailAddress.fromString(nr);
                        return Optional.<String>empty();
                    } catch (Exception e) {
                        return Optional.of(nr);
                    }
                })
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
            if (!invalidNrList.isEmpty()) {
                writeToFile("escada_invalid_schuler_email.txt", invalidNrList);
            }
            // ASSERT
            Assertions.assertThat(invalidNrList)
                .describedAs("Invalid email addresses found")
                .isEmpty();
        }

        @Test
        void validation_for_geb_datum_fails() {
            // Arrange
            // transform file to reader
            Reader reader = createReaderFromPath(Path.of(escadaFile.getPath()));
            // ACT
            List<EscadaCsvDTO> escadaCsvDTOS = escadaCsvService.parseEscadaCSV(reader);

            Set<String> invalidNrList = escadaCsvDTOS.stream()
                .map(e -> Pair.of(e.schuelerId(), e.schuelerGeburtsdatum()))
                .map(pair -> {
                    if (StringUtils.isEmpty(pair.getValue())) {
                        return Optional.of(pair.getKey() + " -> " + pair.getValue());
                    }
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                        LocalDateTime.parse(pair.getValue(), formatter);
                        return Optional.<String>empty();
                    } catch (Exception e) {
                        return Optional.of(pair.getKey() + " -> " + pair.getValue());
                    }
                })
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
            writeToFile("escada_invalid_geb_datum.txt", invalidNrList);
            // ASSERT
            Assertions.assertThat(invalidNrList)
                .describedAs("Invalid geburtsdatum found")
                .isEmpty();
        }
    }

    void writeToFile(String file, Collection<String> lines) {
        Path filePath = getTestClassesDirFilepath(file);
        // Write to file (overwrite if exists)
        try {
            Files.write(filePath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file", e);
        }

    }

    private static Path getTestClassesDirFilepath(@NonNull String filename) {
        // Get the test resources directory dynamically
        URL resourceUrl = EscadaCsvDataAnalysisServiceTest.class.getClassLoader().getResource("");
        if (resourceUrl == null) {
            throw new IllegalStateException("Test resource directory not found!");
        }
        try {
            var dir = Paths.get(resourceUrl.toURI());
            return dir.resolve(filename);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not get test resources directory", e);
        }
    }
}
