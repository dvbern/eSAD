import {expect, Locator, Page, Request} from '@playwright/test';
import {test} from '../../fixtures/test.fixture';
import {AnlassListPageObject} from '../anlass/page-objects/anlass-list.page-object';
import {UntersuchungDatenPageObject} from './page-objects/untersuchung.page-object';
import {format} from 'date-fns';
import {FormSelectComponentObject} from '../../component-objects/form-select.component-object';
import {authStatePath, testUsers} from '../../util/env.util';
import {expectAufgabeInList} from '../../routines/aufgaben.routines';

test.describe('SU durchführen', () => {
  test.use({storageState: authStatePath('ESC_SUPPORT')});
  test.describe.configure({mode: 'serial'});

  test('tab daten', async ({page, testData}) => {
    const untersuchungDaten = new UntersuchungDatenPageObject(page);

    // "daten" is the tab the dossier opens on, so its data is already on the way: the wait has to be
    // registered before the navigation.
    const durchfuehrungLoaded = waitForTabData(page, 'durchfuehrung');
    await openDossierTab(page, testData.anlass3.title);
    await untersuchungDaten.isCurrentPage('daten');
    await durchfuehrungLoaded;

    await chooseOption(page, untersuchungDaten.elements.schularztSelect(), 'Jaques Schularzt');
    await chooseOption(page, untersuchungDaten.elements.mpaSelect(), 'Marta MPA');

    const request = await saveForm(
      page,
      untersuchungDaten.elements.buttonSaveDaten(),
      'PUT',
      '/untersuchung/durchfuehrung',
    );

    const postData = request.postDataJSON();

    // Only the fields this test actually drives are asserted: the Benutzer objects also carry
    // their roles and permissions, and pinning those makes the test fail on unrelated model
    // changes rather than on a broken Schularzt/MPA assignment.
    expect(postData).toMatchObject({
      arzt: {
        id: expect.any(String),
        name: 'Schularzt',
        vorname: 'Jaques',
        fullName: 'Jaques Schularzt',
      },
      mpa: {
        id: expect.any(String),
        name: 'MPA',
        vorname: 'Marta',
        fullName: 'Marta MPA',
      },
      nachkontrolle: false,
      nachzuegler: false,
      neueintritt: false,
    });
  });

  test.describe('tab impfen', () => {
    test.describe.configure({mode: 'serial'});

    test('Impfstatus', async ({page, testData}) => {
      const untersuchungDaten = new UntersuchungDatenPageObject(page);

      await openDossierTab(page, testData.anlass3.title);

      await untersuchungDaten.isCurrentPage('daten');
      const impfungenLoaded = waitForTabData(page, 'impfungen');
      await untersuchungDaten.elements.untersuchungTabImpfungen().filter({hasText: 'Impfungen'}).click();
      await untersuchungDaten.isCurrentPage('impfungen');
      await impfungenLoaded;

      await checkImpfRow(page, 'Diphtherie', {genuegend: true, amount: 1, date: new Date(2025, 11, 1)});
      await checkImpfRow(page, 'Tetanus', {genuegend: true, amount: 2, date: new Date(2025, 11, 1)});
      await checkImpfRow(page, 'Pertussis', {genuegend: true, amount: 3, date: new Date(2025, 11, 1)});
      await checkImpfRow(page, 'Poliomyelitis', {genuegend: true, amount: 4, date: new Date(2025, 11, 1)});

      await checkImpfRow(page, 'Hepatitis B', {empfehlung: true, amount: 5, date: new Date(2025, 11, 1)});
      await checkImpfRow(page, 'HPV', {empfehlung: true, amount: 1, date: new Date(2025, 11, 1)});
      await checkImpfRow(page, 'Masern', {empfehlung: true, amount: 2, date: new Date(2025, 11, 1)});
      await checkImpfRow(page, 'Mumps', {empfehlung: true, amount: 3, date: new Date(2025, 11, 1)});

      await checkImpfRow(page, 'Röteln', {entscheid: true, amount: 4, date: new Date(2025, 11, 1)});
      await checkImpfRow(page, 'Varizellen', {entscheid: true, amount: 5, date: new Date(2025, 11, 1)});
      await checkImpfRow(page, 'Meningokokken ACWY', {entscheid: true, amount: 1, date: new Date(2025, 11, 1)});
      await checkImpfRow(page, 'Meningokokken B', {entscheid: true, amount: 2, date: new Date(2025, 11, 1)});

      const request = await saveForm(
        page,
        untersuchungDaten.elements.buttonSaveImpfstatus().first(),
        'PUT',
        '/untersuchung/impfungen/impfstatus',
      );

      const postData = request.postDataJSON();

      expect(postData.impfstatusList).toEqual(
        expect.arrayContaining([
          expect.objectContaining({
            krankheit: 'DIPHTERIE',
            anzahlImpfungen: '1',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: 'JA',
            impfempfehlung: false,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'TETANUS',
            anzahlImpfungen: '2',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: 'JA',
            impfempfehlung: false,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'PERTUSSIS',
            anzahlImpfungen: '3',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: 'JA',
            impfempfehlung: false,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'POLIO',
            anzahlImpfungen: '4',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: 'JA',
            impfempfehlung: false,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'HEPATITIS_B',
            anzahlImpfungen: '5',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: 'NEIN',
            impfempfehlung: true,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'HPV',
            anzahlImpfungen: '1',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: 'NEIN',
            impfempfehlung: true,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'MASERN',
            anzahlImpfungen: '2',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: 'NEIN',
            impfempfehlung: true,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'MUMPS',
            anzahlImpfungen: '3',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: 'NEIN',
            impfempfehlung: true,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'ROETELN',
            anzahlImpfungen: '4',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: 'NEIN',
            impfempfehlung: false,
            impfentscheid: true,
          }),
          expect.objectContaining({
            krankheit: 'VARIZELLEN',
            anzahlImpfungen: '5',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: 'NEIN',
            impfempfehlung: false,
            impfentscheid: true,
          }),
          expect.objectContaining({
            krankheit: 'MENINGOKOKKEN_A_C_W_Y',
            anzahlImpfungen: '1',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: 'NEIN',
            impfempfehlung: false,
            impfentscheid: true,
          }),
          expect.objectContaining({
            krankheit: 'MENINGOKOKKEN_B',
            anzahlImpfungen: '2',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: 'NEIN',
            impfempfehlung: false,
            impfentscheid: true,
          }),
        ]),
      );
    });

    test('doppelkontrolle', async ({page, browser, testData}) => {
      // A Doppelkontrolle needs a second person to countersign: the Visum is handed to "Monika MPA"
      // and confirmed from her session. That only works on an environment seeded with the test
      // users - a setup where every ESC_* slot points at the same account cannot exercise it.
      // eslint-disable-next-line playwright/no-skipped-test -- requires distinct seeded users, deferred for now
      test.skip(
        testUsers.ESC_MPA.name === testUsers.ESC_SUPPORT.name,
        'needs ESC_MPA to be a different user than ESC_SUPPORT',
      );

      const untersuchungDaten = new UntersuchungDatenPageObject(page);

      await openDossierTab(page, testData.anlass3.title);

      await untersuchungDaten.isCurrentPage('daten');
      await untersuchungDaten.elements.untersuchungTabImpfungen().filter({hasText: 'Impfungen'}).click();

      await untersuchungDaten.isCurrentPage('impfungen');

      // No wait for the tab's data here on purpose: "Visieren" stays disabled until the Impfungen are loaded
      // and while a Visum is being saved, and the clicks rely on that. Each step still waits for its save,
      // because the form shows the next step at once and the save's response resets it - a person chosen
      // before that would be lost.
      await saveForm(
        page,
        untersuchungDaten.elements.buttonVisieren(),
        'POST',
        '/untersuchung/impfungen/visieren/visum1',
      );
      await expect(untersuchungDaten.elements.visumSelect().getFormField(page)).toBeVisible();

      await chooseOption(page, untersuchungDaten.elements.visumSelect(), 'Monika MPA');
      await saveForm(
        page,
        untersuchungDaten.elements.buttonVisieren(),
        'POST',
        '/untersuchung/impfungen/anfordern/visum2',
      );

      const monikaContext = await browser.newContext({
        storageState: authStatePath('ESC_MPA'),
      });

      const monikaPage = await monikaContext.newPage();
      await monikaPage.goto(page.url());

      const monikaUntersuchung = new UntersuchungDatenPageObject(monikaPage);

      await saveForm(
        monikaPage,
        monikaUntersuchung.elements.buttonVisieren(),
        'POST',
        '/untersuchung/impfungen/visieren/visum2',
      );

      await monikaUntersuchung.elements.buttonAufgabenNav().click();

      const aufgabeRow = monikaPage.getByRole('row').filter({hasNotText: 'Filter'}).filter({
        hasText: new RegExp(`Doppelkontrolle Impfentscheid für Ammann Fridolin - 20\\d{2}/\\d{2} I IMPFTERMIN ${testData.anlass3.title}`),
      });

      await expect(aufgabeRow).toBeVisible();

      await monikaContext.close();
    });

    test('checkliste & impfliste', async ({page, testData}) => {
      const untersuchungDaten = new UntersuchungDatenPageObject(page);

      await openDossierTab(page, testData.anlass3.title);

      await untersuchungDaten.isCurrentPage('daten');
      const impfungenLoaded = waitForTabData(page, 'impfungen');
      await untersuchungDaten.elements.untersuchungTabImpfungen().filter({hasText: 'Impfungen'}).click();

      await untersuchungDaten.isCurrentPage('impfungen');
      await impfungenLoaded;

      await checklistCheckbox(page, 'Einverständnis vorhanden?')
      await checklistCheckbox(page, 'Kontraindikationen abgeklärt?')
      await checklistCheckbox(page, 'Doppelkontrolle Impfentscheid gemacht?')

      const checklistRequest = await saveForm(
        page,
        untersuchungDaten.elements.buttonSaveImpfChecklist(),
        'PUT',
        '/untersuchung/impfungen',
      );

      const checklistPostData = checklistRequest.postDataJSON();

      expect(checklistPostData).toMatchObject({
        checkliste: {
          checkEinwilligungVorhanden: true,
          checkKontraindikationenAbgeklaert: true,
          checkDoppelkontrolleImpfentscheidDurchgefuehrt: true,
        },
      });

      await untersuchungDaten.elements.buttonCreateImpfung().click();
      await chooseOption(page, untersuchungDaten.elements.ImpfstoffSelect(), 'Bexsero (Meningokokken B)');
      await chooseOption(page, untersuchungDaten.elements.chargeSelect(), '900');

      const impfungRequest = await saveForm(
        page,
        untersuchungDaten.elements.buttonSaveImpfung(),
        'POST',
        '/untersuchung/impfungen/impfungenintern',
      );

      const impfungPostData = impfungRequest.postDataJSON();

      expect(impfungPostData).toMatchObject({
        impfStatus: 'GEPLANT',
        charge: '900',
        impfstoff: {
          data: {
            name: 'Bexsero',
            krankheiten: ['MENINGOKOKKEN_B'],
          },
        },
        krankheitenImmunitaeten: [
          {
            krankheit: 'MENINGOKOKKEN_B',
            immunitaetGenuegend: false,
          },
        ],
      });

      await expect(page.getByText("Die Daten zur 'Checkliste' wurden gespeichert")).toBeVisible();
    });

    test('massnahmen', async ({page, testData}) => {
      const untersuchungDaten = new UntersuchungDatenPageObject(page);

      await openDossierTab(page, testData.anlass3.title);

      await untersuchungDaten.isCurrentPage('daten');
      const impfungenLoaded = waitForTabData(page, 'impfungen');
      await untersuchungDaten.elements.untersuchungTabImpfungen().filter({hasText: 'Impfungen'}).click();
      await untersuchungDaten.isCurrentPage('impfungen');
      await impfungenLoaded;

      await chooseOption(page, untersuchungDaten.elements.informationElternSelect(), 'Brief');
      await checklistCheckbox(page, 'Rückfrage Impfen Kinderarzt/Kinderärztin')
      await checklistCheckbox(page, 'Information Impfen Kinderarzt/Kinderärztin oder Hausarzt/Hausärztin')

      const request = await saveForm(
        page,
        untersuchungDaten.elements.buttonSaveMassnahmenImpf(),
        'PUT',
        '/untersuchung/impfungen/massnahmen',
      );

      const postData = request.postDataJSON();

      expect(postData).toMatchObject({
        impfberatungMpaDurchgefuehrt: false,
        infomaterialAbgegeben: false,
        rueckfrageHausarzt: true,
        informationHausarzt: true,
        informationEltern: 'BRIEF',
        keineMassnahmen: false,
        vormerkenFuerGruppenFolgetermin: false,
        version: expect.any(Number),
      });

      await expect(page.getByText('Die Änderungen wurden erfolgreich gespeichert')).toBeVisible();

      await untersuchungDaten.elements.buttonAufgabenNav().click();

      const unsavedModal = page.locator('ngb-modal-window');

      // eslint-disable-next-line playwright/no-conditional-in-test
      if (await unsavedModal.isVisible({timeout: 1000})) {
        await unsavedModal.getByRole('button', {name: 'Seite verlassen'}).click();
      }

      await untersuchungDaten.elements.buttonResetFilterAufgaben().click();

      const expectedRows = [
        `Ammann Fridolin: Rückfrage Impfen Kinderarzt/Kinderärztin - 20\\d{2}/\\d{2} I IMPFTERMIN ${testData.anlass3.title}`,
        `Ammann Fridolin: Information Impfen Kinderarzt/Kinderärztin oder Hausarzt/Hausärztin - 20\\d{2}/\\d{2} I IMPFTERMIN ${testData.anlass3.title}`,
        `Ammann Fridolin: Information Impfen an Eltern - 20\\d{2}/\\d{2} I IMPFTERMIN ${testData.anlass3.title}`,
      ];

      for (const text of expectedRows) {
        await expectAufgabeInList(page, testData.anlass3.title, new RegExp(text));
      }
    });
  });

  test('tab sehen', async ({page, testData}) => {
    const untersuchungDaten = new UntersuchungDatenPageObject(page);

    await openDossierTab(page, testData.anlass1.title);

    await untersuchungDaten.isCurrentPage('daten');
    const sehenLoaded = waitForTabData(page, 'sehen/beurteilung');
    await untersuchungDaten.elements.untersuchungTabSehen().click();
    await sehenLoaded;

    await untersuchungDaten.elements.untersuchungSehenDefaultWert().click();

    await checkMultiSelect(page, 'Brille/Linsen', 'Nicht geprüft')
    await checkMultiSelect(page, 'Stereotest', 'Pathologisch')

    const request = await saveForm(
      page,
      untersuchungDaten.elements.buttonSaveSehen(),
      'PUT',
      '/untersuchung/sehen/beurteilung',
    );

    const postData = request.postDataJSON();

    // Every Beurteilung field is sent as a multi-value control ({value: [...]}), including the ones
    // the UI only lets you pick a single option for.
    expect(postData).toMatchObject({
      brilleLinsen: selected('NICHT_GEPRUEFT'),
      stereotest: selected('PATHOLOGISCH'),
      farbsehen: selected('NORMAL'),
      verwendeteTafel: selected('C_TAFEL'),
      fernvisus: selected('NORMAL'),
      fernvisusRechts: 1,
      fernvisusLinks: 1,
      nahvisus: selected('NICHT_GEPRUEFT'),
      motilitaet: selected('NORMAL'),
      strabismus: selected('KEIN_STRABISMUS'),
      strabismusAuspraegung: selected('KEIN_STRABISMUS'),
      strabismusAuftreten: selected('KEIN_STRABISMUS'),
      inspektion: selected('UNAUFFAELLIG'),
      konvergenz: selected('UNAUFFAELLIG'),
      version: expect.any(Number),
    });
  });

  test('tab hoeren', async ({page, testData}) => {
    const untersuchungDaten = new UntersuchungDatenPageObject(page);

    await openDossierTab(page, testData.anlass1.title);

    await untersuchungDaten.isCurrentPage('daten');
    // the tab loads Audiogramm and Otoskopie separately and this test edits both, so waiting for
    // "hoeren" would return as soon as the first of them is in
    const audiogrammLoaded = waitForTabData(page, 'hoeren/audiogramm');
    const otoskopieLoaded = waitForTabData(page, 'hoeren/otoskopie');
    await untersuchungDaten.elements.untersuchungTabHoeren().click();
    await Promise.all([audiogrammLoaded, otoskopieLoaded]);

    await untersuchungDaten.elements.untersuchungHoerenDefaultWert().click();
    await tick(
      page.locator('[id="audioaudiogramm.seite.rechtsdbHz6000DB30"]'),
      page.locator('label[for="audioaudiogramm.seite.rechtsdbHz6000DB30"]'),
    );

    await checkMultiSelect(page, 'Otoskopie rechts', 'Cerumen')
    await checkMultiSelect(page, 'Otoskopie rechts', 'Nicht einsehbar')
    await checkMultiSelect(page, 'Otoskopie links', 'Normal')

    const requestAudiogramm = await saveForm(
      page,
      untersuchungDaten.elements.buttonSaveAudiogramm(),
      'PUT',
      '/untersuchung/hoeren/audiogramm',
    );

    const postDataAudiogramm = requestAudiogramm.postDataJSON();

    expect(postDataAudiogramm).toMatchObject({
      audiogrammRechts: {
        dbHz250: 'DB20',
        dbHz500: 'DB20',
        dbHz1000: 'DB20',
        dbHz2000: 'DB20',
        dbHz4000: 'DB20',
        dbHz6000: 'DB30',
        dbHz8000: 'DB20',
        version: expect.any(Number),
      },
      audiogrammLinks: {
        dbHz250: 'DB20',
        dbHz500: 'DB20',
        dbHz1000: 'DB20',
        dbHz2000: 'DB20',
        dbHz4000: 'DB20',
        dbHz6000: 'DB20',
        dbHz8000: 'DB20',
        version: expect.any(Number),
      },
      audiogrammBeurteilung: selected('NORMAL'),
      zusaetzlicheAngaben: selected('KEINE'),
      version: expect.any(Number),
    });

    const requestOtoskopie = await saveForm(
      page,
      untersuchungDaten.elements.buttonSaveOtoskopie(),
      'PUT',
      '/untersuchung/hoeren/otoskopie',
    );

    const postDataOtoskopie = requestOtoskopie.postDataJSON();

    expect(postDataOtoskopie).toMatchObject({
      otoskopieRechts: selected('TROMMELFELL_CERUMEN', 'NICHT_EINSEHBAR'),
      otoskopieRechtsResultatAndere: '',
      otoskopieLinks: selected('NORMAL'),
      otoskopieLinksResultatAndere: '',
      version: expect.any(Number),
    });
  });

  test('tab groesse & gewicht', async ({page, testData}) => {
    const untersuchungDaten = new UntersuchungDatenPageObject(page);

    await openDossierTab(page, testData.anlass1.title);

    await untersuchungDaten.isCurrentPage('daten');
    const groesseGewichtLoaded = waitForTabData(page, 'groessegewicht');
    await untersuchungDaten.elements.untersuchungTabGroesseGewicht().click();
    await untersuchungDaten.isCurrentPage('groessegewicht');
    await groesseGewichtLoaded;

    await fillStable(untersuchungDaten.elements.groesseInput().getInput(page), '120');
    await fillStable(untersuchungDaten.elements.gewichtInput().getInput(page), '30');

    await checkMultiSelect(page, 'Grösse', 'Normal')
    await checkMultiSelect(page, 'Gewicht', '>90%ile')
    await checkMultiSelect(page, 'BMI', '>90%ile')

    const request = await saveForm(
      page,
      untersuchungDaten.elements.buttonSaveGroesseGewicht(),
      'PUT',
      '/untersuchung/groessegewicht',
    );

    const postData = request.postDataJSON();

    expect(postData).toMatchObject({
      groesse: '120',
      gewicht: '30',
      groessePerzentileSingleValue: {value: ['NORMAL']},
      gewichtPerzentileSingleValue: {value: ['PERZENTILE_HOCH']},
      bmiPerzentileSingleValue: {value: ['PERZENTILE_HOCH']},
      version: expect.any(Number),
    });

  });

  test('tab aerztliche untersuchung', async ({page, testData}) => {
    const untersuchungDaten = new UntersuchungDatenPageObject(page);

    await openDossierTab(page, testData.anlass1.title);

    await untersuchungDaten.isCurrentPage('daten')
    const arztLoaded = waitForTabData(page, 'arzt/beurteilung');
    await untersuchungDaten.elements.untersuchungTabArzt().click();

    await untersuchungDaten.isCurrentPage('arzt')
    await arztLoaded;

    await checkMultiSelect(page, 'Bewegungsapparat', 'Skoliose')
    await checkMultiSelect(page, 'Einbeinstand', 'Nicht möglich')
    await checkMultiSelect(page, 'Einbeinhüpfen', 'Nicht geprüft')
    await checkMultiSelect(page, 'Fingeropposition', 'Altersentsprechend')
    await checkMultiSelect(page, 'Motorik', 'Normal')
    await checkMultiSelect(page, 'Sprache', 'Entwicklungs-verzögerung');
    await checkMultiSelect(page, 'Mund und Zähne', 'Unauffällig');
    await checkMultiSelect(page, 'Haut', 'Nicht geprüft');
    await checkMultiSelect(page, 'Herzauskultation', 'Unauffällig');
    await checkMultiSelect(page, 'Blutdruck', 'Normal');
    await checkMultiSelect(page, 'Verhalten', 'Unauffällig');

    await fillStable(untersuchungDaten.elements.systolischInput().getInput(page), '110');
    await fillStable(untersuchungDaten.elements.diastolischInput().getInput(page), '70');

    const request = await saveForm(
      page,
      untersuchungDaten.elements.buttonSaveArzt(),
      'PUT',
      '/untersuchung/arzt/beurteilung',
    );

    const postData = request.postDataJSON();

    expect(postData).toMatchObject({
      verhalten: 'UNAUFFAELLIG',
      blutdruck: 'NORMAL',
      blutdruckSystolisch: '110',
      blutdruckDiastolisch: '70',
      herzauskultation: 'UNAUFFAELLIG',
      haut: 'NICHT_GEPRUEFT',
      mundAndZaehne: ['UNAUFFAELLIG'],
      sprachfaehigkeitMultiValue: ['ENTWICKLUNGSVERZOEGERUNG'],
      motorik: 'NORMAL',
      bewegungsapparat: ['SKOLIOSE'],
      einbeinstand: 'NICHT_MOEGLICH',
      einbeinhuepfen: 'NICHT_GEPRUEFT',
      fingeropposition: ['ALTERSENTSPRECHEND'],
      version: expect.any(Number),
    });

  })

  test('tab massnahmen', async ({page, testData}) => {
    const untersuchungDaten = new UntersuchungDatenPageObject(page);

    await openDossierTab(page, testData.anlass1.title);

    await untersuchungDaten.isCurrentPage('daten')
    const massnahmenLoaded = waitForTabData(page, 'massnahmen');
    await untersuchungDaten.elements.untersuchungTabMassnahmen().click();

    await untersuchungDaten.isCurrentPage('massnahmen')
    await massnahmenLoaded;

    await checklistCheckbox(page, 'Überweisung an Hausarzt/Hausärztin oder Kinderarzt/Kinderärztin')

    await checklistCheckbox(page, 'Überweisung an Augenarzt/Augenärztin')

    await chooseOption(page, untersuchungDaten.elements.informationElternSelect(), 'Mail');

    const request = await saveForm(
      page,
      untersuchungDaten.elements.buttonSaveMassnahmen(),
      'PUT',
      '/untersuchung/massnahmen',
    );

    const postData = request.postDataJSON();

    expect(postData).toMatchObject({
      informationEltern: "MAIL",
      ueberweisungHausarzt: true,
      ueberweisungAugenarzt: true,
      version: expect.any(Number),
    });

    await untersuchungDaten.elements.buttonAufgabenNav().click();
    await untersuchungDaten.elements.buttonResetFilterAufgaben().click();

    const expectedRows = [
      `Musterkind Max: Überweisung an Augenarzt/Augenärztin - 20\\d{2}/\\d{2} DIV ${testData.anlass1.title}`,
      `Musterkind Max: Information an Eltern - 20\\d{2}/\\d{2} DIV ${testData.anlass1.title}`,
      `Musterkind Max: Überweisung an HA/KA - 20\\d{2}/\\d{2} DIV ${testData.anlass1.title}`,
    ];

    for (const text of expectedRows) {
      await expectAufgabeInList(page, testData.anlass1.title, new RegExp(text));
    }

  })
})

async function openDossierTab(page: Page, anlass: string) {
  const anlassList = new AnlassListPageObject(page);

  await anlassList.goto();

  await anlassList.elements.linkAnlass().filter({hasText: anlass}).first().click();

  await anlassList.elements.invitedKinderCheckbox().click();
  await anlassList.elements.buttonKinderAuswaehlen().click();
}

async function checkImpfRow(
  page: Page,
  title: string,
  options: { genuegend?: boolean; empfehlung?: boolean; entscheid?: boolean; amount?: number; date?: Date },
) {
  const getRow = () => page.getByRole('row', {name: title});

  await expect(getRow()).toBeVisible();

  if (options.genuegend) {
    await tick(getRow().getByTestId('impfschutz-genugend-checkbox'));
  }
  if (options.empfehlung) {
    await tick(getRow().getByTestId('impfempfehlung-checkbox'));
  }
  if (options.entscheid) {
    await tick(getRow().getByTestId('impfentscheid-checkbox'));
  }

  if (options.amount !== undefined) {
    await fillStable(getRow().getByTestId('impfung-amount-input'), String(options.amount));
  }

  if (options.date) {
    // <input type="month"> takes its value as yyyy-MM; typing the segments would follow the
    // browser locale instead.
    const dateInput = getRow().locator('svui-table-control[data-testid="impfung-date-input"] input');

    await fillStable(dateInput, format(options.date, 'yyyy-MM'));
  }
}

/** Shape of a Beurteilung answer in the request payload. */
function selected(...values: string[]) {
  return {value: values};
}

async function checkMultiSelect(page: Page, title: string, option: string) {
  const row = page.locator('svui-kachel-form-section', {hasText: title});
  const control = row.locator('svui-kachel-control', {hasText: option});

  await tick(control.locator('input'), control.locator('label'));
}

async function checklistCheckbox(page: Page, option: string) {
  const untersuchungDaten = new UntersuchungDatenPageObject(page);

  const checklist = page.locator('svui-standalone-control', {hasText: option});

  await expect(checklist).toBeVisible();
  await tick(
    untersuchungDaten.elements.checkboxChecklist().getCheckboxInput(checklist),
    untersuchungDaten.elements.checkboxChecklist().getCheckboxLabel(checklist),
  );
}

/** Fills an input until the value sticks - see {@link waitForTabData} for why that is needed. */
async function fillStable(input: Locator, value: string) {
  await expect(async () => {
    await input.fill(value);
    await expect(input).toHaveValue(value, {timeout: 1000});
  }).toPass({timeout: 10000});

  rememberEdit(input.page(), `${input} = "${value}"`, () => expect(input).toHaveValue(value, {timeout: 1000}));
}

/**
 * Waits for the response that fills a tab's form. The forms patch their controls once the
 * Untersuchung has loaded, silently reverting anything entered before that, so every tab has to
 * wait for its data before the test starts interacting.
 */
function waitForTabData(page: Page, tab: string) {
  return page.waitForResponse(
    res =>
      res.url().includes(`/untersuchung/${tab}`) &&
      // the prefill behind "Standardwerte", which loads next to the tab's own data
      !res.url().endsWith('/default') &&
      res.request().method() === 'GET');
}

/**
 * Ticks a checkbox until the tick sticks - see {@link waitForTabData} for why an edit can be
 * reverted. Pass `label` when the checkbox itself is not the clickable element.
 */
async function tick(checkbox: Locator, label: Locator = checkbox) {
  await expect(async () => {
    if (!(await checkbox.isChecked())) {
      await label.click();
    }
    await expect(checkbox).toBeChecked({timeout: 1000});
  }).toPass({timeout: 10000});

  rememberEdit(checkbox.page(), `${checkbox} ticked`, () => expect(checkbox).toBeChecked({timeout: 1000}));
}

/** Picks an option in a select, remembering the choice so that {@link saveForm} checks it again. */
async function chooseOption(page: Page, select: FormSelectComponentObject, option: string) {
  await select.setOption(page, option);

  const field = select.getFormField(page);
  rememberEdit(page, `${field} = "${option}"`, () => expect(field).toContainText(option, {timeout: 1000}));
}

type Edit = {description: string; stillApplied: () => Promise<void>};

/**
 * Every edit made on a page. An edit that stuck can still be reverted later - by the form resetting
 * itself once more (see {@link waitForTabData}) - so {@link saveForm} checks all of them again right
 * before saving. Saved edits keep being checked, too: saving one form can reset the others.
 */
const edits = new WeakMap<Page, Edit[]>();

function rememberEdit(page: Page, description: string, stillApplied: () => Promise<void>) {
  edits.set(page, [...(edits.get(page) ?? []), {description, stillApplied}]);
}

/**
 * Saves a form and returns the request that did it.
 *
 * First checks that the edits made so far are all still in place, so a reverted edit fails with its
 * own name instead of as a payload diff. Then gives the save ten seconds: a form that was reset or is
 * invalid sends nothing, which would otherwise only surface as the test timing out.
 */
async function saveForm(page: Page, saveButton: Locator, method: 'PUT' | 'POST', path: string): Promise<Request> {
  for (const {description, stillApplied} of edits.get(page) ?? []) {
    await stillApplied().catch((error: Error) => {
      throw new Error(`Reverted before saving: ${description}\n${error.message}`);
    });
  }

  const [response] = await Promise.all([
    page
      .waitForResponse(res => res.request().method() === method && new URL(res.url()).pathname.endsWith(path), {
        timeout: 10000,
      })
      .catch((error: Error) => {
        throw new Error(`Saving sent no ${method} to ${path} - is the form unchanged or invalid?\n${error.message}`);
      }),
    saveButton.click(),
  ]);

  expect(response.ok(), `${method} ${path} answered ${response.status()}`).toBe(true);

  return response.request();
}
