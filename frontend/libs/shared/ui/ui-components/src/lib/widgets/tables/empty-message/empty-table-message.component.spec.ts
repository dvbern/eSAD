import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EmptyTableMessageComponent } from './empty-table-message.component';

describe('EmptyTableMessageComponent', () => {
  let component: EmptyTableMessageComponent;
  let fixture: ComponentFixture<EmptyTableMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmptyTableMessageComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(EmptyTableMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());
});
