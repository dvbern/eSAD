import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {AdresseJson, Geschlecht} from '@dvb/data-access-backend';
import {
  BenutzerOptionsStore,
  BenutzerStore,
  ConfirmService,
  Path,
  TimetravelStore,
  TranslatedStringPipe,
} from '@dvb/feat-shared';
import {
  BadgeComponent,
  EmptyListMessageComponent,
  FancyListItemComponent,
  IconUseCase,
  ListComponent,
  PageHeaderAddButtonComponent,
} from '@dvb/ui-components';
import {
  compareByEntityInfoTimestampErstellt,
  fromISODateString,
  fromISODateTimeString,
  hasNotizEditBerechtigung,
  isNullish,
  joinNotBlank,
} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {DossierStore} from '../../dossier.store';
import {KinderNotizStore} from '../../kinder-notiz/kinder-notiz/kinder-notiz.store';

@Component({
  selector: 'feat-kind-notizen-list',
  templateUrl: './kind-dossier-notizen.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    FormsModule,
    RouterLink,
    ListComponent,
    BadgeComponent,
    FancyListItemComponent,
    TranslatedStringPipe,
    EmptyListMessageComponent,
    PageHeaderAddButtonComponent,
  ],
})
export class KindDossierNotizenComponent {

  readonly dossierStore = inject(DossierStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly notizenStore = inject(KinderNotizStore);
  readonly confirmService = inject(ConfirmService);
  readonly transloco = inject(TranslocoService);
  readonly timetravelStore = inject(TimetravelStore);

  dossier = computed(() => this.dossierStore.selectedDossier());
  kind = computed(() => this.dossier()?.kind);
  fullName = computed(() => joinNotBlank([this.kind()?.data?.vorname, this.kind()?.data?.name]));
  geschlecht = computed(() => this.getUseCaseGechlecht(this.kind()?.data?.geschlecht));
  adresse = computed(() => this.getAdresse(this.kind()?.data?.adresse));
  notizen = computed(
    () => this.notizenStore.entities()
      .sort(compareByEntityInfoTimestampErstellt),
  );

  canEditNotizen = computed(() =>
    this.benutzerStore.createHasRequiredRoleSig(hasNotizEditBerechtigung())() &&
    this.dossierStore.isKindAktiv()
  );
  protected readonly Path = Path;
  protected readonly fromISODateTimeString = fromISODateTimeString;
  protected readonly fromISODateString = fromISODateString;

  private getUseCaseGechlecht(geschlecht: Geschlecht | undefined): IconUseCase {
    if (isNullish(geschlecht)) {
      return 'divers';
    }

    switch (geschlecht) {
      case 'MAENNLICH':
        return 'male';
      case 'WEIBLICH':
        return 'female';
      case 'DIVERS':
        return 'divers';
      case 'UNBEKANNT':
        return 'placeholder_seethrough';
    }
  }

  private getAdresse(adresse: AdresseJson | undefined): string {
    if (isNullish(adresse)) {
      return '';
    }
    return `${adresse.strasseUndNr}, ${adresse?.plz} ${adresse.ort}`;
  }

}
