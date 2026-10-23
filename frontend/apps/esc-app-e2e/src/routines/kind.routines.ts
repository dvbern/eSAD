import {APIRequestContext, expect, Page} from '@playwright/test';
import {KinderListPageObject} from '../e2e/kinder/page-objects/kinder-list.page-object';
import {KinderPageObject} from '../e2e/kinder/page-objects/kinder.page-object';

export type TestKind = {nachname: string; vorname: string};

/**
 * Creates a Kind, with its dossier, through the API.
 *
 * Tests that run at the same time must not open the same Kind: the first time a dossier is opened,
 * the backend creates its Nachkontrolle, and two such requests at once collide on a unique
 * constraint. The seed data has too few Kinder to give every test its own, so tests create theirs.
 *
 * @param request the request context of a logged-in page, e.g. page.request
 */
export async function createKind(request: APIRequestContext, kind: TestKind) {
  const response = await request.post('api/kinder', {
    params: {kindSaveMode: 'NO_CHECK'},
    data: {
      name: kind.nachname,
      vorname: kind.vorname,
      geschlecht: 'UNBEKANNT',
      geburtsdatum: '2019-05-01',
      alter: '',
      kindAktivStatus: 'AKTIV',
      version: -1,
    },
  });

  expect(response.status(), `POST /api/kinder: ${await response.text()}`).toBe(201);
}

/** Opens the dossier of the given Kind through the Kinder list, filtering for its Nachname. */
export async function openKindFromList(page: Page, kind: TestKind) {
  const kinderList = new KinderListPageObject(page);

  await kinderList.goto();
  await kinderList.elements.filterNachname().fill(kind.nachname);

  await expect(kinderList.elements.rows()).toHaveCount(1);
  await expect(kinderList.elements.rows()).toContainText(kind.vorname);

  await kinderList.elements.linkKinder().first().click();

  await new KinderPageObject(page).isCurrentPage('detail');
}
