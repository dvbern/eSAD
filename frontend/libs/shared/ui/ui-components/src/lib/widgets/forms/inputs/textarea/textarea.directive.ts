import {
  AfterViewInit,
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
export class TextareaDirective extends ValidatedInputWithIdDirective implements OnInit, AfterViewInit {
  @HostBinding('class.form-control') readonly bootstrapClass = true;
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

  readonly #viewContainerRef = inject(ViewContainerRef);
  readonly #elementRef = inject<ElementRef<HTMLTextAreaElement>>(ElementRef);

  constructor() {
    const ngCtrl = inject(NgControl, { optional: true, self: true });
    super(ngCtrl);
    this.nativeElement = this.#elementRef.nativeElement;
    this.addMessage();
    this.passInputSignals();
  }

  private passInputSignals() {
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

  private addMessage() {
    this.counterComponent = this.#viewContainerRef.createComponent(CharacterCountComponent);
    // do not pass the inputs here directly because it does not work properly -> use effect
  }

  @HostListener('keyup', ['$event'])
  @HostListener('input', ['$event'])
  @HostListener('change', ['$event'])
  onKeyup(event: Event) {
    const target = event.target as HTMLTextAreaElement | null;
    if (!target) {
      return;
    }
    this.characterCount$.set(target.value.length);
  }

  ngOnInit() {
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
  }

  public override ngAfterViewInit() {
    super.ngAfterViewInit();
    // initial length
    this.characterCount$.set(this.nativeElement.value?.length);
    this.resize();
  }

  @HostListener(':input')
  resizeOnInput() {
    this.resize();
  }

  resize() {
    this.#elementRef.nativeElement.style.height = '0';
    const measuredHeight = Math.min(
      Math.max(this.#elementRef.nativeElement.scrollHeight, this.minHeightPx()),
      this.maxHeightPx(),
    );
    this.#elementRef.nativeElement.style.height = '' + (measuredHeight + 6) + 'px';
  }
}
