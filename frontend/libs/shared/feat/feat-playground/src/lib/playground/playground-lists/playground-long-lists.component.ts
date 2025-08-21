import {ChangeDetectionStrategy, Component} from '@angular/core';
import {FakeBackend} from './list/playground-liste.fake-backend';
import {PlaygroundLongListComponent} from './list/playground-long-list.component';

@Component({
  selector: 'feat-playground-long-list-examples',
  templateUrl: './playground-long-lists.component.html',
  styleUrl: './playground-long-lists.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PlaygroundLongListComponent,
  ],
  providers: [FakeBackend],
})
export class PlaygroundLongListsComponent {

}

