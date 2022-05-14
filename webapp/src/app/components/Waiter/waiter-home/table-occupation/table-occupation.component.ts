import {Component, Input, OnInit} from '@angular/core';
import {Bill, Table} from '../../../../domain/model/data';
import {SubscriberContextComponent} from '../../../../utils/subscriber-context.component';
import {RepositoryService} from '../../../../data/repository/repository.service';
import {MatDialog} from '@angular/material/dialog';
import {OpenBillComponent} from './open-bill/open-bill.component';

@Component({
  selector: 'app-table-occupation',
  templateUrl: './table-occupation.component.html',
  styleUrls: ['./table-occupation.component.css']
})
export class TableOccupationComponent extends SubscriberContextComponent implements OnInit {

  @Input() bill!: Bill | null;
  @Input() table!: Table;


  constructor(
    private repo: RepositoryService,
    private dialog: MatDialog
  ) {
    super();
  }

  ngOnInit(): void {
  }

  getUrl(): string | null {
    if (this.bill) {
      return `/bill/${this.bill.id}`;
    } else {
      return null;
    }
  }

  openBill(): void {
    const dialogRef = this.dialog.open(
      OpenBillComponent, {data: this.table, panelClass: 'app-full-bleed-dialog'}
    );

  }
}
