import {
  ChangeDetectionStrategy,
  Component,
  ComponentRef,
  effect,
  HostBinding,
  HostListener,
  inject,
  input,
  model,
  OnInit,
  ViewContainerRef,
} from '@angular/core';
import {NgControl, ReactiveFormsModule} from '@angular/forms';
import {NgbTypeahead} from '@ng-bootstrap/ng-bootstrap';
import {
  debounceTime,
  distinctUntilChanged,
  filter,
  map,
  merge,
  Observable,
  OperatorFunction,
  Subject,
  switchMap,
} from 'rxjs';
import {ControlType, ValidatedInputWithIdDirective} from '../validated-input-with-id.directive';
import {TypeaheadNoResultsComponent} from './typeahead-no-results/typeahead-no-results.component';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'input[svuiTypeahead]',
  templateUrl: './typeahead.component.html',
  styleUrl: 'typeahead.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  hostDirectives: [NgbTypeahead],
  providers: [
    {
      // See comment on ValidatedInputWithIdDirective for reasons
      provide: ValidatedInputWithIdDirective,
      useExisting: TypeaheadComponent,
    },
  ],
  imports: [ReactiveFormsModule],
})
export class TypeaheadComponent<T> extends ValidatedInputWithIdDirective implements OnInit {
  // type must be specified in caller explicitly, else Angular won't get it
  readonly type = input<'text'>('text');

  readonly openOnFocus = input<boolean>(false);
  readonly noResultsMessage = input.required<string>();
  readonly maxVisibleResults = input<number>();

  readonly filterFn = input.required<(term: string) => Observable<T[]>>();
  readonly formatter = input.required<(option: T) => string>();

  readonly focus$ = new Subject<string>();
  readonly click$ = new Subject<string>();

  override readonly working = model<boolean>(false);
  readonly noResults = model<boolean>(false);

  messageComponent?: ComponentRef<TypeaheadNoResultsComponent>;

  @HostBinding('class.form-control') readonly bootstrapClass = true;

  @HostListener('click', ['$event'])
  onClick(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    if (!target) {
      return;
    }
    this.click$.next(target.value);
  }

  @HostListener('focus', ['$event'])
  onFocus(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    if (!target) {
      return;
    }
    this.focus$.next(target.value);
  }

  get controlType(): ControlType {
    return 'typeahead';
  }

  get isLabelFloatable(): boolean {
    return true;
  }

  public ngbTypaheadInst = inject(NgbTypeahead, { optional: false, self: true });
  private readonly viewContainerRef = inject(ViewContainerRef);

  constructor() {
    const ngCtrl = inject(NgControl, { optional: true, self: true });
    super(ngCtrl);
    this.configureTypeahead();
    this.passInputSignals();
  }

  private configureTypeahead(): void {
    this.ngbTypaheadInst.ngbTypeahead = this.searchFn;
    this.ngbTypaheadInst.editable = false;
  }

  private passInputSignals(): void {
    // pass on our input signals
    effect(() => {
      const noResults = this.noResults();
      const noResultsMessage = this.noResultsMessage();
      if (this.messageComponent) {
        this.messageComponent.setInput('noResults' satisfies keyof TypeaheadNoResultsComponent, noResults);
        this.messageComponent.setInput(
          'noResultsMessage' satisfies keyof TypeaheadNoResultsComponent,
          noResultsMessage,
        );
      }
    });

    effect(() => {
      this.ngbTypaheadInst.resultFormatter = this.formatter();
      this.ngbTypaheadInst.inputFormatter = this.formatter();
      // Beim Setzen der Formatter werden eventuell bereits angezeigte Werte nicht korrekt formatiert.
      // Deshalb setzen wir den Wert des Controls erneut.
      if (this.ngControl) {
        this.ngControl.reset(this.ngControl.value);
      }
    });
  }

  ngOnInit(): void {
    this.addMessage();
  }

  private addMessage(): void {
    this.messageComponent = this.viewContainerRef.createComponent(TypeaheadNoResultsComponent);
    // do not pass the inputs here directly because it does not work properly -> use effect
  }

  searchFn: OperatorFunction<string, readonly T[]> = (text$: Observable<string>) => {
    const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
    const clicksWithClosedPopup$ = this.click$.pipe(filter(() => !this.ngbTypaheadInst.isPopupOpen()));
    const inputFocus$ = this.focus$;

    const triggers$ = this.openOnFocus() ? merge(debouncedText$, inputFocus$, clicksWithClosedPopup$) : debouncedText$;

    return triggers$.pipe(
      switchMap(term => {
        console.debug('typeahead search', term);
        this.working.set(true);
        this.noResults.set(false);
        return this.filterFn()(term).pipe(
          map(results => {
            this.working.set(false);
            if (!results.length) {
              this.noResults.set(true);
            }
            return this.maxVisibleResults() ? results.slice(0, this.maxVisibleResults()) : results;
          }),
        );
      }),
    );
  };
}
