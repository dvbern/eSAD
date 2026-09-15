import { Component, ChangeDetectionStrategy } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { LayoutTwoColumnsComponent } from './layout-two-columns.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [LayoutTwoColumnsComponent],
  template: `
    <svui-layout-two-columns>
      @if (showLeft) {
        <div left>LEFT</div>
      }
      @if (showRight) {
        <div right>RIGHT</div>
      }
    </svui-layout-two-columns>
  `,
})
class HostComponent {
  showLeft = true;
  showRight = true;
}

describe('LayoutTwoColumnsComponent', () => {
  it('projects content into the left and right slots', () => {
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();

    const host = fixture.nativeElement as HTMLElement;

    expect(host.querySelector('.svui_columns__area--left')?.textContent)
      .toContain('LEFT');
    expect(host.querySelector('.svui_columns__area--right')?.textContent)
      .toContain('RIGHT');
  });

  it('projects content declared inside conditional blocks', () => {
    const fixture = TestBed.createComponent(HostComponent);
    fixture.componentInstance.showRight = false;
    fixture.detectChanges();

    const host = fixture.nativeElement as HTMLElement;

    expect(host.querySelector('.svui_columns__area--left')?.textContent)
      .toContain('LEFT');
    expect(host.querySelector('.svui_columns__area--right > *'))
      .toBeNull();
  });
});
