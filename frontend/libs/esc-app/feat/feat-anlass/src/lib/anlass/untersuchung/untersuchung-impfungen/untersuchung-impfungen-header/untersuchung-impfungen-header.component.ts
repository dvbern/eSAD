import {CommonModule, DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject, input, model, output} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {EinladungJson} from '@dvb/data-access-backend';
import {AnlassStore} from '../../../anlass.store';
import {BenutzerStore} from '@dvb/feat-shared';
import {CheckboxDirective, IconOnlyButtonComponent} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {hasImpfungenEditBerechtigung} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';

@Component({
  selector: 'feat-untersuchung-impfungen-header',
  templateUrl: './untersuchung-impfungen-header.component.html',
  styleUrl: './untersuchung-impfungen-header.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule,
    TranslocoPipe,
    CheckboxDirective,
    ReactiveFormsModule,
    DatePipe,
    IconOnlyButtonComponent,
  ]
})
export class UntersuchungImpfungenHeaderComponent {

  readonly transloco = inject(TranslocoService);
  readonly benutzerStore = inject(BenutzerStore);
  readonly anlassStore = inject(AnlassStore);

  protected readonly DetailMode = DetailMode;

  einladung = input<EinladungJson>();
  impfausweisUnvollstaendig = model.required<boolean>();
  impfausweisNichtVorhanden = model<boolean>(false);
  impfausweisZweiPlusEinsSchema = model<boolean>(false);

  hasImpfungenEditBerechtigung = this.benutzerStore.createHasRequiredRoleSig(hasImpfungenEditBerechtigung());

  impfausweisUnvollstaendigChanged = output<boolean>();
  impfausweisNichtVorhandenChanged = output<boolean>();
  impfausweisZweiPlusEinsSchemaChanged = output<boolean>();
  gotoImpfausweisClicked = output<void>();


  public onImpfausweisUnvollstaendigChanged(): void {
    this.impfausweisUnvollstaendigChanged.emit(this.impfausweisUnvollstaendig());
  }

  public onImpfausweisNichtVorhandenChanged(): void {
    this.impfausweisNichtVorhandenChanged.emit(this.impfausweisNichtVorhanden());
  }

  public onImpfausweisZweiPlusEinsSchemaChanged(): void {
    this.impfausweisZweiPlusEinsSchemaChanged.emit(this.impfausweisZweiPlusEinsSchema());
  }

  public onGotoImpfausweisClicked(): void {
    this.gotoImpfausweisClicked.emit();
  }
}
