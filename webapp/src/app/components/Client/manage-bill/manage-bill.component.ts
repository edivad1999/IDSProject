import {Component, OnInit} from '@angular/core';
import {RepositoryService} from '../../../data/repository/repository.service';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {Bill, Dish, SimpleUser} from '../../../domain/model/data';

export interface DishesGrouped {
  [username: string]: Dish[];

  waiterDishes: Dish[];

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
    const result: DishesGrouped = {waiterDishes: []};
    dishes.forEach(dish => {
      if (dish.relatedClient !== null) {
        if (!result[dish.relatedClient.username]) {
          result[dish.relatedClient.username] = [];
        }
        result[dish.relatedClient.username].push(dish);
      } else {
        result.waiterDishes.push(dish);
      }
    });
    return result;
  }

  getUsers(dishes: Dish[]) {
    // tslint:disable-next-line:no-non-null-assertion
    console.log(dishes.filter(it => it.relatedClient).map(it => it.relatedClient!.username));
    return dishes.filter(it => it.relatedClient).map(it => it.relatedClient!.username);
  }
}
