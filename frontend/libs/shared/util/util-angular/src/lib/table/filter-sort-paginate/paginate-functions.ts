import { PaginationDTO } from './dto/pagination-dto';

export function paginate<T>(
  entries: T[] | undefined,
  { pageNr, pageSize }: PaginationDTO,
): {
  list: T[] | undefined;
  page: number;
} {
  if (!entries) {
    return { list: undefined, page: pageNr };
  }

  let startIndex: number = (pageNr - 1) * pageSize;
  let page = pageNr;
  if (startIndex < 0 || startIndex > entries.length - 1) {
    page = 1;
    startIndex = 0;
  }
  const result = entries.slice(startIndex, startIndex + pageSize);
  return {
    list: result,
    page,
  };
}
