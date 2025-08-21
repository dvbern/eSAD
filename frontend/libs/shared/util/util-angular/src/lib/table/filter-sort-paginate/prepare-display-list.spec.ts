import {prepareDisplayList} from './prepare-display-list';
import {Beitrag} from './sort.spec';

describe('filter, sort and paginate', () => {
  test('find substring', () => {
    expect(
      prepareDisplayList(originalList, {
        filter: {
          filters: [
            {
              filterName: 'title',
              filterMethod: 'SUBSTRING',
              filterValue: 'ee' as never,
              filterCombinationMethod: 'AND',
            },
          ],
        },
        sort: {colSorts: [{colName: 'title', sortDirection: 'ASC'}]},
        pagination: {pageNr: 3, pageSize: 2},
        needsOriginalSize: true,
      }),
    ).toStrictEqual({
      displayedList: [
        {title: 'Eee', message: '4'},
        {title: 'Fee', message: '5'},
      ],
      filteredSortedList: [
        {title: '1ee', message: '1'},
        {title: '3eee', message: '1'},
        {title: 'A   eee', message: '1'},
        {title: 'Bee', message: '6'},
        {title: 'Eee', message: '4'},
        {title: 'Fee', message: '5'},
        {title: 'e e eee', message: '1'},
        {title: 'e99eee', message: '1'},
        {title: 'eee', message: '1'},
        {title: 'eeeee', message: '1'},
      ],
      filteredSize: 10,
      originalSize: 13,
      pageNr: 3,
    });
  });

  test('bad page', () => {
    expect(
      prepareDisplayList(originalList, {
        filter: {
          filters: [
            {
              filterName: 'title',
              filterMethod: 'SUBSTRING',
              filterValue: 'ee' as never,
              filterCombinationMethod: 'AND',
            },
          ],
        },
        sort: {colSorts: [{colName: 'title', sortDirection: 'ASC'}]},
        pagination: {pageNr: 33, pageSize: 2},
        needsOriginalSize: true,
      }),
    ).toStrictEqual({
      displayedList: [
        {title: '1ee', message: '1'},
        {title: '3eee', message: '1'},
      ],
      filteredSize: 10,
      originalSize: 13,
      pageNr: 1,
      filteredSortedList: [
        {title: '1ee', message: '1'},
        {title: '3eee', message: '1'},
        {title: 'A   eee', message: '1'},
        {title: 'Bee', message: '6'},
        {title: 'Eee', message: '4'},
        {title: 'Fee', message: '5'},
        {title: 'e e eee', message: '1'},
        {title: 'e99eee', message: '1'},
        {title: 'eee', message: '1'},
        {title: 'eeeee', message: '1'},
      ],
    });
  });
});

export const originalList: Beitrag[] = [
  {title: 'Bee', message: '6'},
  {title: 'Fee', message: '5'},
  {title: 'Eee', message: '4'},
  {title: 'Coucou', message: '3'},
  {title: 'Dada', message: '2'},
  {title: 'Aha', message: '1'},
  {title: 'eee', message: '1'},
  {title: 'eeeee', message: '1'},
  {title: 'e e eee', message: '1'},
  {title: 'e99eee', message: '1'},
  {title: '3eee', message: '1'},
  {title: '1ee', message: '1'},
  {title: 'A   eee', message: '1'},
];

export const filteredAndSorted: Beitrag[] = [
  {title: '1ee', message: '1'},
  {title: '3eee', message: '1'},
  {title: 'A   eee', message: '1'},
  {title: 'Bee', message: '6'},
  {title: 'Eee', message: '4'},
  {title: 'Fee', message: '5'},
  {title: 'e e eee', message: '1'},
  {title: 'e99eee', message: '1'},
  {title: 'eeeee', message: '1'},
  {title: 'eee', message: '1'},
];
