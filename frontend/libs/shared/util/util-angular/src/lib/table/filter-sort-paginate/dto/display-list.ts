/*
DisplayListDTo but with a typed list
 */
import {DisplayListDTO} from './display-list-dto';

export interface DisplayList<T extends object> extends DisplayListDTO {
  displayedList?: T[];
  filteredSortedList?: T[];
}
