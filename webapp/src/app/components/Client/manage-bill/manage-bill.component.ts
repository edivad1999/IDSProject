import {Component, OnInit} from '@angular/core';
import {RepositoryService} from '../../../data/repository/repository.service';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {Bill, Course, Dish, Role, SimpleUser} from '../../../domain/model/data';

export interface DishesGrouped {
  [username: string]: Dish[];
}

@Component({
  selector: 'app-manage-bill',
  templateUrl: './manage-bill.component.html',
  styleUrls: ['./manage-bill.component.css']
})
export class ManageBillComponent extends SubscriberContextComponent implements OnInit {

  bill: Bill | null = null;
  user: SimpleUser | null = null;

  lastOpenNumber = 0;

  constructor(
    private repo: RepositoryService
  ) {
    super();
  }

  ngOnInit(): void {
    this.subscribeWithContext(this.repo.getUser(), it => this.user = it);
    this.subscribeWithContext(this.repo.getBill(), it => {
      this.updateBill(it);
      if (it) {
        // todo uncomment to restart ws
        this.subscribeWithContext(this.repo.billFlow(it.id), bill => this.updateBill(bill));

      }

    });
  }

  updateBill(bill: Bill | null): void {

    this.bill = bill;

  }

  getDateStringFromInstant(instant: number): string {
    const date = (new Date(instant));
    return `${('0' + date.getHours()).slice(-2)}:${('0' + date.getMinutes()).slice(-2)}`;
  }

  getDishesGroupedByUsers(dishes: Dish[]): DishesGrouped {
    const res: DishesGrouped = {};
    dishes.forEach(it => {
      const username: string = it.relatedClient.role >= Role.CLIENT ? 'Cameriere' : it.relatedClient.username;
      if (res[username]) {
        res[username].push(it);
      } else {
        res[username] = [it];
      }
    });
    return res;
  }

  checkDishOwner(dish: Dish): boolean {
    if (this.user) {
      if (this.user.role >= Role.CLIENT) {
        return true;
      } else {
        return this.user.username === dish.relatedClient.username;
      }
    } else {
      return false;
    }

  }

  deleteDish(dish: Dish): void {

  }

  toggleImReady(course: Course, event: Event): void {
    event.stopImmediatePropagation();
    if (course.readyClients.filter(it => it.username === this.user?.username).length < 1) {
      this.subscribeWithContext(
        this.repo.setReady(course.id), action => {
          if (action) {
            // tslint:disable-next-line:no-non-null-assertion
            course.readyClients.push(this.user!);
          }
        });
    } else {
      this.subscribeWithContext(
        this.repo.setReady(course.id), action => {
          if (action) {
            // tslint:disable-next-line:no-non-null-assertion
            course.readyClients = course.readyClients.filter(it => it.username !== this.user?.username);
          }
        });
    }
  }


  amIReady(course: Course): boolean {
    if (this.bill && this.user) {
      return course.readyClients.filter(it => it.username === this.user?.username).length < 1;
    } else {
      return false;
    }
  }
}
