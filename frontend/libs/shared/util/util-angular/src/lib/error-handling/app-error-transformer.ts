// just to avoid the import of @angular/common/http...
import {AppFailureErrorJson, AppValidationErrorJson} from '@dvb/data-access-backend';
import {AppNotification} from '../notifications';

let counter = 1;

export declare interface HttpErrorResponse {
    message: string;
    error: unknown | null;
    ok: boolean;
    status?: number;
}

function instanceOfAppError(object: unknown): object is AppFailureErrorJson {
    if (typeof object === typeof '') {
        return false;
    } // how do we correctly check if type is string?
    return (
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            'id' in object &&
            'summary' in object &&
            'type' in object
    );
}

function instanceOfAppValidation(object: unknown): object is AppValidationErrorJson {
    if (typeof object === typeof '') {
        return false;
    } // how do we correctly check if type is string?
    return (
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            'violations' in object &&
            'type' in object
    );
}

function instanceOfHttpErrorResponse(object: unknown): object is HttpErrorResponse {
    if (typeof object === typeof '') {
        return false;
    } // how do we correctly check if type is string?
    return (
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            'message' in object &&
            'error' in object &&
            'ok' in object
    );
}

export function convertUnknownAppErrorToNotification(
        error: unknown,
): AppNotification {

    if (instanceOfAppError(error)) {
        return {
            id: error.id,
            summary: error.summary,
            alertType: 'danger',
            autohide: false,
        };
    }
    if (instanceOfAppValidation(error)) {
        // this notification should never be displayed but handled directly in the form
        return {
            id: 'appvalidation' + counter++,
            summary: (error.violations
                    .map(each => each.message)
                    .join(', ')),
            alertType: 'warning',
            autohide: false,
        }
    }

    if (instanceOfHttpErrorResponse(error) && error.status && error.message) {
        return {
            id: 'id' + counter++,
            summary: '[' + error.status + '] ' + error.message,
            alertType: 'danger',
            autohide: false,
        }
    }

    console.error('Unknown error', error);
    return {
        id: 'id' + counter++,
        summary: 'Unbekannter Fehler',
        alertType: 'danger',
        autohide: false,
    };

}

