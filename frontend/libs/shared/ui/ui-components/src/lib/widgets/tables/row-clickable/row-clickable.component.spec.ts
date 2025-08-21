import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RowClickableComponent } from './row-clickable.component';

describe('RowClickableComponent', () => {
  let component: RowClickableComponent;
  let fixture: ComponentFixture<RowClickableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RowClickableComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RowClickableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());
});
