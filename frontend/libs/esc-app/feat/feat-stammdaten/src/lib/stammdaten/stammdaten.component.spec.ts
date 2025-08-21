import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTestingTransloco } from '@dvb/util-i18n';
import { ensureProps } from '@dvb/util-shared';
import { render, screen } from '@testing-library/angular';
import { UserEvent, userEvent } from '@testing-library/user-event';
import { of } from 'rxjs';
import { FakeStammdatenService, MyStammdatenDTO } from './fake-stammdaten.service';
import { StammdatenComponent } from './stammdaten.component';
import { StammdatenStore } from './stammdaten.store';

// this test takes a long time to run (30 min or more in pipeline) disable until we know why
describe.skip('StammdatenComponent', () => {
  let user: UserEvent;
  let component: StammdatenComponent;
  let fixture: ComponentFixture<StammdatenComponent>;

  const dbValue: MyStammdatenDTO = ensureProps<MyStammdatenDTO>({
    name: 'dbName',
    vorname: 'dbVorname',
    geburtsDatum: '2000-01-01',
    geschlecht: 'MANN',
  });

  const mockService: FakeStammdatenService = {
    loadById$: jest.fn().mockReturnValue(of(dbValue)),
    update$: jest.fn().mockReturnValue(of(undefined)),
  };

  beforeEach(async () => {
    user = userEvent.setup();

    ({ fixture } = await render(StammdatenComponent, {
      imports: [StammdatenComponent],
      providers: [
        {
          provide: FakeStammdatenService,
          useValue: mockService,
        },
        StammdatenStore,
        provideTestingTransloco(),
      ],
    }));

    TestBed.inject(StammdatenStore).initFromRoute('id');

    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // FIXME remove skip and fix
  it.skip('should create', () => {
    expect(screen.getByText('stammdaten.title works!')).toBeTruthy();

    expect(component.isDirty()).toBe(false);

    // mock data is already loaded
    expect(screen.queryByTestId('stammdaten-loading-spinner')).toBeFalsy();
    expect(screen.getByRole('heading', { name: 'Stammdatenformular' })).toBeTruthy();
  });

  // FIXME remove skip and fix
  describe.skip('setting an invalid value and then submitting', () => {
    beforeEach(async () => {
      const nameElem = screen.getByRole('textbox', {
        name: 'stammdaten.form.name',
      });
      const submitButton = screen.getByRole('button', {
        name: 'stammdaten.form.speichern',
      });

      await user.click(nameElem);
      await user.clear(nameElem);
      await user.click(submitButton);
    });

    it('does not update the store', () => {
      expect(component.store.stammdaten()).toEqual(dbValue);
    });

    it('keeps the component dirty', async () => {
      expect(component.isDirty()).toBe(true);
    });
  });

  // FIXME remove skip and fix
  describe.skip('updating the form to a valid value and then submitting', () => {
    beforeEach(async () => {
      const nameElem = screen.getByRole('textbox', {
        name: 'stammdaten.form.name',
      });
      const submitButton = screen.getByRole('button', {
        name: 'stammdaten.form.speichern',
      });

      await user.click(nameElem);
      await user.clear(nameElem);
      await user.type(nameElem, 'newName');
      await user.click(submitButton);
    });

    it('updates the store to the new value', () => {
      expect(component.store.stammdaten()).toEqual({
        name: 'newName',
        vorname: 'dbVorname',
        geburtsDatum: '2000-01-01',
        geschlecht: 'MANN',
      });
    });

    it('marks the form as pristine/not dirty', async () => {
      expect(component.isDirty()).toBe(false);
    });
  });

  describe('clicking the demo button', () => {
    beforeEach(() => {
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      jest.spyOn(window, 'alert').mockImplementation(() => {});

      component.demo();
    });

    it('triggers the alert', () => {
      expect(window.alert).toHaveBeenCalledWith('Demo');
    });
  });
});
