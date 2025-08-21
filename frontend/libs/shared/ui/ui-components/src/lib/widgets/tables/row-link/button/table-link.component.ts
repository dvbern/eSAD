import {ChangeDetectionStrategy, Component, computed, input} from '@angular/core';
import {Params, RouterLink, UrlTree} from '@angular/router';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbTooltip} from '@ng-bootstrap/ng-bootstrap';
import {LikeAButtonComponent} from '../../../buttons';

export const RowLinkTypes = [
  'icon',
  'text'
] as const;

export type RowLinkType = typeof RowLinkTypes[number];

@Component({
  selector: 'svui-table-link',
  standalone: true,
  templateUrl: './table-link.component.html',
  styleUrl: './table-link.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    LikeAButtonComponent,
    NgbTooltip,
    TranslocoPipe,
    RouterLink,
  ],
})
export class TableLinkComponent {

  type = input<RowLinkType>('icon');

  // prefix because we need to avoid using the original Angular attributes
  myRouterLink = input<string | any[] | UrlTree | null | undefined>();
  // prefix because we need to avoid using the original Angular attributes
  myRouterQueryParams = input<Params>();
  // prefix because we need to avoid using the original Angular attributes
  myTarget = input<string>();

  disabled = computed(() => !this.myRouterLink());



}

