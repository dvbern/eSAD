import {ChangeDetectionStrategy, Component, computed, input} from '@angular/core';
import {isNullish} from '@dvb/util-shared';
import {NgbTooltip} from '@ng-bootstrap/ng-bootstrap';
import {
  bootstrapExclamationCircleFill,
  bootstrapGenderAmbiguous,
  bootstrapGenderFemale,
  bootstrapGenderMale,
  bootstrapInfoCircle,
  bootstrapInfoCircleFill,
  bootstrapQuestion,
} from '@ng-icons/bootstrap-icons';
import {NgIconComponent} from '@ng-icons/core';
import {heroBars3} from '@ng-icons/heroicons/outline';
import {
  heroArrowDownTraySolid,
  heroArrowPathSolid,
  heroArrowTopRightOnSquareSolid,
  heroArrowUpTraySolid,
  heroBookOpenSolid,
  heroCakeSolid,
  heroCalendarDaysSolid,
  heroCheckCircleSolid,
  heroCheckSolid,
  heroChevronDoubleLeftSolid,
  heroChevronDoubleRightSolid,
  heroChevronDownSolid,
  heroChevronLeftSolid,
  heroChevronRightSolid,
  heroChevronUpSolid,
  heroClipboardSolid,
  heroClockSolid,
  heroEllipsisVerticalSolid,
  heroEyeSolid,
  heroFlagSolid,
  heroFunnelSolid,
  heroLanguageSolid,
  heroLinkSolid,
  heroMagnifyingGlassSolid,
  heroMapPinSolid,
  heroMicrophoneSolid,
  heroPencilSolid,
  heroPlusSolid,
  heroScaleSolid,
  heroShieldCheckSolid,
  heroTrashSolid,
  heroUserSolid,
  heroXMarkSolid,
} from '@ng-icons/heroicons/solid';
import {readCssVar} from '../../styles-helper/styles-helper';

// please sort alphabetically
export const IconUseCases = [
  'actionmenu',
  'aktualisieren',
  'aufgaben',
  'aufgeklappt',
  'birthdate',
  'check',
  'check-circle',
  'close',
  'divers',
  'doctor',
  'edit',
  'escada_item',
  'eye',
  'female',
  'filter',
  'first',
  'flag',
  'herunterladen',
  'hinzufuegen',
  'impfausweis',
  'information',
  'kalender',
  'language',
  'last',
  'link',
  'location',
  'loeschen',
  'male',
  'menu',
  'mpa',
  'neu',
  'next',
  'notes',
  'placeholder_seethrough',
  'previous',
  'record',
  'school',
  'sex_unknown',
  'sortieren',
  'suchen',
  'tabelleneintrag',
  'timetravel',
  'tooltip_icon',
  'uhrzeit',
  'upload',
  'user',
  'vaccinated',
  'vaccination_genuegend',
  'vaccination_planned',
  'vaccination_recommended',
  'warning',
  'weight',
  'zugeklappt',
  'zurueck',
] as const;

export type IconUseCase = typeof IconUseCases[number];

@Component({
  selector: 'svui-icon',
  templateUrl: './icon.component.html',
  styleUrl: './icon.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIconComponent, NgbTooltip],
})
export class IconComponent {

  readonly useCase = input.required<IconUseCase>();
  readonly tooltip = input<string>();

  size = computed<string>(() => readCssVar('icon-size'));

  svg = computed(() => this.getIconSvg(this.useCase()));
  customIconFilename = computed(() => this.getCustomIconFileName(this.useCase()));

  /*
   * RULES:
   * - 1. Main Icon Library: HERO (solid if it looks good; feel free to use other variants if they look better)
   * - 2. Fallback Library: BOOTSTRAP
   * - 3. No suitable icon found? Look in other libs: https://ng-icons.github.io/ng-icons/#/browse-icons
   * - 4. Custom icon needed desperately? Follow assets/icons/README.md
   */
  getIconSvg(iconUseCase: IconUseCase): string | null {
    switch (iconUseCase) {

      // please sort alphabetically
      //@formatter:off

      case 'actionmenu': return heroEllipsisVerticalSolid;
      case 'aktualisieren': return heroArrowPathSolid;
      case 'aufgaben': return null;
      case 'aufgeklappt': return heroChevronUpSolid;
      case 'birthdate': return heroCakeSolid;
      case 'check': return heroCheckSolid;
      case 'check-circle': return heroCheckCircleSolid;
      case 'close': return heroXMarkSolid;
      case 'divers': return bootstrapGenderAmbiguous;
      case 'doctor': return null;
      case 'edit': return heroPencilSolid;
      case 'escada_item' : return heroLinkSolid;
      case 'eye': return heroEyeSolid;
      case 'female': return bootstrapGenderFemale;
      case 'filter': return heroFunnelSolid;
      case 'first': return heroChevronDoubleLeftSolid;
      case 'flag': return heroFlagSolid;
      case 'herunterladen': return heroArrowDownTraySolid;
      case 'hinzufuegen': return heroPlusSolid;
      case 'impfausweis': return heroBookOpenSolid;
      case 'information': return bootstrapInfoCircleFill;
      case 'kalender': return heroCalendarDaysSolid;
      case 'language': return heroLanguageSolid;
      case 'last': return heroChevronDoubleRightSolid;
      case 'link': return heroArrowTopRightOnSquareSolid;
      case 'location': return heroMapPinSolid;
      case 'loeschen': return heroTrashSolid;
      case 'male': return bootstrapGenderMale;
      case 'menu': return heroBars3;
      case 'mpa': return null;
      case 'neu': return heroPlusSolid;
      case 'next': return heroChevronRightSolid;
      case 'notes': return heroClipboardSolid;
      case 'placeholder_seethrough': return null;
      case 'previous': return heroChevronLeftSolid;
      case 'record': return heroMicrophoneSolid;
      case 'school': return null;
      case 'sex_unknown': return bootstrapQuestion;
      case 'sortieren': return heroChevronDownSolid;
      case 'suchen': return heroMagnifyingGlassSolid;
      case 'tabelleneintrag': return heroChevronRightSolid;
      case 'timetravel': return heroClockSolid;
      case 'tooltip_icon': return bootstrapInfoCircle;
      case 'uhrzeit': return heroClockSolid;
      case 'upload': return heroArrowUpTraySolid;
      case 'user': return heroUserSolid;
      case 'vaccinated': return heroShieldCheckSolid;
      case 'vaccination_genuegend': return null;
      case 'vaccination_planned': return null;
      case 'vaccination_recommended': return null;
      case 'warning': return bootstrapExclamationCircleFill;
      case 'weight': return heroScaleSolid;
      case 'zugeklappt': return heroChevronDownSolid;
      case 'zurueck': return heroChevronLeftSolid;

      //@formatter:on
      default:
        // exhaustiveness-check
        iconUseCase satisfies never;
        return 'invalid-icon';
    }
  }

  getCustomIconFileName(iconUseCase: IconUseCase): string | null {
    switch (iconUseCase) {
      // please sort alphabetically
      //@formatter:off

      case 'aufgaben': return '_Icon_Consent';
      case 'doctor': return '_Icon_Doctor';
      case 'mpa': return '_Icon_MPA';
      case 'school': return '_Icon_School';
      case 'vaccination_genuegend': return '_Icon_Vaccination_Genuegend';
      case 'vaccination_recommended': return '_Icon_Vaccination_Recommended';
      case 'vaccination_planned': return '_Icon_Vaccination_Planned';
      case 'placeholder_seethrough': return '_Icon_Placeholder_Seethrough';

      //@formatter:on
      default:
        if (isNullish(this.getIconSvg(iconUseCase))) {
          throw Error('missing icon: ' + iconUseCase);
        }
        return null;
    }
  }

}
