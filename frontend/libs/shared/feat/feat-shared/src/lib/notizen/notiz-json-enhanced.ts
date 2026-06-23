import {NotizListItemJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';

/**
 * Enhanced Variante von {@link NotizListItemJson} für die Anzeige in der Notizen-Tabelle.
 *
 * Ergänzt das schlanke Server-DTO um Felder, die nur clientseitig für Filterung / Sortierung
 * benötigt werden (z. B. `labelIds`).
 */
export interface NotizJsonEnhanced extends NotizListItemJson {
  labelIds: Array<BackendEntityId>;
}
