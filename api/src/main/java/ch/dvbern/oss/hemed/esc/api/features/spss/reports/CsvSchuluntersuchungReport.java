package ch.dvbern.oss.hemed.esc.api.features.spss.reports;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einverstaendniserklaerung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.ImpfungenMassnahmenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AbstractCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AerztinAnlassCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AerztinCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AlterCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AnlassArtCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AnlassDatumCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AnlassStatusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AnlassTypCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AusspracheCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BMIBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BMIWertCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BallFangenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BallWerfenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BewegungsapparatCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BlutdruckCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BlutdruckDiastolischCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BlutdruckSystolischCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BrilleLinsenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.CsvColumnConfig;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.EinbeinhuepfenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.EinbeinhuepfenLinksCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.EinbeinhuepfenRechtsCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.EinbeinstandCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.EinbeinstandLinksCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.EinbeinstandRechtsCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.EinverstaendniserklaerungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.EmpfehlungHoerenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.EmpfehlungImpfenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.EmpfehlungSehenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FarbsehenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusLinksCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusRechtsCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FigurNachmalenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FingeroppositionCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeburtsdatumCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeschlechtCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GewichtBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GewichtWertCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GroesseBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GroesseWertCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HampelmannCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HautValueCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HerzauskultationCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HoerenBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HoerenOtoskopieLinksCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HoerenOtoskopieRechtsCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HoerenZusaetzlicheAngabenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.IdNrCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfausweisUnvollstaendingCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfausweisZweiPlusEinsSchemaCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfenMassnahmenFolgeterminCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ImpfenMassnahmenKeineCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.InspektionCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.JahrSuCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KeinImpfausweisIsNotCheckedCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KlassenbezeichnungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KlassenstufeCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KognitionCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KognitionPunkteCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KonvergenzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitAnzahlImpfungenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfStatusImpfschutzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfempfehlungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfentscheidCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfstatusGenugendCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfungDoppelkontrolleVisum1CsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfungDoppelkontrolleVisum2CsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitImpfungVerabreichtCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.KrankheitJahrMonatDerLetztenImpfungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.LesefitCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.LesefitPunkteCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenAbgegebenesInfomaterialCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenAnderesCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBeratungArztCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBeratungMpaCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltAnderesCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltGroesseGewichtCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltHoerenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltMotorikCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltSehenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenBereitsBehandeltSpracheCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenInformationElternCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenKeineCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleAnderesCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleGroesseGewichtCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleHoerenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleMotorikCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleSehenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenNachkontrolleSpracheCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenUeberweisungAugenarztCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MassnahmenUeberweisungHausarztCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MotilitaetCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MotorikCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MotorikPunkteCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MpaAnlassCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MpaCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.MundAndZaehneCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NachkontrolleCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NachzuglerCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NahvisusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NahvisusLinksCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NahvisusRechtsCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NameCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NameSchreibenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NationalitaetCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NeueintrittCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.QuartierCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulartCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulhausnameCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulhausnummerCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulkreisCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SprachfaehigkeitMultiCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SprachfaehigkeitPunkteCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SprachfaehigkeitSingleCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StereotestCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StifthaltungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StrabismusAuftretenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StrabismusAuspraegungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StrabismusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.TeilnahmestatusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.VerhaltenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.VerhaltensfragebogenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.VerwendeteTafelCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.VornameCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.Zahlennachsprechen4CsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.Zahlennachsprechen5CsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ZeichnenDreieckCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ZeichnenKreisCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ZeichnenMenschCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ZeichnenViereckCsvColumn;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CsvSchuluntersuchungReport {
    private final CsvColumnConfig config;

    public List<AbstractCsvColumn> getReport() {
        return List.of(
            // ANLASS
            new AnlassTypCsvColumn(config, null),
            new AnlassArtCsvColumn(config),
            new AnlassStatusCsvColumn(config),
            new AnlassDatumCsvColumn(config),
            new JahrSuCsvColumn(config, null),
            new AerztinAnlassCsvColumn(config),
            new MpaAnlassCsvColumn(config, null),
            new TeilnahmestatusCsvColumn(config, null),

            // SU DATEN
            new NameCsvColumn(config, null),
            new VornameCsvColumn(config, null),
            new IdNrCsvColumn(config, null),
            new GeschlechtCsvColumn(config, null),
            new GeburtsdatumCsvColumn(config),
            new AlterCsvColumn(config, null),
            new NationalitaetCsvColumn(config, null),
            new AerztinCsvColumn(config, null),
            new MpaCsvColumn(config, null),
            new NachkontrolleCsvColumn(config, null),
            new NachzuglerCsvColumn(config, null),
            new NeueintrittCsvColumn(config, null),
            new SchulhausnameCsvColumn(config, null),
            new QuartierCsvColumn(config, null),
            new SchulkreisCsvColumn(config, null),
            new SchulhausnummerCsvColumn(config, null),
            new KlassenstufeCsvColumn(config, null),
            new KlassenbezeichnungCsvColumn(config, null),
            new SchulartCsvColumn(config, null),

            // SU IMPFUNGEN
            new KeinImpfausweisIsNotCheckedCsvColumn(config, null),
            new ImpfausweisUnvollstaendingCsvColumn(config, null),
            new ImpfausweisZweiPlusEinsSchemaCsvColumn(config, null),
            new KrankheitAnzahlImpfungenCsvColumn(config, null, Krankheit.DIPHTERIE),
            new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, Krankheit.DIPHTERIE),
            new KrankheitImpfStatusImpfschutzCsvColumn(config, null, Krankheit.DIPHTERIE),
            new KrankheitImpfempfehlungCsvColumn(config, null, Krankheit.DIPHTERIE),
            new KrankheitImpfentscheidCsvColumn(config, null, Krankheit.DIPHTERIE),
            new KrankheitImpfungVerabreichtCsvColumn(config, null, Krankheit.DIPHTERIE),
            new KrankheitImpfstatusGenugendCsvColumn(config, null, Krankheit.DIPHTERIE),

            new KrankheitAnzahlImpfungenCsvColumn(config, null, Krankheit.TETANUS),
            new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, Krankheit.TETANUS),
            new KrankheitImpfStatusImpfschutzCsvColumn(config, null, Krankheit.TETANUS),
            new KrankheitImpfempfehlungCsvColumn(config, null, Krankheit.TETANUS),
            new KrankheitImpfentscheidCsvColumn(config, null, Krankheit.TETANUS),
            new KrankheitImpfungVerabreichtCsvColumn(config, null, Krankheit.TETANUS),
            new KrankheitImpfstatusGenugendCsvColumn(config, null, Krankheit.TETANUS),

            new KrankheitAnzahlImpfungenCsvColumn(config, null, Krankheit.PERTUSSIS),
            new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, Krankheit.PERTUSSIS),
            new KrankheitImpfStatusImpfschutzCsvColumn(config, null, Krankheit.PERTUSSIS),
            new KrankheitImpfempfehlungCsvColumn(config, null, Krankheit.PERTUSSIS),
            new KrankheitImpfentscheidCsvColumn(config, null, Krankheit.PERTUSSIS),
            new KrankheitImpfungVerabreichtCsvColumn(config, null, Krankheit.PERTUSSIS),
            new KrankheitImpfstatusGenugendCsvColumn(config, null, Krankheit.PERTUSSIS),

            new KrankheitAnzahlImpfungenCsvColumn(config, null, Krankheit.POLIO),
            new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, Krankheit.POLIO),
            new KrankheitImpfStatusImpfschutzCsvColumn(config, null, Krankheit.POLIO),
            new KrankheitImpfempfehlungCsvColumn(config, null, Krankheit.POLIO),
            new KrankheitImpfentscheidCsvColumn(config, null, Krankheit.POLIO),
            new KrankheitImpfungVerabreichtCsvColumn(config, null, Krankheit.POLIO),
            new KrankheitImpfstatusGenugendCsvColumn(config, null, Krankheit.POLIO),

            new KrankheitAnzahlImpfungenCsvColumn(config, null, Krankheit.HEPATITIS_B),
            new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, Krankheit.HEPATITIS_B),
            new KrankheitImpfStatusImpfschutzCsvColumn(config, null, Krankheit.HEPATITIS_B),
            new KrankheitImpfempfehlungCsvColumn(config, null, Krankheit.HEPATITIS_B),
            new KrankheitImpfentscheidCsvColumn(config, null, Krankheit.HEPATITIS_B),
            new KrankheitImpfungVerabreichtCsvColumn(config, null, Krankheit.HEPATITIS_B),
            new KrankheitImpfstatusGenugendCsvColumn(config, null, Krankheit.HEPATITIS_B),

            new KrankheitAnzahlImpfungenCsvColumn(config, null, Krankheit.HPV),
            new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, Krankheit.HPV),
            new KrankheitImpfStatusImpfschutzCsvColumn(config, null, Krankheit.HPV),
            new KrankheitImpfempfehlungCsvColumn(config, null, Krankheit.HPV),
            new KrankheitImpfentscheidCsvColumn(config, null, Krankheit.HPV),
            new KrankheitImpfungVerabreichtCsvColumn(config, null, Krankheit.HPV),
            new KrankheitImpfstatusGenugendCsvColumn(config, null, Krankheit.HPV),

            new KrankheitAnzahlImpfungenCsvColumn(config, null, Krankheit.MASERN),
            new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, Krankheit.MASERN),
            new KrankheitImpfStatusImpfschutzCsvColumn(config, null, Krankheit.MASERN),
            new KrankheitImpfempfehlungCsvColumn(config, null, Krankheit.MASERN),
            new KrankheitImpfentscheidCsvColumn(config, null, Krankheit.MASERN),
            new KrankheitImpfungVerabreichtCsvColumn(config, null, Krankheit.MASERN),
            new KrankheitImpfstatusGenugendCsvColumn(config, null, Krankheit.MASERN),

            new KrankheitAnzahlImpfungenCsvColumn(config, null, Krankheit.MUMPS),
            new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, Krankheit.MUMPS),
            new KrankheitImpfStatusImpfschutzCsvColumn(config, null, Krankheit.MUMPS),
            new KrankheitImpfempfehlungCsvColumn(config, null, Krankheit.MUMPS),
            new KrankheitImpfentscheidCsvColumn(config, null, Krankheit.MUMPS),
            new KrankheitImpfungVerabreichtCsvColumn(config, null, Krankheit.MUMPS),
            new KrankheitImpfstatusGenugendCsvColumn(config, null, Krankheit.MUMPS),

            new KrankheitAnzahlImpfungenCsvColumn(config, null, Krankheit.ROETELN),
            new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, Krankheit.ROETELN),
            new KrankheitImpfStatusImpfschutzCsvColumn(config, null, Krankheit.ROETELN),
            new KrankheitImpfempfehlungCsvColumn(config, null, Krankheit.ROETELN),
            new KrankheitImpfentscheidCsvColumn(config, null, Krankheit.ROETELN),
            new KrankheitImpfungVerabreichtCsvColumn(config, null, Krankheit.ROETELN),
            new KrankheitImpfstatusGenugendCsvColumn(config, null, Krankheit.ROETELN),

            new KrankheitAnzahlImpfungenCsvColumn(config, null, Krankheit.VARIZELLEN),
            new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, Krankheit.VARIZELLEN),
            new KrankheitImpfStatusImpfschutzCsvColumn(config, null, Krankheit.VARIZELLEN),
            new KrankheitImpfempfehlungCsvColumn(config, null, Krankheit.VARIZELLEN),
            new KrankheitImpfentscheidCsvColumn(config, null, Krankheit.VARIZELLEN),
            new KrankheitImpfungVerabreichtCsvColumn(config, null, Krankheit.VARIZELLEN),
            new KrankheitImpfstatusGenugendCsvColumn(config, null, Krankheit.VARIZELLEN),

            new KrankheitAnzahlImpfungenCsvColumn(config, null, Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfStatusImpfschutzCsvColumn(config, null, Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfempfehlungCsvColumn(config, null, Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfentscheidCsvColumn(config, null, Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfungVerabreichtCsvColumn(config, null, Krankheit.MENINGOKOKKEN_A_C_W_Y),
            new KrankheitImpfstatusGenugendCsvColumn(config, null, Krankheit.MENINGOKOKKEN_A_C_W_Y),

            new KrankheitAnzahlImpfungenCsvColumn(config, null, Krankheit.MENINGOKOKKEN_B),
            new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfStatusImpfschutzCsvColumn(config, null, Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfempfehlungCsvColumn(config, null, Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfentscheidCsvColumn(config, null, Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfungVerabreichtCsvColumn(config, null, Krankheit.MENINGOKOKKEN_B),
            new KrankheitImpfstatusGenugendCsvColumn(config, null, Krankheit.MENINGOKOKKEN_B),

            new KrankheitImpfungDoppelkontrolleVisum1CsvColumn(config, null),
            new KrankheitImpfungDoppelkontrolleVisum2CsvColumn(config, null),
            new ImpfenMassnahmenKeineCsvColumn(config),
            new EmpfehlungImpfenCsvColumn(config, ImpfungenMassnahmenEmpfehlung.IMPFUNG_BEIM_HAUSARZT),
            new ImpfenMassnahmenFolgeterminCsvColumn(config),

            // SU Sehen
            new BrilleLinsenCsvColumn(config, null),
            new StereotestCsvColumn(config, null),
            new FarbsehenCsvColumn(config, null),
            new VerwendeteTafelCsvColumn(config, null),
            new FernvisusRechtsCsvColumn(config, null),
            new FernvisusLinksCsvColumn(config, null),
            new FernvisusCsvColumn(config, null, DistanzSehenResultat.NORMAL),
            new FernvisusCsvColumn(config, null, DistanzSehenResultat.RECHTS_REDUZIERT),
            new FernvisusCsvColumn(config, null, DistanzSehenResultat.LINKS_REDUZIERT),
            new FernvisusCsvColumn(config, null, DistanzSehenResultat.NICHT_GEPRUEFT),
            new NahvisusRechtsCsvColumn(config, null),
            new NahvisusLinksCsvColumn(config, null),
            new NahvisusCsvColumn(config, null, DistanzSehenResultat.NORMAL),
            new NahvisusCsvColumn(config, null, DistanzSehenResultat.RECHTS_REDUZIERT),
            new NahvisusCsvColumn(config, null, DistanzSehenResultat.LINKS_REDUZIERT),
            new NahvisusCsvColumn(config, null, DistanzSehenResultat.NICHT_GEPRUEFT),
            new MotilitaetCsvColumn(config, null),
            new StrabismusCsvColumn(config, null, StrabismusResultat.KEIN_STRABISMUS),
            new StrabismusCsvColumn(config, null, StrabismusResultat.EXO),
            new StrabismusCsvColumn(config, null, StrabismusResultat.ESO),
            new StrabismusCsvColumn(config, null, StrabismusResultat.HYPER),
            new StrabismusCsvColumn(config, null, StrabismusResultat.NICHT_GEPRUEFT),
            new StrabismusAuspraegungCsvColumn(config, null),
            new StrabismusAuftretenCsvColumn(config, null),
            new InspektionCsvColumn(config, null),
            new KonvergenzCsvColumn(config, null),
            new EmpfehlungSehenCsvColumn(config, SehenEmpfehlung.UEBERPRUEFUNG_AUGENARZT),
            new EmpfehlungSehenCsvColumn(config, SehenEmpfehlung.UEBERPRUEFUNG_BRILLE),

            // SU Hören
            new HoerenBeurteilungCsvColumn(config, null, AudiogrammBeurteilung.NORMAL),
            new HoerenBeurteilungCsvColumn(config, null, AudiogrammBeurteilung.RECHTS_REDUZIERT),
            new HoerenBeurteilungCsvColumn(config, null, AudiogrammBeurteilung.LINKS_REDUZIERT),
            new HoerenBeurteilungCsvColumn(config, null, AudiogrammBeurteilung.NICHT_GEPRUEFT),
            new HoerenZusaetzlicheAngabenCsvColumn(config, null, HoerenZusaetzlicheAngaben.KEINE),
            new HoerenZusaetzlicheAngabenCsvColumn(config, null, HoerenZusaetzlicheAngaben.ERKAELTUNG),
            new HoerenZusaetzlicheAngabenCsvColumn(config, null, HoerenZusaetzlicheAngaben.HOERHILFE),
            new HoerenOtoskopieRechtsCsvColumn(config, null, OtoskopieResultat.NORMAL),
            new HoerenOtoskopieRechtsCsvColumn(config, null, OtoskopieResultat.TROMMELFELL_CERUMEN),
            new HoerenOtoskopieRechtsCsvColumn(config, null, OtoskopieResultat.NICHT_EINSEHBAR),
            new HoerenOtoskopieRechtsCsvColumn(config, null, OtoskopieResultat.TROMMELFELL_PERFORATION),
            new HoerenOtoskopieRechtsCsvColumn(config, null, OtoskopieResultat.SEROMUKOTYMPANON),
            new HoerenOtoskopieRechtsCsvColumn(config, null, OtoskopieResultat.NICHT_GEPRUEFT),
            new HoerenOtoskopieRechtsCsvColumn(config, null, OtoskopieResultat.ANDERES),
            new HoerenOtoskopieLinksCsvColumn(config, null, OtoskopieResultat.NORMAL),
            new HoerenOtoskopieLinksCsvColumn(config, null, OtoskopieResultat.TROMMELFELL_CERUMEN),
            new HoerenOtoskopieLinksCsvColumn(config, null, OtoskopieResultat.NICHT_EINSEHBAR),
            new HoerenOtoskopieLinksCsvColumn(config, null, OtoskopieResultat.TROMMELFELL_PERFORATION),
            new HoerenOtoskopieLinksCsvColumn(config, null, OtoskopieResultat.SEROMUKOTYMPANON),
            new HoerenOtoskopieLinksCsvColumn(config, null, OtoskopieResultat.NICHT_GEPRUEFT),
            new HoerenOtoskopieLinksCsvColumn(config, null, OtoskopieResultat.ANDERES),
            new EmpfehlungHoerenCsvColumn(config, HoerenEmpfehlung.UEBERPRUEFUNG_ARZT),

            // SU Grösse & Gewicht
            new GroesseWertCsvColumn(config, null),
            new GroesseBeurteilungCsvColumn(config, null),
            new GewichtWertCsvColumn(config, null),
            new GewichtBeurteilungCsvColumn(config, null),
            new BMIWertCsvColumn(config, null),
            new BMIBeurteilungCsvColumn(config, null),

            // SU Ärztliche Untersuchung
            new EinverstaendniserklaerungCsvColumn(config, Einverstaendniserklaerung.ENTBINDUNG_VORHANDEN),
            new EinverstaendniserklaerungCsvColumn(config, Einverstaendniserklaerung.SCHWEIGEPFLICHT_VORHANDEN),
            new EinverstaendniserklaerungCsvColumn(config, Einverstaendniserklaerung.VIDEOAUFNAHME_VORHANDEN),
            new VerhaltensfragebogenCsvColumn(config),
            new EinbeinstandCsvColumn(config, null),
            new EinbeinstandRechtsCsvColumn(config),
            new EinbeinstandLinksCsvColumn(config),
            new EinbeinhuepfenCsvColumn(config, null),
            new EinbeinhuepfenRechtsCsvColumn(config),
            new EinbeinhuepfenLinksCsvColumn(config),
            new HampelmannCsvColumn(config),
            new BallFangenCsvColumn(config),
            new BallWerfenCsvColumn(config),
            new NameSchreibenCsvColumn(config),
            new FigurNachmalenCsvColumn(config),
            new StifthaltungCsvColumn(config),
            new AusspracheCsvColumn(config),
            new LesefitPunkteCsvColumn(config),
            new LesefitCsvColumn(config),
            new Zahlennachsprechen4CsvColumn(config),
            new Zahlennachsprechen5CsvColumn(config),
            new ZeichnenKreisCsvColumn(config),
            new ZeichnenViereckCsvColumn(config),
            new ZeichnenDreieckCsvColumn(config),
            new ZeichnenMenschCsvColumn(config),
            new MotorikPunkteCsvColumn(config),
            new MotorikCsvColumn(config, null),
            new SprachfaehigkeitPunkteCsvColumn(config),
            new SprachfaehigkeitSingleCsvColumn(config),
            new KognitionPunkteCsvColumn(config),
            new KognitionCsvColumn(config),
            new BlutdruckSystolischCsvColumn(config, null),
            new BlutdruckDiastolischCsvColumn(config, null),
            new BlutdruckCsvColumn(config, null),
            new BewegungsapparatCsvColumn(config, null, Bewegungsapparat.GROBKURSORISCH_NORMAL),
            new BewegungsapparatCsvColumn(config, null, Bewegungsapparat.KONGENITALE_FEHLBILDUNG),
            new BewegungsapparatCsvColumn(config, null, Bewegungsapparat.SKOLIOSE),
            new BewegungsapparatCsvColumn(config, null, Bewegungsapparat.GANGSTOERUNG),
            new BewegungsapparatCsvColumn(config, null, Bewegungsapparat.FUSSFEHLSTELLUNG),
            new BewegungsapparatCsvColumn(config, null, Bewegungsapparat.ANDERES),
            new BewegungsapparatCsvColumn(config, null, Bewegungsapparat.NICHT_GEPRUEFT),
            new FingeroppositionCsvColumn(config, null, Fingeropposition.ALTERSENTSPRECHEND),
            new FingeroppositionCsvColumn(config, null, Fingeropposition.RECHTS_AUFFAELLIG),
            new FingeroppositionCsvColumn(config, null, Fingeropposition.LINKS_AUFFAELLIG),
            new FingeroppositionCsvColumn(config, null, Fingeropposition.NICHT_GEPRUEFT),
            new FingeroppositionCsvColumn(config, null, Fingeropposition.ANDERES),
            new SprachfaehigkeitMultiCsvColumn(config, null, Sprachfaehigkeit.GROBKURSORISCH_NORMAL),
            new SprachfaehigkeitMultiCsvColumn(config, null, Sprachfaehigkeit.ENTWICKLUNGSVERZOEGERUNG),
            new SprachfaehigkeitMultiCsvColumn(config, null, Sprachfaehigkeit.ARTIKULATIONSFEHLER),
            new SprachfaehigkeitMultiCsvColumn(config, null, Sprachfaehigkeit.DEUTSCHKENNTNISSE_MINIM),
            new SprachfaehigkeitMultiCsvColumn(config, null, Sprachfaehigkeit.ANDERES),
            new SprachfaehigkeitMultiCsvColumn(config, null, Sprachfaehigkeit.NICHT_GEPRUEFT),
            new MundAndZaehneCsvColumn(config, null, MundAndZaehne.UNAUFFAELLIG),
            new MundAndZaehneCsvColumn(config, null, MundAndZaehne.KARIES),
            new MundAndZaehneCsvColumn(config, null, MundAndZaehne.KARIES_SANIERT),
            new MundAndZaehneCsvColumn(config, null, MundAndZaehne.ADENOIDHYPERPLASIE),
            new MundAndZaehneCsvColumn(config, null, MundAndZaehne.ANDERES),
            new MundAndZaehneCsvColumn(config, null, MundAndZaehne.NICHT_GEPRUEFT),
            new HautValueCsvColumn(config),
            new HerzauskultationCsvColumn(config, null),
            new VerhaltenCsvColumn(config, null),

            // SU Massnahmen
            new MassnahmenKeineCsvColumn(config, null),
            new MassnahmenBeratungMpaCsvColumn(config),
            new MassnahmenBeratungArztCsvColumn(config),
            new MassnahmenAnderesCsvColumn(config, null),
            new MassnahmenBereitsBehandeltSehenCsvColumn(config, null),
            new MassnahmenBereitsBehandeltHoerenCsvColumn(config, null),
            new MassnahmenBereitsBehandeltGroesseGewichtCsvColumn(config, null),
            new MassnahmenBereitsBehandeltSpracheCsvColumn(config, null),
            new MassnahmenBereitsBehandeltMotorikCsvColumn(config, null),
            new MassnahmenBereitsBehandeltAnderesCsvColumn(config, null),
            new MassnahmenInformationElternCsvColumn(config, null),
            new MassnahmenAbgegebenesInfomaterialCsvColumn(config, null),
            new MassnahmenUeberweisungHausarztCsvColumn(config, null),
            new MassnahmenUeberweisungAugenarztCsvColumn(config, null),
            new MassnahmenNachkontrolleSehenCsvColumn(config, null),
            new MassnahmenNachkontrolleHoerenCsvColumn(config, null),
            new MassnahmenNachkontrolleGroesseGewichtCsvColumn(config, null),
            new MassnahmenNachkontrolleSpracheCsvColumn(config, null),
            new MassnahmenNachkontrolleMotorikCsvColumn(config, null),
            new MassnahmenNachkontrolleAnderesCsvColumn(config, null));
    }
}
