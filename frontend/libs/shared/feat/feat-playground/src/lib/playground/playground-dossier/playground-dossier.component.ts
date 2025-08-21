import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  BadgeComponent,
  CardComponent,
  CardHeaderComponent,
  CardSectionComponent,
  CheckboxDirective,
  CollapsibleMenuComponent,
  FancyListItemComponent,
  IconComponent,
  LayoutColumnsComponent,
  ListComponent,
  NgSelectDirective,
  StandaloneControlComponent,
  StatusTextComponent,
  TileComponent,
  TileGridComponent,
} from '@dvb/ui-components';
import { TranslocoPipe } from '@jsverse/transloco';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';

@Component({
    selector: 'feat-playground-dossier',
    templateUrl: './playground-dossier.component.html',
    styleUrl: './playground-dossier.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CardComponent,
        LayoutColumnsComponent,
        TileGridComponent,
        TileComponent,
        IconComponent,
        NgSelectComponent,
        NgOptionComponent,
        NgSelectDirective,
        TranslocoPipe,
        FormsModule,
        CardHeaderComponent,
        StatusTextComponent,
        FancyListItemComponent,
        BadgeComponent,
        ListComponent,
        CheckboxDirective,
        StandaloneControlComponent,
        CollapsibleMenuComponent,
        CardSectionComponent,
    ]
})
export class PlaygroundDossierComponent {
  protected readonly alert = alert;

  exampleDate = new Date();
}
