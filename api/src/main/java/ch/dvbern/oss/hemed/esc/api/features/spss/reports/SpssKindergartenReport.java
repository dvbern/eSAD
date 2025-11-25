package ch.dvbern.oss.hemed.esc.api.features.spss.reports;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AbstractCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AerztinCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AlterCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AnlassTypCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammLinks1000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammLinks2000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammLinks4000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammLinks500HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammLinks6000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammRechts1000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammRechts2000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammRechts4000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammRechts500HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammRechts6000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BMIBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BMIWertCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BewegungsapparatCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BrilleLinsenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.CsvColumnConfig;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.EinbeinhuepfenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.EinbeinstandCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FarbsehenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusLinksCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusRechtsCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FingeroppositionCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeburtsJahrCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeburtsMonatCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeburtsTagCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeschlechtCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GewichtBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GewichtWertCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GroesseBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GroesseWertCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HasNoFragebogenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HautCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HerzauskultationCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HoerenBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HoerenOtoskopieLinksCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HoerenOtoskopieRechtsCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HoerenZusaetzlicheAngabenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.IdNrCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfausweisUnvollstaendingCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfausweisZweiPlusEinsSchemaCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfberatungMPACsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfenInfoElternCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfenInfoKinderarztCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfenRueckfrageKinderarztCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfungGeplantCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.InspektionCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.JahrSuCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KeinImpfausweisIsCheckedCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KeinImpfausweisIsNotCheckedCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KlassenbezeichnungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KlassenstufeCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KonvergenzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitAnzahlImpfungenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfempfehlungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfentscheidCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfschutzGegebenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitJahrDerLetztenImpfungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.LandCodeCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.LastUpdateImpfungenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenAbgegebenesInfomaterialCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenAnderesCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltAnderesCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltGroesseGewichtCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltHoerenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltMotorikCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltSehenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltSelectedCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltSpracheCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenInformationElternCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenKeineCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleAnderesCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleGroesseGewichtCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleHoerenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleMotorikCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleSehenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleSelectedCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleSpracheCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenUeberweisungAugenarztCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenUeberweisungHausarztCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MotilitaetCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MpaCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MundAndZaehneCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NachkontrolleCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NachzuglerCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NahvisusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NahvisusLinksCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NahvisusRechtsCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NeueintrittCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.QuartierCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulartCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulhausnameCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulhausnummerCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulkreisCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SprachfaehigkeitCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StereotestCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StrabismusAuftretenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StrabismusAuspraegungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StrabismusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.UntersuchungJahrCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.UntersuchungMonatCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.UntersuchungTagCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.VerhaltenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.VerwendeteTafelCsvColumn;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpssKindergartenReport {
    private final CsvColumnConfig config;

    public List<AbstractCsvColumn> getReport() {
        return List.of(
            new AnlassTypCsvColumn(config, "ANLASSTYPK"),
            new IdNrCsvColumn(config, "ID_NR_K"),
            new JahrSuCsvColumn(config, "J1_SU_K"),
            new HasNoFragebogenCsvColumn(config, "KEINEF"),
            new GeschlechtCsvColumn(config, "GESCHLEK"),
            new GeburtsTagCsvColumn(config, "GEBTAGK"),
            new GeburtsMonatCsvColumn(config, "GEBMONK"),
            new GeburtsJahrCsvColumn(config, "GEBJAHRK"),
            new UntersuchungTagCsvColumn(config, "UNTTAGK"),
            new UntersuchungMonatCsvColumn(config, "UNTMONK"),
            new UntersuchungJahrCsvColumn(config, "UNTJAHRK"),
            new AlterCsvColumn(config, "Alter_K"),
            new LandCodeCsvColumn(config, "NATIONK"),
            new MpaCsvColumn(config, "MPAK"),
            new AerztinCsvColumn(config, "AERZTINK"),
            new NachkontrolleCsvColumn(config, "Nachkontrolle_K"),
            new NachzuglerCsvColumn(config, "Nachzügler_K"),
            new NeueintrittCsvColumn(config, "Neueintritt_K"),
            new SchulhausnameCsvColumn(config, "Kiganame_K"),
            new QuartierCsvColumn(config, "QUARTK"),
            new SchulkreisCsvColumn(config, "Schulkreis_K"),
            new SchulhausnummerCsvColumn(config, "KIGABEZK"),
            new KlassenstufeCsvColumn(config, "Klassenstufe_K"),
            new KlassenbezeichnungCsvColumn(config, "Klassenbezeichnung_K"),
            new SchulartCsvColumn(config, "Schulart_K"),
            new LastUpdateImpfungenCsvColumn(config, "Tag_Erfassung_IK_K"),
            new KeinImpfausweisIsCheckedCsvColumn(config, "IMPFAUSK"),
            new KeinImpfausweisIsNotCheckedCsvColumn(config, "Mit_Impfausweis_K"),
            new ImpfausweisUnvollstaendingCsvColumn(config, "IMPFUNVK"),
            new ImpfausweisZweiPlusEinsSchemaCsvColumn(config, "NEUES_IS_K"),
            new ImpfungGeplantCsvColumn(config, "Impfung_geplant_K"),
            new KrankheitAnzahlImpfungenCsvColumn(config, "DIPHANZK", Krankheit.DIPHTERIE),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "DIPHJAHRK", Krankheit.DIPHTERIE),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Diphtherie_Status_K", Krankheit.DIPHTERIE),
            new KrankheitImpfempfehlungCsvColumn(config, "Diphtherie_Impfempfehlung_K", Krankheit.DIPHTERIE),
            new KrankheitImpfentscheidCsvColumn(config, "Diphtherie_Impfentscheid_K", Krankheit.DIPHTERIE),
            new KrankheitAnzahlImpfungenCsvColumn(config, "TETAANZK", Krankheit.TETANUS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "TETJAHRK", Krankheit.TETANUS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Tetanus_Status_K", Krankheit.TETANUS),
            new KrankheitImpfempfehlungCsvColumn(config, "Tetanus_Impfempfehlung_K", Krankheit.TETANUS),
            new KrankheitImpfentscheidCsvColumn(config, "Tetanus_Impfentscheid_K", Krankheit.TETANUS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "PERTANZK", Krankheit.PERTUSSIS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "PERJAHRK", Krankheit.PERTUSSIS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Pertussis_Status_K", Krankheit.PERTUSSIS),
            new KrankheitImpfempfehlungCsvColumn(config, "Pertussis_Impfempfehlung_K", Krankheit.PERTUSSIS),
            new KrankheitImpfentscheidCsvColumn(config, "Pertussis_Impfentscheid_K", Krankheit.PERTUSSIS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "POLIOANZK", Krankheit.POLIO),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "POLIOJAHRK", Krankheit.POLIO),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Polio_Status_K", Krankheit.POLIO),
            new KrankheitImpfempfehlungCsvColumn(config, "Polio_Impfempfehlung_K", Krankheit.POLIO),
            new KrankheitImpfentscheidCsvColumn(config, "Polio_Impfentscheid_K", Krankheit.POLIO),
            new KrankheitAnzahlImpfungenCsvColumn(config, "HEPBANZK", Krankheit.HEPATITIS_B),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "HEPBJAHRK", Krankheit.HEPATITIS_B),
            new KrankheitImpfschutzGegebenCsvColumn(config, "HepatitisB_Status_K", Krankheit.HEPATITIS_B),
            new KrankheitImpfempfehlungCsvColumn(config, "HepatitisB_Impfempfehlung_K", Krankheit.HEPATITIS_B),
            new KrankheitImpfentscheidCsvColumn(config, "HepatitisB_Impfentscheid_K", Krankheit.HEPATITIS_B),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MASANZK", Krankheit.MASERN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MASJAHRK", Krankheit.MASERN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Masern_Status_K", Krankheit.MASERN),
            new KrankheitImpfempfehlungCsvColumn(config, "Masern_Impfempfehlung_K", Krankheit.MASERN),
            new KrankheitImpfentscheidCsvColumn(config, "Masern_Impfentscheid_K", Krankheit.MASERN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MUMPANZK", Krankheit.MUMPS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MUMPJAHRK", Krankheit.MUMPS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Mumps_Status_K", Krankheit.MUMPS),
            new KrankheitImpfempfehlungCsvColumn(config, "Mumps_Impfempfehlung_K", Krankheit.MUMPS),
            new KrankheitImpfentscheidCsvColumn(config, "Mumps_Impfentscheid_K", Krankheit.MUMPS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "ROETANZK", Krankheit.ROETELN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "ROEJAHRK", Krankheit.ROETELN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Roeteln_Status_K", Krankheit.ROETELN),
            new KrankheitImpfempfehlungCsvColumn(config, "Roeteln_Impfempfehlung_K", Krankheit.ROETELN),
            new KrankheitImpfentscheidCsvColumn(config, "Roeteln_Impfentscheid_K", Krankheit.ROETELN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "VARIZK", Krankheit.VARIZELLEN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "VARIZJAHRK", Krankheit.VARIZELLEN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Varizellen_Status_K", Krankheit.VARIZELLEN),
            new KrankheitImpfempfehlungCsvColumn(config, "Varizellen_Impfempfehlung_K", Krankheit.VARIZELLEN),
            new KrankheitImpfentscheidCsvColumn(config, "Varizellen_Impfentscheid_K", Krankheit.VARIZELLEN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MENACWYANZK", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MENACWYJAHRK", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfschutzGegebenCsvColumn(config, "MenACWY_Status_K", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfempfehlungCsvColumn(config, "MenACWY_Impfempfehlung_K", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfentscheidCsvColumn(config, "MenACWY_Impfentscheid_K", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MENBANZK", Krankheit.MENINGOKOKKEN_B),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MENBJAHRK", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfschutzGegebenCsvColumn(config, "MenB_Status_K", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfempfehlungCsvColumn(config, "MenB_Impfempfehlung_K", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfentscheidCsvColumn(config, "MenB_Impfentscheid_K", Krankheit.MENINGOKOKKEN_B),
            new ImpfberatungMPACsvColumn(config, "Impfberatung_MPA_K"),
            new ImpfenRueckfrageKinderarztCsvColumn(config, "Impfen_Rückfrage_Kinderarzt_K"),
            new ImpfenInfoKinderarztCsvColumn(config, "Impfen_Info_Kinderarzt_K"),
            new ImpfenInfoElternCsvColumn(config, "Impfen_Info_Eltern_K"),
            new BrilleLinsenCsvColumn(config, "BRILLEK"),
            new StereotestCsvColumn(config, "STEREOK"),
            new FarbsehenCsvColumn(config, "FARBSEHK"),
            new VerwendeteTafelCsvColumn(config, "Sehen_Tafel_K"),
            new FernvisusRechtsCsvColumn(config, "VISREK"),
            new FernvisusLinksCsvColumn(config, "VISLIK"),
            new FernvisusCsvColumn(config, "VNOK", DistanzSehenResultat.NORMAL),
            new FernvisusCsvColumn(config, "VRREDK", DistanzSehenResultat.RECHTS_REDUZIERT),
            new FernvisusCsvColumn(config, "VLREDK", DistanzSehenResultat.LINKS_REDUZIERT),
            new FernvisusCsvColumn(config, "VNGK", DistanzSehenResultat.NICHT_GEPRUEFT),
            new NahvisusRechtsCsvColumn(config, "Sehen_NV_rechts_K"),
            new NahvisusLinksCsvColumn(config, "Sehen_NV_links_K"),
            new NahvisusCsvColumn(config, "Sehen_NV_normal_K", DistanzSehenResultat.NORMAL),
            new NahvisusCsvColumn(config, "Sehen_NV_rechtsReduziert_K", DistanzSehenResultat.RECHTS_REDUZIERT),
            new NahvisusCsvColumn(config, "Sehen_NV_linksReduziert_K", DistanzSehenResultat.LINKS_REDUZIERT),
            new NahvisusCsvColumn(config, "Sehen_NV_NichtGeprüft_K", DistanzSehenResultat.NICHT_GEPRUEFT),
            new MotilitaetCsvColumn(config, "AUGMOTK"),
            new StrabismusCsvColumn(config, "Sehen_Strab_Kein_K", StrabismusResultat.KEIN_STRABISMUS),
            new StrabismusCsvColumn(config, "Sehen_Strab_Exo_K", StrabismusResultat.EXO),
            new StrabismusCsvColumn(config, "Sehen_Strab_Eso_K", StrabismusResultat.ESO),
            new StrabismusCsvColumn(config, "Sehen_Strab_Hyper_K", StrabismusResultat.HYPER),
            new StrabismusCsvColumn(config, "Sehen_Strab_NichtGeprüft_K", StrabismusResultat.NICHT_GEPRUEFT),
            new StrabismusAuspraegungCsvColumn(config, "STRABQK"),
            new StrabismusAuftretenCsvColumn(config, "STRABOK"),
            new InspektionCsvColumn(config, "Sehen_Inspektion_K"),
            new KonvergenzCsvColumn(config, "Sehen_Konvergenz_K"),
            new AudiogrammRechts500HzCsvColumn(config, "AU500REK"),
            new AudiogrammRechts1000HzCsvColumn(config, "A1000REK"),
            new AudiogrammRechts2000HzCsvColumn(config, "A2000REK"),
            new AudiogrammRechts4000HzCsvColumn(config, "A4000REK"),
            new AudiogrammRechts6000HzCsvColumn(config, "A6000REK"),
            new AudiogrammLinks500HzCsvColumn(config, "AU500LIK"),
            new AudiogrammLinks1000HzCsvColumn(config, "A1000LIK"),
            new AudiogrammLinks2000HzCsvColumn(config, "A2000LIK"),
            new AudiogrammLinks4000HzCsvColumn(config, "A4000LIK"),
            new AudiogrammLinks6000HzCsvColumn(config, "A6000LIK"),
            new HoerenBeurteilungCsvColumn(config, "OHRNOK", AudiogrammBeurteilung.NORMAL),
            new HoerenBeurteilungCsvColumn(config, "OHRREDREK", AudiogrammBeurteilung.RECHTS_REDUZIERT),
            new HoerenBeurteilungCsvColumn(config, "OHRREDLIK", AudiogrammBeurteilung.LINKS_REDUZIERT),
            new HoerenBeurteilungCsvColumn(config, "OhrnichtgepK", AudiogrammBeurteilung.NICHT_GEPRUEFT),
            new HoerenZusaetzlicheAngabenCsvColumn(config, "OHRERKÄK", HoerenZusaetzlicheAngaben.ERKAELTUNG),
            new HoerenZusaetzlicheAngabenCsvColumn(config, "Ohr_Hoerhilfe_K", HoerenZusaetzlicheAngaben.HOERHILFE),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Normal_K", OtoskopieResultat.NORMAL),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Cerumen_K", OtoskopieResultat.TROMMELFELL_CERUMEN),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_TF_NichtEinsehbar_K",
                OtoskopieResultat.NICHT_EINSEHBAR),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_TF_Perforation_K",
                OtoskopieResultat.TROMMELFELL_PERFORATION),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_Seromukotympanon_K",
                OtoskopieResultat.SEROMUKOTYMPANON),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_Nicht_geprüft_K",
                OtoskopieResultat.NICHT_GEPRUEFT),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Anderes_K", OtoskopieResultat.ANDERES),

            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Normal_K", OtoskopieResultat.NORMAL),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Cerumen_K", OtoskopieResultat.TROMMELFELL_CERUMEN),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_TF_NichtEinsehbar_K",
                OtoskopieResultat.NICHT_EINSEHBAR),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_TF_Perforation_K",
                OtoskopieResultat.TROMMELFELL_PERFORATION),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_Seromukotympanon_K",
                OtoskopieResultat.SEROMUKOTYMPANON),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Nicht_geprüft_K", OtoskopieResultat.NICHT_GEPRUEFT),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Anderes_K", OtoskopieResultat.ANDERES),
            new GroesseWertCsvColumn(config, "GROESSEK"),
            new GroesseBeurteilungCsvColumn(config, "GROEBEK"),
            new GewichtWertCsvColumn(config, "GEWICHTK"),
            new GewichtBeurteilungCsvColumn(config, "GEWBEK"),
            new BMIWertCsvColumn(config, "BMIK"),
            new BMIBeurteilungCsvColumn(config, "BMIBEK"),
            new BewegungsapparatCsvColumn(config, "GROBKURSNOK", Bewegungsapparat.GROBKURSORISCH_NORMAL),
            new BewegungsapparatCsvColumn(config, "FEHLBLDK", Bewegungsapparat.KONGENITALE_FEHLBILDUNG),
            new BewegungsapparatCsvColumn(config, "SKOLIOK", Bewegungsapparat.SKOLIOSE),
            new BewegungsapparatCsvColumn(config, "GANGSTK", Bewegungsapparat.GANGSTOERUNG),
            new BewegungsapparatCsvColumn(config, "FUESSEK", Bewegungsapparat.FUSSFEHLSTELLUNG),
            new BewegungsapparatCsvColumn(config, "WSANDK", Bewegungsapparat.ANDERES),
            new BewegungsapparatCsvColumn(config, "WSNGK", Bewegungsapparat.NICHT_GEPRUEFT),
            new EinbeinstandCsvColumn(config, "Einbeinstand_K"),
            new EinbeinhuepfenCsvColumn(config, "Einbeinhüpfen_K"),
            new FingeroppositionCsvColumn(config, "FMOTNOK", Fingeropposition.ALTERSENTSPRECHEND),
            new FingeroppositionCsvColumn(config, "FMOTREK", Fingeropposition.RECHTS_AUFFAELLIG),
            new FingeroppositionCsvColumn(config, "FMOTLIK", Fingeropposition.LINKS_AUFFAELLIG),
            new FingeroppositionCsvColumn(config, "FMOTNGK", Fingeropposition.NICHT_GEPRUEFT),
            new FingeroppositionCsvColumn(config, "Fingeropposition_Anderes_K", Fingeropposition.ANDERES),
            new SprachfaehigkeitCsvColumn(config, "SPRANOK", Sprachfaehigkeit.GROBKURSORISCH_NORMAL),
            new SprachfaehigkeitCsvColumn(config, "SPRAENTK", Sprachfaehigkeit.ENTWICKLUNGSVERZOEGERUNG),
            new SprachfaehigkeitCsvColumn(config, "SPRAARTK", Sprachfaehigkeit.ARTIKULATIONSFEHLER),
            new SprachfaehigkeitCsvColumn(config, "SPRADEUK", Sprachfaehigkeit.DEUTSCHKENNTNISSE_MINIM),
            new SprachfaehigkeitCsvColumn(config, "SPRAANDK", Sprachfaehigkeit.ANDERES),
            new SprachfaehigkeitCsvColumn(config, "SPRANGK", Sprachfaehigkeit.NICHT_GEPRUEFT),
            new MundAndZaehneCsvColumn(config, "Mund_Unauffällig_K", MundAndZaehne.UNAUFFAELLIG),
            new MundAndZaehneCsvColumn(config, "KARIESK", MundAndZaehne.KARIES),
            new MundAndZaehneCsvColumn(config, "Mund_KariesSaniert_K", MundAndZaehne.KARIES_SANIERT),
            new MundAndZaehneCsvColumn(config, "Mund_Adenoidhyperplasie_K", MundAndZaehne.ADENOIDHYPERPLASIE),
            new MundAndZaehneCsvColumn(config, "Mund_Anderes_K", MundAndZaehne.ANDERES),
            new MundAndZaehneCsvColumn(config, "Mund_NichtGeprüft_K", MundAndZaehne.NICHT_GEPRUEFT),
            new HautCsvColumn(config, "HAUTNOK", Haut.GROBKURSORISCH_UNAUFFAELLIG),
            new HautCsvColumn(config, "HAUTANDEK", Haut.ANDERES),
            new HautCsvColumn(config, "HAUTNGK", Haut.NICHT_GEPRUEFT),
            new HerzauskultationCsvColumn(config, "AUSKULTK"),
            new VerhaltenCsvColumn(config, "EINDRUK"),
            new MassnahmenKeineCsvColumn(config, "MNKEINK"),
            new MassnahmenInformationElternCsvColumn(config, "MNINFEK"),
            new MassnahmenAbgegebenesInfomaterialCsvColumn(config, "MNINFOMK"),
            new MassnahmenAnderesCsvColumn(config, "MNANDEK"),
            new MassnahmenUeberweisungHausarztCsvColumn(config, "Massnahmen_HA_KA_K"),
            new MassnahmenUeberweisungAugenarztCsvColumn(config, "AUGARZTK"),
            new MassnahmenBereitsBehandeltSelectedCsvColumn(config, "MNBEREIK"),
            new MassnahmenBereitsBehandeltSehenCsvColumn(config, "BERBEHAUK"),
            new MassnahmenBereitsBehandeltHoerenCsvColumn(config, "BERBEHKOK"),
            new MassnahmenBereitsBehandeltGroesseGewichtCsvColumn(config, "BERBEHGEK"),
            new MassnahmenBereitsBehandeltSpracheCsvColumn(config, "BERBEHSPK"),
            new MassnahmenBereitsBehandeltMotorikCsvColumn(config, "BERBEHMOK"),
            new MassnahmenBereitsBehandeltAnderesCsvColumn(config, "BERBEHANK"),
            new MassnahmenNachkontrolleSelectedCsvColumn(config, "MNSÄDK"),
            new MassnahmenNachkontrolleSehenCsvColumn(config, "SÄDAUGK"),
            new MassnahmenNachkontrolleHoerenCsvColumn(config, "SÄDOHRK"),
            new MassnahmenNachkontrolleGroesseGewichtCsvColumn(config, "SÄDGEWK"),
            new MassnahmenNachkontrolleSpracheCsvColumn(config, "SÄDSPRK"),
            new MassnahmenNachkontrolleMotorikCsvColumn(config, "SÄDMOTENK"),
            new MassnahmenNachkontrolleAnderesCsvColumn(config, "SÄDANDGRK")
        );
    }
}
