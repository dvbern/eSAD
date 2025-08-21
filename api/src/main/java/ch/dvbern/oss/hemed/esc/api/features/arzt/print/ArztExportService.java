/*
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.shared.docmerger.DocMergerUtil;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

@Service
public final class ArztExportService {

    private static final String AERZTE_EXPORT_TEMPLATE = "/print-vorlagen/aerzte-export.docx";
    private static final String AERZTE_EXPORT_FILE_NAME = "export_aerzte_%s.docx";
    private static final String MISSING_PLZ = "printer.arztliste.missing.plz";
    private static final String MISSING_ORT = "printer.arztliste.missing.ort";

    private ArztExportService() {
    }

    @NonNull
    public static PrintResultDTO generatAearzteExport(
        @NonNull List<Arzt> aerzte,
        @NonNull String fileTimeStamp,
        @NonNull TL tl
    ) {
        Map<ArztGroupKey, List<ArztMergerDTO>> aerzteByPlz = ArztExportService.arztMergerDtoGroupByPlz(aerzte, tl);
        ArztGoupMergeSource mergeSource = new ArztGoupMergeSource(aerzteByPlz);

        try (InputStream inputStream = BasePrinter.class.getResourceAsStream(AERZTE_EXPORT_TEMPLATE)) {
            byte[] result;
            if (inputStream != null) {
                result = DocMergerUtil.merge(inputStream, mergeSource);
            } else {
                throw new AppFailureException(AppFailureMessage.internalError("Unable to load ressource : " + AERZTE_EXPORT_TEMPLATE));
            }
            return new PrintResultDTO(result, ArztExportService.createFileInfo(result.length, fileTimeStamp));
        } catch (IOException e) {
            throw new AppFailureException(
                AppFailureMessage.internalError("Unable to load ressource : "
                + AERZTE_EXPORT_TEMPLATE), e);
        }
    }

    @NonNull
    public static Map<ArztGroupKey, List<ArztMergerDTO>> arztMergerDtoGroupByPlz(
        @NonNull List<Arzt> aerzte,
        @NonNull TL tl
    ) {
        return aerzte.stream()
            .map(ArztExportService::toArztMergerDTO)
            .collect(Collectors.groupingBy(arztMergerDTO -> new ArztGroupKey(
                StringUtils.trimToEmpty(arztMergerDTO.getPlz()).isEmpty() ?
                    tl.translate(MISSING_PLZ) :
                    requireNonNull(arztMergerDTO.getPlz()),
                StringUtils.trimToEmpty(arztMergerDTO.getOrt()).isEmpty() ?
                    tl.translate(MISSING_ORT) :
                    requireNonNull(arztMergerDTO.getOrt()))));
    }

    @NonNull
    public static ArztMergerDTO toArztMergerDTO(@NonNull Arzt arzt) {
        Optional<Arzt> arztOptional = Optional.of(arzt);
        String formattedPhone = (arzt.getTelefon() != null) ?  arzt.getTelefon().getFormattedPhoneNumber() : null;

        return new ArztMergerDTO(
            formatFullName(arzt),
            formattedPhone,
            arztOptional.map(Arzt::getEmail).map(EmailAddress::toString).orElse(null),
            arzt.getBemerkungen(),
            formatAddressOneLine(arzt.getAdresse()),
            Optional.of(arzt).map(Arzt::getAdresse).map(Adresse::getPlz).orElse(null),
            Optional.of(arzt).map(Arzt::getAdresse).map(Adresse::getOrt).orElse(null));
    }

    @NonNull
    public static FileInfo createFileInfo(long size, @NonNull String fileTimeStamp) {
        String fileName = String.format(AERZTE_EXPORT_FILE_NAME, fileTimeStamp);
        return FileDownloadUtil.buildFileInfo(fileName, FileDownloadUtil.MIME_TYPE_DOCX, size);
    }

    @NonNull
    static String formatFullName(@NonNull Arzt arzt) {
        String[] nameParts = { arzt.getAnrede(), arzt.getName(), arzt.getVorname() };
        return concatNonEmptyStrings(nameParts, " ");
    }

    @NonNull
    static String formatAddressOneLine(@Nullable Adresse adresse) {
        if (adresse == null) {
            return PrintUtil.PLACEHOLDER;
        }
        String plzOrt = String.join(
            " ",
            StringUtils.trimToEmpty(adresse.getPlz()),
            StringUtils.trimToEmpty(adresse.getOrt())
        );

        String[] addressParts ={ adresse.getStrasseUndNrNonBreakingSpaceSeparated(), adresse.getAdresseZusatz(), plzOrt };

        String adresseFormat = concatNonEmptyStrings(addressParts, ", ");

        if (adresseFormat.isBlank()) {
            return PrintUtil.PLACEHOLDER;
        }
        return adresseFormat;
    }

    @NonNull
    private static String concatNonEmptyStrings(@NonNull String[] values, @NonNull String delimiter) {
        return Stream.of(values)
            .map(StringUtils::trimToEmpty)
            .filter(Predicate.not(String::isEmpty))
            .collect(Collectors.joining(delimiter));
    }
}
