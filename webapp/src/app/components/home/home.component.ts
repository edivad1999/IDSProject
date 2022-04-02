import {Component, OnInit} from '@angular/core';
import {SubscriberContextComponent} from '../../utils/subscriber-context.component';
import {RepositoryService} from '../../data/repository/repository.service';
import {Role} from '../../domain/model/data';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent extends SubscriberContextComponent implements OnInit {

  myRole: Role | null = null;

  constructor(
    private repo: RepositoryService
  ) {
    super();
  }

  ngOnInit(): void {

    this.subscribeWithContext(
      this.repo.whoAmI(), action => {
        this.myRole = action;
      }
    );
  }

}
