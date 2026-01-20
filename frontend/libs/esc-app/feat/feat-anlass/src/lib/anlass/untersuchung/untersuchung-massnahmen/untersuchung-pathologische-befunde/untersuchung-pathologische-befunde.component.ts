import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {
  ImpfStatus,
  RequirementSetting,
  UntersuchungArztBemerkungSaveJson,
  UntersuchungArztBeurteilungSaveJson,
  UntersuchungGroesseGewichtSaveJson,
  UntersuchungHoerenAudiogrammSaveJson,
  UntersuchungHoerenOtoskopieSaveJson,
  UntersuchungImpfstatusKrankheitSaveJson,
  UntersuchungImpfungInternJson,
  UntersuchungSehenBeurteilungSaveJson,
} from '@dvb/data-access-backend';
import {BenutzerStore} from '@dvb/feat-shared';
import {HeadingDirective} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {AnlassStore} from '../../../anlass.store';
import {UntersuchungArztStore} from '../../untersuchung-arzt/untersuchung-arzt.store';
import {calculateBmi} from '../../untersuchung-groessegewicht/untersuchung-groessegewicht.form';
import {UntersuchungGroesseGewichtStore} from '../../untersuchung-groessegewicht/untersuchung-groessegewicht.store';
import {
  UntersuchungHoerenAudiogrammStore,
} from '../../untersuchung-hoeren/untersuchung-hoeren-audiogramm/untersuchung-hoeren-audiogramm.store';
import {
  UntersuchungHoerenOtoskopieStore,
} from '../../untersuchung-hoeren/untersuchung-hoeren-otoskopie/untersuchung-hoeren-otoskopie.store';
import {UntersuchungImpfungenStore} from '../../untersuchung-impfungen/untersuchung-impfungen.store';
import {UntersuchungSehenStore} from '../../untersuchung-sehen/untersuchung-sehen.store';

@Component({
  selector: 'feat-untersuchung-pathologische-befunde',
  templateUrl: './untersuchung-pathologische-befunde.component.html',
  styleUrl: './untersuchung-pathologische-befunde.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    TranslocoPipe,
    ReactiveFormsModule,
    HeadingDirective,
  ],
})
export class UntersuchungPathologischeBefundeComponent {

  private readonly excludes = [
    'OHNE_KEINE', 'NICHT_GEPRUEFT', 'NORMAL', 'NO', 'UNAUFFAELLIG', 'GROBKURSORISCH_NORMAL',
    'GROBKURSORISCH_UNAUFFAELLIG', 'SICHER', 'ALTERSENTSPRECHEND', 'KEINE', 'KEIN_STRABISMUS', 'GUT', undefined, null,
  ];

  protected readonly DetailMode = DetailMode;
  protected readonly RequirementSetting = RequirementSetting;

  readonly transloco = inject(TranslocoService);
  readonly benutzerStore = inject(BenutzerStore);
  readonly anlassStore = inject(AnlassStore);
  readonly impfungenStore = inject(UntersuchungImpfungenStore);
  readonly sehenStore = inject(UntersuchungSehenStore);
  readonly hoerenAudiogrammStore = inject(UntersuchungHoerenAudiogrammStore);
  readonly hoerenOtoskopieStore = inject(UntersuchungHoerenOtoskopieStore);
  readonly groesseGewichtStore = inject(UntersuchungGroesseGewichtStore);
  readonly arztStore = inject(UntersuchungArztStore);

  readonly translocoService = inject(TranslocoService);

  impfenMap = computed(() => {
    const impfenMap: Map<string, string> = new Map<string, string>();
    this.addSimpleValue(
      impfenMap,
      'untersuchung.pathologische.befunde.impfen.empfehlungen',
      this.getFilteredImpfstatus(item => !!item.impfempfehlung),
    );
    this.addSimpleValue(
      impfenMap,
      'untersuchung.pathologische.befunde.impfen.entscheide',
      this.getFilteredImpfstatus(item => !!item.impfentscheid),
    );
    this.addSimpleValue(
      impfenMap,
      'untersuchung.pathologische.befunde.impfen.durchgefuehrteImpfungen',
      this.getDurchgefuehrteImpfungen(),
    );
    this.addSimpleValue(
      impfenMap,
      'untersuchung.pathologische.befunde.impfen.achtung',
      this.impfungenStore.massnahmen()?.data?.achtung,
    );

    return impfenMap;
  });

  sehenMap = computed(() => {
    const sehenMap: Map<string, string> = new Map<string, string>();
    const data: UntersuchungSehenBeurteilungSaveJson | undefined = this.sehenStore.sehenBeurteilung()?.data;

    // Inspektion
    this.addEnumValue(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.inspektion',
      data?.inspektion,
      'InspektionKonvergenzResultat',
    );
    // Stereotest
    this.addEnumValue(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.stereotest',
      data?.stereotest,
      'StereotestResultat',
    );
    // Farbsehen
    this.addEnumValue(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.farbsehen',
      data?.farbsehen,
      'FarbsehenResultat',
    );
    // Fernvisus
    const fernvisusPathologisch = this.addFilteredEnumListValue(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.fernvisus',
      data?.fernvisus,
      'DistanzSehenResultat',
    );
    if (fernvisusPathologisch) {
      this.addSimpleValue(
        sehenMap,
        'untersuchung.pathologische.befunde.sehen.fernvisus.rechts',
        data?.fernvisusRechts,
      );
      this.addSimpleValue(
        sehenMap,
        'untersuchung.pathologische.befunde.sehen.fernvisus.links',
        data?.fernvisusLinks,
      );
    }
    // Nahvisus
    const nahvisusPathologisch = this.addFilteredEnumListValue(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.nahvisus',
      data?.nahvisus,
      'DistanzSehenResultat',
    );
    if (nahvisusPathologisch) {
      this.addSimpleValue(
        sehenMap,
        'untersuchung.pathologische.befunde.sehen.nahvisus.rechts',
        data?.nahvisusRechts,
      );
      this.addSimpleValue(
        sehenMap,
        'untersuchung.pathologische.befunde.sehen.nahvisus.links',
        data?.nahvisusLinks,
      );
    }
    // Motilitaet
    this.addEnumValue(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.motilitat',
      data?.motilitaet,
      'MotilitaetResultat',
    );
    // Kovergenz
    this.addEnumValue(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.konvergenz',
      data?.konvergenz,
      'InspektionKonvergenzResultat',
    );
    // Strabismus
    this.addFilteredEnumListValue(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.strabismus',
      data?.strabismus,
      'StrabismusResultat',
    );
    this.addEnumValue(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.strabismusAuftreten',
      data?.strabismusAuftreten,
      'StrabismusAuftretenResultat',
    );
    this.addEnumValue(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.strabismusAuspraegung',
      data?.strabismusAuspraegung,
      'StrabismusAuspraegungResultat',
    );
    // Brille / Linsen
    // Brille / Linsen soll angezeigt werden, wenn eines der oberen pathologisch ist
    this.addEnumValueOverride(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.brillelinsen',
      data?.brilleLinsen,
      'BrilleLinsenResultat',
      sehenMap.size > 0,
    );
    // Empfehlung
    this.addFilteredEnumListValue(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.empfehlung',
      data?.empfehlung,
      'SehenEmpfehlung',
    );
    // Bemerkungen
    this.addSimpleValue(
      sehenMap,
      'untersuchung.pathologische.befunde.sehen.bemerkung',
      data?.bemerkungen,
    );
    return sehenMap;
  });
  hoerenAudiogrammMap = computed(() => {
    const hoerenMap: Map<string, string> = new Map<string, string>();
    const data: UntersuchungHoerenAudiogrammSaveJson | undefined = this.hoerenAudiogrammStore.hoerenAudiogramm()?.audiogrammData;
    // Audiogramm
    this.addFilteredEnumListValue(
      hoerenMap,
      'untersuchung.pathologische.befunde.hoeren.audiogrammBeurteilung',
      data?.audiogrammBeurteilung,
      'AudiogrammBeurteilung',
    );
    // Zusatzliche Angaben
    this.addFilteredEnumListValue(
      hoerenMap,
      'untersuchung.pathologische.befunde.hoeren.zusaetzlicheAngaben',
      data?.zusaetzlicheAngaben,
      'HoerenZusaetzlicheAngaben',
    );
    // Empfehlung
    this.addFilteredEnumListValue(
      hoerenMap,
      'untersuchung.pathologische.befunde.hoeren.empfehlung',
      data?.empfehlung,
      'HoerenEmpfehlung',
    );
    // Bemerkungen
    this.addSimpleValue(
      hoerenMap,
      'untersuchung.pathologische.befunde.hoeren.bemerkung',
      data?.bemerkungen,
    );

    return hoerenMap;
  });

  hoerenOtoskopieMap = computed(() => {
    const hoerenOtoskopieMap: Map<string, string> = new Map<string, string>();
    const data: UntersuchungHoerenOtoskopieSaveJson | undefined = this.hoerenOtoskopieStore.hoerenOtoskopie()?.otoskopieData;
    // Otoskopie
    this.addFilteredEnumListValue(
      hoerenOtoskopieMap,
      'untersuchung.pathologische.befunde.hoeren.otoskopieRechts',
      data?.otoskopieRechts,
      'OtoskopieResultat',
    );
    this.addSimpleValue(
      hoerenOtoskopieMap,
      'untersuchung.pathologische.befunde.hoeren.otoskopieRechtsResultatAndere',
      data?.otoskopieRechtsResultatAndere,
    );
    this.addFilteredEnumListValue(
      hoerenOtoskopieMap,
      'untersuchung.pathologische.befunde.hoeren.otoskopieLinks',
      data?.otoskopieLinks,
      'OtoskopieResultat',
    );
    this.addSimpleValue(
      hoerenOtoskopieMap,
      'untersuchung.pathologische.befunde.hoeren.otoskopieLinksResultatAndere',
      data?.otoskopieLinksResultatAndere,
    );
    // Bemerkungen
    this.addSimpleValue(
      hoerenOtoskopieMap,
      'untersuchung.pathologische.befunde.hoeren.bemerkung',
      data?.bemerkungen,
    );
    return hoerenOtoskopieMap;
  });

  groesseGewichtMap = computed(() => {
    const groesseGewichtMap: Map<string, string> = new Map<string, string>();
    const data: UntersuchungGroesseGewichtSaveJson | undefined = this.groesseGewichtStore.groesseGewicht()?.untersuchungGroesseGewichtJson?.data;
    // Groesse
    const groessePathologisch = this.addEnumValue(
      groesseGewichtMap,
      'untersuchung.pathologische.befunde.groessegewicht.groessePerzentile',
      data?.groessePerzentile,
      'PerzentileGroesse',
    );
    if (groessePathologisch) {
      this.addSimpleValue(
        groesseGewichtMap,
        'untersuchung.pathologische.befunde.groessegewicht.groesse',
        data?.groesse,
      );
    }
    // Gewicht
    const gewichtPathologisch = this.addEnumValue(
      groesseGewichtMap,
      'untersuchung.pathologische.befunde.groessegewicht.gewichtPerzentile',
      data?.gewichtPerzentile,
      'PerzentileGewicht',
    );
    if (gewichtPathologisch) {
      this.addSimpleValue(
        groesseGewichtMap,
        'untersuchung.pathologische.befunde.groessegewicht.gewicht',
        data?.gewicht,
      );
    }
    // BMI
    const bmiPathologisch = this.addEnumValue(
      groesseGewichtMap,
      'untersuchung.pathologische.befunde.groessegewicht.bmiPerzentile',
      data?.bmiPerzentile,
      'PerzentileBmi',
    );
    if (bmiPathologisch) {
      const bmi = calculateBmi(data?.groesse, data?.gewicht);
      this.addSimpleValue(
        groesseGewichtMap,
        'untersuchung.pathologische.befunde.groessegewicht.bmi',
        bmi,
      );
    }
    // Bemerkungen
    this.addSimpleValue(
      groesseGewichtMap,
      'untersuchung.pathologische.befunde.groessegewicht.bemerkung',
      data?.bemerkungen,
    );
    return groesseGewichtMap;
  });

  arztMap = computed(() => {
    const arztMap: Map<string, string> = new Map<string, string>();
    const dataBeurteilung: UntersuchungArztBeurteilungSaveJson | undefined = this.arztStore.untersuchungArztBeurteilung()?.data;
    const dataBemerkung: UntersuchungArztBemerkungSaveJson | undefined = this.arztStore.untersuchungArztBemerkung()?.data;
    // Bewegungsapparat
    this.addFilteredEnumListValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.bewegungsapparat',
      dataBeurteilung?.bewegungsapparat,
      'Bewegungsapparat',
    );
    this.addSimpleValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.bewegungsapparat.bemerkung',
      dataBeurteilung?.bewegungsapparatBemerkung,
    );
    // Einbeinstand
    this.addEnumValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.einbeinstand',
      dataBeurteilung?.einbeinstand,
      'Einbeinstand',
    );
    this.addSimpleValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.einbeinstand.bemerkung',
      dataBeurteilung?.einbeinstandBemerkung,
    );
    // Einbeinhuepfen
    this.addEnumValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.einbeinhuepfen',
      dataBeurteilung?.einbeinhuepfen,
      'Einbeinhuepfen',
    );
    this.addSimpleValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.einbeinhuepfen.bemerkung',
      dataBeurteilung?.einbeinhuepfenBemerkung,
    );
    // Fingeropposition
    this.addFilteredEnumListValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.fingeropposition',
      dataBeurteilung?.fingeropposition,
      'Fingeropposition',
    );
    this.addSimpleValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.fingeropposition.bemerkung',
      dataBeurteilung?.fingeroppositionBemerkung,
    );
    // Motorik
    this.addEnumValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.motorik',
      dataBeurteilung?.motorik,
      'Motorik',
    );
    // Verhalten
    this.addEnumValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.verhalten',
      dataBeurteilung?.verhalten,
      'Verhalten',
    );
    // Sprache
    this.addFilteredEnumListValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.sprachfaehigkeit',
      dataBeurteilung?.sprachfaehigkeitMultiValue,
      'Sprachfaehigkeit',
    );
    this.addEnumValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.sprachfaehigkeit',
      dataBeurteilung?.sprachfaehigkeitSingleValue,
      'Sprachfaehigkeit',
    );
    this.addSimpleValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.sprachfaehigkeit.bemerkung',
      dataBeurteilung?.sprachfaehigkeitBemerkung,
    );
    // Kognition
    this.addEnumValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.kognition',
      dataBeurteilung?.kognition,
      'Kognition',
    );
    // Mund und Zähne
    this.addFilteredEnumListValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.mundAndZaehne',
      dataBeurteilung?.mundAndZaehne,
      'MundAndZaehne',
    );
    this.addSimpleValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.mundAndZaehne.bemerkung',
      dataBeurteilung?.mundAndZaehneBemerkung,
    );
    // Haut
    this.addEnumValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.haut',
      dataBeurteilung?.haut,
      'Haut',
    );
    this.addSimpleValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.haut.bemerkung',
      dataBeurteilung?.hautBemerkung,
    );
    // Herzauskultation
    this.addEnumValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.herzauskultation',
      dataBeurteilung?.herzauskultation,
      'Herzauskultation',
    );
    this.addSimpleValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.herzauskultation.bemerkung',
      dataBeurteilung?.herzauskultationBemerkung,
    );
    // Blutdruck
    const blutdruck = this.addEnumValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.blutdruck',
      dataBeurteilung?.blutdruck,
      'Blutdruck',
    );
    if (blutdruck) {
      this.addSimpleValue(
        arztMap,
        'untersuchung.pathologische.befunde.arzt.blutdruck.systolisch',
        dataBeurteilung?.blutdruckSystolisch,
      );
      this.addSimpleValue(
        arztMap,
        'untersuchung.pathologische.befunde.arzt.blutdruck.diastolisch',
        dataBeurteilung?.blutdruckDiastolisch,
      );
    }
    // Bemerkungen
    this.addSimpleValue(
      arztMap,
      'untersuchung.pathologische.befunde.arzt.bemerkungen',
      dataBemerkung?.bemerkungen,
    );
    return arztMap;
  });

  private getFilteredImpfstatus(condition: (item: UntersuchungImpfstatusKrankheitSaveJson) => boolean): string {
    const list = this.impfungenStore.impfstatusList()?.impfstatusList;
    if (list && list.length > 0) {
      return list.filter(item => condition(item))
        .map(status => this.translocoService.translate(`Krankheit.${status.krankheit}`))
        .join(', ');
    }
    return '-';
  }

  private getDurchgefuehrteImpfungen(): string | undefined {
    const list = this.impfungenStore.impfungenInternList();
    if (list && list.length > 0) {
      return list.filter(item => this.isImpfungInDiesemAnlassDurchgefuehrt(item))
        .map(impfung => impfung.data.impfstoff.data.name)
        .join(', ');
    }
    return undefined;
  }

  private isImpfungInDiesemAnlassDurchgefuehrt(item: UntersuchungImpfungInternJson): boolean {
    return item.data.impfStatus === ImpfStatus.DURCHGEFUEHRT
      && item.anlassImpfungDurchgefuehrtId === this.anlassStore.selectedEntity()?.id;
  }

  private addEnumValue(
    map: Map<string, string>,
    key: string,
    enumValue: string | undefined,
    enumClass: string,
  ): boolean {
    return this.addEnumValueOverride(
      map,
      key,
      enumValue,
      enumClass,
      false,
    );
  }

  private addEnumValueOverride(
    map: Map<string, string>,
    key: string,
    enumValue: string | undefined,
    enumClass: string,
    showEvenIfExcluded: boolean,
  ): boolean {
    if (!this.excludes.includes(enumValue) || showEvenIfExcluded) {
      const translated = this.translocoService.translate(enumClass + '.' + enumValue);
      map.set(this.translocoService.translate(key), translated);
      return true;
    }
    return false;
  }

  private addFilteredEnumListValue(
    map: Map<string, string>,
    key: string,
    enumValueList: string[] | undefined,
    enumClass: string,
  ): boolean {
    if (enumValueList) {
      const filtered = enumValueList
        .filter(item => !this.excludes.includes(item))
        .map(item => this.translocoService.translate(enumClass + '.' + item)).join(', ');
      if (filtered) {
        map.set(this.translocoService.translate(key), filtered);
        return true;
      }
    }
    return false;
  }

  private addSimpleValue(map: Map<string, string>, key: string, value: string | number | undefined): void {
    if (value) {
      map.set(this.translocoService.translate(key), '' + value);
    }
  }
}
