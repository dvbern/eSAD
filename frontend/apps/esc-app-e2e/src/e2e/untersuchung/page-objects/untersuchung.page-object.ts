import { patternBackendEntityId } from '../../../util/url-e2e.utils';
import { FormSelectComponentObject } from '../../../component-objects/form-select.component-object';
import {FormCheckboxComponentObject} from "../../../component-objects/form-checkbox.component-object";
import { FormInputComponentObject } from '../../../component-objects/form-input.component-object';
import {WithSubpaths} from "../../../page-objects/with-subpahts";

export class UntersuchungDatenPageObject extends WithSubpaths<
  'daten' | 'impfungen' | 'sehen' | 'hoeren' | 'groessegewicht' | 'arzt' | 'massnahmen'
> {

  selector = "feat-anlass-detail-page"

  basePath = `/anlass/${patternBackendEntityId}/untersuchung/einladung/${patternBackendEntityId}`

  paths = {
    daten : `${this.basePath}/untersuchungdaten`,
    impfungen: `${this.basePath}/impfungen`,
    sehen: `${this.basePath}/sehen`,
    hoeren: `${this.basePath}/hoeren`,
    groessegewicht: `${this.basePath}/groessegewicht`,
    arzt: `${this.basePath}/arzt`,
    massnahmen: `${this.basePath}/massnahmen`
  }

  elements = {
    untersuchungTabImpfungen: () => this.page.getByTestId('untersuchung-tab-impfungen'),
    untersuchungTabSehen: () => this.page.getByTestId('untersuchung-tab-sehen'),
    untersuchungTabHoeren: () => this.page.getByTestId('untersuchung-tab-hoeren'),
    untersuchungTabGroesseGewicht: () => this.page.getByTestId('untersuchung-tab-groesse-gewicht'),
    untersuchungTabArzt: () => this.page.getByTestId('untersuchung-tab-arzt'),
    untersuchungTabMassnahmen: () => this.page.getByTestId('untersuchung-tab-massnahmen'),
    untersuchungSehenDefaultWert: () => this.page.getByTestId('untersuchung-sehen-defaultwerte'),
    untersuchungHoerenDefaultWert: () => this.page.getByTestId('untersuchung-hoeren-defaultwerte'),

    // buttons
    buttonSaveDaten: () => this.page.getByTestId('button-confirm'),
    buttonSaveImpfstatus: () => this.page.locator('feat-untersuchung-impfstatus').getByTestId('button-confirm'),
    buttonVisieren: () => this.page.getByTestId('button-visieren'),
    buttonAufgabenNav: () => this.page.getByTestId('button-aufgaben-list'),
    buttonCreateImpfung: () => this.page.getByTestId('button-create-impfung'),
    buttonSaveImpfChecklist: () => this.page.locator('feat-untersuchung-impfungen-checklist').getByTestId('button-confirm'),
    buttonSaveImpfung: () => this.page.locator('feat-dialogs-modal').getByTestId('button-confirm'),
    buttonSaveMassnahmenImpf: () => this.page.locator('feat-untersuchung-impfungen-massnahmen').getByTestId('button-confirm'),
    buttonResetFilterAufgaben: () => this.page.getByTestId('button-reset-filter'),
    buttonSaveSehen: () => this.page.locator('feat-untersuchung-sehen-beurteilung').getByTestId('button-confirm'),
    buttonSaveAudiogramm: () => this.page.locator('feat-untersuchung-hoeren-audiogramm').getByTestId('button-confirm'),
    buttonSaveOtoskopie: () => this.page.locator('feat-untersuchung-hoeren-otoskopie').getByTestId('button-confirm'),
    buttonSaveGroesseGewicht: () => this.page.locator('feat-untersuchung-groessegewicht').getByTestId('button-confirm'),
    buttonSaveArzt: () => this.page.locator('feat-untersuchung-arzt-beurteilung').getByTestId('button-confirm'),
    buttonSaveMassnahmen: () => this.page.locator('feat-untersuchung-massnahmen').getByTestId('button-confirm'),

    // checkbox
    checkboxChecklist: () => new FormCheckboxComponentObject('checkbox-checklist'),

    // select
    schularztSelect: () => new FormSelectComponentObject('schularzt-select', 'svui-form-control'),
    mpaSelect: () => new FormSelectComponentObject('mpa-select', 'svui-form-control'),
    visumSelect: () => new FormSelectComponentObject('visum-select', 'svui-form-control'),
    ImpfstoffSelect: () => new FormSelectComponentObject('modal-impfstoff-select', 'svui-form-control'),
    chargeSelect: () => new FormSelectComponentObject('modal-charge-select', 'svui-form-control'),
    informationElternSelect: () => new FormSelectComponentObject('information-eltern-select', 'svui-form-control'),

    // input
    groesseInput: () => new FormInputComponentObject('untersuchung-groesse-input'),
    gewichtInput: () => new FormInputComponentObject('untersuchung-gewicht-input'),
    systolischInput: () => new FormInputComponentObject('untersuchung-systolisch-input'),
    diastolischInput: () => new FormInputComponentObject('untersuchung-diastolisch-input'),
  }

}
