package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.util.Objects;
import java.util.UUID;

import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.QUntersuchungAbschluss;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.types.AbschlussResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.QUntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einverstaendniserklaerung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.QUntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.VerkuerzungHamstringmuskulatur;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.QUntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.QUntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.QAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.QUntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.QUntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammSimpel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungenNurUnvollstaendige;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.QUntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.QUntersuchungLaeusekontrolle;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.types.LaeusekontrolleBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.QUntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.QUntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.ImpfungenMassnahmenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.QUntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Feinmotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GleichgewichtEinbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Grafomotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GrobmotorikEinbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.OhneStuetzradVelofahren;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.QUntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenAugeEinzelnResultat;
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
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.QUntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.QUntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.QUntersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.QUntersuchungVorabklaerungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.ChronischeKrankheiten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.EinverstaendnisImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.FragebogenStatus;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataAbschlussJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataArztJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataAudiogrammJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataBewegungsapparatJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataDurchfuehrungJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataGroesseGewichtJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataHoerenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataImpfungenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataImpfungenMassnahmenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataImpfungenNurUnvollstaendigeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataKindDatenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataLaeusekontrolleJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataMassnahmenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataSchulrelevanteBeeintraechtigungJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataSehenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataSpracheJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataTriageJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataUntersuchungenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataVorabklaerungenJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;

import com.querydsl.core.Tuple;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.AbstractValueEnhanced.extractEnumValue;
import static ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.AbstractValueEnhanced.extractEnumValues;
import static ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.AbstractValueEnhanced.extractNichtGeprueftGrund;

/**
 * Maps query tuples to HelloData investigation DTOs.
 * Each mapping method consumes expressions that must stay in sync with the corresponding
 * expression constants in {@link HelloDataUntersuchungenRepo}.
 */
public final class HelloDataUntersuchungenMapper implements BaseMapper {

    private HelloDataUntersuchungenMapper() {
    }

    /**
     * Maps the full investigation root DTO.
     * Consumes {@link HelloDataUntersuchungenRepo#UNTERSUCHUNG_EXPRS} and delegates to sub-entity mappers.
     */
    @NonNull
    public static HelloDataUntersuchungenJson toDto(@NonNull Tuple tuple) {
        QUntersuchung u = QUntersuchung.untersuchung;
        UUID uuid = tuple.get(u.id);
        Objects.requireNonNull(uuid, "root Untersuchung id must not be null");
        return new HelloDataUntersuchungenJson(
            uuid,
            mapKindDaten(tuple),
            mapDurchfuehrung(tuple),
            mapVorabklaerungen(tuple),
            mapTriage(tuple),
            mapImpfungen(tuple),
            mapSehen(tuple),
            mapHoeren(tuple),
            mapGroesseGewicht(tuple),
            mapArzt(tuple),
            mapSprache(tuple),
            mapBewegungsapparat(tuple),
            mapSchulrelevanteBeeintraechtigung(tuple),
            mapLaeusekontrolle(tuple),
            mapMassnahmen(tuple),
            mapAbschluss(tuple)
        );
    }

    /**
     * Maps KindDaten from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#KIND_DATEN_EXPRS}.
     */
    @Nullable
    public static HelloDataKindDatenJson mapKindDaten(@NonNull Tuple tuple) {
        QUntersuchungKindDaten kd = QUntersuchungKindDaten.untersuchungKindDaten;
        UUID uuid = tuple.get(kd.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        return new HelloDataKindDatenJson(
            uuid,
            tuple.get(kd.klassenstufe),
            tuple.get(kd.klassentyp),
            tuple.get(kd.klasse),
            tuple.get(kd.schulhaus),
            tuple.get(kd.schulstandort),
            tuple.get(kd.quartier),
            tuple.get(kd.schulkreis),
            tuple.get(kd.schulhausnummer),
            tuple.get(kd.nationalitaet),
            tuple.get(kd.geschlecht)
        );
    }

    /**
     * Maps Durchfuehrung from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#DURCHFUEHRUNG_EXPRS}.
     */
    @Nullable
    public static HelloDataDurchfuehrungJson mapDurchfuehrung(@NonNull Tuple tuple) {
        QUntersuchungDurchfuehrung df = QUntersuchungDurchfuehrung.untersuchungDurchfuehrung;
        UUID uuid = tuple.get(df.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        return new HelloDataDurchfuehrungJson(
            uuid,
            Boolean.TRUE.equals(tuple.get(df.nachkontrolle)),
            Boolean.TRUE.equals(tuple.get(df.nachzuegler)),
            Boolean.TRUE.equals(tuple.get(df.neueintritt))
        );
    }

    /**
     * Maps Vorabklaerungen from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#VORABKLAERUNGEN_EXPRS}.
     */
    @Nullable
    public static HelloDataVorabklaerungenJson mapVorabklaerungen(@NonNull Tuple tuple) {
        QUntersuchungVorabklaerungen vk = QUntersuchungVorabklaerungen.untersuchungVorabklaerungen;
        UUID uuid = tuple.get(vk.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        String fragebogenStatusStr = tuple.get(vk.fragebogenStatus.value);
        String einverstaendnisImpfungenStr = tuple.get(vk.einverstaendnisImpfungen.value);
        String chronischeKrankheitenStr = tuple.get(vk.chronischeKrankheiten.value);
        return new HelloDataVorabklaerungenJson(
            uuid,
            extractEnumValue(fragebogenStatusStr, FragebogenStatus.class),
            extractNichtGeprueftGrund(fragebogenStatusStr),
            extractEnumValue(einverstaendnisImpfungenStr, EinverstaendnisImpfungen.class),
            extractNichtGeprueftGrund(einverstaendnisImpfungenStr),
            extractEnumValue(chronischeKrankheitenStr, ChronischeKrankheiten.class),
            extractNichtGeprueftGrund(chronischeKrankheitenStr)
        );
    }

    /**
     * Maps Triage from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#TRIAGE_EXPRS}.
     */
    @Nullable
    public static HelloDataTriageJson mapTriage(@NonNull Tuple tuple) {
        QUntersuchungTriage tr = QUntersuchungTriage.untersuchungTriage;
        UUID uuid = tuple.get(tr.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        return new HelloDataTriageJson(
            uuid,
            Boolean.TRUE.equals(tuple.get(tr.triageSu)),
            Boolean.TRUE.equals(tuple.get(tr.triageMpa)),
            Boolean.TRUE.equals(tuple.get(tr.befundSu)),
            Boolean.TRUE.equals(tuple.get(tr.wunschSchueler)),
            Boolean.TRUE.equals(tuple.get(tr.vorinformationLehrperson))
        );
    }

    /**
     * Maps Impfungen from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#IMPFUNGEN_EXPRS}.
     */
    @Nullable
    public static HelloDataImpfungenJson mapImpfungen(@NonNull Tuple tuple) {
        QUntersuchungImpfungen impf = QUntersuchungImpfungen.untersuchungImpfungen;
        UUID uuid = tuple.get(impf.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        return new HelloDataImpfungenJson(
            uuid,
            Boolean.TRUE.equals(tuple.get(impf.impfausweisUnvollstaendig)),
            Boolean.TRUE.equals(tuple.get(impf.impfausweisNichtVorhanden)),
            Boolean.TRUE.equals(tuple.get(impf.impfausweisZweiPlusEinsSchema)),
            Boolean.TRUE.equals(tuple.get(impf.checkEinwilligungVorhanden)),
            Boolean.TRUE.equals(tuple.get(impf.checkKontraindikationenAbgeklaert)),
            Boolean.TRUE.equals(tuple.get(impf.checkDoppelkontrolleImpfentscheidDurchgefuehrt)),
            mapImpfungenMassnahmen(tuple),
            mapImpfungenNurUnvollstaendige(tuple)
        );
    }

    /**
     * Maps ImpfungenNurUnvollstaendige from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#IMPFUNGEN_NUR_UNVOLLSTAENDIGE_EXPRS}.
     */
    @Nullable
    public static HelloDataImpfungenNurUnvollstaendigeJson mapImpfungenNurUnvollstaendige(@NonNull Tuple tuple) {
        QUntersuchungImpfungenNurUnvollstaendige impfNurUnv =
            QUntersuchungImpfungenNurUnvollstaendige.untersuchungImpfungenNurUnvollstaendige;
        UUID uuid = tuple.get(impfNurUnv.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        return new HelloDataImpfungenNurUnvollstaendigeJson(
            uuid,
            tuple.get(impfNurUnv.impfausweisVollstaendig),
            tuple.get(impfNurUnv.impfausweisGrundUnvollstaendig)
        );
    }

    /**
     * Maps ImpfungenMassnahmen from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#IMPFUNGEN_MASSNAHMEN_EXPRS}.
     */
    @Nullable
    public static HelloDataImpfungenMassnahmenJson mapImpfungenMassnahmen(@NonNull Tuple tuple) {
        QUntersuchungImpfungenMassnahmen impfMass =
            QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen;
        UUID uuid = tuple.get(impfMass.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        return new HelloDataImpfungenMassnahmenJson(
            uuid,
            Boolean.TRUE.equals(tuple.get(impfMass.keineMassnahmen)),
            Boolean.TRUE.equals(tuple.get(impfMass.vormerkenFuerGruppenFolgetermin)),
            Boolean.TRUE.equals(tuple.get(impfMass.impfberatungMpaDurchgefuehrt)),
            Boolean.TRUE.equals(tuple.get(impfMass.infomaterialAbgegeben)),
            tuple.get(impfMass.informationEltern),
            Boolean.TRUE.equals(tuple.get(impfMass.informationHausarzt)),
            Boolean.TRUE.equals(tuple.get(impfMass.rueckfrageHausarzt)),
            EnumSetUtil.fromString(ImpfungenMassnahmenEmpfehlung.class, tuple.get(impfMass.empfehlung))
        );
    }

    /**
     * Maps Audiogramm from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#audiogrammExprs(QAudiogramm)}.
     */
    @Nullable
    public static HelloDataAudiogrammJson mapAudiogramm(@NonNull QAudiogramm qAudio, @NonNull Tuple tuple) {
        UUID uuid = tuple.get(qAudio.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        return new HelloDataAudiogrammJson(
            uuid,
            tuple.get(qAudio.dbHz250),
            tuple.get(qAudio.dbHz500),
            tuple.get(qAudio.dbHz1000),
            tuple.get(qAudio.dbHz2000),
            tuple.get(qAudio.dbHz4000),
            tuple.get(qAudio.dbHz6000),
            tuple.get(qAudio.dbHz8000)
        );
    }

    /**
     * Maps Sehen from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#SEHEN_EXPRS}.
     */
    @Nullable
    public static HelloDataSehenJson mapSehen(@NonNull Tuple tuple) {
        QUntersuchungSehenBeurteilung sb = QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung;
        UUID uuid = tuple.get(sb.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        String brilleLinsenStr = tuple.get(sb.brilleLinsen.value);
        String stereotestStr = tuple.get(sb.stereotest.value);
        String farbsehenStr = tuple.get(sb.farbsehen.value);
        String verwendeteTafelStr = tuple.get(sb.verwendeteTafel.value);
        String fernvisusStr = tuple.get(sb.fernvisus.value);
        String fernvisusAugeLinksStr = tuple.get(sb.fernvisusAugeLinks.value);
        String fernvisusAugeRechtsStr = tuple.get(sb.fernvisusAugeRechts.value);
        String nahvisusStr = tuple.get(sb.nahvisus.value);
        String motilitaetStr = tuple.get(sb.motilitaet.value);
        String strabismusStr = tuple.get(sb.strabismus.value);
        String strabismusAuspraegungStr = tuple.get(sb.strabismusAuspraegung.value);
        String strabismusAuftretenStr = tuple.get(sb.strabismusAuftreten.value);
        String inspektionStr = tuple.get(sb.inspektion.value);
        String konvergenzStr = tuple.get(sb.konvergenz.value);
        String empfehlungStr = tuple.get(sb.empfehlung.value);

        return new HelloDataSehenJson(
            uuid,
            extractEnumValue(brilleLinsenStr, BrilleLinsenResultat.class),
            extractNichtGeprueftGrund(brilleLinsenStr),
            extractEnumValue(stereotestStr, StereotestResultat.class),
            extractNichtGeprueftGrund(stereotestStr),
            extractEnumValue(farbsehenStr, FarbsehenResultat.class),
            extractNichtGeprueftGrund(farbsehenStr),
            extractEnumValue(verwendeteTafelStr, VerwendetTafelResultat.class),
            extractNichtGeprueftGrund(verwendeteTafelStr),
            extractEnumValues(fernvisusStr, DistanzSehenResultat.class),
            extractNichtGeprueftGrund(fernvisusStr),
            tuple.get(sb.fernvisusRechts),
            tuple.get(sb.fernvisusLinks),
            extractEnumValue(fernvisusAugeLinksStr, DistanzSehenAugeEinzelnResultat.class),
            extractNichtGeprueftGrund(fernvisusAugeLinksStr),
            extractEnumValue(fernvisusAugeRechtsStr, DistanzSehenAugeEinzelnResultat.class),
            extractNichtGeprueftGrund(fernvisusAugeRechtsStr),
            extractEnumValues(nahvisusStr, DistanzSehenResultat.class),
            extractNichtGeprueftGrund(nahvisusStr),
            tuple.get(sb.nahvisusRechts),
            tuple.get(sb.nahvisusLinks),
            extractEnumValue(motilitaetStr, MotilitaetResultat.class),
            extractNichtGeprueftGrund(motilitaetStr),
            extractEnumValues(strabismusStr, StrabismusResultat.class),
            extractNichtGeprueftGrund(strabismusStr),
            extractEnumValue(strabismusAuspraegungStr, StrabismusAuspraegungResultat.class),
            extractNichtGeprueftGrund(strabismusAuspraegungStr),
            extractEnumValue(strabismusAuftretenStr, StrabismusAuftretenResultat.class),
            extractNichtGeprueftGrund(strabismusAuftretenStr),
            extractEnumValue(inspektionStr, InspektionKonvergenzResultat.class),
            extractNichtGeprueftGrund(inspektionStr),
            extractEnumValue(konvergenzStr, InspektionKonvergenzResultat.class),
            extractNichtGeprueftGrund(konvergenzStr),
            extractEnumValues(empfehlungStr, SehenEmpfehlung.class),
            extractNichtGeprueftGrund(empfehlungStr)
        );
    }

    /**
     * Maps Hoeren from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#HOEREN_AUDIOGRAMM_EXPRS} and
     * {@link HelloDataUntersuchungenRepo#HOEREN_OTOSKOPIE_EXPRS}.
     */
    @Nullable
    public static HelloDataHoerenJson mapHoeren(@NonNull Tuple tuple) {
        QUntersuchungHoerenAudiogramm ha = QUntersuchungHoerenAudiogramm.untersuchungHoerenAudiogramm;
        QUntersuchungHoerenOtoskopie ho = QUntersuchungHoerenOtoskopie.untersuchungHoerenOtoskopie;

        UUID haId = tuple.get(ha.id);
        UUID hoId = tuple.get(ho.id);

        if (Objects.isNull(haId) && Objects.isNull(hoId)) {
            return null;
        }

        String audioSimpelStr = null;
        String hoerhilfeStr = null;
        String audioBeurteilungStr = null;
        String zusaetzlicheAngabenStr = null;
        String empfehlungStr = null;
        HelloDataAudiogrammJson audioRechts = null;
        HelloDataAudiogrammJson audioLinks = null;

        if (Objects.nonNull(haId)) {
            audioSimpelStr = tuple.get(ha.audiogrammSimpel.value);
            hoerhilfeStr = tuple.get(ha.hoerhilfe.value);
            audioBeurteilungStr = tuple.get(ha.audiogrammBeurteilung.value);
            zusaetzlicheAngabenStr = tuple.get(ha.zusaetzlicheAngaben.value);
            empfehlungStr = tuple.get(ha.empfehlung.value);
            audioRechts = mapAudiogramm(HelloDataUntersuchungenRepo.AUDIO_RECHTS, tuple);
            audioLinks = mapAudiogramm(HelloDataUntersuchungenRepo.AUDIO_LINKS, tuple);
        }

        String otoskopieRechtsStr = null;
        String otoskopieLinksStr = null;

        if (Objects.nonNull(hoId)) {
            otoskopieRechtsStr = tuple.get(ho.otoskopieRechts.value);
            otoskopieLinksStr = tuple.get(ho.otoskopieLinks.value);
        }

        return new HelloDataHoerenJson(
            haId,
            hoId,
            extractEnumValue(audioSimpelStr, AudiogrammSimpel.class),
            extractNichtGeprueftGrund(audioSimpelStr),
            extractEnumValue(hoerhilfeStr, HoerenHoerhilfe.class),
            extractNichtGeprueftGrund(hoerhilfeStr),
            extractEnumValues(audioBeurteilungStr, AudiogrammBeurteilung.class),
            extractNichtGeprueftGrund(audioBeurteilungStr),
            extractEnumValues(zusaetzlicheAngabenStr, HoerenZusaetzlicheAngaben.class),
            extractNichtGeprueftGrund(zusaetzlicheAngabenStr),
            extractEnumValues(empfehlungStr, HoerenEmpfehlung.class),
            extractNichtGeprueftGrund(empfehlungStr),
            extractEnumValues(otoskopieRechtsStr, OtoskopieResultat.class),
            extractNichtGeprueftGrund(otoskopieRechtsStr),
            extractEnumValues(otoskopieLinksStr, OtoskopieResultat.class),
            extractNichtGeprueftGrund(otoskopieLinksStr),
            audioRechts,
            audioLinks
        );
    }

    /**
     * Maps Groesse & Gewicht from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#GROESSE_GEWICHT_EXPRS}.
     */
    @Nullable
    public static HelloDataGroesseGewichtJson mapGroesseGewicht(@NonNull Tuple tuple) {
        QUntersuchungGroesseGewicht gg = QUntersuchungGroesseGewicht.untersuchungGroesseGewicht;
        UUID uuid = tuple.get(gg.id);
        if(Objects.isNull(uuid)) {
            return null;
        }

        String groessePerzSingleStr = tuple.get(gg.groessePerzentileSingleValue.value);
        String groessePerzMultiStr = tuple.get(gg.groessePerzentileMultiValue.value);
        String gewichtPerzSingleStr = tuple.get(gg.gewichtPerzentileSingleValue.value);
        String gewichtPerzMultiStr = tuple.get(gg.gewichtPerzentileMultiValue.value);
        String bmiPerzSingleStr = tuple.get(gg.bmiPerzentileSingleValue.value);
        String bmiPerzMultiStr = tuple.get(gg.bmiPerzentileMultiValue.value);

        return new HelloDataGroesseGewichtJson(
            uuid,
            tuple.get(gg.groesse),
            tuple.get(gg.gewicht),
            tuple.get(gg.bmi),
            extractEnumValue(groessePerzSingleStr, PerzentileGroesse.class),
            extractNichtGeprueftGrund(groessePerzSingleStr),
            extractEnumValues(groessePerzMultiStr, PerzentileGroesse.class),
            extractNichtGeprueftGrund(groessePerzMultiStr),
            extractEnumValue(gewichtPerzSingleStr, PerzentileGewicht.class),
            extractNichtGeprueftGrund(gewichtPerzSingleStr),
            extractEnumValues(gewichtPerzMultiStr, PerzentileGewicht.class),
            extractNichtGeprueftGrund(gewichtPerzMultiStr),
            extractEnumValue(bmiPerzSingleStr, PerzentileBmi.class),
            extractNichtGeprueftGrund(bmiPerzSingleStr),
            extractEnumValues(bmiPerzMultiStr, PerzentileBmi.class),
            extractNichtGeprueftGrund(bmiPerzMultiStr)
        );
    }

    /**
     * Maps Arzt Beurteilung from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#ARZT_BEURTEILUNG_EXPRS}.
     */
    @Nullable
    public static HelloDataArztJson mapArzt(@NonNull Tuple tuple) {
        QUntersuchungArztBeurteilung ab = QUntersuchungArztBeurteilung.untersuchungArztBeurteilung;
        UUID uuid = tuple.get(ab.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        return new HelloDataArztJson(
            uuid,
            EnumSetUtil.fromString(Einverstaendniserklaerung.class, tuple.get(ab.einverstaendniserklaerung)),
            tuple.get(ab.verhaltensfragebogen),
            tuple.get(ab.verhalten),
            tuple.get(ab.blutdruck),
            tuple.get(ab.blutdruckSystolisch),
            tuple.get(ab.blutdruckDiastolisch),
            tuple.get(ab.herzauskultation),
            tuple.get(ab.haut),
            EnumSetUtil.fromString(MundAndZaehne.class, tuple.get(ab.mundAndZaehne)),
            tuple.get(ab.sprachfaehigkeitSingleValue),
            EnumSetUtil.fromString(Sprachfaehigkeit.class, tuple.get(ab.sprachfaehigkeitMultiValue)),
            tuple.get(ab.sprachfaehigkeitPunkte),
            tuple.get(ab.motorik),
            tuple.get(ab.motorikPunkte),
            tuple.get(ab.kognition),
            tuple.get(ab.kognitionPunkte),
            EnumSetUtil.fromString(Bewegungsapparat.class, tuple.get(ab.bewegungsapparat)),
            tuple.get(ab.einbeinstand),
            tuple.get(ab.einbeinstandRechts),
            tuple.get(ab.einbeinstandLinks),
            tuple.get(ab.einbeinhuepfen),
            tuple.get(ab.einbeinhuepfenRechts),
            tuple.get(ab.einbeinhuepfenLinks),
            EnumSetUtil.fromString(Fingeropposition.class, tuple.get(ab.fingeropposition)),
            tuple.get(ab.hampelmann),
            tuple.get(ab.ballFangen),
            tuple.get(ab.ballWerfen),
            tuple.get(ab.nameSchreiben),
            tuple.get(ab.figurNachmalen),
            tuple.get(ab.stifthaltung),
            tuple.get(ab.aussprache),
            tuple.get(ab.lesefit),
            tuple.get(ab.lesefitPunkte),
            tuple.get(ab.zahlenNachsprechen4),
            tuple.get(ab.zahlenNachsprechen5),
            tuple.get(ab.zeichnenKreis),
            tuple.get(ab.zeichnenViereck),
            tuple.get(ab.zeichnenDreieck),
            tuple.get(ab.zeichnenMensch),
            tuple.get(ab.zeichnenMenschPunkte)
        );
    }

    /**
     * Maps Sprache from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#SPRACHE_EXPRS}.
     */
    @Nullable
    public static HelloDataSpracheJson mapSprache(@NonNull Tuple tuple) {
        QUntersuchungSprache sp = QUntersuchungSprache.untersuchungSprache;
        UUID uuid = tuple.get(sp.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        String nachsprechenStr = tuple.get(sp.nachsprechen.value);
        String wortschatzStr = tuple.get(sp.wortschatz.value);
        String sprachverstaendnisStr = tuple.get(sp.sprachverstaendnis.value);
        return new HelloDataSpracheJson(
            uuid,
            extractEnumValue(nachsprechenStr, Nachsprechen.class),
            extractNichtGeprueftGrund(nachsprechenStr),
            extractEnumValue(wortschatzStr, Wortschatz.class),
            extractNichtGeprueftGrund(wortschatzStr),
            extractEnumValue(sprachverstaendnisStr, Sprachverstaendnis.class),
            extractNichtGeprueftGrund(sprachverstaendnisStr)
        );
    }

    /**
     * Maps Bewegungsapparat from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#BEWEGUNGSAPPARAT_EXPRS}.
     */
    @Nullable
    public static HelloDataBewegungsapparatJson mapBewegungsapparat(@NonNull Tuple tuple) {
        QUntersuchungBewegungsapparat ba = QUntersuchungBewegungsapparat.untersuchungBewegungsapparat;
        UUID uuid = tuple.get(ba.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        String skolioseStr = tuple.get(ba.skoliose.value);
        String haltungsschwaecheStr = tuple.get(ba.haltungsschwaeche.value);
        String verkuerzungStr = tuple.get(ba.verkuerzungHamstringmuskulatur.value);
        return new HelloDataBewegungsapparatJson(
            uuid,
            extractEnumValue(skolioseStr, Skoliose.class),
            extractNichtGeprueftGrund(skolioseStr),
            extractEnumValue(haltungsschwaecheStr, Haltungsschwaeche.class),
            extractNichtGeprueftGrund(haltungsschwaecheStr),
            extractEnumValue(verkuerzungStr, VerkuerzungHamstringmuskulatur.class),
            extractNichtGeprueftGrund(verkuerzungStr)
        );
    }

    /**
     * Maps Schulrelevante Beeintraechtigung from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#SCHULRELEVANTE_BEEINTRAECHTIGUNG_EXPRS}.
     */
    @Nullable
    public static HelloDataSchulrelevanteBeeintraechtigungJson mapSchulrelevanteBeeintraechtigung(@NonNull Tuple tuple) {
        QUntersuchungSchulrelevanteBeeintraechtigung srb =
            QUntersuchungSchulrelevanteBeeintraechtigung.untersuchungSchulrelevanteBeeintraechtigung;
        UUID uuid = tuple.get(srb.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        String freiesGehenStr = tuple.get(srb.freiesGehen.value);
        String ohneStuetzradStr = tuple.get(srb.ohneStuetzradVelofahren.value);
        String grafomotorikStr = tuple.get(srb.grafomotorik.value);
        String feinmotorikStr = tuple.get(srb.feinmotorik.value);
        String grobmotorikStr = tuple.get(srb.grobmotorikEinbeinhuepfen.value);
        String gleichgewichtStr = tuple.get(srb.gleichgewichtEinbeinstand.value);

        return new HelloDataSchulrelevanteBeeintraechtigungJson(
            uuid,
            extractEnumValue(freiesGehenStr, FreiesGehen.class),
            extractNichtGeprueftGrund(freiesGehenStr),
            tuple.get(srb.anzahlMonateFreiesGehen),
            extractEnumValue(ohneStuetzradStr, OhneStuetzradVelofahren.class),
            extractNichtGeprueftGrund(ohneStuetzradStr),
            extractEnumValue(grafomotorikStr, Grafomotorik.class),
            extractNichtGeprueftGrund(grafomotorikStr),
            extractEnumValue(feinmotorikStr, Feinmotorik.class),
            extractNichtGeprueftGrund(feinmotorikStr),
            extractEnumValue(grobmotorikStr, GrobmotorikEinbeinhuepfen.class),
            extractNichtGeprueftGrund(grobmotorikStr),
            extractEnumValue(gleichgewichtStr, GleichgewichtEinbeinstand.class),
            extractNichtGeprueftGrund(gleichgewichtStr)
        );
    }

    /**
     * Maps Laeusekontrolle from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#LAEUSEKONTROLLE_EXPRS}.
     */
    @Nullable
    public static HelloDataLaeusekontrolleJson mapLaeusekontrolle(@NonNull Tuple tuple) {
        QUntersuchungLaeusekontrolle lk = QUntersuchungLaeusekontrolle.untersuchungLaeusekontrolle;
        UUID uuid = tuple.get(lk.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        return new HelloDataLaeusekontrolleJson(
            uuid,
            EnumSetUtil.fromString(LaeusekontrolleBeurteilung.class, tuple.get(lk.beurteilung))
        );
    }

    /**
     * Maps Massnahmen from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#MASSNAHMEN_EXPRS}.
     */
    @Nullable
    public static HelloDataMassnahmenJson mapMassnahmen(@NonNull Tuple tuple) {
        QUntersuchungMassnahmen mass = QUntersuchungMassnahmen.untersuchungMassnahmen;
        UUID uuid = tuple.get(mass.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        return new HelloDataMassnahmenJson(
            uuid,
            Boolean.TRUE.equals(tuple.get(mass.keineMassnahmen)),
            Boolean.TRUE.equals(tuple.get(mass.beratungMPA)),
            Boolean.TRUE.equals(tuple.get(mass.beratungSchulAerztin)),
            Boolean.TRUE.equals(tuple.get(mass.anderes)),
            Boolean.TRUE.equals(tuple.get(mass.bereitsBehandeltSehen)),
            Boolean.TRUE.equals(tuple.get(mass.bereitsBehandeltHoeren)),
            Boolean.TRUE.equals(tuple.get(mass.bereitsBehandeltGroesseGewicht)),
            Boolean.TRUE.equals(tuple.get(mass.bereitsBehandeltSprache)),
            Boolean.TRUE.equals(tuple.get(mass.bereitsBehandeltMotorik)),
            Boolean.TRUE.equals(tuple.get(mass.bereitsBehandeltAnderes))
        );
    }

    /**
     * Maps Abschluss from {@link Tuple}.
     * Must stay in sync with {@link HelloDataUntersuchungenRepo#ABSCHLUSS_EXPRS}.
     */
    @Nullable
    public static HelloDataAbschlussJson mapAbschluss(@NonNull Tuple tuple) {
        QUntersuchungAbschluss aa = QUntersuchungAbschluss.untersuchungAbschluss;
        UUID uuid = tuple.get(aa.id);
        if(Objects.isNull(uuid)) {
            return null;
        }
        String ueberweisungStr = tuple.get(aa.ueberweisungErfolgt.value);
        String aerztlValStr = tuple.get(aa.aerztlicheValidierungErforderlich.value);
        String beratungStr = tuple.get(aa.beratungDurchgefuehrt.value);
        return new HelloDataAbschlussJson(
            uuid,
            extractEnumValue(ueberweisungStr, AbschlussResultat.class),
            extractNichtGeprueftGrund(ueberweisungStr),
            extractEnumValue(aerztlValStr, AbschlussResultat.class),
            extractNichtGeprueftGrund(aerztlValStr),
            extractEnumValue(beratungStr, AbschlussResultat.class),
            extractNichtGeprueftGrund(beratungStr)
        );
    }
}
