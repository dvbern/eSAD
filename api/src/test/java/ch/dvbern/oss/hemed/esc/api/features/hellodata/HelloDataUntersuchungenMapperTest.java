package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.QUntersuchungAbschluss;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.types.AbschlussResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.QUntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.QUntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.QAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.QUntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.QUntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammSimpel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.QUntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.QUntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.ImpfungenMassnahmenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.QUntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Grafomotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.OhneStuetzradVelofahren;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.QUntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.QUntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.QUntersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.QUntersuchungVorabklaerungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.EinverstaendnisImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.FragebogenStatus;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataAbschlussJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataAudiogrammJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataBewegungsapparatJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataGroesseGewichtJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataHoerenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataImpfungenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataImpfungenMassnahmenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataKindDatenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataSchulrelevanteBeeintraechtigungJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataSehenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataSpracheJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataUntersuchungenJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataVorabklaerungenJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(ReplaceUnderscores.class)
class HelloDataUntersuchungenMapperTest {

    @Test
    void mapImpfungen_returns_null_when_impf_id_is_null() {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(QUntersuchungImpfungen.untersuchungImpfungen.id)).thenReturn(null);

        assertThat(HelloDataUntersuchungenMapper.mapImpfungen(tuple)).isNull();
    }

    @Test
    void mapImpfungen_maps_flags_and_measures() {
        Tuple tuple = mock(Tuple.class);
        QUntersuchungImpfungen impf = QUntersuchungImpfungen.untersuchungImpfungen;
        QUntersuchungImpfungenMassnahmen impfMass =
            QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen;

        UUID impfId = UUID.randomUUID();
        UUID massId = UUID.randomUUID();

        when(tuple.get(impf.id)).thenReturn(impfId);
        when(tuple.get(impf.impfausweisUnvollstaendig)).thenReturn(true);
        when(tuple.get(impf.impfausweisNichtVorhanden)).thenReturn(false);
        when(tuple.get(impf.impfausweisZweiPlusEinsSchema)).thenReturn(true);
        when(tuple.get(impf.checkEinwilligungVorhanden)).thenReturn(true);
        when(tuple.get(impf.checkKontraindikationenAbgeklaert)).thenReturn(false);
        when(tuple.get(impf.checkDoppelkontrolleImpfentscheidDurchgefuehrt)).thenReturn(true);

        when(tuple.get(impfMass.id)).thenReturn(massId);
        when(tuple.get(impfMass.keineMassnahmen)).thenReturn(false);
        when(tuple.get(impfMass.vormerkenFuerGruppenFolgetermin)).thenReturn(true);
        when(tuple.get(impfMass.impfberatungMpaDurchgefuehrt)).thenReturn(true);
        when(tuple.get(impfMass.infomaterialAbgegeben)).thenReturn(true);
        when(tuple.get(impfMass.informationEltern)).thenReturn(InformationEltern.BRIEF);
        when(tuple.get(impfMass.informationHausarzt)).thenReturn(false);
        when(tuple.get(impfMass.rueckfrageHausarzt)).thenReturn(true);
        when(tuple.get(impfMass.empfehlung)).thenReturn("IMPFUNG_BEIM_HAUSARZT");

        HelloDataImpfungenJson result = HelloDataUntersuchungenMapper.mapImpfungen(tuple);

        assertThat(result).isNotNull()
            .extracting(HelloDataImpfungenJson::getId)
            .isEqualTo(impfId);
        assertThat(result.isImpfausweisUnvollstaendig()).isTrue();
        assertThat(result.isImpfausweisNichtVorhanden()).isFalse();
        assertThat(result.isImpfausweisZweiPlusEinsSchema()).isTrue();
        assertThat(result.isCheckEinwilligungVorhanden()).isTrue();
        assertThat(result.isCheckKontraindikationenAbgeklaert()).isFalse();
        assertThat(result.isCheckDoppelkontrolleImpfentscheidDurchgefuehrt()).isTrue();

        HelloDataImpfungenMassnahmenJson massnahmenDto = result.getMassnahmen();

        assertThat(massnahmenDto).isNotNull()
            .extracting(HelloDataImpfungenMassnahmenJson::getId)
            .isEqualTo(massId);
        assertThat(massnahmenDto.isKeineMassnahmen()).isFalse();
        assertThat(massnahmenDto.isVormerkenFuerGruppenFolgetermin()).isTrue();
        assertThat(massnahmenDto.isImpfberatungMpaDurchgefuehrt()).isTrue();
        assertThat(massnahmenDto.isInfomaterialAbgegeben()).isTrue();
        assertThat(massnahmenDto.getInformationEltern()).isEqualTo(InformationEltern.BRIEF);
        assertThat(massnahmenDto.isInformationHausarzt()).isFalse();
        assertThat(massnahmenDto.isRueckfrageHausarzt()).isTrue();
        assertThat(massnahmenDto.getEmpfehlung()).containsExactly(ImpfungenMassnahmenEmpfehlung.IMPFUNG_BEIM_HAUSARZT);
    }

    @Test
    void mapHoeren_maps_nested_audiogramm_links_and_rechts() {
        Tuple tuple = mock(Tuple.class);
        QUntersuchungHoerenAudiogramm ha = QUntersuchungHoerenAudiogramm.untersuchungHoerenAudiogramm;
        QAudiogramm audioR = HelloDataUntersuchungenRepo.AUDIO_RECHTS;
        QAudiogramm audioL = HelloDataUntersuchungenRepo.AUDIO_LINKS;

        UUID audioRId = UUID.randomUUID();
        UUID audioLId = UUID.randomUUID();

        when(tuple.get(ha.id)).thenReturn(UUID.randomUUID());
        when(tuple.get(audioR.id)).thenReturn(audioRId);
        when(tuple.get(audioR.dbHz250)).thenReturn(Dezibel.DB10);
        when(tuple.get(audioR.dbHz500)).thenReturn(Dezibel.DB20);
        when(tuple.get(audioR.dbHz1000)).thenReturn(Dezibel.DB20);
        when(tuple.get(audioR.dbHz2000)).thenReturn(Dezibel.DB30);
        when(tuple.get(audioR.dbHz4000)).thenReturn(Dezibel.DB30);
        when(tuple.get(audioR.dbHz6000)).thenReturn(Dezibel.DB40);
        when(tuple.get(audioR.dbHz8000)).thenReturn(Dezibel.DB40);

        when(tuple.get(audioL.id)).thenReturn(audioLId);
        when(tuple.get(audioL.dbHz250)).thenReturn(Dezibel.DB20);
        when(tuple.get(audioL.dbHz500)).thenReturn(Dezibel.DB20);
        when(tuple.get(audioL.dbHz1000)).thenReturn(Dezibel.DB30);
        when(tuple.get(audioL.dbHz2000)).thenReturn(Dezibel.DB30);
        when(tuple.get(audioL.dbHz4000)).thenReturn(Dezibel.DB40);
        when(tuple.get(audioL.dbHz6000)).thenReturn(Dezibel.DB40);
        when(tuple.get(audioL.dbHz8000)).thenReturn(Dezibel.DB50);

        HelloDataHoerenJson result = HelloDataUntersuchungenMapper.mapHoeren(tuple);

        assertThat(result).isNotNull();
        HelloDataAudiogrammJson mappedR = result.getAudiogrammRechts();
        assertThat(mappedR).isNotNull();
        assertThat(mappedR.getId()).isEqualTo(audioRId);
        assertThat(mappedR.getDbHz250()).isEqualTo(Dezibel.DB10);
        assertThat(mappedR.getDbHz500()).isEqualTo(Dezibel.DB20);
        assertThat(mappedR.getDbHz1000()).isEqualTo(Dezibel.DB20);
        assertThat(mappedR.getDbHz2000()).isEqualTo(Dezibel.DB30);
        assertThat(mappedR.getDbHz4000()).isEqualTo(Dezibel.DB30);
        assertThat(mappedR.getDbHz6000()).isEqualTo(Dezibel.DB40);
        assertThat(mappedR.getDbHz8000()).isEqualTo(Dezibel.DB40);

        HelloDataAudiogrammJson mappedL = result.getAudiogrammLinks();
        assertThat(mappedL).isNotNull();
        assertThat(mappedL.getId()).isEqualTo(audioLId);
        assertThat(mappedL.getDbHz250()).isEqualTo(Dezibel.DB20);
        assertThat(mappedL.getDbHz500()).isEqualTo(Dezibel.DB20);
        assertThat(mappedL.getDbHz1000()).isEqualTo(Dezibel.DB30);
        assertThat(mappedL.getDbHz2000()).isEqualTo(Dezibel.DB30);
        assertThat(mappedL.getDbHz4000()).isEqualTo(Dezibel.DB40);
        assertThat(mappedL.getDbHz6000()).isEqualTo(Dezibel.DB40);
        assertThat(mappedL.getDbHz8000()).isEqualTo(Dezibel.DB50);
    }

    @Test
    void toDto_with_single_tuple_maps_dto() {
        Tuple tuple = mock(Tuple.class);
        UUID uId = UUID.randomUUID();
        QUntersuchungImpfungen impf = QUntersuchungImpfungen.untersuchungImpfungen;

        when(tuple.get(QUntersuchung.untersuchung.id)).thenReturn(uId);
        when(tuple.get(impf.id)).thenReturn(UUID.randomUUID());
        when(tuple.get(impf.impfausweisUnvollstaendig)).thenReturn(false);

        HelloDataUntersuchungenJson result = HelloDataUntersuchungenMapper.toDto(tuple);

        assertThat(result.getId()).isEqualTo(uId);
        assertThat(result.getImpfungen()).isNotNull();
        assertThat(result.getImpfungen().isImpfausweisUnvollstaendig()).isFalse();
    }

    @Test
    void mapVorabklaerungen_maps_values_and_nicht_geprueft_grund() {
        Tuple tuple = mock(Tuple.class);
        QUntersuchungVorabklaerungen vk = QUntersuchungVorabklaerungen.untersuchungVorabklaerungen;

        UUID vkId = UUID.randomUUID();
        when(tuple.get(vk.id)).thenReturn(vkId);
        when(tuple.get(vk.fragebogenStatus.value)).thenReturn("$1$ERHALTEN_VOLLSTAENDIG$1$$2$$2$");
        when(tuple.get(vk.einverstaendnisImpfungen.value)).thenReturn(
            "$1$NICHT_ERHALTEN$1$$2$UNTERSUCHUNG_NICHT_INDIZIERT$2$");
        when(tuple.get(vk.chronischeKrankheiten.value)).thenReturn(null);

        HelloDataVorabklaerungenJson result = HelloDataUntersuchungenMapper.mapVorabklaerungen(tuple);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(vkId);
        assertThat(result.getFragebogenStatus()).isEqualTo(FragebogenStatus.ERHALTEN_VOLLSTAENDIG);
        assertThat(result.getFragebogenStatusNichtGeprueftGrund()).isNull();
        assertThat(result.getEinverstaendnisImpfungen()).isEqualTo(EinverstaendnisImpfungen.NICHT_ERHALTEN);
        assertThat(result.getEinverstaendnisImpfungenNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.UNTERSUCHUNG_NICHT_INDIZIERT);
        assertThat(result.getChronischeKrankheiten()).isNull();
        assertThat(result.getChronischeKrankheitenNichtGeprueftGrund()).isNull();
    }

    @Test
    void mapSehen_maps_single_and_multi_value_enhanced_fields() {
        Tuple tuple = mock(Tuple.class);
        QUntersuchungSehenBeurteilung sb = QUntersuchungSehenBeurteilung.untersuchungSehenBeurteilung;

        UUID sbId = UUID.randomUUID();
        when(tuple.get(sb.id)).thenReturn(sbId);
        when(tuple.get(sb.brilleLinsen.value)).thenReturn("$1$BRILLE$1$$2$$2$");
        when(tuple.get(sb.stereotest.value)).thenReturn("$1$NICHT_GEPRUEFT$1$$2$VERWEIGERTE_UNTERSUCHUNG$2$");
        when(tuple.get(sb.fernvisus.value)).thenReturn("$1$NORMAL,RECHTS_REDUZIERT$1$$2$$2$");
        when(tuple.get(sb.fernvisusRechts)).thenReturn(BigDecimal.valueOf(0.8));
        when(tuple.get(sb.fernvisusLinks)).thenReturn(BigDecimal.valueOf(1.0));
        when(tuple.get(sb.empfehlung.value)).thenReturn("$1$UEBERPRUEFUNG_AUGENARZT,UEBERPRUEFUNG_BRILLE$1$$2$$2$");

        HelloDataSehenJson result = HelloDataUntersuchungenMapper.mapSehen(tuple);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sbId);
        assertThat(result.getBrilleLinsen()).isEqualTo(BrilleLinsenResultat.BRILLE);
        assertThat(result.getBrilleLinsenNichtGeprueftGrund()).isNull();
        assertThat(result.getStereotest()).isEqualTo(StereotestResultat.NICHT_GEPRUEFT);
        assertThat(result.getStereotestNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.VERWEIGERTE_UNTERSUCHUNG);
        assertThat(result.getFernvisus()).containsExactlyInAnyOrder(
            DistanzSehenResultat.NORMAL,
            DistanzSehenResultat.RECHTS_REDUZIERT);
        assertThat(result.getFernvisusNichtGeprueftGrund()).isNull();
        assertThat(result.getFernvisusRechts()).isEqualTo(BigDecimal.valueOf(0.8));
        assertThat(result.getFernvisusLinks()).isEqualTo(BigDecimal.valueOf(1.0));
        assertThat(result.getEmpfehlung()).containsExactlyInAnyOrder(
            SehenEmpfehlung.UEBERPRUEFUNG_AUGENARZT,
            SehenEmpfehlung.UEBERPRUEFUNG_BRILLE);
    }

    @Test
    void mapAbschluss_maps_values_and_nicht_geprueft_grund() {
        Tuple tuple = mock(Tuple.class);
        QUntersuchungAbschluss aa = QUntersuchungAbschluss.untersuchungAbschluss;

        UUID aaId = UUID.randomUUID();
        when(tuple.get(aa.id)).thenReturn(aaId);
        when(tuple.get(aa.ueberweisungErfolgt.value)).thenReturn("$1$JA$1$$2$$2$");
        when(tuple.get(aa.aerztlicheValidierungErforderlich.value)).thenReturn("$1$NEIN$1$$2$$2$");
        when(tuple.get(aa.beratungDurchgefuehrt.value)).thenReturn(
            "$1$KEINE_ANGABE$1$$2$UNTERSUCHUNG_NICHT_INDIZIERT$2$");

        HelloDataAbschlussJson result = HelloDataUntersuchungenMapper.mapAbschluss(tuple);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(aaId);
        assertThat(result.getUeberweisungErfolgt()).isEqualTo(AbschlussResultat.JA);
        assertThat(result.getUeberweisungErfolgtNichtGeprueftGrund()).isNull();
        assertThat(result.getAerztlicheValidierungErforderlich()).isEqualTo(AbschlussResultat.NEIN);
        assertThat(result.getAerztlicheValidierungErforderlichNichtGeprueftGrund()).isNull();
        assertThat(result.getBeratungDurchgefuehrt()).isEqualTo(AbschlussResultat.KEINE_ANGABE);
        assertThat(result.getBeratungDurchgefuehrtNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.UNTERSUCHUNG_NICHT_INDIZIERT);
    }

    @Test
    void mapAbschluss_returns_null_when_id_is_null() {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(QUntersuchungAbschluss.untersuchungAbschluss.id)).thenReturn(null);

        assertThat(HelloDataUntersuchungenMapper.mapAbschluss(tuple)).isNull();
    }

    @Test
    void mapGroesseGewicht_returns_null_when_id_is_null() {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(QUntersuchungGroesseGewicht.untersuchungGroesseGewicht.id)).thenReturn(null);

        assertThat(HelloDataUntersuchungenMapper.mapGroesseGewicht(tuple)).isNull();
    }

    @Test
    void mapGroesseGewicht_maps_perzentile_values_and_nicht_geprueft_grund() {
        Tuple tuple = mock(Tuple.class);
        QUntersuchungGroesseGewicht gg = QUntersuchungGroesseGewicht.untersuchungGroesseGewicht;

        UUID ggId = UUID.randomUUID();
        when(tuple.get(gg.id)).thenReturn(ggId);
        when(tuple.get(gg.groesse)).thenReturn(140);
        when(tuple.get(gg.gewicht)).thenReturn(BigDecimal.valueOf(35.5));
        when(tuple.get(gg.bmi)).thenReturn(BigDecimal.valueOf(18.1));
        when(tuple.get(gg.groessePerzentileSingleValue.value)).thenReturn("$1$NORMAL$1$$2$$2$");
        when(tuple.get(gg.groessePerzentileMultiValue.value)).thenReturn("$1$NORMAL,PERZENTILE_HOCH$1$$2$$2$");
        when(tuple.get(gg.gewichtPerzentileSingleValue.value)).thenReturn("$1$NICHT_GEPRUEFT$1$$2$KRANK$2$");
        when(tuple.get(gg.gewichtPerzentileMultiValue.value)).thenReturn("$1$$1$$2$UNTERSUCHUNG_NICHT_INDIZIERT$2$");
        when(tuple.get(gg.bmiPerzentileSingleValue.value)).thenReturn(null);
        when(tuple.get(gg.bmiPerzentileMultiValue.value)).thenReturn("");

        HelloDataGroesseGewichtJson result = HelloDataUntersuchungenMapper.mapGroesseGewicht(tuple);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ggId);
        assertThat(result.getGroesse()).isEqualTo(140);
        assertThat(result.getGewicht()).isEqualTo(BigDecimal.valueOf(35.5));
        assertThat(result.getBmi()).isEqualTo(BigDecimal.valueOf(18.1));
        assertThat(result.getGroessePerzentileSingleValue()).isEqualTo(PerzentileGroesse.NORMAL);
        assertThat(result.getGroessePerzentileSingleValueNichtGeprueftGrund()).isNull();
        assertThat(result.getGroessePerzentileMultiValue()).containsExactlyInAnyOrder(
            PerzentileGroesse.NORMAL,
            PerzentileGroesse.PERZENTILE_HOCH
        );
        assertThat(result.getGroessePerzentileMultiValueNichtGeprueftGrund()).isNull();
        assertThat(result.getGewichtPerzentileSingleValue()).isEqualTo(PerzentileGewicht.NICHT_GEPRUEFT);
        assertThat(result.getGewichtPerzentileSingleValueNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.KRANK);
        assertThat(result.getGewichtPerzentileMultiValue()).isEmpty();
        assertThat(result.getGewichtPerzentileMultiValueNichtGeprueftGrund())
            .isEqualTo(NichtGeprueftGrund.UNTERSUCHUNG_NICHT_INDIZIERT);
        assertThat(result.getBmiPerzentileSingleValue()).isNull();
        assertThat(result.getBmiPerzentileSingleValueNichtGeprueftGrund()).isNull();
        assertThat(result.getBmiPerzentileMultiValue()).isEmpty();
        assertThat(result.getBmiPerzentileMultiValueNichtGeprueftGrund()).isNull();
    }

    @Test
    void mapHoeren_returns_null_when_both_audiogramm_and_otoskopie_are_null() {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(QUntersuchungHoerenAudiogramm.untersuchungHoerenAudiogramm.id)).thenReturn(null);
        when(tuple.get(QUntersuchungHoerenOtoskopie.untersuchungHoerenOtoskopie.id)).thenReturn(null);

        assertThat(HelloDataUntersuchungenMapper.mapHoeren(tuple)).isNull();
    }

    @Test
    void mapHoeren_maps_otoskopie_and_audiogramm_value_enhanced_fields() {
        Tuple tuple = mock(Tuple.class);
        QUntersuchungHoerenAudiogramm ha = QUntersuchungHoerenAudiogramm.untersuchungHoerenAudiogramm;
        QUntersuchungHoerenOtoskopie ho = QUntersuchungHoerenOtoskopie.untersuchungHoerenOtoskopie;

        UUID haId = UUID.randomUUID();
        UUID hoId = UUID.randomUUID();
        when(tuple.get(ha.id)).thenReturn(haId);
        when(tuple.get(ha.audiogrammSimpel.value)).thenReturn("$1$DB_TIEF$1$$2$$2$");
        when(tuple.get(ha.hoerhilfe.value)).thenReturn("$1$NICHT_GEPRUEFT$1$$2$KRANK$2$");
        when(tuple.get(ha.audiogrammBeurteilung.value)).thenReturn("$1$NORMAL,RECHTS_REDUZIERT$1$$2$$2$");
        when(tuple.get(ha.zusaetzlicheAngaben.value)).thenReturn("$1$$1$$2$UNTERSUCHUNG_NICHT_INDIZIERT$2$");
        when(tuple.get(ha.empfehlung.value)).thenReturn("$1$UEBERPRUEFUNG_ARZT$1$$2$$2$");

        when(tuple.get(ho.id)).thenReturn(hoId);
        when(tuple.get(ho.otoskopieRechts.value)).thenReturn("$1$NORMAL,TROMMELFELL_CERUMEN$1$$2$$2$");
        when(tuple.get(ho.otoskopieLinks.value)).thenReturn("$1$$1$$2$VERWEIGERTE_UNTERSUCHUNG$2$");

        HelloDataHoerenJson result = HelloDataUntersuchungenMapper.mapHoeren(tuple);

        assertThat(result).isNotNull();
        assertThat(result.getAudiogrammId()).isEqualTo(haId);
        assertThat(result.getOtoskopieId()).isEqualTo(hoId);
        assertThat(result.getAudiogrammSimpel()).isEqualTo(AudiogrammSimpel.DB_TIEF);
        assertThat(result.getAudiogrammSimpelNichtGeprueftGrund()).isNull();
        assertThat(result.getHoerhilfe()).isEqualTo(HoerenHoerhilfe.NICHT_GEPRUEFT);
        assertThat(result.getHoerhilfeNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.KRANK);
        assertThat(result.getAudiogrammBeurteilung()).containsExactlyInAnyOrder(
            AudiogrammBeurteilung.NORMAL,
            AudiogrammBeurteilung.RECHTS_REDUZIERT
        );
        assertThat(result.getAudiogrammBeurteilungNichtGeprueftGrund()).isNull();
        assertThat(result.getZusaetzlicheAngaben()).isEmpty();
        assertThat(result.getZusaetzlicheAngabenNichtGeprueftGrund())
            .isEqualTo(NichtGeprueftGrund.UNTERSUCHUNG_NICHT_INDIZIERT);
        assertThat(result.getEmpfehlung()).containsExactly(HoerenEmpfehlung.UEBERPRUEFUNG_ARZT);
        assertThat(result.getEmpfehlungNichtGeprueftGrund()).isNull();
        assertThat(result.getOtoskopieRechts()).containsExactlyInAnyOrder(
            OtoskopieResultat.NORMAL,
            OtoskopieResultat.TROMMELFELL_CERUMEN
        );
        assertThat(result.getOtoskopieRechtsNichtGeprueftGrund()).isNull();
        assertThat(result.getOtoskopieLinks()).isEmpty();
        assertThat(result.getOtoskopieLinksNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.VERWEIGERTE_UNTERSUCHUNG);
    }

    @Test
    void mapSprache_returns_null_when_id_is_null() {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(QUntersuchungSprache.untersuchungSprache.id)).thenReturn(null);

        assertThat(HelloDataUntersuchungenMapper.mapSprache(tuple)).isNull();
    }

    @Test
    void mapSprache_maps_single_value_enhanced_fields() {
        Tuple tuple = mock(Tuple.class);
        QUntersuchungSprache sp = QUntersuchungSprache.untersuchungSprache;

        UUID spId = UUID.randomUUID();
        when(tuple.get(sp.id)).thenReturn(spId);
        when(tuple.get(sp.nachsprechen.value)).thenReturn("$1$BESTANDEN$1$$2$$2$");
        when(tuple.get(sp.wortschatz.value)).thenReturn("$1$NICHT_GEPRUEFT$1$$2$KRANK$2$");
        when(tuple.get(sp.sprachverstaendnis.value)).thenReturn("$1$$1$$2$$2$");

        HelloDataSpracheJson result = HelloDataUntersuchungenMapper.mapSprache(tuple);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(spId);
        assertThat(result.getNachsprechen()).isEqualTo(Nachsprechen.BESTANDEN);
        assertThat(result.getNachsprechenNichtGeprueftGrund()).isNull();
        assertThat(result.getWortschatz()).isEqualTo(Wortschatz.NICHT_GEPRUEFT);
        assertThat(result.getWortschatzNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.KRANK);
        assertThat(result.getSprachverstaendnis()).isNull();
        assertThat(result.getSprachverstaendnisNichtGeprueftGrund()).isNull();
    }

    @Test
    void mapBewegungsapparat_returns_null_when_id_is_null() {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(QUntersuchungBewegungsapparat.untersuchungBewegungsapparat.id)).thenReturn(null);

        assertThat(HelloDataUntersuchungenMapper.mapBewegungsapparat(tuple)).isNull();
    }

    @Test
    void mapBewegungsapparat_maps_single_value_enhanced_fields() {
        Tuple tuple = mock(Tuple.class);
        QUntersuchungBewegungsapparat ba = QUntersuchungBewegungsapparat.untersuchungBewegungsapparat;

        UUID baId = UUID.randomUUID();
        when(tuple.get(ba.id)).thenReturn(baId);
        when(tuple.get(ba.skoliose.value)).thenReturn("$1$SKOLIOSE_NEIN$1$$2$$2$");
        when(tuple.get(ba.haltungsschwaeche.value)).thenReturn("$1$$1$$2$UNTERSUCHUNG_NICHT_INDIZIERT$2$");
        when(tuple.get(ba.verkuerzungHamstringmuskulatur.value)).thenReturn(null);

        HelloDataBewegungsapparatJson result = HelloDataUntersuchungenMapper.mapBewegungsapparat(tuple);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(baId);
        assertThat(result.getSkoliose()).isEqualTo(Skoliose.SKOLIOSE_NEIN);
        assertThat(result.getSkolioseNichtGeprueftGrund()).isNull();
        assertThat(result.getHaltungsschwaeche()).isNull();
        assertThat(result.getHaltungsschwaecheNichtGeprueftGrund())
            .isEqualTo(NichtGeprueftGrund.UNTERSUCHUNG_NICHT_INDIZIERT);
        assertThat(result.getVerkuerzungHamstringmuskulatur()).isNull();
        assertThat(result.getVerkuerzungHamstringmuskulaturNichtGeprueftGrund()).isNull();
    }

    @Test
    void mapKindDaten_returns_null_when_id_is_null() {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(QUntersuchungKindDaten.untersuchungKindDaten.id)).thenReturn(null);

        assertThat(HelloDataUntersuchungenMapper.mapKindDaten(tuple)).isNull();
    }

    @Test
    void mapKindDaten_maps_all_fields_including_geschlecht() {
        Tuple tuple = mock(Tuple.class);
        QUntersuchungKindDaten kd = QUntersuchungKindDaten.untersuchungKindDaten;

        UUID kdId = UUID.randomUUID();
        when(tuple.get(kd.id)).thenReturn(kdId);
        when(tuple.get(kd.klassenstufe)).thenReturn(Klassenstufe.SEK_I_KLASSE_1);
        when(tuple.get(kd.klassentyp)).thenReturn("Sekundarschule");
        when(tuple.get(kd.klasse)).thenReturn("7a");
        when(tuple.get(kd.schulhaus)).thenReturn("Schulhaus West");
        when(tuple.get(kd.schulstandort)).thenReturn("Standort 1");
        when(tuple.get(kd.quartier)).thenReturn("Quartier A");
        when(tuple.get(kd.schulkreis)).thenReturn("Kreis Mitte");
        when(tuple.get(kd.schulhausnummer)).thenReturn("42");
        when(tuple.get(kd.nationalitaet)).thenReturn(Land.SCHWEIZ);
        when(tuple.get(kd.geschlecht)).thenReturn(Geschlecht.WEIBLICH);

        HelloDataKindDatenJson result = HelloDataUntersuchungenMapper.mapKindDaten(tuple);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(kdId);
        assertThat(result.getKlassenstufe()).isEqualTo(Klassenstufe.SEK_I_KLASSE_1);
        assertThat(result.getKlassentyp()).isEqualTo("Sekundarschule");
        assertThat(result.getKlasse()).isEqualTo("7a");
        assertThat(result.getSchulhaus()).isEqualTo("Schulhaus West");
        assertThat(result.getSchulstandort()).isEqualTo("Standort 1");
        assertThat(result.getQuartier()).isEqualTo("Quartier A");
        assertThat(result.getSchulkreis()).isEqualTo("Kreis Mitte");
        assertThat(result.getSchulhausnummer()).isEqualTo("42");
        assertThat(result.getNationalitaet()).isEqualTo(Land.SCHWEIZ);
        assertThat(result.getGeschlecht()).isEqualTo(Geschlecht.WEIBLICH);
    }

    @Test
    void mapSchulrelevanteBeeintraechtigung_returns_null_when_id_is_null() {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(QUntersuchungSchulrelevanteBeeintraechtigung.untersuchungSchulrelevanteBeeintraechtigung.id))
            .thenReturn(null);

        assertThat(HelloDataUntersuchungenMapper.mapSchulrelevanteBeeintraechtigung(tuple)).isNull();
    }

    @Test
    void mapSchulrelevanteBeeintraechtigung_maps_fields_and_nicht_geprueft_grund() {
        Tuple tuple = mock(Tuple.class);
        QUntersuchungSchulrelevanteBeeintraechtigung srb =
            QUntersuchungSchulrelevanteBeeintraechtigung.untersuchungSchulrelevanteBeeintraechtigung;

        UUID srbId = UUID.randomUUID();
        when(tuple.get(srb.id)).thenReturn(srbId);
        when(tuple.get(srb.freiesGehen.value)).thenReturn("$1$NICHT_GEPRUEFT$1$$2$$2$");
        when(tuple.get(srb.anzahlMonateFreiesGehen)).thenReturn(14);
        when(tuple.get(srb.ohneStuetzradVelofahren.value)).thenReturn(
            "$1$NICHT_GEPRUEFT$1$$2$VERWEIGERTE_UNTERSUCHUNG$2$");
        when(tuple.get(srb.grafomotorik.value)).thenReturn("$1$GUT$1$$2$$2$");
        when(tuple.get(srb.feinmotorik.value)).thenReturn("$1$$1$$2$KRANK$2$");
        when(tuple.get(srb.grobmotorikEinbeinhuepfen.value)).thenReturn("$1$$1$$2$$2$");
        when(tuple.get(srb.gleichgewichtEinbeinstand.value)).thenReturn(null);

        HelloDataSchulrelevanteBeeintraechtigungJson result =
            HelloDataUntersuchungenMapper.mapSchulrelevanteBeeintraechtigung(tuple);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(srbId);
        assertThat(result.getFreiesGehen()).isEqualTo(FreiesGehen.NICHT_GEPRUEFT);
        assertThat(result.getFreiesGehenNichtGeprueftGrund()).isNull();
        assertThat(result.getAnzahlMonateFreiesGehen()).isEqualTo(14);
        assertThat(result.getOhneStuetzradVelofahren()).isEqualTo(OhneStuetzradVelofahren.NICHT_GEPRUEFT);
        assertThat(result.getOhneStuetzradVelofahrenNichtGeprueftGrund())
            .isEqualTo(NichtGeprueftGrund.VERWEIGERTE_UNTERSUCHUNG);
        assertThat(result.getGrafomotorik()).isEqualTo(Grafomotorik.GUT);
        assertThat(result.getGrafomotorikNichtGeprueftGrund()).isNull();
        assertThat(result.getFeinmotorik()).isNull();
        assertThat(result.getFeinmotorikNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.KRANK);
        assertThat(result.getGrobmotorikEinbeinhuepfen()).isNull();
        assertThat(result.getGrobmotorikEinbeinhuepfenNichtGeprueftGrund()).isNull();
        assertThat(result.getGleichgewichtEinbeinstand()).isNull();
        assertThat(result.getGleichgewichtEinbeinstandNichtGeprueftGrund()).isNull();
    }

    @Test
    void mapVorabklaerungen_handles_all_value_enhanced_boundary_conditions() {
        Tuple tuple = mock(Tuple.class);
        QUntersuchungVorabklaerungen vk = QUntersuchungVorabklaerungen.untersuchungVorabklaerungen;

        when(tuple.get(vk.id)).thenReturn(UUID.randomUUID());
        // 1. Value present, reason empty
        when(tuple.get(vk.fragebogenStatus.value)).thenReturn("$1$NICHT_ERHALTEN$1$$2$$2$");
        // 2. Value empty, reason present
        when(tuple.get(vk.einverstaendnisImpfungen.value)).thenReturn("$1$$1$$2$KRANK$2$");
        // 3. Both empty in formatted string
        when(tuple.get(vk.chronischeKrankheiten.value)).thenReturn("$1$$1$$2$$2$");

        HelloDataVorabklaerungenJson result = HelloDataUntersuchungenMapper.mapVorabklaerungen(tuple);

        assertThat(result).isNotNull();
        assertThat(result.getFragebogenStatus()).isEqualTo(FragebogenStatus.NICHT_ERHALTEN);
        assertThat(result.getFragebogenStatusNichtGeprueftGrund()).isNull();
        assertThat(result.getEinverstaendnisImpfungen()).isNull();
        assertThat(result.getEinverstaendnisImpfungenNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.KRANK);
        assertThat(result.getChronischeKrankheiten()).isNull();
        assertThat(result.getChronischeKrankheitenNichtGeprueftGrund()).isNull();
    }

    @Test
    void toDto_consumes_all_selected_expressions_from_repo() {
        Set<Expression<?>> requestedExpressions = new LinkedHashSet<>();
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(any(Expression.class))).thenAnswer(invocation -> {
            Expression<?> expr = invocation.getArgument(0);
            requestedExpressions.add(expr);
            if (UUID.class.equals(expr.getType())) {
                return UUID.randomUUID();
            }
            return null;
        });

        HelloDataUntersuchungenJson result = HelloDataUntersuchungenMapper.toDto(tuple);

        assertThat(result).isNotNull();
        List<Expression<?>> allRepoExprs = HelloDataUntersuchungenRepo.ALL_EXPRESSIONS;
        assertThat(requestedExpressions)
            .describedAs("Expressions consumed by mapper must exactly match expressions selected in repo")
            .containsExactlyInAnyOrderElementsOf(allRepoExprs);
        assertThat(allRepoExprs)
            .describedAs("Repo expressions must not contain duplicates")
            .doesNotHaveDuplicates();
    }

    @Test
    void each_sub_entity_expression_list_is_non_empty_and_free_of_duplicates() {
        List<List<Expression<?>>> subEntityLists = List.of(
            HelloDataUntersuchungenRepo.UNTERSUCHUNG_EXPRS,
            HelloDataUntersuchungenRepo.KIND_DATEN_EXPRS,
            HelloDataUntersuchungenRepo.DURCHFUEHRUNG_EXPRS,
            HelloDataUntersuchungenRepo.VORABKLAERUNGEN_EXPRS,
            HelloDataUntersuchungenRepo.TRIAGE_EXPRS,
            HelloDataUntersuchungenRepo.IMPFUNGEN_EXPRS,
            HelloDataUntersuchungenRepo.IMPFUNGEN_MASSNAHMEN_EXPRS,
            HelloDataUntersuchungenRepo.IMPFUNGEN_NUR_UNVOLLSTAENDIGE_EXPRS,
            HelloDataUntersuchungenRepo.SEHEN_EXPRS,
            HelloDataUntersuchungenRepo.HOEREN_AUDIOGRAMM_EXPRS,
            HelloDataUntersuchungenRepo.AUDIOGRAMM_RECHTS_EXPRS,
            HelloDataUntersuchungenRepo.AUDIOGRAMM_LINKS_EXPRS,
            HelloDataUntersuchungenRepo.HOEREN_OTOSKOPIE_EXPRS,
            HelloDataUntersuchungenRepo.GROESSE_GEWICHT_EXPRS,
            HelloDataUntersuchungenRepo.ARZT_BEURTEILUNG_EXPRS,
            HelloDataUntersuchungenRepo.SPRACHE_EXPRS,
            HelloDataUntersuchungenRepo.BEWEGUNGSAPPARAT_EXPRS,
            HelloDataUntersuchungenRepo.SCHULRELEVANTE_BEEINTRAECHTIGUNG_EXPRS,
            HelloDataUntersuchungenRepo.LAEUSEKONTROLLE_EXPRS,
            HelloDataUntersuchungenRepo.MASSNAHMEN_EXPRS,
            HelloDataUntersuchungenRepo.ABSCHLUSS_EXPRS
        );

        for (List<Expression<?>> list : subEntityLists) {
            assertThat(list).isNotEmpty();
            assertThat(list).doesNotHaveDuplicates();
        }
    }

    @Test
    void sub_entity_mappers_consume_their_respective_repo_expression_lists() {
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapKindDaten,
            HelloDataUntersuchungenRepo.KIND_DATEN_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapDurchfuehrung,
            HelloDataUntersuchungenRepo.DURCHFUEHRUNG_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapVorabklaerungen,
            HelloDataUntersuchungenRepo.VORABKLAERUNGEN_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapTriage,
            HelloDataUntersuchungenRepo.TRIAGE_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapImpfungenMassnahmen,
            HelloDataUntersuchungenRepo.IMPFUNGEN_MASSNAHMEN_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapImpfungenNurUnvollstaendige,
            HelloDataUntersuchungenRepo.IMPFUNGEN_NUR_UNVOLLSTAENDIGE_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapSehen,
            HelloDataUntersuchungenRepo.SEHEN_EXPRS
        );
        assertConsumedExpressions(
            t -> HelloDataUntersuchungenMapper.mapAudiogramm(HelloDataUntersuchungenRepo.AUDIO_RECHTS, t),
            HelloDataUntersuchungenRepo.AUDIOGRAMM_RECHTS_EXPRS
        );
        assertConsumedExpressions(
            t -> HelloDataUntersuchungenMapper.mapAudiogramm(HelloDataUntersuchungenRepo.AUDIO_LINKS, t),
            HelloDataUntersuchungenRepo.AUDIOGRAMM_LINKS_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapGroesseGewicht,
            HelloDataUntersuchungenRepo.GROESSE_GEWICHT_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapArzt,
            HelloDataUntersuchungenRepo.ARZT_BEURTEILUNG_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapSprache,
            HelloDataUntersuchungenRepo.SPRACHE_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapBewegungsapparat,
            HelloDataUntersuchungenRepo.BEWEGUNGSAPPARAT_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapSchulrelevanteBeeintraechtigung,
            HelloDataUntersuchungenRepo.SCHULRELEVANTE_BEEINTRAECHTIGUNG_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapLaeusekontrolle,
            HelloDataUntersuchungenRepo.LAEUSEKONTROLLE_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapMassnahmen,
            HelloDataUntersuchungenRepo.MASSNAHMEN_EXPRS
        );
        assertConsumedExpressions(
            HelloDataUntersuchungenMapper::mapAbschluss,
            HelloDataUntersuchungenRepo.ABSCHLUSS_EXPRS
        );
    }

    private void assertConsumedExpressions(
        java.util.function.Consumer<Tuple> mapperCall,
        List<Expression<?>> expectedExpressions
    ) {
        Set<Expression<?>> requested = new LinkedHashSet<>();
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(any(Expression.class))).thenAnswer(invocation -> {
            Expression<?> expr = invocation.getArgument(0);
            requested.add(expr);
            if (UUID.class.equals(expr.getType())) {
                return UUID.randomUUID();
            }
            return null;
        });

        mapperCall.accept(tuple);

        assertThat(requested)
            .describedAs("Consumed expressions must match expected expression list")
            .containsExactlyInAnyOrderElementsOf(expectedExpressions);
    }
}
