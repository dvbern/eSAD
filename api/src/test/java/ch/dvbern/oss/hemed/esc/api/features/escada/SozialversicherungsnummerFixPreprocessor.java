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

package ch.dvbern.oss.hemed.esc.api.features.escada;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummerTestGenerator;
import ch.dvbern.oss.hemed.esc.api.features.escada.csv.EscadaCsvDTO;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public class SozialversicherungsnummerFixPreprocessor {

    private final SozialversicherungsNummerTestGenerator AHVNR_GENERATOR =
        new SozialversicherungsNummerTestGenerator(756123400000L);

    /**
     * This method preprocesses the csvDTOs and fixes the invalid sozialversicherungsnummer
     * by generating a new one. If the sozialversicherungsnummer is valid, it will be kept.
     * Every broken one will always be replaced with the same generated sozialversicherungsnummer.
     *
     * @param csvDTOs the list of csvDTOs to preprocess
     * @return the list of csvDTOs with fixed sozialversicherungsnummer
     */
    public static List<EscadaCsvDTO> preprocessAndFixInvalidCSVNumbers(@NonNull List<EscadaCsvDTO> csvDTOs) {
        Map<String, SozialversicherungsNummer> genAhvMapping = new HashMap<>();

        Set<SozialversicherungsNummer> alreadyTakenValidAhv = extractSetOfValidAhvNummern(csvDTOs);

        // collect all the sozialversicherungsnummer and generate a new one if it is not valid
        for (EscadaCsvDTO item : csvDTOs) {
            if (StringUtils.isEmpty(item.schuelerSozialversicheurng())) {
                continue;
            }
            try {
                SozialversicherungsNummer svnr =
                    new SozialversicherungsNummer(item.schuelerSozialversicheurng());
                Validate.isTrue(svnr.isValid());
            } catch (AppValidationException e) {
                if (genAhvMapping.containsKey(item.schuelerSozialversicheurng())) {
                    continue;
                }

                SozialversicherungsNummer genNr =
                    fixPruefzifferAndEnsureUnique(item.schuelerSozialversicheurng(), alreadyTakenValidAhv);
                alreadyTakenValidAhv.add(genNr);
                genAhvMapping.put(item.schuelerSozialversicheurng(), genNr);
            }
        }

        List<EscadaCsvDTO> resultWithFixedAhv = new ArrayList<>();
        // replace the sozialversicherungsnummer with the generated one
        for (EscadaCsvDTO escadaCsvDTO : csvDTOs) {
            if (StringUtils.isNotEmpty(escadaCsvDTO.schuelerSozialversicheurng())
                && genAhvMapping.containsKey(escadaCsvDTO.schuelerSozialversicheurng())
            ) {
                var fixed =
                    escadaCsvDTO.withSchuelerSozialversicheurng(genAhvMapping.get(escadaCsvDTO.schuelerSozialversicheurng())
                        .toFormattedString());
                resultWithFixedAhv.add(fixed);
            } else {

                resultWithFixedAhv.add(escadaCsvDTO);
            }
        }
        return resultWithFixedAhv;

    }

    @NonNull
    private static SozialversicherungsNummer fixPruefzifferAndEnsureUnique(
        @NonNull String svnrString,
        @NonNull Set<SozialversicherungsNummer> alreadyTakenValidAhv
    ) {
        var genNr = SozialversicherungsNummerTestGenerator.repairPruefziffer(svnrString);
        if (!alreadyTakenValidAhv.contains(genNr)) {
            return genNr;
        }
        int tries = 0;
        do {
            genNr = AHVNR_GENERATOR.getNextSozialversicherungsnummer();
        } while (alreadyTakenValidAhv.contains(genNr) && tries++ < 100);
        return genNr;
    }

    /**
     * This method takes schulerId and SozialversicherungsNummer and makes sure that every SozialversicherungsNummer
     * is unique for each schulerId. If not, it will generate a new one.
     */
    @NonNull
    public static List<EscadaCsvDTO> preprocToEnsureUniqueAhvPerSchuelerID(@NonNull List<EscadaCsvDTO> csvDTOs) {

        Multimap<String, String> multimap = ArrayListMultimap.create();

        csvDTOs.stream()
            .filter(item -> StringUtils.isNotEmpty(item.schuelerSozialversicheurng()))
            .forEach(item -> multimap.put(item.schuelerSozialversicheurng(), item.schuelerId()));

        Map<String, SozialversicherungsNummer> deduplicateAhv = new HashMap<>();
        multimap.asMap().entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .forEach(entry -> {
                for (String schuelerId : new HashSet<>(entry.getValue())) {
                    SozialversicherungsNummer genNr =
                        AHVNR_GENERATOR.getNextSozialversicherungsnummer();
                    var putResult = deduplicateAhv.put(schuelerId, genNr);
                    Validate.isTrue(putResult == null, "Duplicate schuelerId found: %s", schuelerId);
                }
            });

        List<EscadaCsvDTO> resultWithFixedAhv = new ArrayList<>();
        // replace the sozialversicherungsnummer with the generated one if it is a duplicate
        for (EscadaCsvDTO escadaCsvDTO : csvDTOs) {
            if (StringUtils.isNotEmpty(escadaCsvDTO.schuelerSozialversicheurng())
                && deduplicateAhv.containsKey(escadaCsvDTO.schuelerId())
            ) {
                var fixed =
                    escadaCsvDTO.withSchuelerSozialversicheurng(deduplicateAhv.get(escadaCsvDTO.schuelerId())
                        .toFormattedString());
                resultWithFixedAhv.add(fixed);
            } else {
                resultWithFixedAhv.add(escadaCsvDTO);
            }
        }
        return resultWithFixedAhv;
    }

    @NonNull
    private static Set<SozialversicherungsNummer> extractSetOfValidAhvNummern(@NonNull List<EscadaCsvDTO> csvDTOs) {
        return csvDTOs.stream()
            .filter(item -> StringUtils.isNotEmpty(item.schuelerSozialversicheurng()))
            .map(item -> {
                try {
                    return new SozialversicherungsNummer(item.schuelerSozialversicheurng());
                } catch (AppValidationException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    @NonNull
    public static Set<String> extractSetOfInvalidAhvNummern(@NonNull List<EscadaCsvDTO> csvDTOs) {
        return csvDTOs.stream()
            .filter(item -> StringUtils.isNotEmpty(item.schuelerSozialversicheurng()))
            .map(item -> {
                try {
                    return Optional.<String>empty();
                } catch (AppValidationException e) {
                    return Optional.of(item.schuelerSozialversicheurng());
                }
            })
            .flatMap(Optional::stream)
            .collect(Collectors.toSet());
    }
}
