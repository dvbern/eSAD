import {
  ComponentRef,
  DestroyRef,
  Directive,
  effect,
  ElementRef,
  HostBinding,
  HostListener,
  inject,
  input,
  model,
  OnInit,
  Optional,
  Self,
  ViewContainerRef,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NgControl } from '@angular/forms';
import { ControlType, ValidatedInputWithIdDirective } from '../validated-input-with-id.directive';
import { CharacterCountComponent } from './textarea-counter/textarea-counter.component';

@Directive({
  selector: 'textarea[svuiTextarea]',
  standalone: true,
  providers: [
    {
      // See comment on ValidatedInputWithIdDirective for reasons
      provide: ValidatedInputWithIdDirective,
      useExisting: TextareaDirective,
    },
  ],
})
export class TextareaDirective extends ValidatedInputWithIdDirective implements OnInit {
  @HostBinding('class.form-control') private readonly bootstrapClass = true;
  readonly defaultMinHeightPx = 60;
  readonly defaultMaxHeightPx = 500;
  readonly charCountMax = input<number>();
  readonly minHeightPx = input<number>(this.defaultMinHeightPx);
  readonly maxHeightPx = input<number>(this.defaultMaxHeightPx);

  counterComponent?: ComponentRef<CharacterCountComponent>;
  protected readonly characterCount$ = model<number>();

  nativeElement: HTMLTextAreaElement;
  readonly #destroyRef = inject(DestroyRef);

  // maxlength without brackets for browser support
  @HostBinding('attr.maxlength') get maxlength(): number | undefined {
    return this.charCountMax();
  }

  constructor(
    @Optional() @Self() public override ngControl: NgControl,
    private readonly viewContainerRef: ViewContainerRef,
    private readonly elementRef: ElementRef<HTMLTextAreaElement>,
  ) {
    super(ngControl);
    this.nativeElement = elementRef.nativeElement;
    this.addMessage();
    this.passInputSignals();
  }

  private passInputSignals(): void {
    // pass on our input signals
    effect(() => {
      const maxCharacters = this.charCountMax();
      if (this.counterComponent) {
        this.counterComponent.setInput('maxCharacters' satisfies keyof CharacterCountComponent, maxCharacters);
      }
    });
    effect(() => {
      const characterCount = this.characterCount$();
      if (this.counterComponent) {
        this.counterComponent.setInput('characterCount' satisfies keyof CharacterCountComponent, characterCount);
      }
    });
  }

  get controlType(): ControlType {
    return 'textarea';
  }

  get isLabelFloatable(): boolean {
    return true;
  }

  private addMessage(): void {
    const counterComponent = this.viewContainerRef.createComponent(CharacterCountComponent);
    this.counterComponent = counterComponent;
    // do not pass the inputs here directly because it does not work properly -> use effect
  }

  @HostListener('keyup', ['$event. target'])
  @HostListener('input', ['$event. target'])
  @HostListener('change', ['$event. target'])
  onKeyup(target: HTMLTextAreaElement): void {
    this.characterCount$.set(target.value.length);
  }

  ngOnInit(): void {
    // initial length
    this.characterCount$.set(this.nativeElement.value?.length);

    // subscribe to value changes
    if (this.ngControl?.valueChanges) {
      this.ngControl.valueChanges.pipe(takeUntilDestroyed(this.#destroyRef)).subscribe({
        next: value => {
          // character count
          this.characterCount$.set(value?.length);

          // autoheight
          this.resize();
        },
      });
    }

    if (this.elementRef.nativeElement.scrollHeight) {
      setTimeout(() => this.resize());
    }

    this.resize();
  }

  @HostListener(':input')
  resizeOnInput(): void {
    this.resize();
  }

  resize(): void {
    this.elementRef.nativeElement.style.height = '0';
    const measuredHeight = Math.min(
      Math.max(this.elementRef.nativeElement.scrollHeight, this.minHeightPx()),
      this.maxHeightPx(),
    );
    this.elementRef.nativeElement.style.height = '' + (measuredHeight + 6) + 'px';
  }
}
