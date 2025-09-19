package ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Test Provider for SozialversicherungsNummer, it provides tries to
 * generate valid SozialversicherungsNummer for testing purposes.
 * Each number should be unique. The Service will track the generated numbers
 * and return a new one each time it is called.
 */
public class SozialversicherungsNummerTestGenerator {

    private static final long INITIAL_SEED_NUMBER = 756123456789L;
    public static final SozialversicherungsNummerTestGenerator INSTANCE = new SozialversicherungsNummerTestGenerator(INITIAL_SEED_NUMBER);


    public SozialversicherungsNummerTestGenerator(long counter) {
        this.counter = counter;
    }

    // seed number for the generation of the next Sozialversicherungsnummer, omits pruefziffer
    private long counter = INITIAL_SEED_NUMBER;

    public synchronized SozialversicherungsNummer getNextSozialversicherungsnummer() {

        int pz = SozialversicherungsNummer.berechnePruefziffer(counter * 10);
        long nextSvnr = Long.parseLong(counter + "" + pz);
        SozialversicherungsNummer sozialversicherungsNummer = new SozialversicherungsNummer(nextSvnr);
        counter++;
        return sozialversicherungsNummer;

    }

    /**
     * Repairs the pruefziffer of a Sozialversicherungsnummer.
     *
     * @param svnrString the Sozialversicherungsnummer as a string
     * @return the repaired Sozialversicherungsnummer
     */
    @NonNull
    public static SozialversicherungsNummer repairPruefziffer(@NonNull String svnrString) {
        String svnrNoDots = svnrString.replace(".", "");
        long seed = Long.parseLong(svnrNoDots.substring(0, svnrNoDots.length() - 1));
        int pz = SozialversicherungsNummer.berechnePruefziffer(seed * 10);
        long nextSvnr = Long.parseLong(seed + "" + pz);
        SozialversicherungsNummer sozialversicherungsNummer = new SozialversicherungsNummer(nextSvnr);
        return sozialversicherungsNummer;
    }
}
