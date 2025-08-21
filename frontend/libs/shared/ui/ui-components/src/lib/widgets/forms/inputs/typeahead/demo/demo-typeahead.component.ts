import { JsonPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { delay, Observable, of } from 'rxjs';
import { FormControlComponent } from '../../../controls/form-control';
import { TypeaheadComponent } from '../typeahead.component';

const states = [
  'Alabama',
  'Alaska',
  'American Samoa',
  'Arizona',
  'Arkansas',
  'California',
  'Colorado',
  'Connecticut',
  'Delaware',
  'District Of Columbia',
  'Federated States Of Micronesia',
  'Florida',
  'Georgia',
  'Guam',
  'Hawaii',
  'Idaho',
  'Illinois',
  'Indiana',
  'Iowa',
  'Kansas',
  'Kentucky',
  'Louisiana',
  'Maine',
  'Marshall Islands',
  'Maryland',
  'Massachusetts',
  'Michigan',
  'Minnesota',
  'Mississippi',
  'Missouri',
  'Montana',
  'Nebraska',
  'Nevada',
  'New Hampshire',
  'New Jersey',
  'New Mexico',
  'New York',
  'North Carolina',
  'North Dakota',
  'Northern Mariana Islands',
  'Ohio',
  'Oklahoma',
  'Oregon',
  'Palau',
  'Pennsylvania',
  'Puerto Rico',
  'Rhode Island',
  'South Carolina',
  'South Dakota',
  'Tennessee',
  'Texas',
  'Utah',
  'Vermont',
  'Virgin Islands',
  'Virginia',
  'Washington',
  'West Virginia',
  'Wisconsin',
  'Wyoming',
];

@Component({
    selector: 'svui-playground-typeahead',
    templateUrl: './demo-typeahead.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [FormsModule, FormControlComponent, TypeaheadComponent, JsonPipe]
})
export class DemoTypeaheadComponent {
  readonly openOnFocus = input<boolean>(false);
  readonly label = input<string>('Typeahead');

  model: string | undefined;
  delayMs = 1200;

  formatter = (state: string): string => state + ' (' + state.length + ' characters)';

  getResultsForTerm$(term: string): Observable<string[]> {
    return term === ''
      ? of(states).pipe(delay(this.delayMs))
      : of(states.filter(v => v.toLowerCase().indexOf(term.toLowerCase()) > -1)).pipe(delay(this.delayMs));
  }
}
