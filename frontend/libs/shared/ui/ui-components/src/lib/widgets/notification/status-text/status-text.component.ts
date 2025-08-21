import {NgTemplateOutlet} from '@angular/common';
import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
import {IconComponent, IconUseCase} from '../../icons/icon';

export const StatusColorTypes = ['danger', 'success', 'info'];
export type StatusColorType = typeof StatusColorTypes[number];

@Component({
    selector: 'svui-status-text',
    templateUrl: './status-text.component.html',
    styleUrl: './status-text.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [IconComponent, NgTemplateOutlet]
})
export class StatusTextComponent {
  readonly icon = input.required<IconUseCase>();
  readonly buttonRouterLink = input<string>();
  readonly statusColor = input.required<StatusColorType>();

  readonly hasClickOutput = input<boolean>();
  readonly clickAction = output();
}
