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
public class SpssKlasse4Report {
    private final CsvColumnConfig config;

    public List<AbstractCsvColumn> getReport() {
        return List.of(
            new AnlassTypCsvColumn(config, "ANLASSTYP4"),
            new IdNrCsvColumn(config, "ID_NR_4"),
            new JahrSuCsvColumn(config, "J3_SU_4"),
            new HasNoFragebogenCsvColumn(config, "KEINEF4"),
            new GeschlechtCsvColumn(config, "GESCHLE4"),
            new GeburtsTagCsvColumn(config, "GEBTAG4"),
            new GeburtsMonatCsvColumn(config, "GEBMON4"),
            new GeburtsJahrCsvColumn(config, "GEBJAHR4"),
            new UntersuchungTagCsvColumn(config, "UNTTAG4"),
            new UntersuchungMonatCsvColumn(config, "UNTMON4"),
            new UntersuchungJahrCsvColumn(config, "UNTJAHR4"),
            new AlterCsvColumn(config, "Alter_4"),
            new LandCodeCsvColumn(config, "NATION4"),
            new MpaCsvColumn(config, "MPA4"),
            new AerztinCsvColumn(config, "AERZTIN4"),
            new NachkontrolleCsvColumn(config, "Nachkontrolle_4"),
            new NachzuglerCsvColumn(config, "Nachzügler_4"),
            new NeueintrittCsvColumn(config, "Neueintritt_4"),
            new SchulhausnameCsvColumn(config, "Schulhausname_4"),
            new QuartierCsvColumn(config, "QUART4"),
            new SchulkreisCsvColumn(config, "Schulkreis_4"),
            new SchulhausnummerCsvColumn(config, "SCHUNR4"),
            new KlassenstufeCsvColumn(config, "Klassenstufe_4"),
            new KlassenbezeichnungCsvColumn(config, "Klassenbezeichnung_4"),
            new SchulartCsvColumn(config, "Schulart_4"),
            new LastUpdateImpfungenCsvColumn(config, "Tag_Erfassung_IK_4"),
            new KeinImpfausweisIsCheckedCsvColumn(config, "IMPFAUS4"),
            new KeinImpfausweisIsNotCheckedCsvColumn(config, "Mit_Impfausweis_4"),
            new ImpfausweisUnvollstaendingCsvColumn(config, "IMPFUNV4"),
            new ImpfausweisZweiPlusEinsSchemaCsvColumn(config, "NEUES_IS_4"),
            new ImpfungGeplantCsvColumn(config, "Impfung_geplant_4"),
            new KrankheitAnzahlImpfungenCsvColumn(config, "DIPHANZ4", Krankheit.DIPHTERIE),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "DIPHJAHR4", Krankheit.DIPHTERIE),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Diphtherie_Status_4", Krankheit.DIPHTERIE),
            new KrankheitImpfempfehlungCsvColumn(config, "Diphtherie_Impfempfehlung_4", Krankheit.DIPHTERIE),
            new KrankheitImpfentscheidCsvColumn(config, "Diphtherie_Impfentscheid_4", Krankheit.DIPHTERIE),
            new KrankheitImpfungVerabreichtCsvColumn(config, "DIPSÄD4", Krankheit.DIPHTERIE),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Diphtherie_Genügend_nach_Impf_4", Krankheit.DIPHTERIE),
            new KrankheitImpfstoffNameCsvColumn(config, "Diphtherie_Impfstoff_4", Krankheit.DIPHTERIE),
            new KrankheitAnzahlImpfungenCsvColumn(config, "TETAANZ4", Krankheit.TETANUS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "TETJAHR4", Krankheit.TETANUS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Tetanus_Status_4", Krankheit.TETANUS),
            new KrankheitImpfempfehlungCsvColumn(config, "Tetanus_Impfempfehlung_4", Krankheit.TETANUS),
            new KrankheitImpfentscheidCsvColumn(config, "Tetanus_Impfentscheid_4", Krankheit.TETANUS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "TETSÄD4", Krankheit.TETANUS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Tetanus_Genügend_nach_Impf_4", Krankheit.TETANUS),
            new KrankheitImpfstoffNameCsvColumn(config, "Tetanus_Impfstoff_4", Krankheit.TETANUS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "PERTANZ4", Krankheit.PERTUSSIS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "PERJAHR4", Krankheit.PERTUSSIS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Pertussis_Status_4", Krankheit.PERTUSSIS),
            new KrankheitImpfempfehlungCsvColumn(config, "Pertussis_Impfempfehlung_4", Krankheit.PERTUSSIS),
            new KrankheitImpfentscheidCsvColumn(config, "Pertussis_Impfentscheid_4", Krankheit.PERTUSSIS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "PERTSÄD4", Krankheit.PERTUSSIS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Pertussis_Genügend_nach_Impf_4", Krankheit.PERTUSSIS),
            new KrankheitImpfstoffNameCsvColumn(config, "Pertussis_Impfstoff_4", Krankheit.PERTUSSIS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "POLIOANZ4", Krankheit.POLIO),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "POLIOJAHR4", Krankheit.POLIO),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Polio_Status_4", Krankheit.POLIO),
            new KrankheitImpfempfehlungCsvColumn(config, "Polio_Impfempfehlung_4", Krankheit.POLIO),
            new KrankheitImpfentscheidCsvColumn(config, "Polio_Impfentscheid_4", Krankheit.POLIO),
            new KrankheitImpfungVerabreichtCsvColumn(config, "POLSÄD4", Krankheit.POLIO),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Polio_Genügend_nach_Impf_4", Krankheit.POLIO),
            new KrankheitImpfstoffNameCsvColumn(config, "Polio_Impfstoff_4", Krankheit.POLIO),
            new KrankheitAnzahlImpfungenCsvColumn(config, "HEPBANZ4", Krankheit.HEPATITIS_B),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "HEPBJAHR4", Krankheit.HEPATITIS_B),
            new KrankheitImpfschutzGegebenCsvColumn(config, "HepatitisB_Status_4", Krankheit.HEPATITIS_B),
            new KrankheitImpfempfehlungCsvColumn(config, "HepatitisB_Impfempfehlung_4", Krankheit.HEPATITIS_B),
            new KrankheitImpfentscheidCsvColumn(config, "HepatitisB_Impfentscheid_4", Krankheit.HEPATITIS_B),
            new KrankheitImpfungVerabreichtCsvColumn(config, "HepatitisB_Impfung_SÄD_4", Krankheit.HEPATITIS_B),
            new KrankheitImpfstatusGenugendCsvColumn(config, "HepatitisB_Genügend_nach_Impf_4", Krankheit.HEPATITIS_B),
            new KrankheitImpfstoffNameCsvColumn(config, "HepatitisB_Impfstoff_4", Krankheit.HEPATITIS_B),
            new KrankheitAnzahlImpfungenCsvColumn(config, "HPVANZ4", Krankheit.HPV),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "HPVJAHR4", Krankheit.HPV),
            new KrankheitImpfschutzGegebenCsvColumn(config, "HPV_Status_4", Krankheit.HPV),
            new KrankheitImpfempfehlungCsvColumn(config, "HPV_Impfempfehlung_4", Krankheit.HPV),
            new KrankheitImpfentscheidCsvColumn(config, "HPV_Impfentscheid_4", Krankheit.HPV),
            new KrankheitImpfungVerabreichtCsvColumn(config, "HPV_Impfung_SÄD_4", Krankheit.HPV),
            new KrankheitImpfstatusGenugendCsvColumn(config, "HPV_Genügend_nach_Impf_4", Krankheit.HPV),
            new KrankheitImpfstoffNameCsvColumn(config, "HPV_Impfstoff_4", Krankheit.HPV),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MASANZ4", Krankheit.MASERN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MASJAHR4", Krankheit.MASERN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Masern_Status_4", Krankheit.MASERN),
            new KrankheitImpfempfehlungCsvColumn(config, "Masern_Impfempfehlung_4", Krankheit.MASERN),
            new KrankheitImpfentscheidCsvColumn(config, "Masern_Impfentscheid_4", Krankheit.MASERN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MASSÄD4", Krankheit.MASERN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Masern_Genügend_nach_Impf_4", Krankheit.MASERN),
            new KrankheitImpfstoffNameCsvColumn(config, "Masern_Impfstoff_4", Krankheit.MASERN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MUMPANZ4", Krankheit.MUMPS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MUMPJAHR4", Krankheit.MUMPS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Mumps_Status_4", Krankheit.MUMPS),
            new KrankheitImpfempfehlungCsvColumn(config, "Mumps_Impfempfehlung_4", Krankheit.MUMPS),
            new KrankheitImpfentscheidCsvColumn(config, "Mumps_Impfentscheid_4", Krankheit.MUMPS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MUMSÄD4", Krankheit.MUMPS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Mumps_Genügend_nach_Impf_4", Krankheit.MUMPS),
            new KrankheitImpfstoffNameCsvColumn(config, "Mumps_Impfstoff_4", Krankheit.MUMPS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "ROETANZ4", Krankheit.ROETELN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "ROEJAHR4", Krankheit.ROETELN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Roeteln_Status_4", Krankheit.ROETELN),
            new KrankheitImpfempfehlungCsvColumn(config, "Roeteln_Impfempfehlung_4", Krankheit.ROETELN),
            new KrankheitImpfentscheidCsvColumn(config, "Roeteln_Impfentscheid_4", Krankheit.ROETELN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "ROESÄD4", Krankheit.ROETELN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Roeteln_Genügend_nach_Impf_4", Krankheit.ROETELN),
            new KrankheitImpfstoffNameCsvColumn(config, "Roeteln_Impfstoff_4", Krankheit.ROETELN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "VARIZ4", Krankheit.VARIZELLEN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "VARIZJAHR4", Krankheit.VARIZELLEN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Varizellen_Status_4", Krankheit.VARIZELLEN),
            new KrankheitImpfempfehlungCsvColumn(config, "Varizellen_Impfempfehlung_4", Krankheit.VARIZELLEN),
            new KrankheitImpfentscheidCsvColumn(config, "Varizellen_Impfentscheid_4", Krankheit.VARIZELLEN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "Varizellen_Impfung_SÄD_4", Krankheit.VARIZELLEN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Varizellen_Genügend_nach_Impf_4", Krankheit.VARIZELLEN),
            new KrankheitImpfstoffNameCsvColumn(config, "Varizellen_Impfstoff_4", Krankheit.VARIZELLEN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MENACWYANZ4", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MENACWYJAHR4", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfschutzGegebenCsvColumn(config, "MenACWY_Status_4", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfempfehlungCsvColumn(config, "MenACWY_Impfempfehlung_4", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfentscheidCsvColumn(config, "MenACWY_Impfentscheid_4", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MenACWY_Impfung_SÄD_4", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfstatusGenugendCsvColumn(
                config,
                "MenACWY_Genügend_nach_Impf_4",
                Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfstoffNameCsvColumn(config, "MenACWY_Impfstoff_4", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MENBANZ4", Krankheit.MENINGOKOKKEN_B),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MENBJAHR4", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfschutzGegebenCsvColumn(config, "MenB_Status_4", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfempfehlungCsvColumn(config, "MenB_Impfempfehlung_4", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfentscheidCsvColumn(config, "MenB_Impfentscheid_4", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MenB_Impfung_SÄD_4", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfstatusGenugendCsvColumn(config, "MenB_Genügend_nach_Impf_4", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfstoffNameCsvColumn(config, "MenB_Impfstoff_4", Krankheit.MENINGOKOKKEN_B),
            new ImpfberatungMPACsvColumn(config, "Impfberatung_MPA_4"),
            new ImpfenRueckfrageKinderarztCsvColumn(config, "Impfen_Rückfrage_Kinderarzt_4"),
            new ImpfenInfoKinderarztCsvColumn(config, "Impfen_Info_Kinderarzt_4"),
            new ImpfenInfoElternCsvColumn(config, "Impfen_Info_Eltern_4"),
            new BrilleLinsenCsvColumn(config, "BRILLE4"),
            new FarbsehenCsvColumn(config, "FARBSEH4"),
            new VerwendeteTafelCsvColumn(config, "Sehen_Tafel_4"),
            new FernvisusRechtsCsvColumn(config, "FVISRE4"),
            new FernvisusLinksCsvColumn(config, "FVISLI4"),
            new FernvisusCsvColumn(config, "FVNO4", DistanzSehenResultat.NORMAL),
            new FernvisusCsvColumn(config, "FVRRED4", DistanzSehenResultat.RECHTS_REDUZIERT),
            new FernvisusCsvColumn(config, "FVLRED4", DistanzSehenResultat.LINKS_REDUZIERT),
            new FernvisusCsvColumn(config, "FVNG4", DistanzSehenResultat.NICHT_GEPRUEFT),
            new NahvisusRechtsCsvColumn(config, "NVISRE4"),
            new NahvisusLinksCsvColumn(config, "NVISLI4"),
            new NahvisusCsvColumn(config, "NVNO4", DistanzSehenResultat.NORMAL),
            new NahvisusCsvColumn(config, "NVRRED4", DistanzSehenResultat.RECHTS_REDUZIERT),
            new NahvisusCsvColumn(config, "NVLRED4", DistanzSehenResultat.LINKS_REDUZIERT),
            new NahvisusCsvColumn(config, "NVNG4", DistanzSehenResultat.NICHT_GEPRUEFT),
            new AudiogrammRechts250HzCsvColumn(config, "AU250RE4"),
            new AudiogrammRechts500HzCsvColumn(config, "AU500RE4"),
            new AudiogrammRechts1000HzCsvColumn(config, "A1000RE4"),
            new AudiogrammRechts2000HzCsvColumn(config, "A2000RE4"),
            new AudiogrammRechts4000HzCsvColumn(config, "A4000RE4"),
            new AudiogrammRechts6000HzCsvColumn(config, "A6000RE4"),
            new AudiogrammRechts8000HzCsvColumn(config, "A8000RE4"),
            new AudiogrammLinks250HzCsvColumn(config, "AU250LI4"),
            new AudiogrammLinks500HzCsvColumn(config, "AU500LI4"),
            new AudiogrammLinks1000HzCsvColumn(config, "A1000LI4"),
            new AudiogrammLinks2000HzCsvColumn(config, "A2000LI4"),
            new AudiogrammLinks4000HzCsvColumn(config, "A4000LI4"),
            new AudiogrammLinks6000HzCsvColumn(config, "A6000LI4"),
            new AudiogrammLinks8000HzCsvColumn(config, "A8000LI4"),
            new HoerenBeurteilungCsvColumn(config, "GEHNO4", AudiogrammBeurteilung.NORMAL),
            new HoerenBeurteilungCsvColumn(config, "GEHRRED4", AudiogrammBeurteilung.RECHTS_REDUZIERT),
            new HoerenBeurteilungCsvColumn(config, "GEHLRED4", AudiogrammBeurteilung.LINKS_REDUZIERT),
            new HoerenBeurteilungCsvColumn(config, "GEHNG4", AudiogrammBeurteilung.NICHT_GEPRUEFT),
            new HoerenZusaetzlicheAngabenCsvColumn(config, "GEHERKÄL4", HoerenZusaetzlicheAngaben.ERKAELTUNG),
            new HoerenZusaetzlicheAngabenCsvColumn(config, "Ohr_Hoerhilfe_4", HoerenZusaetzlicheAngaben.HOERHILFE),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Normal_4", OtoskopieResultat.NORMAL),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Cerumen_4", OtoskopieResultat.TROMMELFELL_CERUMEN),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_TF_NichtEinsehbar_4",
                OtoskopieResultat.NICHT_EINSEHBAR),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_TF_Perforation_4",
                OtoskopieResultat.TROMMELFELL_PERFORATION),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_Seromukotympanon_4",
                OtoskopieResultat.SEROMUKOTYMPANON),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_Nicht_geprüft_4",
                OtoskopieResultat.NICHT_GEPRUEFT),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Anderes_4", OtoskopieResultat.ANDERES),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Normal_4", OtoskopieResultat.NORMAL),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Cerumen_4", OtoskopieResultat.TROMMELFELL_CERUMEN),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_TF_NichtEinsehbar_4",
                OtoskopieResultat.NICHT_EINSEHBAR),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_TF_Perforation_4",
                OtoskopieResultat.TROMMELFELL_PERFORATION),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_Seromukotympanon_4",
                OtoskopieResultat.SEROMUKOTYMPANON),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Nicht_geprüft_4", OtoskopieResultat.NICHT_GEPRUEFT),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Anderes_4", OtoskopieResultat.ANDERES),
            new GroesseWertCsvColumn(config, "GROESSE4"),
            new GroesseBeurteilungCsvColumn(config, "GROEBE4"),
            new GewichtWertCsvColumn(config, "GEWICHT4"),
            new GewichtBeurteilungCsvColumn(config, "GEWBE4"),
            new BMIWertCsvColumn(config, "BMI4"),
            new BMIBeurteilungCsvColumn(config, "BMIBE4"),
            new TriageLehrpersonCsvColumn(config, "Triage_Lehrperson_4"),
            new TriageSuCsvColumn(config, "Triage_SÄ_FB_4"),
            new TriageMpaCsvColumn(config, "Triage_MPA_4"),
            new TriageBefundSuCsvColumn(config, "Triage_Befund_SU_4"),
            new TriageWunschSchuelerCsvColumn(config, "Triage_Wunsch_SuS_4"),
            new MassnahmenKeineCsvColumn(config, "MNKEIN4"),
            new MassnahmenInformationElternCsvColumn(config, "MNINFE4"),
            new MassnahmenAbgegebenesInfomaterialCsvColumn(config, "MNINFOM4"),
            new MassnahmenAnderesCsvColumn(config, "MNANDE4"),
            new MassnahmenUeberweisungHausarztCsvColumn(config, "Massnahmen_HA_KA_4"),
            new MassnahmenUeberweisungAugenarztCsvColumn(config, "UEBERWAU4"),
            new MassnahmenBereitsBehandeltSelectedCsvColumn(config, "MNBEREI4"),
            new MassnahmenBereitsBehandeltSehenCsvColumn(config, "BERBEHAU4"),
            new MassnahmenBereitsBehandeltHoerenCsvColumn(config, "BERBEHKO4"),
            new MassnahmenBereitsBehandeltGroesseGewichtCsvColumn(config, "BERBEHGE4"),
            new MassnahmenBereitsBehandeltSpracheCsvColumn(config, "BERBEHSP4"),
            new MassnahmenBereitsBehandeltMotorikCsvColumn(config, "BERBEHMO4"),
            new MassnahmenBereitsBehandeltAnderesCsvColumn(config, "BERBEHAN4"),
            new MassnahmenNachkontrolleSelectedCsvColumn(config, "MNSÄD4"),
            new MassnahmenNachkontrolleSehenCsvColumn(config, "SÄDAUG4"),
            new MassnahmenNachkontrolleHoerenCsvColumn(config, "SÄDOHR4"),
            new MassnahmenNachkontrolleGroesseGewichtCsvColumn(config, "SÄDGEW4"),
            new MassnahmenNachkontrolleSpracheCsvColumn(config, "SÄDSPR4"),
            new MassnahmenNachkontrolleMotorikCsvColumn(config, "SÄDMOTEN4"),
            new MassnahmenNachkontrolleAnderesCsvColumn(config, "SÄDANDGR4")
        );
    }
}
