import {Component, OnInit} from '@angular/core';
import {RepositoryService} from '../../../data/repository/repository.service';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {Bill} from '../../../domain/model/data';

@Component({
  selector: 'app-manage-bill',
  templateUrl: './manage-bill.component.html',
  styleUrls: ['./manage-bill.component.css']
})
export class ManageBillComponent extends SubscriberContextComponent implements OnInit {

  bill: Bill | null = null;

  constructor(
    private repo: RepositoryService
  ) {
    super();
  }

  ngOnInit(): void {
    this.subscribeWithContext(this.repo.getBill(), it => this.bill = it);
  }

  getDateStringFromInstant(instant: number): string {
    const date = (new Date(instant));

    return `${date.getHours()}:${date.getMinutes()}`;
  }
}
