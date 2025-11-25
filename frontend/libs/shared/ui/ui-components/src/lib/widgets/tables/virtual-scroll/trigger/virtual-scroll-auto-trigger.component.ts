import {AfterViewInit, ChangeDetectionStrategy, Component, OnDestroy, output, ViewContainerRef, inject} from '@angular/core';
import { requireValue } from '@dvb/util-shared';

@Component({
  selector: 'svui-virtual-scroll-auto-trigger',
  templateUrl: './virtual-scroll-auto-trigger.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [],
})
export class VirtualScrollAutoTriggerComponent implements AfterViewInit, OnDestroy {
  trigger = output<void>();

  observedElement: Element | undefined;
  observer: IntersectionObserver | undefined;

  readonly #vcRef = inject(ViewContainerRef);

  ngAfterViewInit() {
    this.observedElement = this.#vcRef.element.nativeElement.parentElement;

    this.observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        this.trigger.emit();
      }
    });
    this.observer.observe(requireValue(this.observedElement));
  }

  public ngOnDestroy(): void {
    if (this.observer) {
      this.observer.unobserve(requireValue(this.observedElement));
    }
  }
}
