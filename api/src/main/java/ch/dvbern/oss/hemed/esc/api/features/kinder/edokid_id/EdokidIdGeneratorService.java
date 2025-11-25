package ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id;

import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
public class EdokidIdGeneratorService {

    public static final String MISSING_SCHULE_NUM = "000";

    private final AppClock appClock;
    private final EdokidIdRepo edokidIdRepo;

    @NonNull
    public EdokidId generateNextEdokidId(@Nullable Schule schule) {
        int eintrittsJahr = getEintrittsJahr();
        EdokidIdSequence sequence = getNextEdokidIdSequence(eintrittsJahr);
        String schulhausNummer = getSchulhausNummer(schule);
        Validate.isTrue(
            sequence.getEintrittsJahr() == eintrittsJahr,
            "Eintrittsjahr in EdokidIdSequence does not match current Eintrittsjahr");

        return new EdokidId(
            sequence.getCharacter(),
            sequence.getFormattedLaufnummer(),
            schulhausNummer,
            sequence.getEintrittsJahr());

    }

    EdokidIdSequence getNextEdokidIdSequence(int eintrittsJahr) {
        return edokidIdRepo.incrementLaufNummer(eintrittsJahr);

    }

    @NonNull
    public String getSchulhausNummer(@Nullable Schule schule) {
        if (schule == null) {
            return MISSING_SCHULE_NUM;
        }
        return schule.getFormattedSchulhausnummer();
    }

    // Return Eintrittsjahr in the format of the EdokidId (e.g. 24, not 2024)
    public int getEintrittsJahr() {
        LocalDate currentDate = appClock.getToday();
        return calculateEintrittsJahr(currentDate);
    }

    public static int calculateEintrittsJahr(@NonNull LocalDate stichtag) {
        int jahr = stichtag.isBefore(LocalDate.of(stichtag.getYear(), Month.AUGUST, 1)) ?
            stichtag.getYear() - 1 :
            stichtag.getYear();

        return Integer.parseInt(Integer.toString(jahr).substring(2));
    }

}
