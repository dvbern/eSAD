package ch.dvbern.oss.hemed.esc.api.shared.util;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungKrankheitImmunitaet;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfschutzGegeben;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class UntersuchungImpfungenUtil {

    @NonNull
    public static List<UntersuchungImpfstatusKrankheit> completeListFromExistingForEachKrankheit(
        @NonNull UntersuchungImpfungen impfungen,
        @NonNull List<UntersuchungImpfstatusKrankheit> impfstatusLastAnlass,
        @NonNull List<UntersuchungImpfungIntern> impfungenInternListLastAnlass
    ) {
        return createOrUpdateImpstatusForAllKrankheiten(impfungen, impfungenInternListLastAnlass, impfstatusLastAnlass);
    }
    @NonNull
    public static List<UntersuchungImpfstatusKrankheit> completeListFromExistingForEachKrankheit(@NonNull UntersuchungImpfungen impfungen) {
        return createOrUpdateImpstatusForAllKrankheiten(impfungen, null, null);
    }

    @NonNull
    private static List<UntersuchungImpfstatusKrankheit> createOrUpdateImpstatusForAllKrankheiten(
        @NonNull UntersuchungImpfungen impfungen,
        @Nullable List<UntersuchungImpfungIntern> impfungenInternListLastAnlass,
        @Nullable List<UntersuchungImpfstatusKrankheit> impfstatusLastAnlass
    ) {
        AnlassConfig config = impfungen.getUntersuchung().getAnlass().getAnlassConfig();
        EnumSet<Krankheit> krankheiten = getKrankheitenFromAnlassConfig(config);

        return krankheiten.stream().map( krankheit -> {
            UntersuchungImpfstatusKrankheit newImpfung =
                UntersuchungImpfstatusKrankheit.forKrankheit(impfungen, krankheit);

            // initialized with version zero but not yet persisted
            // avoid creating the version zero when actually persisting
            newImpfung.setVersion(-1L);

            // Falls beim letzten Anlass der Impfschutz als genügend eingestuft worden ist, werden die Werte
            // übernommen. Ansonsten werden leere Einträge erstellt.
            if (krankheit.benoetigtAuffrischung() && impfstatusLastAnlass != null && impfungenInternListLastAnlass != null) {
                setUebernehmbareImpfstatus(impfstatusLastAnlass, impfungenInternListLastAnlass, krankheit, newImpfung);
            }

            return newImpfung;
        }).toList();
    }

    private static EnumSet<Krankheit> getKrankheitenFromAnlassConfig(AnlassConfig config) {
        EnumSet<Krankheit> krankheiten = config.getUntersuchungImpfungKrankheitenLinksValues();
        krankheiten.addAll(config.getUntersuchungImpfungKrankheitenRechtsValues());

        return krankheiten;
    }

    private static void setUebernehmbareImpfstatus(
        @NonNull List<UntersuchungImpfstatusKrankheit> impfstatusLastAnlass,
        @NonNull List<UntersuchungImpfungIntern> impfungenInternListLastAnlass,
        @NonNull Krankheit krankheit,
        @NonNull UntersuchungImpfstatusKrankheit newImpfung
    ) {
        Optional<UntersuchungImpfstatusKrankheit> oldImpfstatus = impfstatusLastAnlass.stream()
            .filter(impfstatus -> impfstatus.getKrankheit() == krankheit
                && impfstatus.getImpfschutzGegeben() == ImpfschutzGegeben.JA)
            .findFirst();

        if (oldImpfstatus.isPresent()) {
            newImpfung.setAnzahlImpfungen(oldImpfstatus.get().getAnzahlImpfungen());
            newImpfung.setImpfschutzGegeben(oldImpfstatus.get().getImpfschutzGegeben());
            newImpfung.setDatumLetzteImpfung(oldImpfstatus.get().getDatumLetzteImpfung());
        }

        // Falls in der Impfliste des letzten Anlasses bei einer Krankheit der Impfschutz als genügend
        // eingestuft worden ist, werden diese Werte übernommen.

        // Für die Krankheit werden sämtliche Impfungen des letzten Anlasses gesucht.
        Optional<UntersuchungImpfungIntern> impfungInternLastAnlassOptional = impfungenInternListLastAnlass.stream()
            .filter(impfungLastAnlass -> impfungLastAnlass.getImpfstoff().getKrankheiten().contains(krankheit)
                && isImpfungFromLastAnlass(impfungLastAnlass)
                && impfungLastAnlass.getKrankheitenImmunitaeten()
            .stream()
            .anyMatch(ki -> ki.getKrankheit() == krankheit && ki.isImmunitaetGenuegend()))
            .findFirst();

        if (impfungInternLastAnlassOptional.isPresent()) {
            int bisherigeAnzahlImpfungen = impfstatusLastAnlass.stream()
                .filter(impfstatus -> impfstatus.getKrankheit() == krankheit)
                .findFirst()
                .map(UntersuchungImpfstatusKrankheit::getAnzahlImpfungen)
                .orElse(0);

            newImpfung.setImpfschutzGegeben(ImpfschutzGegeben.JA);
            newImpfung.setAnzahlImpfungen(bisherigeAnzahlImpfungen + 1);
            newImpfung.setDatumLetzteImpfung(impfungInternLastAnlassOptional.get().getDatumImpfung());
        }
    }

    private static boolean isImpfungFromLastAnlass(@NonNull UntersuchungImpfungIntern impfungLastAnlass) {
        return impfungLastAnlass.getAnlassImpfungDurchgefuehrt() != null
            && impfungLastAnlass.getUntersuchungImpfungen().getUntersuchung().getAnlass().getId()
            .equals(impfungLastAnlass.getAnlassImpfungDurchgefuehrt().getId());
    }

    @NonNull
    public static UntersuchungImpfungIntern copyImpfung(
        @NonNull UntersuchungImpfungen untersuchungImpfungen,
        @NonNull UntersuchungImpfungIntern impfung
    ) {
        UntersuchungImpfungIntern newImpfung = UntersuchungImpfungIntern.forUntersuchungImpfung(untersuchungImpfungen);
        newImpfung.setImpfstoff(impfung.getImpfstoff());
        newImpfung.setCharge(impfung.getCharge());
        newImpfung.setDatumImpfung(impfung.getDatumImpfung());
        newImpfung.setImpfStatus(impfung.getImpfStatus());
        newImpfung.setUserDurchgefuehrt(impfung.getUserDurchgefuehrt());
        newImpfung.setGrundNichtDurchfuehrung(impfung.getGrundNichtDurchfuehrung());
        newImpfung.setAnlassImpfungErfasst(impfung.getAnlassImpfungErfasst());
        newImpfung.setUserErfasst(impfung.getUserErfasst());
        newImpfung.setAnlassImpfungDurchgefuehrt(impfung.getAnlassImpfungDurchgefuehrt());

        newImpfung.setKrankheitenImmunitaeten(impfung.getKrankheitenImmunitaeten()
            .stream()
            .map(immunitaet -> {
                UntersuchungImpfungKrankheitImmunitaet newImmunitaet =
                    new UntersuchungImpfungKrankheitImmunitaet();
                newImmunitaet.setImpfung(newImpfung);
                newImmunitaet.setKrankheit(immunitaet.getKrankheit());
                newImmunitaet.setImmunitaetGenuegend(immunitaet.isImmunitaetGenuegend());
                return newImmunitaet;
            })
            .collect(Collectors.toSet()));

        return newImpfung;
    }

}
