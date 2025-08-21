import { paginate } from './paginate-functions';

describe('paginate', () => {
  test('normal page 1', () => {
    expect(paginate(originalList, { pageNr: 1, pageSize: 2 })).toStrictEqual({
      list: [1, 2],
      page: 1,
    });
  });
  test('normal page 2', () => {
    expect(paginate(originalList, { pageNr: 2, pageSize: 2 })).toStrictEqual({
      list: [3, 4],
      page: 2,
    });
  });

  test('bad page: 0', () => {
    expect(paginate(originalList, { pageNr: 0, pageSize: 2 })).toStrictEqual({
      list: [1, 2],
      page: 1,
    });
  });
  test('bad page: 11', () => {
    expect(paginate(originalList, { pageNr: 11, pageSize: 2 })).toStrictEqual({
      list: [1, 2],
      page: 1,
    });
  });

  test('normal page 10', () => {
    expect(paginate(originalList, { pageNr: 10, pageSize: 2 })).toStrictEqual({
      list: [19, 20],
      page: 10,
    });
  });

  test('normal big page 1', () => {
    expect(paginate(originalList, { pageNr: 1, pageSize: 18 })).toStrictEqual({
      list: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18],
      page: 1,
    });
  });
  test('normal big page 2', () => {
    expect(paginate(originalList, { pageNr: 2, pageSize: 18 })).toStrictEqual({
      list: [19, 20],
      page: 2,
    });
  });
});

const originalList: number[] = [
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
];
