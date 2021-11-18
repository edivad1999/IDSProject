import {NgModule} from '@angular/core';
import {CentralColumnComponent} from './central-column.component';
import {CommonModule} from '@angular/common';

@NgModule({
  declarations: [CentralColumnComponent],
  imports: [CommonModule],
  exports: [CentralColumnComponent]
})
export class CentralColumnModule {
}
