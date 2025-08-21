import {AfterViewInit, ChangeDetectionStrategy, Component, OnDestroy, output, ViewContainerRef} from '@angular/core';

@Component({
  selector: 'svui-virtual-scroll-auto-trigger',
  templateUrl: './virtual-scroll-auto-trigger.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [],
})
export class VirtualScrollAutoTriggerComponent implements AfterViewInit, OnDestroy {

  trigger = output<void>();

  observedElement: any;
  observer: IntersectionObserver | undefined;

  constructor(private vcRef: ViewContainerRef) {
  }

  ngAfterViewInit() {
    this.observedElement = this.vcRef.element.nativeElement.parentElement;

    this.observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        this.trigger.emit();
      }
    });
    this.observer.observe(this.observedElement);
  }

  public ngOnDestroy(): void {
    if (this.observer) {
      this.observer.unobserve(this.observedElement);
    }
  }

}
