import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideTestingTransloco} from '@dvb/util-i18n';
import {NgxFileDropEntry} from 'ngx-file-drop';
import {FileDropComponent} from './file-drop.component';

function createEntry(name: string): NgxFileDropEntry {
  const file = new File([''], name, {type: 'application/pdf'});
  const fileEntry = {
    isFile: true,
    isDirectory: false,
    name,
    file: (cb: (f: File) => void) => setTimeout(() => cb(file), 0),
  };

  return new NgxFileDropEntry(name, fileEntry as unknown as FileSystemFileEntry);
}

describe('FileDropComponent', () => {
  let component: FileDropComponent;
  let fixture: ComponentFixture<FileDropComponent>;

  const stagedFileNames = (): string[] =>
    Array.from<HTMLElement>(
      fixture.nativeElement.querySelectorAll('.file_drop__upload_list_filename span'),
    )
      .map(span => span.textContent?.trim() ?? '');

  // the FileSystemFileEntry.file() callback is a macrotask, which whenStable() does not await
  const flushFileCallbacks = async (): Promise<void> => {
    await new Promise(resolve => setTimeout(resolve, 0));
    await fixture.whenStable();
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileDropComponent],
      providers: [provideTestingTransloco()],
    }).compileComponents();

    fixture = TestBed.createComponent(FileDropComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('acceptedFiletypes', 'application/pdf');
    fixture.detectChanges();
  });

  it('should create', () => expect(component)
    .toBeTruthy());

  it('renders no staged file list before any file is dropped', () => {
    expect(fixture.nativeElement.querySelector('[data-testid="upload-staged-files"]'))
      .toBeNull();
  });

  // the file() callback is a non-Angular async callback, so nothing marks the view dirty
  // unless the staged file list is a signal
  it('renders the staged files after they are dropped', async () => {
    component.dropped([createEntry('first.pdf'), createEntry('second.pdf')]);

    await flushFileCallbacks();

    expect(stagedFileNames())
      .toEqual(['first.pdf', 'second.pdf']);
  });

  // regression: removeFileFromUpload() used to mutate the array without any change detection
  // of its own, so it only repainted because the delete button's event binding happened to
  // mark the view dirty
  it('stops rendering a staged file once it is removed', async () => {
    const first = createEntry('first.pdf');
    const second = createEntry('second.pdf');
    component.dropped([first, second]);
    await flushFileCallbacks();

    component.removeFileFromUpload(first);
    await flushFileCallbacks();

    expect(stagedFileNames())
      .toEqual(['second.pdf']);
  });

  it('emits the remaining files after one is removed', async () => {
    const emitted: string[][] = [];
    component.filesToUpload.subscribe(files => emitted.push(files.map(f => f.name)));
    const first = createEntry('first.pdf');
    const second = createEntry('second.pdf');
    component.dropped([first, second]);
    await flushFileCallbacks();

    component.removeFileFromUpload(first);
    await flushFileCallbacks();

    expect(emitted.at(-1))
      .toEqual(['second.pdf']);
  });

  it('renders no staged file list after clear()', async () => {
    component.dropped([createEntry('first.pdf')]);
    await flushFileCallbacks();

    component.clear();
    await flushFileCallbacks();

    expect(fixture.nativeElement.querySelector('[data-testid="upload-staged-files"]'))
      .toBeNull();
  });
});
