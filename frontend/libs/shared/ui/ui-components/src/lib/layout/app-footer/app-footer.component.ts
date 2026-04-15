import {ChangeDetectionStrategy, Component} from "@angular/core";

@Component({
  selector: 'svui-app-footer',
  templateUrl: './app-footer.component.html',
  styleUrl: './app-footer.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: []
})
export class AppFooterComponent {
}
