import {ChangeDetectionStrategy, Component, output} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Path} from '@dvb/feat-shared';
import {
  ButtonComponent,
  CardComponent,
  CheckboxDirective,
  FormButtonsComponent,
  FormLayoutComponent,
  HeadingDirective,
  KachelControlComponent,
  KachelFormSectionComponent,
  RadioDirective,
  SelectItem,
} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {fromISODateString} from '@dvb/util-shared';
import {delay, Observable, of} from 'rxjs';
import {PlaygroundBorderedFormInputForm} from './playground-bordered-form-input.form';
import {PlaygroundDemoStates, PlaygroundFormOutputType} from './playground-form.types';

@Component({
    selector: 'feat-playground-form-input',
    templateUrl: './playground-bordered-form-input.component.html',
    styleUrl: './playground-bordered-form-input.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        FormsModule,
        CardComponent,
        ButtonComponent,
        RadioDirective,
        ReactiveFormsModule,
        CardComponent,
        HeadingDirective,
        CheckboxDirective,
        KachelFormSectionComponent,
        KachelControlComponent,
        FormLayoutComponent,
        FormButtonsComponent,
        KachelControlComponent,
    ]
})
export class PlaygroundBorderedFormInputComponent {
  file: string | undefined = 'Datei.pdf';
  readonly form = new PlaygroundBorderedFormInputForm();
  mode: DetailMode = DetailMode.EDIT;

  readonly formValue = output<PlaygroundFormOutputType>();

  selectItems: SelectItem[] = [
    {value: 'eins', label: 'Eins'},
    {value: 'zwei', label: 'Zwei'},
    {value: 'drei', label: 'Drei'},
    {value: '4', label: 'Vier'},
    {value: '5', label: 'Fünf'},
    {value: '6', label: 'Sechs'},
    {value: '7', label: 'Sieben'},
    {value: '8', label: 'Acht'},
  ];

  /**
   * Zum erstellen der Captchas gibt es einen API-Call UnauthenticatedService.getCaptchaImage();
   */
  captchaId = 0;
  captchaWidthPx = 200;
  captchaHeightPx = 50;

  captcha = [
    `data:image/png;base64,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`,
    `data:image/png;base64,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`,
  ];

  onRefresh(): void {
    this.captchaId = this.captchaId === 0 ? 1 : 0;
  }

  formatter = (state: string): string => state;

  delayMs = 1200;

  getResultsForTerm$(term: string): Observable<string[]> {
    return term === ''
      ? of(PlaygroundDemoStates).pipe(delay(this.delayMs))
      : of(PlaygroundDemoStates.filter(v => v.toLowerCase().indexOf(term.toLowerCase()) > -1)).pipe(
        delay(this.delayMs),
      );
  }

  async submit(): Promise<void> {
    if (await this.form.validate()) {
      console.log('submitting valid form');
      this.formValue.emit(this.form.valueOnValid());
    }
  }

  reset(): void {
    this.form.reset();
  }

  loeschen(): void {
    this.file = undefined;
  }

  fillRandom(): void {
    this.form.group.setValue({
      typeahead: 'dhfsdfds',
      captcha: 'sdfsdf',
      text: 'Abrakadabra',
      email: 'blablbala@iuoieurew.ch',
      date: '2024-05-30',
      time: '14:35',
      radios: 'YELLOW',
      radios1: 'RED',
      radios2: 'RED',
      checkbox: true,
      select: 'abd',
      multiSelect: ['zwei', 'drei'],
      textarea: 'Hans im Glück',
      typeahead2: 'Blaumeise',
      text2: 'werwere',
      text3: 'sdf',
      text4: '234',
      select2: '8koiuij',
    });
  }

  protected readonly fromISODateString = fromISODateString;
  protected readonly DetailMode = DetailMode;
  protected readonly Path = Path;
}
