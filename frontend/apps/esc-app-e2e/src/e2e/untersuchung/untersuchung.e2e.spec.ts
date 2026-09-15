import {expect, Page} from '@playwright/test';
import {test} from '../../fixtures/test.fixture';
import {AnlassListPageObject} from '../anlass/page-objects/anlass-list.page-object';
import {UntersuchungDatenPageObject} from './page-objects/untersuchung.page-object';
import {format} from 'date-fns';

test.describe('SU durchführen', () => {
  test.use({storageState: './playwright/.auth/ESC_SUPPORT'});
  test.describe.configure({mode: 'serial'});

  test('tab daten', async ({page, testData}) => {
    const untersuchungDaten = new UntersuchungDatenPageObject(page);

    await openDossierTab(page, testData.anlass3.title);
    await untersuchungDaten.isCurrentPage('daten');

    await untersuchungDaten.elements.schularztSelect().setOption(page, 'Jaques Schularzt');
    await page.waitForTimeout(500);
    await untersuchungDaten.elements.mpaSelect().setOption(page, 'Marta MPA');

    const [request] = await Promise.all([
      page.waitForRequest(req => req.url().includes('/untersuchung/durchfuehrung') && req.method() === 'PUT'),
      untersuchungDaten.elements.buttonSaveDaten().click(),
    ]);

    const postData = request.postDataJSON();

    expect(postData).toEqual({
      arzt: {
        id: expect.any(String),
        name: 'Schularzt',
        vorname: 'Jaques',
        fullName: 'Jaques Schularzt',
        fachrollen: ['SCHULAERZTIN'],
        aktiv: true,
      },
      mpa: {
        id: expect.any(String),
        name: 'MPA',
        vorname: 'Marta',
        fullName: 'Marta MPA',
        fachrollen: ['MPA'],
        aktiv: true,
      },
      nachkontrolle: false,
      nachzuegler: false,
      neueintritt: false,
      version: expect.any(Number),
    });
  });

  test.describe('tab impfen', () => {
    test.describe.configure({mode: 'serial'});

    test('Impfstatus', async ({page, testData}) => {
      const untersuchungDaten = new UntersuchungDatenPageObject(page);

      await openDossierTab(page, testData.anlass3.title);

      await untersuchungDaten.isCurrentPage('daten');
      await untersuchungDaten.elements.untersuchungTabImpfungen().filter({hasText: 'Impfungen'}).click();
      await untersuchungDaten.isCurrentPage('impfungen');

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

      const [request] = await Promise.all([
        page.waitForRequest(req => req.url().includes('/untersuchung/impfungen/impfstatus') && req.method() === 'PUT'),
        untersuchungDaten.elements.buttonSaveImpfstatus().first().click(),
      ]);

      const postData = request.postDataJSON();

      expect(postData.impfstatusList).toEqual(
        expect.arrayContaining([
          expect.objectContaining({
            krankheit: 'DIPHTERIE',
            anzahlImpfungen: '1',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: true,
            impfempfehlung: false,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'TETANUS',
            anzahlImpfungen: '2',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: true,
            impfempfehlung: false,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'PERTUSSIS',
            anzahlImpfungen: '3',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: true,
            impfempfehlung: false,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'POLIO',
            anzahlImpfungen: '4',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: true,
            impfempfehlung: false,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'HEPATITIS_B',
            anzahlImpfungen: '5',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: false,
            impfempfehlung: true,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'HPV',
            anzahlImpfungen: '1',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: false,
            impfempfehlung: true,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'MASERN',
            anzahlImpfungen: '2',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: false,
            impfempfehlung: true,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'MUMPS',
            anzahlImpfungen: '3',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: false,
            impfempfehlung: true,
            impfentscheid: false,
          }),
          expect.objectContaining({
            krankheit: 'ROETELN',
            anzahlImpfungen: '4',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: false,
            impfempfehlung: false,
            impfentscheid: true,
          }),
          expect.objectContaining({
            krankheit: 'VARIZELLEN',
            anzahlImpfungen: '5',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: false,
            impfempfehlung: false,
            impfentscheid: true,
          }),
          expect.objectContaining({
            krankheit: 'MENINGOKOKKEN_A_C_W_Y',
            anzahlImpfungen: '1',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: false,
            impfempfehlung: false,
            impfentscheid: true,
          }),
          expect.objectContaining({
            krankheit: 'MENINGOKOKKEN_B',
            anzahlImpfungen: '2',
            datumLetzteImpfung: '2025-12-01',
            impfschutzGegeben: false,
            impfempfehlung: false,
            impfentscheid: true,
          }),
        ]),
      );
    });

    test('doppelkontrolle', async ({page, browser, testData}) => {
      const untersuchungDaten = new UntersuchungDatenPageObject(page);

      await openDossierTab(page, testData.anlass3.title);

      await untersuchungDaten.isCurrentPage('daten');
      await untersuchungDaten.elements.untersuchungTabImpfungen().filter({hasText: 'Impfungen'}).click();

      await untersuchungDaten.isCurrentPage('impfungen');

      await untersuchungDaten.elements.buttonVisieren().click();
      await expect(untersuchungDaten.elements.visumSelect().getFormField(page)).toBeVisible();

      await untersuchungDaten.elements.visumSelect().setOption(page, 'Monika MPA');
      await untersuchungDaten.elements.buttonVisieren().click();

      const monikaContext = await browser.newContext({
        storageState: './playwright/.auth/ESC_MPA',
      });

      const monikaPage = await monikaContext.newPage();
      await monikaPage.goto(page.url());

      const monikaUntersuchung = new UntersuchungDatenPageObject(monikaPage);

      await monikaUntersuchung.elements.buttonVisieren().click();

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
      await untersuchungDaten.elements.untersuchungTabImpfungen().filter({hasText: 'Impfungen'}).click();

      await untersuchungDaten.isCurrentPage('impfungen');

      await checklistCheckbox(page, 'Einverständnis vorhanden?')
      await checklistCheckbox(page, 'Kontraindikationen abgeklärt?')
      await checklistCheckbox(page, 'Doppelkontrolle Impfentscheid gemacht?')

      const [checklistRequest] = await Promise.all([
        page.waitForRequest(req => req.url().includes('/untersuchung/impfungen') && req.method() === 'PUT'),
        untersuchungDaten.elements.buttonSaveImpfChecklist().click(),
      ]);

      const checklistPostData = checklistRequest.postDataJSON();

      expect(checklistPostData).toMatchObject({
        checkliste: {
          checkEinwilligungVorhanden: true,
          checkKontraindikationenAbgeklaert: true,
          checkDoppelkontrolleImpfentscheidDurchgefuehrt: true,
        },
      });

      await untersuchungDaten.elements.buttonCreateImpfung().click();
      await untersuchungDaten.elements.ImpfstoffSelect().setOption(page, 'Bexsero (Meningokokken B)');
      await untersuchungDaten.elements.chargeSelect().setOption(page, '900');

      const [impfungRequest] = await Promise.all([
        page.waitForRequest(
          req => req.url().includes('/untersuchung/impfungen/impfungenintern') && req.method() === 'POST',
        ),
        untersuchungDaten.elements.buttonSaveImpfung().click(),
      ]);

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
      await untersuchungDaten.elements.untersuchungTabImpfungen().filter({hasText: 'Impfungen'}).click();
      await untersuchungDaten.isCurrentPage('impfungen');

      await untersuchungDaten.elements.informationElternSelect().setOption(page, 'Brief');
      await checklistCheckbox(page, 'Rückfrage Impfen Kinderarzt/Kinderärztin')
      await checklistCheckbox(page, 'Information Impfen Kinderarzt/Kinderärztin oder Hausarzt/Hausärztin')

      const [response] = await Promise.all([
        page.waitForResponse(
          res =>
            res.url().includes('/untersuchung/impfungen/massnahmen') &&
            res.request().method() === 'PUT' &&
            res.status() === 200,
        ),
        untersuchungDaten.elements.buttonSaveMassnahmenImpf().click(),
      ]);

      const postData = response.request().postDataJSON();

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
        const row = page.getByRole('row').filter({hasNotText: 'Filter'}).filter({hasText: new RegExp(text)});

        await expect(row).toBeVisible();
      }
    });
  });

  test('tab sehen', async ({page, testData}) => {
    const untersuchungDaten = new UntersuchungDatenPageObject(page);

    await openDossierTab(page, testData.anlass1.title);

    await untersuchungDaten.isCurrentPage('daten');
    await untersuchungDaten.elements.untersuchungTabSehen().click();

    await untersuchungDaten.elements.untersuchungSehenDefaultWert().click({timeout: 1000});

    await checkMultiSelect(page, 'Brille/Linsen', 'Nicht geprüft')
    await checkMultiSelect(page, 'Stereotest', 'Pathologisch')

    const [response] = await Promise.all([
      page.waitForResponse(
        res =>
          res.url().includes('/untersuchung/sehen/beurteilung') &&
          res.request().method() === 'PUT' &&
          res.status() === 200,
      ),
      untersuchungDaten.elements.buttonSaveSehen().click(),
    ]);

    const postData = response.request().postDataJSON();

    expect(postData).toMatchObject({
      brilleLinsen: 'NICHT_GEPRUEFT',
      stereotest: 'PATHOLOGISCH',
      farbsehen: 'NORMAL',
      verwendeteTafel: 'C_TAFEL',
      fernvisus: ['NORMAL'],
      fernvisusRechts: 1,
      fernvisusLinks: 1,
      nahvisus: ['NICHT_GEPRUEFT'],
      motilitaet: 'NORMAL',
      strabismus: ['KEIN_STRABISMUS'],
      strabismusAuspraegung: 'KEIN_STRABISMUS',
      strabismusAuftreten: 'KEIN_STRABISMUS',
      inspektion: 'UNAUFFAELLIG',
      konvergenz: 'UNAUFFAELLIG',
      version: expect.any(Number),
    });
  });

  test('tab hoeren', async ({page, testData}) => {
    const untersuchungDaten = new UntersuchungDatenPageObject(page);

    await openDossierTab(page, testData.anlass1.title);

    await untersuchungDaten.isCurrentPage('daten');
    await untersuchungDaten.elements.untersuchungTabHoeren().click({timeout: 1000});

    await untersuchungDaten.elements.untersuchungHoerenDefaultWert().click();
    await page.locator('label[for="audioaudiogramm.seite.rechtsdbHz6000DB30"]').click();

    await checkMultiSelect(page, 'Otoskopie rechts', 'Cerumen')
    await checkMultiSelect(page, 'Otoskopie rechts', 'Nicht einsehbar')
    await checkMultiSelect(page, 'Otoskopie links', 'Normal')

    const [responseAudiogramm] = await Promise.all([
      page.waitForResponse(
        res =>
          res.url().includes('/untersuchung/hoeren/audiogramm') &&
          res.request().method() === 'PUT' &&
          res.status() === 200,
      ),
      untersuchungDaten.elements.buttonSaveAudiogramm().click(),
    ]);

    const postDataAudiogramm = responseAudiogramm.request().postDataJSON();

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
      audiogrammBeurteilung: ['NORMAL'],
      zusaetzlicheAngaben: ['KEINE'],
      version: expect.any(Number),
    });

    const [responseOtoskopie] = await Promise.all([
      page.waitForResponse(
        res =>
          res.url().includes('/untersuchung/hoeren/otoskopie') &&
          res.request().method() === 'PUT' &&
          res.status() === 200,
      ),
      untersuchungDaten.elements.buttonSaveOtoskopie().click(),
    ]);

    const postDataOtoskopie = responseOtoskopie.request().postDataJSON();

    expect(postDataOtoskopie).toMatchObject({
      otoskopieRechts: ['TROMMELFELL_CERUMEN', 'NICHT_EINSEHBAR'],
      otoskopieRechtsResultatAndere: '',
      otoskopieLinks: ['NORMAL'],
      otoskopieLinksResultatAndere: '',
      version: expect.any(Number),
    });
  });

  test('tab groesse & gewicht', async ({page, testData}) => {
    const untersuchungDaten = new UntersuchungDatenPageObject(page);

    await openDossierTab(page, testData.anlass1.title);

    await untersuchungDaten.isCurrentPage('daten');
    await untersuchungDaten.elements.untersuchungTabGroesseGewicht().click();
    await untersuchungDaten.isCurrentPage('groessegewicht');

    await untersuchungDaten.elements.groesseInput().getInput(page).fill('120');
    await untersuchungDaten.elements.gewichtInput().getInput(page).fill('30');

    await checkMultiSelect(page, 'Grösse', 'Normal')
    await checkMultiSelect(page, 'Gewicht', '>90%ile')
    await checkMultiSelect(page, 'BMI', '>90%ile')

    const [response] = await Promise.all([
      page.waitForResponse(
        res =>
          res.url().includes('/untersuchung/groessegewicht') &&
          res.request().method() === 'PUT' &&
          res.status() === 200,
      ),
      untersuchungDaten.elements.buttonSaveGroesseGewicht().click(),
    ]);

    const postData = response.request().postDataJSON();

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
    await untersuchungDaten.elements.untersuchungTabArzt().click();

    await untersuchungDaten.isCurrentPage('arzt')

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

    await untersuchungDaten.elements.systolischInput().getInput(page).fill('110');
    await untersuchungDaten.elements.diastolischInput().getInput(page).fill('70');

    const [response] = await Promise.all([
      page.waitForResponse(
        res =>
          res.url().includes('/untersuchung/arzt/beurteilung') &&
          res.request().method() === 'PUT' &&
          res.status() === 200,
      ),
      untersuchungDaten.elements.buttonSaveArzt().click(),
    ]);

    const postData = response.request().postDataJSON();

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
    await untersuchungDaten.elements.untersuchungTabMassnahmen().click();

    await untersuchungDaten.isCurrentPage('massnahmen')

    await checklistCheckbox(page, 'Überweisung an Hausarzt/Hausärztin oder Kinderarzt/Kinderärztin')

    await checklistCheckbox(page, 'Überweisung an Augenarzt/Augenärztin')

    await untersuchungDaten.elements.informationElternSelect().setOption(page, 'Mail');

    const [response] = await Promise.all([
      page.waitForResponse(
        res =>
          res.url().includes('/untersuchung/massnahmen') &&
          res.request().method() === 'PUT' &&
          res.status() === 200,
      ),
      untersuchungDaten.elements.buttonSaveMassnahmen().click(),
    ]);

    const postData = response.request().postDataJSON();

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
      const row = page.getByRole('row').filter({hasNotText: 'Filter'}).filter({hasText: new RegExp(text)});
      await expect(row).toBeVisible();
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
    await getRow().getByTestId('impfschutz-genugend-checkbox').click();
  }
  if (options.empfehlung) {
    await getRow().getByTestId('impfempfehlung-checkbox').click();
  }
  if (options.entscheid) {
    await getRow().getByTestId('impfentscheid-checkbox').click();
  }

  if (options.amount !== undefined) {
    await getRow().getByTestId('impfung-amount-input').fill(String(options.amount));
  }

  if (options.date) {
    const mm = format(options.date, 'MM');
    const yyyy = format(options.date, 'yyyy');
    const input = getRow().locator('svui-table-control[data-testid="impfung-date-input"] input');

    await input.pressSequentially(mm);
    await input.press('ArrowRight');
    await input.pressSequentially(yyyy);
  }
}

async function checkMultiSelect(page: Page, title: string, option: string) {
  const row = page.locator('svui-kachel-form-section', {hasText: title});

  await row.locator('svui-kachel-control', {hasText: option}).locator('label').click();
}

async function checklistCheckbox(page: Page, option: string) {
  const untersuchungDaten = new UntersuchungDatenPageObject(page);

  const checklist = page.locator('svui-standalone-control', {hasText: option});

  await expect(checklist).toBeVisible();
  await untersuchungDaten.elements.checkboxChecklist().getCheckboxLabel(checklist).click();
}
