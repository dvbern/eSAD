import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VirtualScrollTriggerComponent } from './virtual-scroll-trigger.component';

describe('VirtualScrollTriggerComponent', () => {
  let component: VirtualScrollTriggerComponent;
  let fixture: ComponentFixture<VirtualScrollTriggerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VirtualScrollTriggerComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(VirtualScrollTriggerComponent);
    fixture.componentRef.setInput('loadedRowCount', '0');
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());
});
