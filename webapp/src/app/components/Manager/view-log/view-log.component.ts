import {Component, OnInit} from '@angular/core';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {RepositoryService} from '../../../data/repository/repository.service';

@Component({
  selector: 'app-view-log',
  templateUrl: './view-log.component.html',
  styleUrls: ['./view-log.component.css']
})
export class ViewLogComponent extends SubscriberContextComponent implements OnInit {

  constructor(private repo: RepositoryService) {
    super();
  }

  logLines: string[] = [];

  ngOnInit(): void {
    this.subscribeWithContext(
      this.repo.getLog(), it => this.logLines = it
    );

  }

}
