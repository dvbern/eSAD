 import { sortList } from './sort-functions';

export interface Beitrag {
  id?: string;
  message: string;
  title: string;
}

describe('sort', () => {
  test('no sort', () => {
    expect(
      sortList(originalList, {
        colSorts: [],
      })
    ).toStrictEqual(originalList);
  });
  test('empty sort', () => {
    expect(
      sortList(originalList, {
        colSorts: [{ colName: 'title', sortDirection: 'NONE' }],
      })
    ).toStrictEqual(originalList);
  });

  test('title asc', () => {
    expect(
      sortList(originalList, {
        colSorts: [{ colName: 'title', sortDirection: 'ASC' }],
      })
    ).toStrictEqual(titleAsc);
  });
  test('title desc', () => {
    expect(
      sortList(originalList, {
        colSorts: [{ colName: 'title', sortDirection: 'DESC' }],
      })
    ).toStrictEqual([...titleAsc].reverse());
  });

  test('message asc', () => {
    expect(
      sortList(originalList, {
        colSorts: [{ colName: 'message', sortDirection: 'ASC' }],
      })
    ).toStrictEqual(messageAsc);
  });
  test('message desc', () => {
    expect(
      sortList(originalList, {
        colSorts: [{ colName: 'message', sortDirection: 'DESC' }],
      })
    ).toStrictEqual([...messageAsc].reverse());
  });

  test('id asc', () => {
    expect(
      sortList(originalList, {
        colSorts: [{ colName: 'id', sortDirection: 'ASC' }],
      })
    ).toStrictEqual(idAsc);
  });
  test('id desc', () => {
    expect(
      sortList(originalList, {
        colSorts: [{ colName: 'id', sortDirection: 'DESC' }],
      })?.slice(-3)
    ) // ignore the order inside the undefineds but make sure they are at the right place
      .toStrictEqual([...idAsc].reverse().slice(-3));
  });

  test('multi: not implemented', () => {
    expect(() =>
      sortList(originalList, {
        colSorts: [
          { colName: 'title', sortDirection: 'ASC' },
          { colName: 'message', sortDirection: 'ASC' },
        ],
      })
    ).toThrow();
  });
});

export const originalList: Beitrag[] = [
  { title: 'B', message: '6', id: '6' },
  { title: 'F', message: '5', id: '5' },
  { title: 'E', message: '4', id: undefined },
  { title: 'C', message: '3', id: undefined },
  { title: 'D', message: '2', id: '2' },
  { title: 'A', message: '1', id: undefined },
];

export const titleAsc: Beitrag[] = [
  { title: 'A', message: '1', id: undefined },
  { title: 'B', message: '6', id: '6' },
  { title: 'C', message: '3', id: undefined },
  { title: 'D', message: '2', id: '2' },
  { title: 'E', message: '4', id: undefined },
  { title: 'F', message: '5', id: '5' },
];

export const messageAsc: Beitrag[] = [
  { title: 'A', message: '1', id: undefined },
  { title: 'D', message: '2', id: '2' },
  { title: 'C', message: '3', id: undefined },
  { title: 'E', message: '4', id: undefined },
  { title: 'F', message: '5', id: '5' },
  { title: 'B', message: '6', id: '6' },
];

export const idAsc: Beitrag[] = [
  { title: 'D', message: '2', id: '2' },
  { title: 'F', message: '5', id: '5' },
  { title: 'B', message: '6', id: '6' },
  { title: 'E', message: '4', id: undefined },
  { title: 'C', message: '3', id: undefined },
  { title: 'A', message: '1', id: undefined },
];
