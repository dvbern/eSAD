import {ComponentFixture, TestBed} from '@angular/core/testing';
import {GlobalErrorsComponent} from './global-errors.component';

describe('GlobalErrorsComponent', () => {
  let component: GlobalErrorsComponent;
  let fixture: ComponentFixture<GlobalErrorsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GlobalErrorsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(GlobalErrorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
