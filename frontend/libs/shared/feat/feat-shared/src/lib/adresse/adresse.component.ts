import {ChangeDetectionStrategy, Component, effect, inject, input, OnDestroy} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Land} from '@dvb/data-access-backend';
import {FormControlComponent, FormLayoutComponent, NgSelectDirective, TextInputDirective} from '@dvb/ui-components';
import {DetailMode} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AdresseForm} from './adresse.form';
import {AdresseStore} from './store/adresse.store';

@Component({
  selector: 'feat-adresse',
  templateUrl: './adresse.component.html',
  styleUrl: './adresse.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    TranslocoPipe,
    FormControlComponent,
    TextInputDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    FormLayoutComponent,
  ],
})
export class AdresseComponent implements OnDestroy {

  form = input.required<AdresseForm>();
  mode = input.required<DetailMode>();
  isEscadaObject = input.required<boolean>();

  nationalitaeten = Object.values(Land);
  adresseStore = inject(AdresseStore);

  constructor() {
    effect(() => {
      const list = this.adresseStore.searchResults()[this.form().parentIdentifier]?.plzDataList || [];
      if (list.length) {
        this.form().group.controls.ort.setValue(list[0].ortsBezeichnung);
      }
    });
  }

  ngOnDestroy(): void {
    this.adresseStore.resetSearchResult(this.form().parentIdentifier);
  }
}
