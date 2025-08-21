import {CommonModule} from '@angular/common';
import {
    AfterContentInit,
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnInit,
    Output,
} from '@angular/core';
import {ButtonComponent} from '@dvb/ui-components';
import {requireValue} from '@dvb/util-shared';
import {ChartData} from '../../../d3/_interface';
import {NamedChartDataCollection} from '../../_interface';

@Component({
    selector: 'svui-button-filters',
    templateUrl: './button-filters.component.html',
    styleUrls: ['./button-filters.component.scss'],
    imports: [
        CommonModule,
        ButtonComponent,
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ButtonFiltersComponent<T extends ChartData> implements OnInit, AfterContentInit {

    @Input()
    datasets!: NamedChartDataCollection<T>[];

    @Input()
    initiallyActiveDatasetId!: string;

    @Input()
    public activeDataId!: string;

    @Output()
    useDataset = new EventEmitter<T>();

    activeData!: T;

    public ngOnInit(): void {
        this.activeDataId = this.initiallyActiveDatasetId;
        this.activeData = requireValue(this.datasets.find(dataset => dataset.id === this.activeDataId)).data;
    }

    public ngAfterContentInit(): void {
        this.useDataset.emit(this.activeData);
    }

    chooseDataset(dataset: NamedChartDataCollection<T>): void {
        this.activeDataId = dataset.id;
        this.activeData = dataset.data;
        this.useDataset.emit(dataset.data);
    }
}
