package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.types.AbschlussResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.VerkuerzungHamstringmuskulatur;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammSimpel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Feinmotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GleichgewichtEinbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Grafomotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GrobmotorikEinbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.OhneStuetzradVelofahren;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenAugeEinzelnResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.Immunsuppression;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatusMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGeprueft;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfempfehlungMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfungDurchgefuehrtMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.ChronischeKrankheiten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.EinverstaendnisImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.FragebogenStatus;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Mit dem SingleValueEnhanced werden die Enums im JSON als String ans Frontend geliefert,
 * damit die Component wiederverwendet werden kann.
 * Damit die Types trotzdem generiert werden, gibt es im DeveloperResource einen Endpoint,
 * welcher mit diesem Json alle Enums referenziert.
 */
@JaxDTO
@Value
@NoArgsConstructor
public class UnusedEnumsToGenerateJson {

    @Nullable
    @Schema(required = false)
    private Nachsprechen nachsprechen = Nachsprechen.BESTANDEN;

    @Nullable
    @Schema(required = false)
    private NichtGeprueftGrund nichtGeprueftGrund = NichtGeprueftGrund.ANDERER_GRUND;

    @Nullable
    @Schema(required = false)
    private AudiogrammSimpel audiogrammSimpel = AudiogrammSimpel.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private HoerenHoerhilfe hoerenHoerhilfe = HoerenHoerhilfe.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private DistanzSehenAugeEinzelnResultat distanzsehenaugeeinzelnresultat = DistanzSehenAugeEinzelnResultat.NORMAL;

    @Nullable
    @Schema(required = false)
    private ImpfausweisGeprueft impfausweisGeprueft = ImpfausweisGeprueft.JA;

    @Nullable
    @Schema(required = false)
    private Immunsuppression immunsuppression = Immunsuppression.NEIN;

    @Nullable
    @Schema(required = false)
    private ImpfStatusMinimal impfStatusMinimal = ImpfStatusMinimal.VOLLSTAENDIG;

    @Nullable
    @Schema(required = false)
    private ImpfempfehlungMinimal impfempfehlungMinimal = ImpfempfehlungMinimal.NEIN;

    @Nullable
    @Schema(required = false)
    private ImpfungDurchgefuehrtMinimal impfungDurchgefuehrtMinimal = ImpfungDurchgefuehrtMinimal.JA;

    @Nullable
    @Schema(required = false)
    private AbschlussResultat abschlussResultat = AbschlussResultat.KEINE_ANGABE;

    @Nullable
    @Schema(required = false)
    private FragebogenStatus fragebogenStatus = FragebogenStatus.NICHT_ERHALTEN;

    @Nullable
    @Schema(required = false)
    private EinverstaendnisImpfungen einverstaendnisImpfungen = EinverstaendnisImpfungen.NICHT_ERHALTEN;

    @Nullable
    @Schema(required = false)
    private ChronischeKrankheiten chronischeKrankheiten = ChronischeKrankheiten.NICHT_VORHANDEN;

    @Nullable
    @Schema(required = false)
    private Wortschatz wortschatz = Wortschatz.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private Sprachverstaendnis sprachverstaendnis = Sprachverstaendnis.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private Skoliose skoliose = Skoliose.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private Haltungsschwaeche haltungsschwaeche = Haltungsschwaeche.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private VerkuerzungHamstringmuskulatur verkuerzungHamstringmuskulatur = VerkuerzungHamstringmuskulatur.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private FreiesGehen freiesGehen = FreiesGehen.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private OhneStuetzradVelofahren ohneStuetzradVelofahren = OhneStuetzradVelofahren.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private Grafomotorik grafomotorik = Grafomotorik.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private Feinmotorik feinmotorik = Feinmotorik.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private GrobmotorikEinbeinhuepfen grobmotorikEinbeinhuepfen = GrobmotorikEinbeinhuepfen.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private GleichgewichtEinbeinstand gleichgewichtEinbeinstand = GleichgewichtEinbeinstand.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private BrilleLinsenResultat brilleLinsenResultat = BrilleLinsenResultat.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private StereotestResultat stereotestResultat = StereotestResultat.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private FarbsehenResultat farbsehenResultat = FarbsehenResultat.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private VerwendetTafelResultat verwendetTafelResultat = VerwendetTafelResultat.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private MotilitaetResultat motilitaetResultat = MotilitaetResultat.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private StrabismusAuspraegungResultat strabismusAuspraegungResultat = StrabismusAuspraegungResultat.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private StrabismusAuftretenResultat strabismusAuftretenResultat = StrabismusAuftretenResultat.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private InspektionKonvergenzResultat inspektionKonvergenzResultat = InspektionKonvergenzResultat.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private PerzentileGroesse perzentileGroesse = PerzentileGroesse.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private PerzentileGewicht perzentileGewicht = PerzentileGewicht.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private PerzentileBmi perzentileBmi = PerzentileBmi.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private OtoskopieResultat otoskopieResultat = OtoskopieResultat.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private AudiogrammBeurteilung audiogrammBeurteilung = AudiogrammBeurteilung.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private HoerenZusaetzlicheAngaben zusaetzlicheAngaben = HoerenZusaetzlicheAngaben.KEINE;

    @Nullable
    @Schema(required = false)
    private HoerenEmpfehlung hoerenEmpfehlung = HoerenEmpfehlung.UEBERPRUEFUNG_ARZT;

    @Nullable
    @Schema(required = false)
    private DistanzSehenResultat distanzSehenResultat = DistanzSehenResultat.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private StrabismusResultat strabismusResultat = StrabismusResultat.NICHT_GEPRUEFT;

    @Nullable
    @Schema(required = false)
    private SehenEmpfehlung sehenEmpfehlung = SehenEmpfehlung.UEBERPRUEFUNG_AUGENARZT;
}
