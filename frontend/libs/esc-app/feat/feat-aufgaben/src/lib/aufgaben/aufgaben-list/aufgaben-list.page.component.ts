import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {AufgabenStore, BenutzerOptionsStore} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {AufgabenListComponent} from '@dvb/feat-shared';;
import {PageHeaderAddButtonComponent} from '@dvb/ui-components';
import {hasAufgabenEditBerechtigungen} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
    selector: 'feat-aufgaben',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        PageComponent,
        TranslocoPipe,
        ReactiveFormsModule,
        PageHeaderAddButtonComponent,
        AufgabenListComponent,
    ],
    templateUrl: './aufgaben-list.page.component.html',
    styleUrl: './aufgaben-list.page.component.scss'
})
export class AufgabenListPageComponent {
  readonly store = inject(AufgabenStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  protected readonly Path = Path;

  canEditAufgabe = this.benutzerStore.createHasRequiredRoleSig(hasAufgabenEditBerechtigungen());

}
