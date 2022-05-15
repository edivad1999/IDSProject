import {Component, OnInit} from '@angular/core';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {RepositoryService} from '../../../data/repository/repository.service';
import {Bill, Table} from '../../../domain/model/data';


@Component({
  selector: 'app-waiter-home',
  templateUrl: './waiter-home.component.html',
  styleUrls: ['./waiter-home.component.css']
})
export class WaiterHomeComponent extends SubscriberContextComponent implements OnInit {

  bills: Bill[] = [];
  tables: Table[] = [];

  constructor(
    private repo: RepositoryService
  ) {
    super();
  }

  ngOnInit(): void {
    this.update();
  }

  update(): void {
    this.subscribeWithContext(
      this.repo.billList(), action => {
        this.bills = action;
      }
    );
    this.subscribeWithContext(
      this.repo.getAllTables(), action => {
        this.tables = action.sort((a: Table, b: Table) => a.number > b.number ? 1 : -1);
      }
    );

  }

  findRelatedBill(table: Table): Bill | null {
    const filtered = this.bills.filter(it => it.closedAt == null).filter(it => it.relatedTable.id === table.id);
    return filtered.length > 0 ? filtered[0] : null;
  }

}
