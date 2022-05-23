import {Component, OnInit} from '@angular/core';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {RepositoryService} from '../../../data/repository/repository.service';
import {Role} from '../../../domain/model/data';

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
    this.repo.role.subscribe(
      role => this.myRole = role
    );
  }

  isManager(role: Role): boolean | null {
    if (this.myRole !== null) {
      return role === 'MANAGER';
    } else {
      return null;
    }
  }

  isClient(role: Role): boolean | null {
    if (this.myRole !== null) {
      return role === 'CLIENT';
    } else {
      return null;
    }
  }

  isWaiter(role: Role): boolean | null {
    if (this.myRole !== null) {
      return role === 'WAITER';
    } else {
      return null;
    }
  }

  isKitchen(role: Role): boolean | null {
    if (this.myRole !== null) {
      return role === 'KITCHEN';
    } else {
      return null;
    }
  }

}
