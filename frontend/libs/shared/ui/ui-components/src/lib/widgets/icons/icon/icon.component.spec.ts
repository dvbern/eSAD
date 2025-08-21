import {ComponentRef} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {IconComponent, IconUseCases} from './icon.component';

describe('IconComponent', () => {
  let component: IconComponent;
  let componentRef: ComponentRef<IconComponent>;
  let fixture: ComponentFixture<IconComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IconComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(IconComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('useCase', 'user');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('all icons need to be defined', () => {
    const useCases = IconUseCases;

    for (const useCase of useCases) {
      const libSvg = component.getIconSvg(useCase);
      const customFilename = component.getCustomIconFileName(useCase)
      expect(libSvg ?? customFilename).toBeTruthy();
    }

  })
});
