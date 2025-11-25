package ch.dvbern.oss.hemed.esc.api.features.spss.reports;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AbstractCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AerztinCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AlterCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AnlassTypCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammLinks1000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammLinks2000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammLinks250HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammLinks4000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammLinks500HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammLinks6000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammLinks8000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammRechts1000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammRechts2000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammRechts250HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammRechts4000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammRechts500HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammRechts6000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammRechts8000HzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BMIBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BMIWertCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BewegungsapparatCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BlutdruckCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BlutdruckDiastolischCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BlutdruckSystolischCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BrilleLinsenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.CsvColumnConfig;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FarbsehenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusLinksCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusRechtsCsvColumn;
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
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.JahrSuCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KeinImpfausweisIsCheckedCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KeinImpfausweisIsNotCheckedCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KlassenbezeichnungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KlassenstufeCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitAnzahlImpfungenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfempfehlungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfentscheidCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfschutzGegebenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfstatusGenugendCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfstoffNameCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfungVerabreichtCsvColumn;
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
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MotorikCsvColumn;
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
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StereotestCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.TriageBefundSuCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.TriageLehrpersonCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.TriageMpaCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.TriageSuCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.TriageWunschSchuelerCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.UntersuchungJahrCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.UntersuchungMonatCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.UntersuchungTagCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.VerhaltenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.VerwendeteTafelCsvColumn;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpssFremdsprachenReport {
    private final CsvColumnConfig config;

    public List<AbstractCsvColumn> getReport() {
        return List.of(
            new AnlassTypCsvColumn(config, "ANLASSTYPF"),
            new IdNrCsvColumn(config, "ID_NR_F"),
            new JahrSuCsvColumn(config, "J7_SU_F"),
            new HasNoFragebogenCsvColumn(config, "KEINEFF"),
            new GeschlechtCsvColumn(config, "GESCHLEF"),
            new GeburtsTagCsvColumn(config, "GEBTAGF"),
            new GeburtsMonatCsvColumn(config, "GEBMONF"),
            new GeburtsJahrCsvColumn(config, "GEBJAHRF"),
            new UntersuchungTagCsvColumn(config, "UNTTAGF"),
            new UntersuchungMonatCsvColumn(config, "UNTMONF"),
            new UntersuchungJahrCsvColumn(config, "UNTJAHRF"),
            new AlterCsvColumn(config, "Alter_F"),
            new LandCodeCsvColumn(config, "NATIONF"),
            new MpaCsvColumn(config, "MPAF"),
            new AerztinCsvColumn(config, "AERZTINF"),
            new NachkontrolleCsvColumn(config, "Nachkontrolle_F"),
            new NachzuglerCsvColumn(config, "Nachzügler_F"),
            new NeueintrittCsvColumn(config, "Neueintritt_F"),
            new SchulhausnameCsvColumn(config, "Schulhausname_F"),
            new QuartierCsvColumn(config, "QUARTF"),
            new SchulkreisCsvColumn(config, "Schulkreis_F"),
            new SchulhausnummerCsvColumn(config, "SCHUNRF"),
            new KlassenstufeCsvColumn(config, "Klassenstufe_F"),
            new KlassenbezeichnungCsvColumn(config, "Klassenbezeichnung_F"),
            new SchulartCsvColumn(config, "Schulart_F"),
            new LastUpdateImpfungenCsvColumn(config, "Tag_Erfassung_IK_F"),
            new KeinImpfausweisIsCheckedCsvColumn(config, "IMPFAUSF"),
            new KeinImpfausweisIsNotCheckedCsvColumn(config, "Mit_Impfausweis_F"),
            new ImpfausweisUnvollstaendingCsvColumn(config, "IMPFUNVF"),
            new ImpfausweisZweiPlusEinsSchemaCsvColumn(config, "NEUES_IS_F"),
            new ImpfungGeplantCsvColumn(config, "Impfung_geplant_F"),
            new KrankheitAnzahlImpfungenCsvColumn(config, "DIPHANZF", Krankheit.DIPHTERIE),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "DIPHJAHRF", Krankheit.DIPHTERIE),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Diphtherie_Status_F", Krankheit.DIPHTERIE),
            new KrankheitImpfempfehlungCsvColumn(config, "Diphtherie_Impfempfehlung_F", Krankheit.DIPHTERIE),
            new KrankheitImpfentscheidCsvColumn(config, "Diphtherie_Impfentscheid_F", Krankheit.DIPHTERIE),
            new KrankheitImpfungVerabreichtCsvColumn(config, "DIPSÄDF", Krankheit.DIPHTERIE),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Diphtherie_Genügend_nach_Impf_F", Krankheit.DIPHTERIE),
            new KrankheitImpfstoffNameCsvColumn(config, "Diphtherie_Impfstoff_F", Krankheit.DIPHTERIE),
            new KrankheitAnzahlImpfungenCsvColumn(config, "TETAANZF", Krankheit.TETANUS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "TETJAHRF", Krankheit.TETANUS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Tetanus_Status_F", Krankheit.TETANUS),
            new KrankheitImpfempfehlungCsvColumn(config, "Tetanus_Impfempfehlung_F", Krankheit.TETANUS),
            new KrankheitImpfentscheidCsvColumn(config, "Tetanus_Impfentscheid_F", Krankheit.TETANUS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "TETSÄDF", Krankheit.TETANUS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Tetanus_Genügend_nach_Impf_F", Krankheit.TETANUS),
            new KrankheitImpfstoffNameCsvColumn(config, "Tetanus_Impfstoff_F", Krankheit.TETANUS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "PERTANZF", Krankheit.PERTUSSIS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "PERJAHRF", Krankheit.PERTUSSIS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Pertussis_Status_F", Krankheit.PERTUSSIS),
            new KrankheitImpfempfehlungCsvColumn(config, "Pertussis_Impfempfehlung_F", Krankheit.PERTUSSIS),
            new KrankheitImpfentscheidCsvColumn(config, "Pertussis_Impfentscheid_F", Krankheit.PERTUSSIS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "PERTSÄDF", Krankheit.PERTUSSIS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Pertussis_Genügend_nach_Impf_F", Krankheit.PERTUSSIS),
            new KrankheitImpfstoffNameCsvColumn(config, "Pertussis_Impfstoff_F", Krankheit.PERTUSSIS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "POLIOANZF", Krankheit.POLIO),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "POLIOJAHRF", Krankheit.POLIO),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Polio_Status_F", Krankheit.POLIO),
            new KrankheitImpfempfehlungCsvColumn(config, "Polio_Impfempfehlung_F", Krankheit.POLIO),
            new KrankheitImpfentscheidCsvColumn(config, "Polio_Impfentscheid_F", Krankheit.POLIO),
            new KrankheitImpfungVerabreichtCsvColumn(config, "POLSÄDF", Krankheit.POLIO),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Polio_Genügend_nach_Impf_F", Krankheit.POLIO),
            new KrankheitImpfstoffNameCsvColumn(config, "Polio_Impfstoff_F", Krankheit.POLIO),
            new KrankheitAnzahlImpfungenCsvColumn(config, "HEPBANZF", Krankheit.HEPATITIS_B),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "HEPBJAHRF", Krankheit.HEPATITIS_B),
            new KrankheitImpfschutzGegebenCsvColumn(config, "HepatitisB_Status_F", Krankheit.HEPATITIS_B),
            new KrankheitImpfempfehlungCsvColumn(config, "HepatitisB_Impfempfehlung_F", Krankheit.HEPATITIS_B),
            new KrankheitImpfentscheidCsvColumn(config, "HepatitisB_Impfentscheid_F", Krankheit.HEPATITIS_B),
            new KrankheitImpfungVerabreichtCsvColumn(config, "HepatitisB_Impfung_SÄD_F", Krankheit.HEPATITIS_B),
            new KrankheitImpfstatusGenugendCsvColumn(config, "HepatitisB_Genügend_nach_Impf_F", Krankheit.HEPATITIS_B),
            new KrankheitImpfstoffNameCsvColumn(config, "HepatitisB_Impfstoff_F", Krankheit.HEPATITIS_B),
            new KrankheitAnzahlImpfungenCsvColumn(config, "HPVANZF", Krankheit.HPV),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "HPVJAHRF", Krankheit.HPV),
            new KrankheitImpfschutzGegebenCsvColumn(config, "HPV_Status_F", Krankheit.HPV),
            new KrankheitImpfempfehlungCsvColumn(config, "HPV_Impfempfehlung_F", Krankheit.HPV),
            new KrankheitImpfentscheidCsvColumn(config, "HPV_Impfentscheid_F", Krankheit.HPV),
            new KrankheitImpfungVerabreichtCsvColumn(config, "HPV_Impfung_SÄD_F", Krankheit.HPV),
            new KrankheitImpfstatusGenugendCsvColumn(config, "HPV_Genügend_nach_Impf_F", Krankheit.HPV),
            new KrankheitImpfstoffNameCsvColumn(config, "HPV_Impfstoff_F", Krankheit.HPV),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MASANZF", Krankheit.MASERN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MASJAHRF", Krankheit.MASERN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Masern_Status_F", Krankheit.MASERN),
            new KrankheitImpfempfehlungCsvColumn(config, "Masern_Impfempfehlung_F", Krankheit.MASERN),
            new KrankheitImpfentscheidCsvColumn(config, "Masern_Impfentscheid_F", Krankheit.MASERN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MASSÄDF", Krankheit.MASERN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Masern_Genügend_nach_Impf_F", Krankheit.MASERN),
            new KrankheitImpfstoffNameCsvColumn(config, "Masern_Impfstoff_F", Krankheit.MASERN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MUMPANZF", Krankheit.MUMPS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MUMPJAHRF", Krankheit.MUMPS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Mumps_Status_F", Krankheit.MUMPS),
            new KrankheitImpfempfehlungCsvColumn(config, "Mumps_Impfempfehlung_F", Krankheit.MUMPS),
            new KrankheitImpfentscheidCsvColumn(config, "Mumps_Impfentscheid_F", Krankheit.MUMPS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MUMSÄDF", Krankheit.MUMPS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Mumps_Genügend_nach_Impf_F", Krankheit.MUMPS),
            new KrankheitImpfstoffNameCsvColumn(config, "Mumps_Impfstoff_F", Krankheit.MUMPS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "ROETANZF", Krankheit.ROETELN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "ROEJAHRF", Krankheit.ROETELN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Roeteln_Status_F", Krankheit.ROETELN),
            new KrankheitImpfempfehlungCsvColumn(config, "Roeteln_Impfempfehlung_F", Krankheit.ROETELN),
            new KrankheitImpfentscheidCsvColumn(config, "Roeteln_Impfentscheid_F", Krankheit.ROETELN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "ROESÄDF", Krankheit.ROETELN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Roeteln_Genügend_nach_Impf_F", Krankheit.ROETELN),
            new KrankheitImpfstoffNameCsvColumn(config, "Roeteln_Impfstoff_F", Krankheit.ROETELN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "VARIZF", Krankheit.VARIZELLEN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "VARIZJAHRF", Krankheit.VARIZELLEN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Varizellen_Status_F", Krankheit.VARIZELLEN),
            new KrankheitImpfempfehlungCsvColumn(config, "Varizellen_Impfempfehlung_F", Krankheit.VARIZELLEN),
            new KrankheitImpfentscheidCsvColumn(config, "Varizellen_Impfentscheid_F", Krankheit.VARIZELLEN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "Varizellen_Impfung_SÄD_F", Krankheit.VARIZELLEN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Varizellen_Genügend_nach_Impf_F", Krankheit.VARIZELLEN),
            new KrankheitImpfstoffNameCsvColumn(config, "Varizellen_Impfstoff_F", Krankheit.VARIZELLEN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MENACWYANZF", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MENACWYJAHRF", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfschutzGegebenCsvColumn(config, "MenACWY_Status_F", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfempfehlungCsvColumn(config, "MenACWY_Impfempfehlung_F", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfentscheidCsvColumn(config, "MenACWY_Impfentscheid_F", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MenACWY_Impfung_SÄD_F", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfstatusGenugendCsvColumn(
                config,
                "MenACWY_Genügend_nach_Impf_F",
                Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfstoffNameCsvColumn(config, "MenACWY_Impfstoff_F", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MENBANZF", Krankheit.MENINGOKOKKEN_B),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MENBJAHRF", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfschutzGegebenCsvColumn(config, "MenB_Status_F", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfempfehlungCsvColumn(config, "MenB_Impfempfehlung_F", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfentscheidCsvColumn(config, "MenB_Impfentscheid_F", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MenB_Impfung_SÄD_F", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfstatusGenugendCsvColumn(config, "MenB_Genügend_nach_Impf_F", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfstoffNameCsvColumn(config, "MenB_Impfstoff_F", Krankheit.MENINGOKOKKEN_B),
            new ImpfberatungMPACsvColumn(config, "Impfberatung_MPA_F"),
            new ImpfenRueckfrageKinderarztCsvColumn(config, "Impfen_Rückfrage_Kinderarzt_F"),
            new ImpfenInfoKinderarztCsvColumn(config, "Impfen_Info_Kinderarzt_F"),
            new ImpfenInfoElternCsvColumn(config, "Impfen_Info_Eltern_F"),
            new BrilleLinsenCsvColumn(config, "BRILLEF"),
            new StereotestCsvColumn(config, "STEREOF"),
            new FarbsehenCsvColumn(config, "FARBSEHF"),
            new VerwendeteTafelCsvColumn(config, "Sehen_Tafel_F"),
            new FernvisusRechtsCsvColumn(config, "FVISREF"),
            new FernvisusLinksCsvColumn(config, "FVISLIF"),
            new FernvisusCsvColumn(config, "FVNOF", DistanzSehenResultat.NORMAL),
            new FernvisusCsvColumn(config, "FVRREDF", DistanzSehenResultat.RECHTS_REDUZIERT),
            new FernvisusCsvColumn(config, "FVLREDF", DistanzSehenResultat.LINKS_REDUZIERT),
            new FernvisusCsvColumn(config, "FVNGF", DistanzSehenResultat.NICHT_GEPRUEFT),
            new NahvisusRechtsCsvColumn(config, "NVISREF"),
            new NahvisusLinksCsvColumn(config, "NVISLIF"),
            new NahvisusCsvColumn(config, "NVNOF", DistanzSehenResultat.NORMAL),
            new NahvisusCsvColumn(config, "NVRREDF", DistanzSehenResultat.RECHTS_REDUZIERT),
            new NahvisusCsvColumn(config, "NVLREDF", DistanzSehenResultat.LINKS_REDUZIERT),
            new NahvisusCsvColumn(config, "NVNGF", DistanzSehenResultat.NICHT_GEPRUEFT),
            new AudiogrammRechts250HzCsvColumn(config, "AU250REF"),
            new AudiogrammRechts500HzCsvColumn(config, "AU500REF"),
            new AudiogrammRechts1000HzCsvColumn(config, "A1000REF"),
            new AudiogrammRechts2000HzCsvColumn(config, "A2000REF"),
            new AudiogrammRechts4000HzCsvColumn(config, "A4000REF"),
            new AudiogrammRechts6000HzCsvColumn(config, "A6000REF"),
            new AudiogrammRechts8000HzCsvColumn(config, "A8000REF"),
            new AudiogrammLinks250HzCsvColumn(config, "AU250LIF"),
            new AudiogrammLinks500HzCsvColumn(config, "AU500LIF"),
            new AudiogrammLinks1000HzCsvColumn(config, "A1000LIF"),
            new AudiogrammLinks2000HzCsvColumn(config, "A2000LIF"),
            new AudiogrammLinks4000HzCsvColumn(config, "A4000LIF"),
            new AudiogrammLinks6000HzCsvColumn(config, "A6000LIF"),
            new AudiogrammLinks8000HzCsvColumn(config, "A8000LIF"),
            new HoerenBeurteilungCsvColumn(config, "GEHNOF", AudiogrammBeurteilung.NORMAL),
            new HoerenBeurteilungCsvColumn(config, "GEHRREDF", AudiogrammBeurteilung.RECHTS_REDUZIERT),
            new HoerenBeurteilungCsvColumn(config, "GEHLREDF", AudiogrammBeurteilung.LINKS_REDUZIERT),
            new HoerenBeurteilungCsvColumn(config, "GEHNGF", AudiogrammBeurteilung.NICHT_GEPRUEFT),
            new HoerenZusaetzlicheAngabenCsvColumn(config, "GEHERKÄLF", HoerenZusaetzlicheAngaben.ERKAELTUNG),
            new HoerenZusaetzlicheAngabenCsvColumn(config, "Ohr_Hoerhilfe_F", HoerenZusaetzlicheAngaben.HOERHILFE),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Normal_F", OtoskopieResultat.NORMAL),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Cerumen_F", OtoskopieResultat.TROMMELFELL_CERUMEN),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_TF_NichtEinsehbar_F",
                OtoskopieResultat.NICHT_EINSEHBAR),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_TF_Perforation_F",
                OtoskopieResultat.TROMMELFELL_PERFORATION),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_Seromukotympanon_F",
                OtoskopieResultat.SEROMUKOTYMPANON),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_Nicht_geprüft_F",
                OtoskopieResultat.NICHT_GEPRUEFT),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Anderes_F", OtoskopieResultat.ANDERES),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Normal_F", OtoskopieResultat.NORMAL),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Cerumen_F", OtoskopieResultat.TROMMELFELL_CERUMEN),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_TF_NichtEinsehbar_F",
                OtoskopieResultat.NICHT_EINSEHBAR),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_TF_Perforation_F",
                OtoskopieResultat.TROMMELFELL_PERFORATION),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_Seromukotympanon_F",
                OtoskopieResultat.SEROMUKOTYMPANON),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Nicht geprüft_F", OtoskopieResultat.NICHT_GEPRUEFT),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Anderes_F", OtoskopieResultat.ANDERES),
            new GroesseWertCsvColumn(config, "GROESSEF"),
            new GroesseBeurteilungCsvColumn(config, "GROEBEF"),
            new GewichtWertCsvColumn(config, "GEWICHTF"),
            new GewichtBeurteilungCsvColumn(config, "GEWBEF"),
            new BMIWertCsvColumn(config, "BMIF"),
            new BMIBeurteilungCsvColumn(config, "BMIBEF"),
            new TriageLehrpersonCsvColumn(config, "Triage_Lehrperson_F"),
            new TriageSuCsvColumn(config, "Triage_SÄ_FB_F"),
            new TriageMpaCsvColumn(config, "Triage_MPA_F"),
            new TriageBefundSuCsvColumn(config, "Triage_Befund_SU_F"),
            new TriageWunschSchuelerCsvColumn(config, "Triage_Wunsch_SuS_F"),
            new BewegungsapparatCsvColumn(config, "GROBKURSNOF", Bewegungsapparat.GROBKURSORISCH_NORMAL),
            new BewegungsapparatCsvColumn(config, "FEHLBLDF", Bewegungsapparat.KONGENITALE_FEHLBILDUNG),
            new BewegungsapparatCsvColumn(config, "SKOLIOF", Bewegungsapparat.SKOLIOSE),
            new BewegungsapparatCsvColumn(config, "GANGSTF", Bewegungsapparat.GANGSTOERUNG),
            new BewegungsapparatCsvColumn(config, "FUESSEF", Bewegungsapparat.FUSSFEHLSTELLUNG),
            new BewegungsapparatCsvColumn(config, "WSANDF", Bewegungsapparat.ANDERES),
            new BewegungsapparatCsvColumn(config, "WSNGF", Bewegungsapparat.NICHT_GEPRUEFT),
            new MotorikCsvColumn(config, "Motorik_F"),
            new MundAndZaehneCsvColumn(config, "Mund_Unauffällig_F", MundAndZaehne.UNAUFFAELLIG),
            new MundAndZaehneCsvColumn(config, "KARIESF", MundAndZaehne.KARIES),
            new MundAndZaehneCsvColumn(config, "Mund_KariesSaniert_F", MundAndZaehne.KARIES_SANIERT),
            new MundAndZaehneCsvColumn(config, "Mund_Adenoidhyperplasie_F", MundAndZaehne.ADENOIDHYPERPLASIE),
            new MundAndZaehneCsvColumn(config, "Mund_Anderes_F", MundAndZaehne.ANDERES),
            new MundAndZaehneCsvColumn(config, "Mund_NichtGeprüft_F", MundAndZaehne.NICHT_GEPRUEFT),
            new HautCsvColumn(config, "HAUTNOF", Haut.GROBKURSORISCH_UNAUFFAELLIG),
            new HautCsvColumn(config, "HAUTANDEF", Haut.ANDERES),
            new HautCsvColumn(config, "HAUTNGF", Haut.NICHT_GEPRUEFT),
            new HerzauskultationCsvColumn(config, "AUSKULTF"),
            new BlutdruckCsvColumn(config, "Blutdruck_Beurteilung_F"),
            new BlutdruckSystolischCsvColumn(config, "Blutdruck_Systolisch_F"),
            new BlutdruckDiastolischCsvColumn(config, "Blutdruck_Diastolisch_F"),
            new VerhaltenCsvColumn(config, "EINDRUF"),
            new MassnahmenKeineCsvColumn(config, "MNKEINF"),
            new MassnahmenInformationElternCsvColumn(config, "MNINFEF"),
            new MassnahmenAbgegebenesInfomaterialCsvColumn(config, "MNINFOMF"),
            new MassnahmenAnderesCsvColumn(config, "MNANDEF"),
            new MassnahmenUeberweisungHausarztCsvColumn(config, "Massnahmen_HA_KA_F"),
            new MassnahmenUeberweisungAugenarztCsvColumn(config, "UEBERWAUF"),
            new MassnahmenBereitsBehandeltSelectedCsvColumn(config, "MNBEREIF"),
            new MassnahmenBereitsBehandeltSehenCsvColumn(config, "BERBEHAUF"),
            new MassnahmenBereitsBehandeltHoerenCsvColumn(config, "BERBEHKOF"),
            new MassnahmenBereitsBehandeltGroesseGewichtCsvColumn(config, "BERBEHGEF"),
            new MassnahmenBereitsBehandeltSpracheCsvColumn(config, "BERBEHSPF"),
            new MassnahmenBereitsBehandeltMotorikCsvColumn(config, "BERBEHMOF"),
            new MassnahmenBereitsBehandeltAnderesCsvColumn(config, "BERBEHANF"),
            new MassnahmenNachkontrolleSelectedCsvColumn(config, "MNSÄDF"),
            new MassnahmenNachkontrolleSehenCsvColumn(config, "SÄDAUGF"),
            new MassnahmenNachkontrolleHoerenCsvColumn(config, "SÄDOHRF"),
            new MassnahmenNachkontrolleGroesseGewichtCsvColumn(config, "SÄDGEWF"),
            new MassnahmenNachkontrolleSpracheCsvColumn(config, "SÄDSPRF"),
            new MassnahmenNachkontrolleMotorikCsvColumn(config, "SÄDMOTENF"),
            new MassnahmenNachkontrolleAnderesCsvColumn(config, "SÄDANDGRF")
        );
    }
}
