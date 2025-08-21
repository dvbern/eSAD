package ch.dvbern.oss.hemed.esc.api.features.spss.reports;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.DistanzSehenResultat;
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
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MpaCsvColumn;
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
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.TriageBefundSuCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.TriageLehrpersonCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.TriageMpaCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.TriageSuCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.TriageWunschSchuelerCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.UntersuchungJahrCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.UntersuchungMonatCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.UntersuchungTagCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.VerwendeteTafelCsvColumn;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpssKlasse8Report {
    private final CsvColumnConfig config;

    public List<AbstractCsvColumn> getReport() {
        return List.of(
            new AnlassTypCsvColumn(config, "ANLASSTYP8"),
            new IdNrCsvColumn(config, "ID_NR_8"),
            new JahrSuCsvColumn(config, "J5_SU_8"),
            new HasNoFragebogenCsvColumn(config, "KEINEF8"),
            new GeschlechtCsvColumn(config, "GESCHLE8"),
            new GeburtsTagCsvColumn(config, "GEBTAG8"),
            new GeburtsMonatCsvColumn(config, "GEBMON8"),
            new GeburtsJahrCsvColumn(config, "GEBJAHR8"),
            new UntersuchungTagCsvColumn(config, "UNTTAG8"),
            new UntersuchungMonatCsvColumn(config, "UNTMON8"),
            new UntersuchungJahrCsvColumn(config, "UNTJAHR8"),
            new AlterCsvColumn(config, "Alter_8"),
            new LandCodeCsvColumn(config, "NATION8"),
            new MpaCsvColumn(config, "MPA8"),
            new AerztinCsvColumn(config, "AERZTIN8"),
            new NachkontrolleCsvColumn(config, "Nachkontrolle_8"),
            new NachzuglerCsvColumn(config, "Nachzügler_8"),
            new NeueintrittCsvColumn(config, "Neueintritt_8"),
            new SchulhausnameCsvColumn(config, "Schulhausname_8"),
            new QuartierCsvColumn(config, "QUART8"),
            new SchulkreisCsvColumn(config, "Schulkreis_8"),
            new SchulhausnummerCsvColumn(config, "SCHUNR8"),
            new KlassenstufeCsvColumn(config, "Klassenstufe_8"),
            new KlassenbezeichnungCsvColumn(config, "Klassenbezeichnung_8"),
            new SchulartCsvColumn(config, "Schulart_8"),
            new LastUpdateImpfungenCsvColumn(config, "Tag_Erfassung_IK_8"),
            new KeinImpfausweisIsCheckedCsvColumn(config, "IMPFAUS8"),
            new KeinImpfausweisIsNotCheckedCsvColumn(config, "Mit_Impfausweis_8"),
            new ImpfausweisUnvollstaendingCsvColumn(config, "IMPFUNV8"),
            new ImpfausweisZweiPlusEinsSchemaCsvColumn(config, "NEUES_IS_8"),
            new ImpfungGeplantCsvColumn(config, "Impfung_geplant_8"),
            new KrankheitAnzahlImpfungenCsvColumn(config, "DIPHANZ8", Krankheit.DIPHTERIE),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "DIPHJAHR8", Krankheit.DIPHTERIE),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Diphtherie_Status_8", Krankheit.DIPHTERIE),
            new KrankheitImpfempfehlungCsvColumn(config, "Diphtherie_Impfempfehlung_8", Krankheit.DIPHTERIE),
            new KrankheitImpfentscheidCsvColumn(config, "Diphtherie_Impfentscheid_8", Krankheit.DIPHTERIE),
            new KrankheitImpfungVerabreichtCsvColumn(config, "DIPSÄD8", Krankheit.DIPHTERIE),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Diphtherie_Genügend_nach_Impf_8", Krankheit.DIPHTERIE),
            new KrankheitImpfstoffNameCsvColumn(config, "Diphtherie_Impfstoff_8", Krankheit.DIPHTERIE),
            new KrankheitAnzahlImpfungenCsvColumn(config, "TETAANZ8", Krankheit.TETANUS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "TETJAHR8", Krankheit.TETANUS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Tetanus_Status_8", Krankheit.TETANUS),
            new KrankheitImpfempfehlungCsvColumn(config, "Tetanus_Impfempfehlung_8", Krankheit.TETANUS),
            new KrankheitImpfentscheidCsvColumn(config, "Tetanus_Impfentscheid_8", Krankheit.TETANUS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "TETSÄD8", Krankheit.TETANUS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Tetanus_Genügend_nach_Impf_8", Krankheit.TETANUS),
            new KrankheitImpfstoffNameCsvColumn(config, "Tetanus_Impfstoff_8", Krankheit.TETANUS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "PERTANZ8", Krankheit.PERTUSSIS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "PERJAHR8", Krankheit.PERTUSSIS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Pertussis_Status_8", Krankheit.PERTUSSIS),
            new KrankheitImpfempfehlungCsvColumn(config, "Pertussis_Impfempfehlung_8", Krankheit.PERTUSSIS),
            new KrankheitImpfentscheidCsvColumn(config, "Pertussis_Impfentscheid_8", Krankheit.PERTUSSIS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "PERTSÄD8", Krankheit.PERTUSSIS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Pertussis_Genügend_nach_Impf_8", Krankheit.PERTUSSIS),
            new KrankheitImpfstoffNameCsvColumn(config, "Pertussis_Impfstoff_8", Krankheit.PERTUSSIS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "POLIOANZ8", Krankheit.POLIO),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "POLIOJAHR8", Krankheit.POLIO),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Polio_Status_8", Krankheit.POLIO),
            new KrankheitImpfempfehlungCsvColumn(config, "Polio_Impfempfehlung_8", Krankheit.POLIO),
            new KrankheitImpfentscheidCsvColumn(config, "Polio_Impfentscheid_8", Krankheit.POLIO),
            new KrankheitImpfungVerabreichtCsvColumn(config, "POLSÄD8", Krankheit.POLIO),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Polio_Genügend_nach_Impf_8", Krankheit.POLIO),
            new KrankheitImpfstoffNameCsvColumn(config, "Polio_Impfstoff_8", Krankheit.POLIO),
            new KrankheitAnzahlImpfungenCsvColumn(config, "HEPBANZ8", Krankheit.HEPATITIS_B),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "HEPBJAHR8", Krankheit.HEPATITIS_B),
            new KrankheitImpfschutzGegebenCsvColumn(config, "HepatitisB_Status_8", Krankheit.HEPATITIS_B),
            new KrankheitImpfempfehlungCsvColumn(config, "HepatitisB_Impfempfehlung_8", Krankheit.HEPATITIS_B),
            new KrankheitImpfentscheidCsvColumn(config, "HepatitisB_Impfentscheid_8", Krankheit.HEPATITIS_B),
            new KrankheitImpfungVerabreichtCsvColumn(config, "HepatitisB_Impfung_SÄD_8", Krankheit.HEPATITIS_B),
            new KrankheitImpfstatusGenugendCsvColumn(config, "HepatitisB_Genügend_nach_Impf_8", Krankheit.HEPATITIS_B),
            new KrankheitImpfstoffNameCsvColumn(config, "HepatitisB_Impfstoff_8", Krankheit.HEPATITIS_B),
            new KrankheitAnzahlImpfungenCsvColumn(config, "HPVANZ8", Krankheit.HPV),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "HPVJAHR8", Krankheit.HPV),
            new KrankheitImpfschutzGegebenCsvColumn(config, "HPV_Status_8", Krankheit.HPV),
            new KrankheitImpfempfehlungCsvColumn(config, "HPV_Impfempfehlung_8", Krankheit.HPV),
            new KrankheitImpfentscheidCsvColumn(config, "HPV_Impfentscheid_8", Krankheit.HPV),
            new KrankheitImpfungVerabreichtCsvColumn(config, "HPV_Impfung_SÄD_8", Krankheit.HPV),
            new KrankheitImpfstatusGenugendCsvColumn(config, "HPV_Genügend_nach_Impf_8", Krankheit.HPV),
            new KrankheitImpfstoffNameCsvColumn(config, "HPV_Impfstoff_8", Krankheit.HPV),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MASANZ8", Krankheit.MASERN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MASJAHR8", Krankheit.MASERN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Masern_Status_8", Krankheit.MASERN),
            new KrankheitImpfempfehlungCsvColumn(config, "Masern_Impfempfehlung_8", Krankheit.MASERN),
            new KrankheitImpfentscheidCsvColumn(config, "Masern_Impfentscheid_8", Krankheit.MASERN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MASSÄD8", Krankheit.MASERN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Masern_Genügend_nach_Impf_8", Krankheit.MASERN),
            new KrankheitImpfstoffNameCsvColumn(config, "Masern_Impfstoff_8", Krankheit.MASERN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MUMPANZ8", Krankheit.MUMPS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MUMPJAHR8", Krankheit.MUMPS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Mumps_Status_8", Krankheit.MUMPS),
            new KrankheitImpfempfehlungCsvColumn(config, "Mumps_Impfempfehlung_8", Krankheit.MUMPS),
            new KrankheitImpfentscheidCsvColumn(config, "Mumps_Impfentscheid_8", Krankheit.MUMPS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MUMSÄD8", Krankheit.MUMPS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Mumps_Genügend_nach_Impf_8", Krankheit.MUMPS),
            new KrankheitImpfstoffNameCsvColumn(config, "Mumps_Impfstoff_8", Krankheit.MUMPS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "ROETANZ8", Krankheit.ROETELN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "ROEJAHR8", Krankheit.ROETELN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Roeteln_Status_8", Krankheit.ROETELN),
            new KrankheitImpfempfehlungCsvColumn(config, "Roeteln_Impfempfehlung_8", Krankheit.ROETELN),
            new KrankheitImpfentscheidCsvColumn(config, "Roeteln_Impfentscheid_8", Krankheit.ROETELN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "ROESÄD8", Krankheit.ROETELN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Roeteln_Genügend_nach_Impf_8", Krankheit.ROETELN),
            new KrankheitImpfstoffNameCsvColumn(config, "Roeteln_Impfstoff_8", Krankheit.ROETELN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "VARIZ8", Krankheit.VARIZELLEN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "VARIZJAHR8", Krankheit.VARIZELLEN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Varizellen_Status_8", Krankheit.VARIZELLEN),
            new KrankheitImpfempfehlungCsvColumn(config, "Varizellen_Impfempfehlung_8", Krankheit.VARIZELLEN),
            new KrankheitImpfentscheidCsvColumn(config, "Varizellen_Impfentscheid_8", Krankheit.VARIZELLEN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "Varizellen_Impfung_SÄD_8", Krankheit.VARIZELLEN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Varizellen_Genügend_nach_Impf_8", Krankheit.VARIZELLEN),
            new KrankheitImpfstoffNameCsvColumn(config, "Varizellen_Impfstoff_8", Krankheit.VARIZELLEN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MENACWYANZ8", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MENACWYJAHR8", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfschutzGegebenCsvColumn(config, "MenACWY_Status_8", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfempfehlungCsvColumn(config, "MenACWY_Impfempfehlung_8", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfentscheidCsvColumn(config, "MenACWY_Impfentscheid_8", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MenACWY_Impfung_SÄD_8", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfstatusGenugendCsvColumn(
                config,
                "MenACWY_Genügend_nach_Impf_8",
                Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfstoffNameCsvColumn(config,"MenACWY_Impfstoff_8", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MENBANZ8", Krankheit.MENINGOKOKKEN_B),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MENBJAHR8", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfschutzGegebenCsvColumn(config, "MenB_Status_8", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfempfehlungCsvColumn(config, "MenB_Impfempfehlung_8", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfentscheidCsvColumn(config, "MenB_Impfentscheid_8", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MenB_Impfung_SÄD_8", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfstatusGenugendCsvColumn(config, "MenB_Genügend_nach_Impf_8", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfstoffNameCsvColumn(config, "MenB_Impfstoff_8", Krankheit.MENINGOKOKKEN_B),
            new ImpfberatungMPACsvColumn(config, "Impfberatung_MPA_8"),
            new ImpfenRueckfrageKinderarztCsvColumn(config, "Impfen_Rückfrage_Kinderarzt_8"),
            new ImpfenInfoKinderarztCsvColumn(config, "Impfen_Info_Kinderarzt_8"),
            new ImpfenInfoElternCsvColumn(config, "Impfen_Info_Eltern_8"),
            new BrilleLinsenCsvColumn(config, "BRILLE8"),
            new FarbsehenCsvColumn(config, "FARBSEH8"),
            new VerwendeteTafelCsvColumn(config, "Sehen_Tafel_8"),
            new FernvisusRechtsCsvColumn(config, "FVISRE8"),
            new FernvisusLinksCsvColumn(config, "FVISLI8"),
            new FernvisusCsvColumn(config, "FVNO8", DistanzSehenResultat.NORMAL),
            new FernvisusCsvColumn(config, "FVRRED8", DistanzSehenResultat.RECHTS_REDUZIERT),
            new FernvisusCsvColumn(config, "FVLRED8", DistanzSehenResultat.LINKS_REDUZIERT),
            new FernvisusCsvColumn(config, "FVNG8", DistanzSehenResultat.NICHT_GEPRUEFT),
            new NahvisusRechtsCsvColumn(config, "NVISRE8"),
            new NahvisusLinksCsvColumn(config, "NVISLI8"),
            new NahvisusCsvColumn(config, "NVNO8", DistanzSehenResultat.NORMAL),
            new NahvisusCsvColumn(config, "NVRRED8", DistanzSehenResultat.RECHTS_REDUZIERT),
            new NahvisusCsvColumn(config, "NVLRED8", DistanzSehenResultat.LINKS_REDUZIERT),
            new NahvisusCsvColumn(config, "NVNG8", DistanzSehenResultat.NICHT_GEPRUEFT),
            new AudiogrammRechts250HzCsvColumn(config, "AU250RE8"),
            new AudiogrammRechts500HzCsvColumn(config, "AU500RE8"),
            new AudiogrammRechts1000HzCsvColumn(config, "A1000RE8"),
            new AudiogrammRechts2000HzCsvColumn(config, "A2000RE8"),
            new AudiogrammRechts4000HzCsvColumn(config, "A4000RE8"),
            new AudiogrammRechts6000HzCsvColumn(config, "A6000RE8"),
            new AudiogrammRechts8000HzCsvColumn(config, "A8000RE8"),
            new AudiogrammLinks250HzCsvColumn(config, "AU250LI8"),
            new AudiogrammLinks500HzCsvColumn(config, "AU500LI8"),
            new AudiogrammLinks1000HzCsvColumn(config, "A1000LI8"),
            new AudiogrammLinks2000HzCsvColumn(config, "A2000LI8"),
            new AudiogrammLinks4000HzCsvColumn(config, "A4000LI8"),
            new AudiogrammLinks6000HzCsvColumn(config, "A6000LI8"),
            new AudiogrammLinks8000HzCsvColumn(config, "A8000LI8"),
            new HoerenBeurteilungCsvColumn(config, "GEHNO8", AudiogrammBeurteilung.NORMAL),
            new HoerenBeurteilungCsvColumn(config, "GEHRRED8", AudiogrammBeurteilung.RECHTS_REDUZIERT),
            new HoerenBeurteilungCsvColumn(config, "GEHLRED8", AudiogrammBeurteilung.LINKS_REDUZIERT),
            new HoerenBeurteilungCsvColumn(config, "GEHNG8", AudiogrammBeurteilung.NICHT_GEPRUEFT),
            new HoerenZusaetzlicheAngabenCsvColumn(config, "GEHERKÄL8", HoerenZusaetzlicheAngaben.ERKAELTUNG),
            new HoerenZusaetzlicheAngabenCsvColumn(config, "Ohr_Hoerhilfe_8", HoerenZusaetzlicheAngaben.HOERHILFE),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Normal_8", OtoskopieResultat.NORMAL),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Cerumen_8", OtoskopieResultat.TROMMELFELL_CERUMEN),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_TF_NichtEinsehbar_8",
                OtoskopieResultat.NICHT_EINSEHBAR),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_TF_Perforation_8",
                OtoskopieResultat.TROMMELFELL_PERFORATION),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_Seromukotympanon_8",
                OtoskopieResultat.SEROMUKOTYMPANON),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_Nicht_geprüft_8",
                OtoskopieResultat.NICHT_GEPRUEFT),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Anderes_8", OtoskopieResultat.ANDERES),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Normal_8", OtoskopieResultat.NORMAL),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Cerumen_8", OtoskopieResultat.TROMMELFELL_CERUMEN),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_TF_NichtEinsehbar_8",
                OtoskopieResultat.NICHT_EINSEHBAR),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_TF_Perforation_8",
                OtoskopieResultat.TROMMELFELL_PERFORATION),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_Seromukotympanon_8",
                OtoskopieResultat.SEROMUKOTYMPANON),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Nicht_geprüft_8", OtoskopieResultat.NICHT_GEPRUEFT),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Anderes_8", OtoskopieResultat.ANDERES),
            new GroesseWertCsvColumn(config, "GROESSE8"),
            new GroesseBeurteilungCsvColumn(config, "GROEBE8"),
            new GewichtWertCsvColumn(config, "GEWICHT8"),
            new GewichtBeurteilungCsvColumn(config, "GEWBE8"),
            new BMIWertCsvColumn(config, "BMI8"),
            new BMIBeurteilungCsvColumn(config, "BMIBE8"),
            new TriageLehrpersonCsvColumn(config, "Triage_Lehrperson_8"),
            new TriageSuCsvColumn(config, "Triage_SÄ_FB_8"),
            new TriageMpaCsvColumn(config, "Triage_MPA_8"),
            new TriageBefundSuCsvColumn(config, "Triage_Befund_SU_8"),
            new TriageWunschSchuelerCsvColumn(config, "Triage_Wunsch_SuS_8"),
            new MassnahmenKeineCsvColumn(config, "MNKEIN8"),
            new MassnahmenInformationElternCsvColumn(config, "MNINFE8"),
            new MassnahmenAbgegebenesInfomaterialCsvColumn(config, "MNINFOM8"),
            new MassnahmenAnderesCsvColumn(config, "MNANDE8"),
            new MassnahmenUeberweisungHausarztCsvColumn(config, "Massnahmen_HA_KA_8"),
            new MassnahmenUeberweisungAugenarztCsvColumn(config, "UEBERWAU8"),
            new MassnahmenBereitsBehandeltSelectedCsvColumn(config, "MNBEREI8"),
            new MassnahmenBereitsBehandeltSehenCsvColumn(config, "BERBEHAU8"),
            new MassnahmenBereitsBehandeltHoerenCsvColumn(config, "BERBEHKO8"),
            new MassnahmenBereitsBehandeltGroesseGewichtCsvColumn(config, "BERBEHGE8"),
            new MassnahmenBereitsBehandeltSpracheCsvColumn(config, "BERBEHSP8"),
            new MassnahmenBereitsBehandeltMotorikCsvColumn(config, "BERBEHMO8"),
            new MassnahmenBereitsBehandeltAnderesCsvColumn(config, "BERBEHAN8"),
            new MassnahmenNachkontrolleSelectedCsvColumn(config, "MNSÄD8"),
            new MassnahmenNachkontrolleSehenCsvColumn(config, "SÄDAUG8"),
            new MassnahmenNachkontrolleHoerenCsvColumn(config, "SÄDOHR8"),
            new MassnahmenNachkontrolleGroesseGewichtCsvColumn(config, "SÄDGEW8"),
            new MassnahmenNachkontrolleSpracheCsvColumn(config, "SÄDSPR8"),
            new MassnahmenNachkontrolleMotorikCsvColumn(config, "SÄDMOTEN8"),
            new MassnahmenNachkontrolleAnderesCsvColumn(config, "SÄDANDGR8")
        );
    }
}
