import {Injectable} from '@angular/core';
import {DisplayConfigDTO, prepareDisplayList} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';
import {delay, Observable, of} from 'rxjs';
import {MyDummyItem} from './playground-liste.store';

@Injectable()
export class FakeBackend {

  dummyBackendList = this.generateDummyList(88);

  loadDummyListPaginated$(displayConfig: DisplayConfigDTO<MyDummyItem>): Observable<{
    page: number,
    pageSize: number,
    totalItemCount: number,
    data: MyDummyItem[]
  }> {

    const list = prepareDisplayList(this.dummyBackendList, displayConfig);

    const response = {
      page: displayConfig.pagination?.pageNr ?? 1,
      pageSize: displayConfig.pagination?.pageSize ?? 1,
      totalItemCount: list.filteredSize ?? 0,
      data: requireValue(list.displayedList),
    };

    console.log('fake backend: loadDummyListPaginated$', displayConfig, response);
    return of(response).pipe(delay(500 + (Math.random() * 2000)));

  }

  loadDummyListFull$(): Observable<MyDummyItem[]> {
    const list = this.dummyBackendList;

    console.log('fake backend: loadDummyListFull$', list);

    return of(list).pipe(delay(500 + (Math.random() * 2000)));

  }

  generateDummyList(size: number): MyDummyItem[] {
    const list = [];
    for (let i = 0; i < size; i++) {
      const dummy: MyDummyItem = {
        name: this.getRandomStrings(5 + Math.floor(Math.random() * 10)),
        vorname: this.getRandomStrings(5 + Math.floor(Math.random() * 10)),
      };
      list.push(dummy);
    }
    return list;
  }

  getRandomStrings(length: number) {
    const value = 'abcdefghijklmnopqrstuvwxy';
    const randoms = [];
    for (let i = 0; i < length; i++) {
      randoms.push(value[Math.floor(Math.random() * value.length)]);
    }
    randoms[0] = randoms[0].toUpperCase();
    return randoms.join('');
  }

}
