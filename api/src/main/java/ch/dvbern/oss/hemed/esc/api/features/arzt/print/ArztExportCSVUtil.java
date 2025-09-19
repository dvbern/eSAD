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

package ch.dvbern.oss.hemed.esc.api.features.arzt.print;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.hemed.esc.api.features.arzt.print.ArztExportService.TRANSLATION_PREFIX;

@UtilityClass
public class ArztExportCSVUtil {


    public static byte[] generatAearzteExportCsv(
        @NonNull List<Arzt> aerzte,
        @NonNull TL tl
    ) {
        List<List<String>> records = aerzte.stream().map(arzt -> arztToCsvRow(arzt, tl)).toList();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(getArztCsvHeaders(tl)).get();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (final CSVPrinter printer = new CSVPrinter(
            new OutputStreamWriter(baos, StandardCharsets.UTF_8),
            csvFormat)) {
            printer.printRecords(records);
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error while generating Arzt CSV export"), e);
        }

        return baos.toByteArray();

    }

    @NonNull
    private static List<String> arztToCsvRow(@NonNull Arzt arzt, @NonNull TL tl) {
        return Stream.of(
            arzt.getAnrede(),
            arzt.getName(),
            arzt.getVorname(),
            Optional.of(arzt).map(Arzt::getAdresse).map(Adresse::getAdresseZusatz).orElse(""),
            Optional.of(arzt).map(Arzt::getAdresse).map(Adresse::getStrasseUndNr).orElse(""),
            Optional.of(arzt).map(Arzt::getAdresse).map(Adresse::getPlz).orElse(""),
            Optional.of(arzt).map(Arzt::getAdresse).map(Adresse::getOrt).orElse(""),
            Optional.of(arzt).map(Arzt::getEmail).map(EmailAddress::toString).orElse(""),
            Optional.of(arzt).map(Arzt::getTelefon).map(NormalizedPhoneNumber::getFormattedPhoneNumber).orElse(""),
            arzt.getBemerkungen(),
            tl.translate(TRANSLATION_PREFIX + "hpvteilnahme." + arzt.getHpvTeilnahme().name())
        ).map(cellValue -> cellValue == null ? "" : cellValue).toList();
    }

    private static String[] getArztCsvHeaders(@NonNull TL tl) {
        return Stream.of(
                "anrede",
                "name",
                "vorname",
                "adressezusatz",
                "strasseundnr",
                "plz",
                "ort",
                "email",
                "telfon",
                "bemerkung",
                "hpvteilnahme"
            ).map(header -> tl.translate(TRANSLATION_PREFIX + header))
            .toArray(String[]::new);
    }

}
