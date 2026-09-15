import {ChangeDetectionStrategy, Component, computed, inject, input, OnDestroy} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {FileDataJson, PreviewService} from '@dvb/data-access-backend';
import {requireValidBackendEntityId} from '@dvb/data-access-types';
import {IconComponent} from '@dvb/ui-components';
import {TranslocoPipe} from '@jsverse/transloco';
import {catchError, map, Observable, of, startWith, switchMap, tap} from 'rxjs';

type PreviewStatus = 'loading' | 'ready' | 'unsupported' | 'error';

interface PreviewImageModel {
  url: string | null; // actually a bloburl
  status: PreviewStatus;
}

const SUPPORTED_MIME_TYPES: ReadonlySet<string> = new Set([
  'application/pdf',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'image/png',
  'image/jpeg',
]);

const INITIAL_STATE: PreviewImageModel = { url: null, status: 'loading' };

@Component({
  selector: 'feat-file-preview',
  templateUrl: './file-preview.component.html',
  styleUrl: './file-preview.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoPipe, IconComponent],
})
export class FilePreviewComponent implements OnDestroy {
  readonly file = input.required<FileDataJson>();
  readonly notizId = input<string | null>(null);
  readonly aufgabeId = input<string | null>(null);

  readonly #previewService = inject(PreviewService);

  readonly title = computed(() => this.file().calculatedFileName);

  /**
   * Track the current Object URL so we can revoke it when it's replaced
   * or when the component is destroyed.
   */
  #currentObjectUrl: string | null = null;

  readonly #previewImgModel = toSignal(
    toObservable(this.file) //whenever selected file changes
      .pipe(
        switchMap(file => this.#loadPreview$(file)),
        tap(state => this.#changeCurrentObjectURL(state.url)),
      ),
    { initialValue: INITIAL_STATE },
  );

  readonly previewUrl = computed(() => this.#previewImgModel().url);
  readonly status = computed(() => this.#previewImgModel().status);

  ngOnDestroy(): void {
    this.#changeCurrentObjectURL(null);
  }

  #loadPreview$(file: FileDataJson): Observable<PreviewImageModel> {
    if (!SUPPORTED_MIME_TYPES.has(file.mimeType)) {
      return of({ url: null, status: 'unsupported' });
    }

    const request$ = this.#createPreviewRequest$(file);
    if (!request$) {
      return of({ url: null, status: 'error' });
    }

    return request$.pipe(
      map(blob => ({ url: URL.createObjectURL(blob), status: 'ready' as const })),
      startWith<PreviewImageModel>({ url: null, status: 'loading' }),
      catchError(() => of<PreviewImageModel>({ url: null, status: 'error' })),
    );
  }

  #createPreviewRequest$(file: FileDataJson): Observable<Blob> | null {
    const notizId = this.notizId();
    const aufgabeId = this.aufgabeId();
    const opts = ['body', false, { httpHeaderAccept: 'application/octet-stream' }] as const;
    // either  notiz or aufgabe doc
    if (notizId) {
      return this.#previewService.downloadPreviewNotizFile$(
        { notizId: requireValidBackendEntityId(notizId), fileId: file.id },
        ...opts,
      );
    }
    if (aufgabeId) {
      return this.#previewService.downloadPreviewAufgabeFile$(
        { aufgabeId: requireValidBackendEntityId(aufgabeId), fileId: file.id },
        ...opts,
      );
    }
    return null;
  }

  /**
   * Revokes the previously tracked Object URL if it's being replaced,
   * then remembers the new one. Pass `null` on destroy to revoke any leftover URL.
   */
  #changeCurrentObjectURL(newUrl: string | null): void {
    if (this.#currentObjectUrl && this.#currentObjectUrl !== newUrl) {
      URL.revokeObjectURL(this.#currentObjectUrl);
    }
    this.#currentObjectUrl = newUrl;
  }
}
