package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithDateTimeJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Value
@AllArgsConstructor
public class AnlassUntersuchungPrintDTO {

    @NonNull
    Anlass anlass;
    @Nullable
    Untersuchung untersuchung;
    @Nullable
    Einladung einladung;
    @Nullable
    String hash;
    @NonNull
    List<UntersuchungImpfungIntern> impfungInternList;
    @NonNull
    List<UntersuchungImpfstatusKrankheit> impfstatusList;
    @NonNull
    List<AnlassWithDateTimeJson> folgeImpfAnlassList;

    @NonNull
    public static AnlassUntersuchungPrintDTO ofEinladung(
        @NonNull Einladung einladung,
        @NonNull List<UntersuchungImpfungIntern> impfungInternList,
        @NonNull List<UntersuchungImpfstatusKrankheit> impfstatusList,
        @NonNull List<AnlassWithDateTimeJson> folgeImpfAnlassList
    ) {
        return new AnlassUntersuchungPrintDTO(
            einladung.getAnlass(),
            einladung.getUntersuchung(),
            einladung,
            null,
            impfungInternList,
            impfstatusList,
            folgeImpfAnlassList);
    }

    @NonNull
    public static AnlassUntersuchungPrintDTO anonymous(
        @NonNull Anlass anlass,
        @Nullable Untersuchung untersuchung,
        @NonNull String hash,
        @NonNull List<UntersuchungImpfungIntern> impfungInternList,
        @NonNull List<UntersuchungImpfstatusKrankheit> impfstatusList,
        @NonNull List<AnlassWithDateTimeJson> folgeImpfAnlassList
    ) {
        return new AnlassUntersuchungPrintDTO(
            anlass,
            untersuchung,
            null,
            hash,
            impfungInternList,
            impfstatusList,
            folgeImpfAnlassList);
    }

    public boolean isAnonymous() {
        return einladung == null;
    }

    @NonNull
    public EinladungStatus getEffectiveEinladungStatus() {
        return einladung == null ? EinladungStatus.TEILNAHME_BESTAETIGT : einladung.getEinladungStatus();
    }
}
