import {Component, Input} from '@angular/core';

@Component({
  selector: 'lib-central-column',
  templateUrl: './central-column.component.html',
  styleUrls: ['./central-column.component.scss']
})
export class CentralColumnComponent {
  @Input()
  stretchWidth = true;
  @Input()
  hasSmallBorders = false;
}
