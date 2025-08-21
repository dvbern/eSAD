package ch.dvbern.oss.hemed.esc.api.features.spss.reports;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AbstractCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AerztinCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AlterCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AnlassTypCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.CsvColumnConfig;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeburtsJahrCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeburtsMonatCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeburtsTagCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeschlechtCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HasNoFragebogenCsvColumn;
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
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MpaCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NachkontrolleCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NachzuglerCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NeueintrittCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.QuartierCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulartCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulhausnameCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulhausnummerCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulkreisCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.UntersuchungJahrCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.UntersuchungMonatCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.UntersuchungTagCsvColumn;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpssImpfanlaesseReport {
    private final CsvColumnConfig config;

    public List<AbstractCsvColumn> getReport() {
        return List.of(
            new AnlassTypCsvColumn(config, "ANLASSTYPI"),
            new IdNrCsvColumn(config, "ID_NR_I"),
            new JahrSuCsvColumn(config, "J_SU_I"),
            new HasNoFragebogenCsvColumn(config, "KEINEFI"),
            new GeschlechtCsvColumn(config, "GESCHLEI"),
            new GeburtsTagCsvColumn(config, "GEBTAGI"),
            new GeburtsMonatCsvColumn(config, "GEBMONI"),
            new GeburtsJahrCsvColumn(config, "GEBJAHRI"),
            new UntersuchungTagCsvColumn(config, "UNTTAGI"),
            new UntersuchungMonatCsvColumn(config, "UNTMONI"),
            new UntersuchungJahrCsvColumn(config, "UNTJAHRI"),
            new AlterCsvColumn(config, "Alter_I"),
            new LandCodeCsvColumn(config, "NATIONI"),
            new MpaCsvColumn(config, "MPAI"),
            new AerztinCsvColumn(config, "AERZTINI"),
            new NachkontrolleCsvColumn(config, "Nachkontrolle_I"),
            new NachzuglerCsvColumn(config, "Nachzügler_I"),
            new NeueintrittCsvColumn(config, "Neueintritt_I"),
            new SchulhausnameCsvColumn(config, "Schulhausname_I"),
            new QuartierCsvColumn(config, "QUARTI"),
            new SchulkreisCsvColumn(config, "Schulkreis_I"),
            new SchulhausnummerCsvColumn(config, "SCHUNRI"),
            new KlassenstufeCsvColumn(config, "Klassenstufe_I"),
            new KlassenbezeichnungCsvColumn(config, "Klassenbezeichnung_I"),
            new SchulartCsvColumn(config, "Schulart_I"),
            new LastUpdateImpfungenCsvColumn(config, "Tag_Erfassung_IK_I"),
            new KeinImpfausweisIsCheckedCsvColumn(config, "IMPFAUSI"),
            new KeinImpfausweisIsNotCheckedCsvColumn(config, "Mit_Impfausweis_I"),
            new ImpfausweisUnvollstaendingCsvColumn(config, "IMPFUNVI"),
            new ImpfausweisZweiPlusEinsSchemaCsvColumn(config, "NEUES_IS_I"),
            new ImpfungGeplantCsvColumn(config, "Impfung_geplant_I"),
            new KrankheitAnzahlImpfungenCsvColumn(config, "DIPHANZI", Krankheit.DIPHTERIE),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "DIPHJAHRI", Krankheit.DIPHTERIE),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Diphtherie_Status_I", Krankheit.DIPHTERIE),
            new KrankheitImpfempfehlungCsvColumn(config, "Diphtherie_Impfempfehlung_I", Krankheit.DIPHTERIE),
            new KrankheitImpfentscheidCsvColumn(config, "Diphtherie_Impfentscheid_I", Krankheit.DIPHTERIE),
            new KrankheitImpfungVerabreichtCsvColumn(config, "DIPSÄDI", Krankheit.DIPHTERIE),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Diphtherie_Genügend_nach_Impf_I", Krankheit.DIPHTERIE),
            new KrankheitImpfstoffNameCsvColumn(config, "Diphtherie_Impfstoff_I", Krankheit.DIPHTERIE),
            new KrankheitAnzahlImpfungenCsvColumn(config, "TETAANZI", Krankheit.TETANUS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "TETJAHRI", Krankheit.TETANUS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Tetanus_Status_I", Krankheit.TETANUS),
            new KrankheitImpfempfehlungCsvColumn(config, "Tetanus_Impfempfehlung_I", Krankheit.TETANUS),
            new KrankheitImpfentscheidCsvColumn(config, "Tetanus_Impfentscheid_I", Krankheit.TETANUS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "TETSÄDI", Krankheit.TETANUS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Tetanus_Genügend_nach_Impf_I", Krankheit.TETANUS),
            new KrankheitImpfstoffNameCsvColumn(config, "Tetanus_Impfstoff_I", Krankheit.TETANUS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "PERTANZI", Krankheit.PERTUSSIS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "PERJAHRI", Krankheit.PERTUSSIS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Pertussis_Status_I", Krankheit.PERTUSSIS),
            new KrankheitImpfempfehlungCsvColumn(config, "Pertussis_Impfempfehlung_I", Krankheit.PERTUSSIS),
            new KrankheitImpfentscheidCsvColumn(config, "Pertussis_Impfentscheid_I", Krankheit.PERTUSSIS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "PERTSÄDI", Krankheit.PERTUSSIS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Pertussis_Genügend_nach_Impf_I", Krankheit.PERTUSSIS),
            new KrankheitImpfstoffNameCsvColumn(config, "Pertussis_Impfstoff_I", Krankheit.PERTUSSIS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "POLIOANZI", Krankheit.POLIO),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "POLIOJAHRI", Krankheit.POLIO),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Polio_Status_I", Krankheit.POLIO),
            new KrankheitImpfempfehlungCsvColumn(config, "Polio_Impfempfehlung_I", Krankheit.POLIO),
            new KrankheitImpfentscheidCsvColumn(config, "Polio_Impfentscheid_I", Krankheit.POLIO),
            new KrankheitImpfungVerabreichtCsvColumn(config, "POLSÄDI", Krankheit.POLIO),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Polio_Genügend_nach_Impf_I", Krankheit.POLIO),
            new KrankheitImpfstoffNameCsvColumn(config, "Polio_Impfstoff_I", Krankheit.POLIO),
            new KrankheitAnzahlImpfungenCsvColumn(config, "HEPBANZI", Krankheit.HEPATITIS_B),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "HEPBJAHRI", Krankheit.HEPATITIS_B),
            new KrankheitImpfschutzGegebenCsvColumn(config, "HepatitisB_Status_I", Krankheit.HEPATITIS_B),
            new KrankheitImpfempfehlungCsvColumn(config, "HepatitisB_Impfempfehlung_I", Krankheit.HEPATITIS_B),
            new KrankheitImpfentscheidCsvColumn(config, "HepatitisB_Impfentscheid_I", Krankheit.HEPATITIS_B),
            new KrankheitImpfungVerabreichtCsvColumn(config, "HepatitisB_Impfung_SÄD_I", Krankheit.HEPATITIS_B),
            new KrankheitImpfstatusGenugendCsvColumn(config, "HepatitisB_Genügend_nach_Impf_I", Krankheit.HEPATITIS_B),
            new KrankheitImpfstoffNameCsvColumn(config, "HepatitisB_Impfstoff_I", Krankheit.HEPATITIS_B),
            new KrankheitAnzahlImpfungenCsvColumn(config, "HPVANZI", Krankheit.HPV),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "HPVJAHRI", Krankheit.HPV),
            new KrankheitImpfschutzGegebenCsvColumn(config, "HPV_Status_I", Krankheit.HPV),
            new KrankheitImpfempfehlungCsvColumn(config, "HPV_Impfempfehlung_I", Krankheit.HPV),
            new KrankheitImpfentscheidCsvColumn(config, "HPV_Impfentscheid_I", Krankheit.HPV),
            new KrankheitImpfungVerabreichtCsvColumn(config, "HPV_Impfung_SÄD_I", Krankheit.HPV),
            new KrankheitImpfstatusGenugendCsvColumn(config, "HPV_Genügend_nach_Impf_I", Krankheit.HPV),
            new KrankheitImpfstoffNameCsvColumn(config, "HPV_Impfstoff_I", Krankheit.HPV),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MASANZI", Krankheit.MASERN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MASJAHRI", Krankheit.MASERN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Masern_Status_I", Krankheit.MASERN),
            new KrankheitImpfempfehlungCsvColumn(config, "Masern_Impfempfehlung_I", Krankheit.MASERN),
            new KrankheitImpfentscheidCsvColumn(config, "Masern_Impfentscheid_I", Krankheit.MASERN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MASSÄDI", Krankheit.MASERN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Masern_Genügend_nach_Impf_I", Krankheit.MASERN),
            new KrankheitImpfstoffNameCsvColumn(config, "Masern_Impfstoff_I", Krankheit.MASERN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MUMPANZI", Krankheit.MUMPS),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MUMPJAHRI", Krankheit.MUMPS),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Mumps_Status_I", Krankheit.MUMPS),
            new KrankheitImpfempfehlungCsvColumn(config, "Mumps_Impfempfehlung_I", Krankheit.MUMPS),
            new KrankheitImpfentscheidCsvColumn(config, "Mumps_Impfentscheid_I", Krankheit.MUMPS),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MUMSÄDI", Krankheit.MUMPS),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Mumps_Genügend_nach_Impf_I", Krankheit.MUMPS),
            new KrankheitImpfstoffNameCsvColumn(config, "Mumps_Impfstoff_I", Krankheit.MUMPS),
            new KrankheitAnzahlImpfungenCsvColumn(config, "ROETANZI", Krankheit.ROETELN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "ROEJAHRI", Krankheit.ROETELN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Roeteln_Status_I", Krankheit.ROETELN),
            new KrankheitImpfempfehlungCsvColumn(config, "Roeteln_Impfempfehlung_I", Krankheit.ROETELN),
            new KrankheitImpfentscheidCsvColumn(config, "Roeteln_Impfentscheid_I", Krankheit.ROETELN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "ROESÄDI", Krankheit.ROETELN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Roeteln_Genügend_nach_Impf_I", Krankheit.ROETELN),
            new KrankheitImpfstoffNameCsvColumn(config, "Roeteln_Impfstoff_I", Krankheit.ROETELN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "VARIZI", Krankheit.VARIZELLEN),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "VARIZJAHRI", Krankheit.VARIZELLEN),
            new KrankheitImpfschutzGegebenCsvColumn(config, "Varizellen_Status_I", Krankheit.VARIZELLEN),
            new KrankheitImpfempfehlungCsvColumn(config, "Varizellen_Impfempfehlung_I", Krankheit.VARIZELLEN),
            new KrankheitImpfentscheidCsvColumn(config, "Varizellen_Impfentscheid_I", Krankheit.VARIZELLEN),
            new KrankheitImpfungVerabreichtCsvColumn(config, "Varizellen_Impfung_SÄD_I", Krankheit.VARIZELLEN),
            new KrankheitImpfstatusGenugendCsvColumn(config, "Varizellen_Genügend_nach_Impf_I", Krankheit.VARIZELLEN),
            new KrankheitImpfstoffNameCsvColumn(config, "Varizellen_Impfstoff_I", Krankheit.VARIZELLEN),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MENACWYANZI", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MENACWYJAHRI", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfschutzGegebenCsvColumn(config, "MenACWY_Status_I", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfempfehlungCsvColumn(config, "MenACWY_Impfempfehlung_I", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfentscheidCsvColumn(config, "MenACWY_Impfentscheid_I", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MenACWY_Impfung_SÄD_I", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfstatusGenugendCsvColumn(
                config,
                "MenACWY_Genügend_nach_Impf_I",
                Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfstoffNameCsvColumn(config, "MenACWY_Impfstoff_I", Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitAnzahlImpfungenCsvColumn(config, "MENBANZI", Krankheit.MENINGOKOKKEN_B),
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, "MENBJAHRI", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfschutzGegebenCsvColumn(config, "MenB_Status_I", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfempfehlungCsvColumn(config, "MenB_Impfempfehlung_I", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfentscheidCsvColumn(config, "MenB_Impfentscheid_I", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfungVerabreichtCsvColumn(config, "MenB_Impfung_SÄD_I", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfstatusGenugendCsvColumn(config, "MenB_Genügend_nach_Impf_I", Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfstoffNameCsvColumn(config, "MenB_Impfstoff_I", Krankheit.MENINGOKOKKEN_B),
            new ImpfberatungMPACsvColumn(config, "Impfberatung_MPA_I"),
            new ImpfenRueckfrageKinderarztCsvColumn(config, "Impfen_Rückfrage_Kinderarzt_I"),
            new ImpfenInfoKinderarztCsvColumn(config, "Impfen_Info_Kinderarzt_I"),
            new ImpfenInfoElternCsvColumn(config, "Impfen_Info_Eltern_I")
        );
    }
}
