import {ChangeDetectionStrategy, Component, input} from "@angular/core";
import {RouterLink} from "@angular/router";
import {TranslocoPipe} from "@jsverse/transloco";
import {TenantEnum} from "@dvb/data-access-backend";
import {IconComponent} from "@dvb/ui-components";
import {Path} from "../../../core/path";

@Component({
  selector: 'feat-page-tenant-footer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    RouterLink,
    IconComponent
  ],
  templateUrl: './tenant-footer.component.html',
  styleUrl: './tenant-footer.component.scss',
})
export class TenantFooterComponent {

  tenant = input<TenantEnum>();
  description= input<string>();
  showDoc365Link = input<boolean>(false);
  Path = Path;
  readonly TenantEnum = TenantEnum;
}
