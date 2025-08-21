package ch.dvbern.oss.hemed.esc.api.features.search.types;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import ch.dvbern.oss.hemed.esc.api.Util;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

@Value
public class KinderQuickSearchTerm {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    // pattern for edokid Id it should extract the longest sequence of digits and whitespaces
    // that may be a potential edokid id that looks like A0 01 456 24
    private static final Pattern EDOKID_ID_PATTERN = Pattern.compile(
        "([a-zA-Z]\\d[0-9 ]{0,10})|(\\d\\d[0-9 ]{0,10})"
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
        "(\\d{1,2}\\.\\d{1,2}\\.\\d{4})"
    );

    List<String> nameSearchTerms = new ArrayList<>();
    List<LocalDate> dateSearchTerms = new ArrayList<>();
    List<String> edokidIdSearchTerms = new ArrayList<>();

    public KinderQuickSearchTerm(@NonNull String fullSearchTerm) {
        // try to find and extract date search terms and return the string without the date search terms
        String searchTermWithoutDates = extractDateSearchTerms(fullSearchTerm);
        // since esad id may have lots of whitespaes try to identify potential esadId search terms separately
        // to search for them as a continous string
        recognizeEsadIdSearchTerms(searchTermWithoutDates);
        List<String> stringsToSearch = splitSearchTermsAndRemoveEmpty(searchTermWithoutDates);
        siftNameSearchTermsAndNormalize(stringsToSearch);
    }

    @NonNull
    private String extractDateSearchTerms(@NonNull String searchTerm) {
        // try to find and extract date search terms and return the string without the date search terms
        var matcher = DATE_PATTERN.matcher(searchTerm);
        while (matcher.find()) {
            String matchedString = matcher.group().trim();
            if (!matchedString.isEmpty()) {
                Optional<LocalDate> date = parseAsDate(matchedString);
                date.ifPresent(dateSearchTerms::add);
            }
        }
        return matcher.replaceAll(StringUtils.EMPTY);
    }

    private void recognizeEsadIdSearchTerms(@NonNull String searchTerm) {
        var matcher = EDOKID_ID_PATTERN.matcher(searchTerm);
        while (matcher.find()) {
            String esadId = matcher.group().trim();
            if (!esadId.isEmpty()) {
                edokidIdSearchTerms.add(esadId);
            }
        }
    }

    private void siftNameSearchTermsAndNormalize(@NonNull List<String> strings) {
        for (String searchTerm : strings) {
            // assume that the search Term for name does not include digits Only
            // to reduce the numbner of search terms to run against the names
            if (!searchTerm.chars().allMatch(this::isNumberOrPointOrComma) &&
                !isStartOfEsadid(searchTerm)
            ) {
                nameSearchTerms.add(searchTerm.toLowerCase());
            }
        }
    }

    /**
     * @return true if the string is 2 digits long and has the form [A-Z][0-9]
     */
    private boolean isStartOfEsadid(@NonNull String searchTerm) {
        return searchTerm.length() == 2 && Character.isAlphabetic(searchTerm.charAt(0))
            && Character.isDigit(searchTerm.charAt(1));
    }

    private boolean isNumberOrPointOrComma(int codePoint) {
        char c = (char) codePoint;
        return Character.isDigit(c) || c == '.' || c == ',';
    }

    @NonNull
    private static List<String> splitSearchTermsAndRemoveEmpty(@NonNull String searchTerm) {
        // keep quote string blocks together
        List<String> tokens = Util.tokenizeStringKeepingQuotedParts(searchTerm, ' ', '"');
        return tokens.stream()
            .filter(s -> !s.isEmpty())
            .map(String::trim)
            .toList();
    }

    @NonNull
    private static Optional<LocalDate> parseAsDate(@NonNull String input) {
        try {
            LocalDate date = DATE_FORMATTER.parse(input, LocalDate::from);
            return Optional.of(date);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
