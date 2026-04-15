import { ChangeDetectionStrategy, Component } from '@angular/core';
import { AppHeaderComponent } from '../../app-header/app-header.component';
import { WithHeadingDirective } from '../../../widgets';
import { PageHeaderComponent } from '../page-header/page-header.component';
import {AppFooterComponent} from "../../app-footer/app-footer.component";

@Component({
  selector: 'svui-page-layout',
  templateUrl: './page-layout.component.html',
  styleUrl: './page-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    AppHeaderComponent,
    PageHeaderComponent,
    AppFooterComponent,
  ],
})
export class PageLayoutComponent extends WithHeadingDirective {}
