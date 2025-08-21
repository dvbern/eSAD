package ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.Util;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.util.StringNormalizerUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@Getter
@ToString
public class DuplikatSearchTerm {

    @Nullable
    ID<Kind> currentKindId = null;

    @NonNull
    List<String> nameSearchTerms = new ArrayList<>();

    @NonNull
    List<String> vornameSearchTerms = new ArrayList<>();

    @NonNull
    LocalDate geburtsdatumSearchTerm;

    @NonNull
    Geschlecht geschlechtSearchTerm;


    public DuplikatSearchTerm(
        @Nullable ID<Kind> id,
        @NonNull String name,
        @NonNull String vorname,
        @NonNull LocalDate geburtsdatum,
        @NonNull Geschlecht geschlecht
    ) {
        this.currentKindId = id;
        this.nameSearchTerms = splitSearchTermsAndRemoveEmpty(name);
        this.vornameSearchTerms = splitSearchTermsAndRemoveEmpty(vorname);
        this.geburtsdatumSearchTerm = geburtsdatum;
        this.geschlechtSearchTerm = geschlecht;
    }

    @NonNull
    private static List<String> splitSearchTermsAndRemoveEmpty(@NonNull String searchTerm) {
        List<String> tokens = Util.tokenizeNames(searchTerm);
        return tokens.stream()
            .filter(s -> !s.isEmpty())
            .map(String::trim)
            .map(StringNormalizerUtil::normalizeAndStripDiarcritics)
            .toList();
    }
}
