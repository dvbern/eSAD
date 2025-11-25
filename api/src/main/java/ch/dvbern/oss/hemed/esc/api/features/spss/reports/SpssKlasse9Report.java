package ch.dvbern.oss.hemed.esc.api.features.spss.reports;

import java.util.List;

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
public class SpssKlasse9Report {
    private final CsvColumnConfig config;

    public List<AbstractCsvColumn> getReport() {
        return List.of(
            new AnlassTypCsvColumn(config, "ANLASSTYP9"),
            new IdNrCsvColumn(config, "ID_NR_9"),
            new JahrSuCsvColumn(config, "J5_SU_9"),
            new HasNoFragebogenCsvColumn(config, "KEINEF9"),
            new GeschlechtCsvColumn(config, "GESCHLE9"),
            new GeburtsTagCsvColumn(config, "GEBTAG9"),
            new GeburtsMonatCsvColumn(config, "GEBMON9"),
            new GeburtsJahrCsvColumn(config, "GEBJAHR9"),
            new UntersuchungTagCsvColumn(config, "UNTTAG9"),
            new UntersuchungMonatCsvColumn(config, "UNTMON9"),
            new UntersuchungJahrCsvColumn(config, "UNTJAHR9"),
            new AlterCsvColumn(config, "Alter_9"),
            new LandCodeCsvColumn(config, "NATION9"),
            new MpaCsvColumn(config, "MPA9"),
            new AerztinCsvColumn(config, "AERZTIN9"),
            new NachkontrolleCsvColumn(config, "Nachkontrolle_9"),
            new NachzuglerCsvColumn(config, "Nachzügler_9"),
            new NeueintrittCsvColumn(config, "Neueintritt_9"),
            new SchulhausnameCsvColumn(config, "Schulhausname_9"),
            new QuartierCsvColumn(config, "QUART9"),
            new SchulkreisCsvColumn(config, "Schulkreis_9"),
            new SchulhausnummerCsvColumn(config, "SCHUNR9"),
            new KlassenstufeCsvColumn(config, "Klassenstufe_9"),
            new KlassenbezeichnungCsvColumn(config, "Klassenbezeichnung_9"),
            new SchulartCsvColumn(config, "Schulart_9"),
            new LastUpdateImpfungenCsvColumn(config, "Tag_Erfassung_IK_9"),
            new KeinImpfausweisIsCheckedCsvColumn(config, "IMPFAUS9"),
            new KeinImpfausweisIsNotCheckedCsvColumn(config, "Mit_Impfausweis_9"),
            new ImpfausweisUnvollstaendingCsvColumn(config, "IMPFUNV9"),
            new ImpfausweisZweiPlusEinsSchemaCsvColumn(config, "NEUES_IS_9"),
            new ImpfungGeplantCsvColumn(config, "Impfung_geplant_9"),
            new KrankheitAnzahlImpfungenCsvColumn(config, "DIPHANZ9", Krankheit.DIPHTERIE),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "DIPHJAHR9", Krankheit.DIPHTERIE),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Diphtherie_Status_9", Krankheit.DIPHTERIE),
            new KrankheitImpfempfehlungCsvColumn(config, "Diphtherie_Impfempfehlung_9", Krankheit.DIPHTERIE),
            new KrankheitImpfentscheidCsvColumn(config, "Diphtherie_Impfentscheid_9", Krankheit.DIPHTERIE),
            new KrankheitImpfungVerabreichtCsvColumn(config, "DIPSÄD9", Krankheit.DIPHTERIE),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Diphtherie_Genügend_nach_Impf_9", Krankheit.DIPHTERIE),
            new KrankheitImpfstoffNameCsvColumn(config, "Diphtherie_Impfstoff_9", Krankheit.DIPHTERIE),
            new KrankheitAnzahlImpfungenCsvColumn(config, "TETAANZ9", Krankheit.TETANUS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "TETJAHR9", Krankheit.TETANUS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Tetanus_Status_9", Krankheit.TETANUS),
            new KrankheitImpfempfehlungCsvColumn(config, "Tetanus_Impfempfehlung_9", Krankheit.TETANUS),
            new KrankheitImpfentscheidCsvColumn(config, "Tetanus_Impfentscheid_9", Krankheit.TETANUS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "TETSÄD9", Krankheit.TETANUS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Tetanus_Genügend_nach_Impf_9", Krankheit.TETANUS),
            new KrankheitImpfstoffNameCsvColumn(config, "Tetanus_Impfstoff_9", Krankheit.TETANUS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "PERTANZ9", Krankheit.PERTUSSIS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "PERJAHR9", Krankheit.PERTUSSIS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Pertussis_Status_9", Krankheit.PERTUSSIS),
            new KrankheitImpfempfehlungCsvColumn(config, "Pertussis_Impfempfehlung_9", Krankheit.PERTUSSIS),
            new KrankheitImpfentscheidCsvColumn(config, "Pertussis_Impfentscheid_9", Krankheit.PERTUSSIS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "PERTSÄD9", Krankheit.PERTUSSIS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Pertussis_Genügend_nach_Impf_9", Krankheit.PERTUSSIS),
            new KrankheitImpfstoffNameCsvColumn(config, "Pertussis_Impfstoff_9", Krankheit.PERTUSSIS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "POLIOANZ9", Krankheit.POLIO),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "POLIOJAHR9", Krankheit.POLIO),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Polio_Status_9", Krankheit.POLIO),
            new KrankheitImpfempfehlungCsvColumn(config, "Polio_Impfempfehlung_9", Krankheit.POLIO),
            new KrankheitImpfentscheidCsvColumn(config, "Polio_Impfentscheid_9", Krankheit.POLIO),
            new KrankheitImpfungVerabreichtCsvColumn(config, "POLSÄD9", Krankheit.POLIO),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Polio_Genügend_nach_Impf_9", Krankheit.POLIO),
            new KrankheitImpfstoffNameCsvColumn(config, "Polio_Impfstoff_9", Krankheit.POLIO),
            new KrankheitAnzahlImpfungenCsvColumn(config, "HEPBANZ9", Krankheit.HEPATITIS_B),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "HEPBJAHR9", Krankheit.HEPATITIS_B),
            new KrankheitImpfschutzGegebenCsvColumn(config, "HepatitisB_Status_9", Krankheit.HEPATITIS_B),
            new KrankheitImpfempfehlungCsvColumn(config, "HepatitisB_Impfempfehlung_9", Krankheit.HEPATITIS_B),
            new KrankheitImpfentscheidCsvColumn(config, "HepatitisB_Impfentscheid_9", Krankheit.HEPATITIS_B),
            new KrankheitImpfungVerabreichtCsvColumn(config, "HepatitisB_Impfung_SÄD_9", Krankheit.HEPATITIS_B),
            new KrankheitImpfstatusGenugendCsvColumn(config, "HepatitisB_Genügend_nach_Impf_9", Krankheit.HEPATITIS_B),
            new KrankheitImpfstoffNameCsvColumn(config, "HepatitisB_Impfstoff_9", Krankheit.HEPATITIS_B),
            new KrankheitAnzahlImpfungenCsvColumn(config, "HPVANZ9", Krankheit.HPV),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "HPVJAHR9", Krankheit.HPV),
            new KrankheitImpfschutzGegebenCsvColumn(config, "HPV_Status_9", Krankheit.HPV),
            new KrankheitImpfempfehlungCsvColumn(config, "HPV_Impfempfehlung_9", Krankheit.HPV),
            new KrankheitImpfentscheidCsvColumn(config, "HPV_Impfentscheid_9", Krankheit.HPV),
            new KrankheitImpfungVerabreichtCsvColumn(config, "HPV_Impfung_SÄD_9", Krankheit.HPV),
            new KrankheitImpfstatusGenugendCsvColumn(config, "HPV_Genügend_nach_Impf_9", Krankheit.HPV),
            new KrankheitImpfstoffNameCsvColumn(config, "HPV_Impfstoff_9", Krankheit.HPV),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MASANZ9", Krankheit.MASERN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MASJAHR9", Krankheit.MASERN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Masern_Status_9", Krankheit.MASERN),
            new KrankheitImpfempfehlungCsvColumn(config, "Masern_Impfempfehlung_9", Krankheit.MASERN),
            new KrankheitImpfentscheidCsvColumn(config, "Masern_Impfentscheid_9", Krankheit.MASERN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MASSÄD9", Krankheit.MASERN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Masern_Genügend_nach_Impf_9", Krankheit.MASERN),
            new KrankheitImpfstoffNameCsvColumn(config, "Masern_Impfstoff_9", Krankheit.MASERN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MUMPANZ9", Krankheit.MUMPS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MUMPJAHR9", Krankheit.MUMPS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Mumps_Status_9", Krankheit.MUMPS),
            new KrankheitImpfempfehlungCsvColumn(config, "Mumps_Impfempfehlung_9", Krankheit.MUMPS),
            new KrankheitImpfentscheidCsvColumn(config, "Mumps_Impfentscheid_9", Krankheit.MUMPS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MUMSÄD9", Krankheit.MUMPS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Mumps_Genügend_nach_Impf_9", Krankheit.MUMPS),
            new KrankheitImpfstoffNameCsvColumn(config, "Mumps_Impfstoff_9", Krankheit.MUMPS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "ROETANZ9", Krankheit.ROETELN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "ROEJAHR9", Krankheit.ROETELN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Roeteln_Status_9", Krankheit.ROETELN),
            new KrankheitImpfempfehlungCsvColumn(config, "Roeteln_Impfempfehlung_9", Krankheit.ROETELN),
            new KrankheitImpfentscheidCsvColumn(config, "Roeteln_Impfentscheid_9", Krankheit.ROETELN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "ROESÄD9", Krankheit.ROETELN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Roeteln_Genügend_nach_Impf_9", Krankheit.ROETELN),
            new KrankheitImpfstoffNameCsvColumn(config, "Roeteln_Impfstoff_9", Krankheit.ROETELN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "VARIZ9", Krankheit.VARIZELLEN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "VARIZJAHR9", Krankheit.VARIZELLEN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Varizellen_Status_9", Krankheit.VARIZELLEN),
            new KrankheitImpfempfehlungCsvColumn(config, "Varizellen_Impfempfehlung_9", Krankheit.VARIZELLEN),
            new KrankheitImpfentscheidCsvColumn(config, "Varizellen_Impfentscheid_9", Krankheit.VARIZELLEN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "Varizellen_Impfung_SÄD_9", Krankheit.VARIZELLEN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Varizellen_Genügend_nach_Impf_9", Krankheit.VARIZELLEN),
            new KrankheitImpfstoffNameCsvColumn(config, "Varizellen_Impfstoff_9", Krankheit.VARIZELLEN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MENACWYANZ9", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MENACWYJAHR9", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfschutzGegebenCsvColumn(config, "MenACWY_Status_9", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfempfehlungCsvColumn(config, "MenACWY_Impfempfehlung_9", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfentscheidCsvColumn(config, "MenACWY_Impfentscheid_9", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MenACWY_Impfung_SÄD_9", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfstatusGenugendCsvColumn(
                config,
                "MenACWY_Genügend_nach_Impf_9",
                Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfstoffNameCsvColumn(config, "MenACWY_Impfstoff_9", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MENBANZ9", Krankheit.MENINGOKOKKEN_B),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MENBJAHR9", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfschutzGegebenCsvColumn(config, "MenB_Status_9", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfempfehlungCsvColumn(config, "MenB_Impfempfehlung_9", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfentscheidCsvColumn(config, "MenB_Impfentscheid_9", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MenB_Impfung_SÄD_9", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfstatusGenugendCsvColumn(config, "MenB_Genügend_nach_Impf_9", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfstoffNameCsvColumn(config, "MenB_Impfstoff_9", Krankheit.MENINGOKOKKEN_B),
            new ImpfberatungMPACsvColumn(config, "Impfberatung_MPA_9"),
            new ImpfenRueckfrageKinderarztCsvColumn(config, "Impfen_Rückfrage_Kinderarzt_9"),
            new ImpfenInfoKinderarztCsvColumn(config, "Impfen_Info_Kinderarzt_9"),
            new ImpfenInfoElternCsvColumn(config, "Impfen_Info_Eltern_9"),
            new BrilleLinsenCsvColumn(config, "BRILLE9"),
            new FarbsehenCsvColumn(config, "FARBSEH9"),
            new VerwendeteTafelCsvColumn(config, "Sehen_Tafel_9"),
            new FernvisusRechtsCsvColumn(config, "FVISRE9"),
            new FernvisusLinksCsvColumn(config, "FVISLI9"),
            new FernvisusCsvColumn(config, "FVNO9", DistanzSehenResultat.NORMAL),
            new FernvisusCsvColumn(config, "FVRRED9", DistanzSehenResultat.RECHTS_REDUZIERT),
            new FernvisusCsvColumn(config, "FVLRED9", DistanzSehenResultat.LINKS_REDUZIERT),
            new FernvisusCsvColumn(config, "FVNG9", DistanzSehenResultat.NICHT_GEPRUEFT),
            new NahvisusRechtsCsvColumn(config, "NVISRE9"),
            new NahvisusLinksCsvColumn(config, "NVISLI9"),
            new NahvisusCsvColumn(config, "NVNO9", DistanzSehenResultat.NORMAL),
            new NahvisusCsvColumn(config, "NVRRED9", DistanzSehenResultat.RECHTS_REDUZIERT),
            new NahvisusCsvColumn(config, "NVLRED9", DistanzSehenResultat.LINKS_REDUZIERT),
            new NahvisusCsvColumn(config, "NVNG9", DistanzSehenResultat.NICHT_GEPRUEFT),
            new AudiogrammRechts250HzCsvColumn(config, "AU250RE9"),
            new AudiogrammRechts500HzCsvColumn(config, "AU500RE9"),
            new AudiogrammRechts1000HzCsvColumn(config, "A1000RE9"),
            new AudiogrammRechts2000HzCsvColumn(config, "A2000RE9"),
            new AudiogrammRechts4000HzCsvColumn(config, "A4000RE9"),
            new AudiogrammRechts6000HzCsvColumn(config, "A6000RE9"),
            new AudiogrammRechts8000HzCsvColumn(config, "A8000RE9"),
            new AudiogrammLinks250HzCsvColumn(config, "AU250LI9"),
            new AudiogrammLinks500HzCsvColumn(config, "AU500LI9"),
            new AudiogrammLinks1000HzCsvColumn(config, "A1000LI9"),
            new AudiogrammLinks2000HzCsvColumn(config, "A2000LI9"),
            new AudiogrammLinks4000HzCsvColumn(config, "A4000LI9"),
            new AudiogrammLinks6000HzCsvColumn(config, "A6000LI9"),
            new AudiogrammLinks8000HzCsvColumn(config, "A8000LI9"),
            new HoerenBeurteilungCsvColumn(config, "GEHNO9", AudiogrammBeurteilung.NORMAL),
            new HoerenBeurteilungCsvColumn(config, "GEHRRED9", AudiogrammBeurteilung.RECHTS_REDUZIERT),
            new HoerenBeurteilungCsvColumn(config, "GEHLRED9", AudiogrammBeurteilung.LINKS_REDUZIERT),
            new HoerenBeurteilungCsvColumn(config, "GEHNG9", AudiogrammBeurteilung.NICHT_GEPRUEFT),
            new HoerenZusaetzlicheAngabenCsvColumn(config, "GEHERKÄL9", HoerenZusaetzlicheAngaben.ERKAELTUNG),
            new HoerenZusaetzlicheAngabenCsvColumn(config, "Ohr_Hoerhilfe_9", HoerenZusaetzlicheAngaben.HOERHILFE),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Normal_9", OtoskopieResultat.NORMAL),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Cerumen_9", OtoskopieResultat.TROMMELFELL_CERUMEN),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_TF_NichtEinsehbar_9",
                OtoskopieResultat.NICHT_EINSEHBAR),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_TF_Perforation_9",
                OtoskopieResultat.TROMMELFELL_PERFORATION),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_Seromukotympanon_9",
                OtoskopieResultat.SEROMUKOTYMPANON),
            new HoerenOtoskopieRechtsCsvColumn(
                config,
                "Otoskopie_Re_Nicht_geprüft_9",
                OtoskopieResultat.NICHT_GEPRUEFT),
            new HoerenOtoskopieRechtsCsvColumn(config, "Otoskopie_Re_Anderes_9", OtoskopieResultat.ANDERES),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Normal_9", OtoskopieResultat.NORMAL),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Cerumen_9", OtoskopieResultat.TROMMELFELL_CERUMEN),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_TF_NichtEinsehbar_9",
                OtoskopieResultat.NICHT_EINSEHBAR),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_TF_Perforation_9",
                OtoskopieResultat.TROMMELFELL_PERFORATION),
            new HoerenOtoskopieLinksCsvColumn(
                config,
                "Otoskopie_Li_Seromukotympanon_9",
                OtoskopieResultat.SEROMUKOTYMPANON),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Nicht_geprüft_9", OtoskopieResultat.NICHT_GEPRUEFT),
            new HoerenOtoskopieLinksCsvColumn(config, "Otoskopie_Li_Anderes_9", OtoskopieResultat.ANDERES),
            new GroesseWertCsvColumn(config, "GROESSE9"),
            new GroesseBeurteilungCsvColumn(config, "GROEBE9"),
            new GewichtWertCsvColumn(config, "GEWICHT9"),
            new GewichtBeurteilungCsvColumn(config, "GEWBE9"),
            new BMIWertCsvColumn(config, "BMI9"),
            new BMIBeurteilungCsvColumn(config, "BMIBE9"),
            new TriageLehrpersonCsvColumn(config, "Triage_Lehrperson_9"),
            new TriageSuCsvColumn(config, "Triage_SÄ_FB_9"),
            new TriageMpaCsvColumn(config, "Triage_MPA_9"),
            new TriageBefundSuCsvColumn(config, "Triage_Befund_SU_9"),
            new TriageWunschSchuelerCsvColumn(config, "Triage_Wunsch_SuS_9"),
            new MassnahmenKeineCsvColumn(config, "MNKEIN9"),
            new MassnahmenInformationElternCsvColumn(config, "MNINFE9"),
            new MassnahmenAbgegebenesInfomaterialCsvColumn(config, "MNINFOM9"),
            new MassnahmenAnderesCsvColumn(config, "MNANDE9"),
            new MassnahmenUeberweisungHausarztCsvColumn(config, "Massnahmen_HA_KA_9"),
            new MassnahmenUeberweisungAugenarztCsvColumn(config, "UEBERWAU9"),
            new MassnahmenBereitsBehandeltSelectedCsvColumn(config, "MNBEREI9"),
            new MassnahmenBereitsBehandeltSehenCsvColumn(config, "BERBEHAU9"),
            new MassnahmenBereitsBehandeltHoerenCsvColumn(config, "BERBEHKO9"),
            new MassnahmenBereitsBehandeltGroesseGewichtCsvColumn(config, "BERBEHGE9"),
            new MassnahmenBereitsBehandeltSpracheCsvColumn(config, "BERBEHSP9"),
            new MassnahmenBereitsBehandeltMotorikCsvColumn(config, "BERBEHMO9"),
            new MassnahmenBereitsBehandeltAnderesCsvColumn(config, "BERBEHAN9"),
            new MassnahmenNachkontrolleSelectedCsvColumn(config, "MNSÄD9"),
            new MassnahmenNachkontrolleSehenCsvColumn(config, "SÄDAUG9"),
            new MassnahmenNachkontrolleHoerenCsvColumn(config, "SÄDOHR9"),
            new MassnahmenNachkontrolleGroesseGewichtCsvColumn(config, "SÄDGEW9"),
            new MassnahmenNachkontrolleSpracheCsvColumn(config, "SÄDSPR9"),
            new MassnahmenNachkontrolleMotorikCsvColumn(config, "SÄDMOTEN9"),
            new MassnahmenNachkontrolleAnderesCsvColumn(config, "SÄDANDGR9")
        );
    }
}
