package ch.dvbern.oss.hemed.esc.api.features.spss.reports;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einverstaendniserklaerung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.ImpfungenMassnahmenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenAugeEinzelnResultat;
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
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammHoerHilfeCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AudiogrammSimpelCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AusspracheCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BMIBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BMIWertCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BallFangenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BallWerfenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BewegungsapparatCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BewegungsapparatHaltungsschwacheCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BewegungsapparatSkolioseCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.BewegungsapparatVerkuerzungHamstringmuskulaturCsvColumn;
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
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FeinmotorikCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusAugeLinksCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusAugeRechtsCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusLinksCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FernvisusRechtsCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FigurNachmalenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FingeroppositionCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.FreiesGehenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeburtsdatumCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GeschlechtCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GewichtBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GewichtWertCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GleichgewichtEinbeinstandCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GrafomotorikCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GrobmotorikEinbeinhuepfenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GroesseBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.GroesseWertCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HampelmannCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HautValueCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HerzauskultationCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HoerenBeurteilungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HoerenOtoskopieLinksCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HoerenOtoskopieRechtsCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.HoerenZusaetzlicheAngabenCsvColumn;
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
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NameSchreibenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NationalitaetCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.NeueintrittCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.OhneStuetzradVelofahrenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.QuartierCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulartCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulhausnameCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulhausnummerCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SchulkreisCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SpracheNachsprechenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SpracheSprachverstaendnisCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SpracheWortschatzCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SprachfaehigkeitMultiCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SprachfaehigkeitPunkteCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SprachfaehigkeitSingleCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StereotestCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StifthaltungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StrabismusAuftretenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StrabismusAuspraegungCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.StrabismusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.SystemIdCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.TeilnahmestatusCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.VerhaltenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.VerhaltensfragebogenCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.VerwendeteTafelCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.Zahlennachsprechen4CsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.Zahlennachsprechen5CsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ZeichnenDreieckCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ZeichnenKreisCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ZeichnenMenschCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.ZeichnenViereckCsvColumn;
import lombok.RequiredArgsConstructor;

import static ch.dvbern.oss.hemed.esc.api.features.spss.CsvReportUtil.addEnumColumnsToReportList;

@RequiredArgsConstructor
public class CsvSchuluntersuchungReport {
    private final CsvColumnConfig config;

    public List<AbstractCsvColumn> getReport() {
        List<AbstractCsvColumn> reportList = new ArrayList<>(List.of(
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
            new SystemIdCsvColumn(config, null),
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
            new ImpfausweisZweiPlusEinsSchemaCsvColumn(config, null)
        ));

        for (Krankheit krankheit : Krankheit.values()) {
            reportList.addAll(List.of(
                new KrankheitAnzahlImpfungenCsvColumn(config, null, krankheit),
                new KrankheitJahrMonatDerLetztenImpfungCsvColumn(config, null, krankheit),
                new KrankheitImpfStatusImpfschutzCsvColumn(config, null, krankheit),
                new KrankheitImpfempfehlungCsvColumn(config, null, krankheit),
                new KrankheitImpfentscheidCsvColumn(config, null, krankheit),
                new KrankheitImpfungVerabreichtCsvColumn(config, null, krankheit),
                new KrankheitImpfstatusGenugendCsvColumn(config, null, krankheit)));
        }

        reportList.add(new KrankheitImpfungDoppelkontrolleVisum1CsvColumn(config, null));
        reportList.add(new KrankheitImpfungDoppelkontrolleVisum2CsvColumn(config, null));
        reportList.add(new ImpfenMassnahmenKeineCsvColumn(config));

        addEnumColumnsToReportList(
            reportList,
            ImpfungenMassnahmenEmpfehlung.class,
            impfungenMassnahmenEmpfehlung -> new EmpfehlungImpfenCsvColumn(config, impfungenMassnahmenEmpfehlung));

        reportList.addAll(List.of(
            new ImpfenMassnahmenFolgeterminCsvColumn(config),

            // SU Sehen
            new BrilleLinsenCsvColumn(config, null),
            new StereotestCsvColumn(config, null),
            new FarbsehenCsvColumn(config, null),
            new VerwendeteTafelCsvColumn(config, null),
            new FernvisusRechtsCsvColumn(config, null),
            new FernvisusLinksCsvColumn(config, null)
        ));

        addEnumColumnsToReportList(
            reportList,
            DistanzSehenResultat.class,
            distanzSehenResultat -> new FernvisusCsvColumn(config, null, distanzSehenResultat));

        addEnumColumnsToReportList(
            reportList,
            DistanzSehenAugeEinzelnResultat.class,
            distanzSehenEinzelResultat -> new FernvisusAugeLinksCsvColumn(config, null, distanzSehenEinzelResultat));

        addEnumColumnsToReportList(
            reportList,
            DistanzSehenAugeEinzelnResultat.class,
            distanzSehenEinzelResultat -> new FernvisusAugeRechtsCsvColumn(config, null, distanzSehenEinzelResultat));

        reportList.add(new NahvisusRechtsCsvColumn(config, null));
        reportList.add(new NahvisusLinksCsvColumn(config, null));

        addEnumColumnsToReportList(
            reportList,
            DistanzSehenResultat.class,
            distanzSehenResultat -> new NahvisusCsvColumn(config, null, distanzSehenResultat));

        reportList.add(new MotilitaetCsvColumn(config, null));

        addEnumColumnsToReportList(
            reportList,
            StrabismusResultat.class,
            strabismusResultat -> new StrabismusCsvColumn(config, null, strabismusResultat));

        reportList.addAll(List.of(
            new StrabismusAuspraegungCsvColumn(config, null),
            new StrabismusAuftretenCsvColumn(config, null),
            new InspektionCsvColumn(config, null),
            new KonvergenzCsvColumn(config, null)
        ));

        addEnumColumnsToReportList(
            reportList,
            SehenEmpfehlung.class,
            sehenEmpfehlung -> new EmpfehlungSehenCsvColumn(config, sehenEmpfehlung));

        // SU Hören
        reportList.add(new AudiogrammSimpelCsvColumn(config, null));
        addEnumColumnsToReportList(
            reportList,
            HoerenHoerhilfe.class,
            hoerenHoerhilfe -> new AudiogrammHoerHilfeCsvColumn(config, null, hoerenHoerhilfe));
        addEnumColumnsToReportList(
            reportList,
            AudiogrammBeurteilung.class,
            audiogrammBeurteilung -> new HoerenBeurteilungCsvColumn(config, null, audiogrammBeurteilung));
        addEnumColumnsToReportList(
            reportList,
            HoerenZusaetzlicheAngaben.class,
            hoerenZusaetzlicheAngaben -> new HoerenZusaetzlicheAngabenCsvColumn(config,null, hoerenZusaetzlicheAngaben));
        addEnumColumnsToReportList(
            reportList,
            OtoskopieResultat.class,
            otoskopieResultat -> new HoerenOtoskopieRechtsCsvColumn(config, null, otoskopieResultat));
        addEnumColumnsToReportList(
            reportList,
            OtoskopieResultat.class,
            otoskopieResultat -> new HoerenOtoskopieLinksCsvColumn(config, null, otoskopieResultat));
        addEnumColumnsToReportList(
            reportList,
            HoerenEmpfehlung.class,
            hoerenEmpfehlung -> new EmpfehlungHoerenCsvColumn(config, hoerenEmpfehlung));

        // SU Grösse & Gewicht
        reportList.addAll(List.of(
            new GroesseWertCsvColumn(config, null),
            new GroesseBeurteilungCsvColumn(config, null),
            new GewichtWertCsvColumn(config, null),
            new GewichtBeurteilungCsvColumn(config, null),
            new BMIWertCsvColumn(config, null),
            new BMIBeurteilungCsvColumn(config, null)
        ));

        // SU Ärztliche Untersuchung
        addEnumColumnsToReportList(
            reportList,
            Einverstaendniserklaerung.class,
            einverstaendniserklaerung -> new EinverstaendniserklaerungCsvColumn(config, einverstaendniserklaerung));

        reportList.addAll(List.of(
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
            new BlutdruckCsvColumn(config, null)
        ));

        addEnumColumnsToReportList(
            reportList,
            Bewegungsapparat.class,
            bewegungsapparat -> new BewegungsapparatCsvColumn(config, null, bewegungsapparat));
        addEnumColumnsToReportList(
            reportList,
            Fingeropposition.class,
            fingeropposition -> new FingeroppositionCsvColumn(config, null, fingeropposition));
        addEnumColumnsToReportList(
            reportList,
            Sprachfaehigkeit.class,
            sprachfaehigkeit -> new SprachfaehigkeitMultiCsvColumn(config, null, sprachfaehigkeit));
        addEnumColumnsToReportList(
            reportList,
            MundAndZaehne.class,
            mundAndZaehne -> new MundAndZaehneCsvColumn(config, null, mundAndZaehne));

        reportList.addAll(List.of(
            new HautValueCsvColumn(config),
            new HerzauskultationCsvColumn(config, null),
            new VerhaltenCsvColumn(config, null),

            // SU Sprache
            new SpracheNachsprechenCsvColumn(config, null),
            new SpracheWortschatzCsvColumn(config, null),
            new SpracheSprachverstaendnisCsvColumn(config, null),

            // SU Bewegungsapparat
            new BewegungsapparatSkolioseCsvColumn(config),
            new BewegungsapparatHaltungsschwacheCsvColumn(config),
            new BewegungsapparatVerkuerzungHamstringmuskulaturCsvColumn(config),

            // SU Schulrelevante Beeinträchtigung
            new FreiesGehenCsvColumn(config),
            new OhneStuetzradVelofahrenCsvColumn(config),
            new GrafomotorikCsvColumn(config),
            new FeinmotorikCsvColumn(config),
            new GrobmotorikEinbeinhuepfenCsvColumn(config),
            new GleichgewichtEinbeinstandCsvColumn(config),

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
            new MassnahmenNachkontrolleAnderesCsvColumn(config, null)
        ));
        return reportList;
    }
}
