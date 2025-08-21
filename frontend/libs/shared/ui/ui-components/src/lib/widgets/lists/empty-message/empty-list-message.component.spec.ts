import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EmptyListMessageComponent } from './empty-list-message.component';

describe('EmptyListMessageComponent', () => {
  let component: EmptyListMessageComponent;
  let fixture: ComponentFixture<EmptyListMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmptyListMessageComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(EmptyListMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());
});
