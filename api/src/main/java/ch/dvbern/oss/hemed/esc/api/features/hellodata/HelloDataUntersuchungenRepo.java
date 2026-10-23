package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.ReadOnlyRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.QUntersuchungAbschluss;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.QUntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.QUntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.QUntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.QUntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.QAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.QUntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.QUntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungenNurUnvollstaendige;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.QUntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.QUntersuchungLaeusekontrolle;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.QUntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.QUntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.QUntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.QUntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.QUntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.QUntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.QUntersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.QUntersuchungVorabklaerungen;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataUntersuchungenJson;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.jpa.HibernateHints;

@Repository
@RequiredArgsConstructor
public class HelloDataUntersuchungenRepo extends ReadOnlyRepo {

    private static final int FETCH_SIZE = 500;

    public static final QAudiogramm AUDIO_RECHTS = new QAudiogramm("audioR");
    public static final QAudiogramm AUDIO_LINKS = new QAudiogramm("audioL");

    /**
     * Expression list for Untersuchung root entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#toDto(Tuple)}.
     */
    public static final List<Expression<?>> UNTERSUCHUNG_EXPRS = List.of(
        QUntersuchung.untersuchung.id
    );

    /**
     * Expression list for KindDaten sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapKindDaten(Tuple)}.
     */
    public static final List<Expression<?>> KIND_DATEN_EXPRS = List.of(
        QUntersuchungKindDaten.untersuchungKindDaten.id,
        QUntersuchungKindDaten.untersuchungKindDaten.klassenstufe,
        QUntersuchungKindDaten.untersuchungKindDaten.klassentyp,
        QUntersuchungKindDaten.untersuchungKindDaten.klasse,
        QUntersuchungKindDaten.untersuchungKindDaten.schulhaus,
        QUntersuchungKindDaten.untersuchungKindDaten.schulstandort,
        QUntersuchungKindDaten.untersuchungKindDaten.quartier,
        QUntersuchungKindDaten.untersuchungKindDaten.schulkreis,
        QUntersuchungKindDaten.untersuchungKindDaten.schulhausnummer,
        QUntersuchungKindDaten.untersuchungKindDaten.nationalitaet,
        QUntersuchungKindDaten.untersuchungKindDaten.geschlecht
    );

    /**
     * Expression list for Durchfuehrung sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapDurchfuehrung(Tuple)}.
     */
    public static final List<Expression<?>> DURCHFUEHRUNG_EXPRS = List.of(
        QUntersuchungDurchfuehrung.untersuchungDurchfuehrung.id,
        QUntersuchungDurchfuehrung.untersuchungDurchfuehrung.nachkontrolle,
        QUntersuchungDurchfuehrung.untersuchungDurchfuehrung.nachzuegler,
        QUntersuchungDurchfuehrung.untersuchungDurchfuehrung.neueintritt
    );

    /**
     * Expression list for Vorabklaerungen sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapVorabklaerungen(Tuple)}.
     */
    public static final List<Expression<?>> VORABKLAERUNGEN_EXPRS = List.of(
        QUntersuchungVorabklaerungen.untersuchungVorabklaerungen.id,
        QUntersuchungVorabklaerungen.untersuchungVorabklaerungen.fragebogenStatus.value,
        QUntersuchungVorabklaerungen.untersuchungVorabklaerungen.einverstaendnisImpfungen.value,
        QUntersuchungVorabklaerungen.untersuchungVorabklaerungen.chronischeKrankheiten.value
    );

    /**
     * Expression list for Triage sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapTriage(Tuple)}.
     */
    public static final List<Expression<?>> TRIAGE_EXPRS = List.of(
        QUntersuchungTriage.untersuchungTriage.id,
        QUntersuchungTriage.untersuchungTriage.triageSu,
        QUntersuchungTriage.untersuchungTriage.triageMpa,
        QUntersuchungTriage.untersuchungTriage.befundSu,
        QUntersuchungTriage.untersuchungTriage.wunschSchueler,
        QUntersuchungTriage.untersuchungTriage.vorinformationLehrperson
    );

    /**
     * Expression list for Impfungen sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapImpfungen(Tuple)}.
     */
    public static final List<Expression<?>> IMPFUNGEN_EXPRS = List.of(
        QUntersuchungImpfungen.untersuchungImpfungen.id,
        QUntersuchungImpfungen.untersuchungImpfungen.impfausweisUnvollstaendig,
        QUntersuchungImpfungen.untersuchungImpfungen.impfausweisNichtVorhanden,
        QUntersuchungImpfungen.untersuchungImpfungen.impfausweisZweiPlusEinsSchema,
        QUntersuchungImpfungen.untersuchungImpfungen.checkEinwilligungVorhanden,
        QUntersuchungImpfungen.untersuchungImpfungen.checkKontraindikationenAbgeklaert,
        QUntersuchungImpfungen.untersuchungImpfungen.checkDoppelkontrolleImpfentscheidDurchgefuehrt
    );

    /**
     * Expression list for Impfungen Massnahmen sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapImpfungenMassnahmen(Tuple)}.
     */
    public static final List<Expression<?>> IMPFUNGEN_MASSNAHMEN_EXPRS = List.of(
        QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen.id,
        QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen.keineMassnahmen,
        QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen.vormerkenFuerGruppenFolgetermin,
        QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen.impfberatungMpaDurchgefuehrt,
        QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen.infomaterialAbgegeben,
        QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen.informationEltern,
        QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen.informationHausarzt,
        QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen.rueckfrageHausarzt,
        QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen.empfehlung
    );

    /**
     * Expression list for Impfungen NurUnvollstaendige sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapImpfungenNurUnvollstaendige(Tuple)}.
     */
    public static final List<Expression<?>> IMPFUNGEN_NUR_UNVOLLSTAENDIGE_EXPRS = List.of(
        QUntersuchungImpfungenNurUnvollstaendige.untersuchungImpfungenNurUnvollstaendige.id,
        QUntersuchungImpfungenNurUnvollstaendige.untersuchungImpfungenNurUnvollstaendige.impfausweisVollstaendig,
        QUntersuchungImpfungenNurUnvollstaendige.untersuchungImpfungenNurUnvollstaendige.impfausweisGrundUnvollstaendig
    );

    /**
     * Expression list for Sehen sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapSehen(Tuple)}.
     */
    public static final List<Expression<?>> SEHEN_EXPRS = List.of(
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.id,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.brilleLinsen.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.stereotest.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.farbsehen.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.verwendeteTafel.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.fernvisus.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.fernvisusRechts,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.fernvisusLinks,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.fernvisusAugeLinks.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.fernvisusAugeRechts.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.nahvisus.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.nahvisusRechts,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.nahvisusLinks,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.motilitaet.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.strabismus.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.strabismusAuspraegung.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.strabismusAuftreten.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.inspektion.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.konvergenz.value,
        QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung.empfehlung.value
    );

    /**
     * Expression list for Hoeren Audiogramm sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapHoeren(Tuple)}.
     */
    public static final List<Expression<?>> HOEREN_AUDIOGRAMM_EXPRS = List.of(
        QUntersuchungHoerenAudiogramm.untersuchungHoerenAudiogramm.id,
        QUntersuchungHoerenAudiogramm.untersuchungHoerenAudiogramm.audiogrammSimpel.value,
        QUntersuchungHoerenAudiogramm.untersuchungHoerenAudiogramm.hoerhilfe.value,
        QUntersuchungHoerenAudiogramm.untersuchungHoerenAudiogramm.audiogrammBeurteilung.value,
        QUntersuchungHoerenAudiogramm.untersuchungHoerenAudiogramm.zusaetzlicheAngaben.value,
        QUntersuchungHoerenAudiogramm.untersuchungHoerenAudiogramm.empfehlung.value
    );

    /**
     * Expression list for Audiogramm (links / rechts).
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapAudiogramm(QAudiogramm, Tuple)}.
     */
    @NonNull
    public static List<Expression<?>> audiogrammExprs(@NonNull QAudiogramm audio) {
        return List.of(
            audio.id,
            audio.dbHz250,
            audio.dbHz500,
            audio.dbHz1000,
            audio.dbHz2000,
            audio.dbHz4000,
            audio.dbHz6000,
            audio.dbHz8000
        );
    }

    static final List<Expression<?>> AUDIOGRAMM_RECHTS_EXPRS = audiogrammExprs(AUDIO_RECHTS);
    static final List<Expression<?>> AUDIOGRAMM_LINKS_EXPRS = audiogrammExprs(AUDIO_LINKS);

    /**
     * Expression list for Hoeren Otoskopie sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapHoeren(Tuple)}.
     */
    public static final List<Expression<?>> HOEREN_OTOSKOPIE_EXPRS = List.of(
        QUntersuchungHoerenOtoskopie.untersuchungHoerenOtoskopie.id,
        QUntersuchungHoerenOtoskopie.untersuchungHoerenOtoskopie.otoskopieRechts.value,
        QUntersuchungHoerenOtoskopie.untersuchungHoerenOtoskopie.otoskopieLinks.value
    );

    /**
     * Expression list for Groesse & Gewicht sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapGroesseGewicht(Tuple)}.
     */
    public static final List<Expression<?>> GROESSE_GEWICHT_EXPRS = List.of(
        QUntersuchungGroesseGewicht.untersuchungGroesseGewicht.id,
        QUntersuchungGroesseGewicht.untersuchungGroesseGewicht.groesse,
        QUntersuchungGroesseGewicht.untersuchungGroesseGewicht.gewicht,
        QUntersuchungGroesseGewicht.untersuchungGroesseGewicht.bmi,
        QUntersuchungGroesseGewicht.untersuchungGroesseGewicht.groessePerzentileSingleValue.value,
        QUntersuchungGroesseGewicht.untersuchungGroesseGewicht.groessePerzentileMultiValue.value,
        QUntersuchungGroesseGewicht.untersuchungGroesseGewicht.gewichtPerzentileSingleValue.value,
        QUntersuchungGroesseGewicht.untersuchungGroesseGewicht.gewichtPerzentileMultiValue.value,
        QUntersuchungGroesseGewicht.untersuchungGroesseGewicht.bmiPerzentileSingleValue.value,
        QUntersuchungGroesseGewicht.untersuchungGroesseGewicht.bmiPerzentileMultiValue.value
    );

    /**
     * Expression list for Arzt Beurteilung sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapArzt(Tuple)}.
     */
    public static final List<Expression<?>> ARZT_BEURTEILUNG_EXPRS = List.of(
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.id,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.einverstaendniserklaerung,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.verhaltensfragebogen,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.verhalten,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.blutdruck,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.blutdruckSystolisch,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.blutdruckDiastolisch,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.herzauskultation,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.haut,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.mundAndZaehne,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.sprachfaehigkeitSingleValue,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.sprachfaehigkeitMultiValue,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.sprachfaehigkeitPunkte,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.motorik,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.motorikPunkte,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.kognition,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.kognitionPunkte,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.bewegungsapparat,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.einbeinstand,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.einbeinstandRechts,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.einbeinstandLinks,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.einbeinhuepfen,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.einbeinhuepfenRechts,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.einbeinhuepfenLinks,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.fingeropposition,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.hampelmann,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.ballFangen,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.ballWerfen,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.nameSchreiben,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.figurNachmalen,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.stifthaltung,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.aussprache,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.lesefit,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.lesefitPunkte,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.zahlenNachsprechen4,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.zahlenNachsprechen5,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.zeichnenKreis,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.zeichnenViereck,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.zeichnenDreieck,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.zeichnenMensch,
        QUntersuchungArztBeurteilung.untersuchungArztBeurteilung.zeichnenMenschPunkte
    );

    /**
     * Expression list for Sprache sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapSprache(Tuple)}.
     */
    public static final List<Expression<?>> SPRACHE_EXPRS = List.of(
        QUntersuchungSprache.untersuchungSprache.id,
        QUntersuchungSprache.untersuchungSprache.nachsprechen.value,
        QUntersuchungSprache.untersuchungSprache.wortschatz.value,
        QUntersuchungSprache.untersuchungSprache.sprachverstaendnis.value
    );

    /**
     * Expression list for Bewegungsapparat sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapBewegungsapparat(Tuple)}.
     */
    public static final List<Expression<?>> BEWEGUNGSAPPARAT_EXPRS = List.of(
        QUntersuchungBewegungsapparat.untersuchungBewegungsapparat.id,
        QUntersuchungBewegungsapparat.untersuchungBewegungsapparat.skoliose.value,
        QUntersuchungBewegungsapparat.untersuchungBewegungsapparat.haltungsschwaeche.value,
        QUntersuchungBewegungsapparat.untersuchungBewegungsapparat.verkuerzungHamstringmuskulatur.value
    );

    /**
     * Expression list for Schulrelevante Beeintraechtigung sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapSchulrelevanteBeeintraechtigung(Tuple)}.
     */
    public static final List<Expression<?>> SCHULRELEVANTE_BEEINTRAECHTIGUNG_EXPRS = List.of(
        QUntersuchungSchulrelevanteBeeintraechtigung.untersuchungSchulrelevanteBeeintraechtigung.id,
        QUntersuchungSchulrelevanteBeeintraechtigung.untersuchungSchulrelevanteBeeintraechtigung.freiesGehen.value,
        QUntersuchungSchulrelevanteBeeintraechtigung.untersuchungSchulrelevanteBeeintraechtigung.anzahlMonateFreiesGehen,
        QUntersuchungSchulrelevanteBeeintraechtigung.untersuchungSchulrelevanteBeeintraechtigung.ohneStuetzradVelofahren.value,
        QUntersuchungSchulrelevanteBeeintraechtigung.untersuchungSchulrelevanteBeeintraechtigung.grafomotorik.value,
        QUntersuchungSchulrelevanteBeeintraechtigung.untersuchungSchulrelevanteBeeintraechtigung.feinmotorik.value,
        QUntersuchungSchulrelevanteBeeintraechtigung.untersuchungSchulrelevanteBeeintraechtigung.grobmotorikEinbeinhuepfen.value,
        QUntersuchungSchulrelevanteBeeintraechtigung.untersuchungSchulrelevanteBeeintraechtigung.gleichgewichtEinbeinstand.value
    );

    /**
     * Expression list for Laeusekontrolle sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapLaeusekontrolle(Tuple)}.
     */
    public static final List<Expression<?>> LAEUSEKONTROLLE_EXPRS = List.of(
        QUntersuchungLaeusekontrolle.untersuchungLaeusekontrolle.id,
        QUntersuchungLaeusekontrolle.untersuchungLaeusekontrolle.beurteilung
    );

    /**
     * Expression list for Massnahmen sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapMassnahmen(Tuple)}.
     */
    public static final List<Expression<?>> MASSNAHMEN_EXPRS = List.of(
        QUntersuchungMassnahmen.untersuchungMassnahmen.id,
        QUntersuchungMassnahmen.untersuchungMassnahmen.keineMassnahmen,
        QUntersuchungMassnahmen.untersuchungMassnahmen.beratungMPA,
        QUntersuchungMassnahmen.untersuchungMassnahmen.beratungSchulAerztin,
        QUntersuchungMassnahmen.untersuchungMassnahmen.anderes,
        QUntersuchungMassnahmen.untersuchungMassnahmen.bereitsBehandeltSehen,
        QUntersuchungMassnahmen.untersuchungMassnahmen.bereitsBehandeltHoeren,
        QUntersuchungMassnahmen.untersuchungMassnahmen.bereitsBehandeltGroesseGewicht,
        QUntersuchungMassnahmen.untersuchungMassnahmen.bereitsBehandeltSprache,
        QUntersuchungMassnahmen.untersuchungMassnahmen.bereitsBehandeltMotorik,
        QUntersuchungMassnahmen.untersuchungMassnahmen.bereitsBehandeltAnderes
    );

    /**
     * Expression list for Abschluss sub-entity.
     * Keep in sync with {@link HelloDataUntersuchungenMapper#mapAbschluss(Tuple)}.
     */
    public static final List<Expression<?>> ABSCHLUSS_EXPRS = List.of(
        QUntersuchungAbschluss.untersuchungAbschluss.id,
        QUntersuchungAbschluss.untersuchungAbschluss.ueberweisungErfolgt.value,
        QUntersuchungAbschluss.untersuchungAbschluss.aerztlicheValidierungErforderlich.value,
        QUntersuchungAbschluss.untersuchungAbschluss.beratungDurchgefuehrt.value
    );

    static final List<Expression<?>> ALL_EXPRESSIONS = buildAllExpressions();
    private static final Expression<?>[] ALL_EXPRESSIONS_ARRAY = ALL_EXPRESSIONS.toArray(Expression[]::new);

    @NonNull
    private static List<Expression<?>> buildAllExpressions() {
        List<Expression<?>> selectExprs = new ArrayList<>();
        selectExprs.addAll(UNTERSUCHUNG_EXPRS);
        selectExprs.addAll(KIND_DATEN_EXPRS);
        selectExprs.addAll(DURCHFUEHRUNG_EXPRS);
        selectExprs.addAll(VORABKLAERUNGEN_EXPRS);
        selectExprs.addAll(TRIAGE_EXPRS);
        selectExprs.addAll(IMPFUNGEN_EXPRS);
        selectExprs.addAll(IMPFUNGEN_MASSNAHMEN_EXPRS);
        selectExprs.addAll(IMPFUNGEN_NUR_UNVOLLSTAENDIGE_EXPRS);
        selectExprs.addAll(SEHEN_EXPRS);
        selectExprs.addAll(HOEREN_AUDIOGRAMM_EXPRS);
        selectExprs.addAll(AUDIOGRAMM_RECHTS_EXPRS);
        selectExprs.addAll(AUDIOGRAMM_LINKS_EXPRS);
        selectExprs.addAll(HOEREN_OTOSKOPIE_EXPRS);
        selectExprs.addAll(GROESSE_GEWICHT_EXPRS);
        selectExprs.addAll(ARZT_BEURTEILUNG_EXPRS);
        selectExprs.addAll(SPRACHE_EXPRS);
        selectExprs.addAll(BEWEGUNGSAPPARAT_EXPRS);
        selectExprs.addAll(SCHULRELEVANTE_BEEINTRAECHTIGUNG_EXPRS);
        selectExprs.addAll(LAEUSEKONTROLLE_EXPRS);
        selectExprs.addAll(MASSNAHMEN_EXPRS);
        selectExprs.addAll(ABSCHLUSS_EXPRS);
        return List.copyOf(selectExprs);
    }

    @NonNull
    public static List<Expression<?>> getAllExpressions() {
        return ALL_EXPRESSIONS;
    }

    @NonNull
    public Stream<HelloDataUntersuchungenJson> streamAll() {
        return streamTuples().map(HelloDataUntersuchungenMapper::toDto);
    }

    @NonNull
    public Stream<Tuple> streamTuples() {
        QUntersuchung u = QUntersuchung.untersuchung;
        QUntersuchungKindDaten kd = QUntersuchungKindDaten.untersuchungKindDaten;
        QUntersuchungDurchfuehrung df = QUntersuchungDurchfuehrung.untersuchungDurchfuehrung;
        QUntersuchungVorabklaerungen vk = QUntersuchungVorabklaerungen.untersuchungVorabklaerungen;
        QUntersuchungTriage tr = QUntersuchungTriage.untersuchungTriage;
        QUntersuchungImpfungen impf = QUntersuchungImpfungen.untersuchungImpfungen;
        QUntersuchungImpfungenMassnahmen impfMass =
            QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen;
        QUntersuchungImpfungenNurUnvollstaendige impfNurUnv =
            QUntersuchungImpfungenNurUnvollstaendige.untersuchungImpfungenNurUnvollstaendige;
        QUntersuchungSehenBeurteilung sb = QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung;
        QUntersuchungHoerenAudiogramm ha = QUntersuchungHoerenAudiogramm.untersuchungHoerenAudiogramm;
        QUntersuchungHoerenOtoskopie ho = QUntersuchungHoerenOtoskopie.untersuchungHoerenOtoskopie;
        QUntersuchungGroesseGewicht gg = QUntersuchungGroesseGewicht.untersuchungGroesseGewicht;
        QUntersuchungArztBeurteilung ab = QUntersuchungArztBeurteilung.untersuchungArztBeurteilung;
        QUntersuchungSprache sp = QUntersuchungSprache.untersuchungSprache;
        QUntersuchungBewegungsapparat ba = QUntersuchungBewegungsapparat.untersuchungBewegungsapparat;
        QUntersuchungSchulrelevanteBeeintraechtigung srb =
            QUntersuchungSchulrelevanteBeeintraechtigung.untersuchungSchulrelevanteBeeintraechtigung;
        QUntersuchungLaeusekontrolle lk = QUntersuchungLaeusekontrolle.untersuchungLaeusekontrolle;
        QUntersuchungMassnahmen mass = QUntersuchungMassnahmen.untersuchungMassnahmen;
        QUntersuchungAbschluss aa = QUntersuchungAbschluss.untersuchungAbschluss;

        return db.select(ALL_EXPRESSIONS_ARRAY)
            .from(u)
            .leftJoin(u.untersuchungKindDaten, kd)
            .leftJoin(u.untersuchungDurchfuehrung, df)
            .leftJoin(u.untersuchungVorabklaerungen, vk)
            .leftJoin(u.untersuchungTriage, tr)
            .leftJoin(u.untersuchungImpfungen, impf)
            .leftJoin(impf.untersuchungImpfungenMassnahmen, impfMass)
            .leftJoin(impf.untersuchungImpfungenNurUnvollstaendige, impfNurUnv)
            .leftJoin(u.untersuchungSehenBeurteilung, sb)
            .leftJoin(u.untersuchungHoerenAudiogramm, ha)
            .leftJoin(ha.audiogrammRechts, AUDIO_RECHTS)
            .leftJoin(ha.audiogrammLinks, AUDIO_LINKS)
            .leftJoin(u.untersuchungHoerenOtoskopie, ho)
            .leftJoin(u.untersuchungGroesseGewicht, gg)
            .leftJoin(u.untersuchungArztBeurteilung, ab)
            .leftJoin(u.untersuchungSprache, sp)
            .leftJoin(u.untersuchungBewegungsapparat, ba)
            .leftJoin(u.untersuchungSchulrelevanteBeeintraechtigung, srb)
            .leftJoin(u.untersuchungLaeusekontrolle, lk)
            .leftJoin(u.untersuchungMassnahmen, mass)
            .leftJoin(u.untersuchungAbschluss, aa)
            .orderBy(u.id.asc())
            .setHint(HibernateHints.HINT_FETCH_SIZE, FETCH_SIZE)
            .stream();
    }
}
