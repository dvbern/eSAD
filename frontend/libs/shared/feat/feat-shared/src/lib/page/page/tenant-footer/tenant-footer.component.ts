import {ChangeDetectionStrategy, Component, input} from "@angular/core";
import {TranslocoPipe} from "@jsverse/transloco";
import {TenantEnum} from "@dvb/data-access-backend";

@Component({
  selector: 'feat-page-tenant-footer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe
  ],
  templateUrl: './tenant-footer.component.html',
  styleUrl: './tenant-footer.component.scss',
})
export class TenantFooterComponent {

  tenant = input<TenantEnum>();
  description= input<string>();
}
