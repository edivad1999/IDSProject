import {Component, OnInit} from '@angular/core';
import {RepositoryService} from '../../../data/repository/repository.service';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {Bill, compareRole, Course, Dish, SimpleUser} from '../../../domain/model/data';
import {filter, map, mergeMap} from 'rxjs/operators';
import {ActivatedRoute} from '@angular/router';

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

  lastOpenNumber: number | null = null;

  setLastOpen(n: number): void {

    this.lastOpenNumber = n;
  }

  constructor(
    private route: ActivatedRoute,
    private repo: RepositoryService
  ) {
    super();
  }

  ngOnInit(): void {
    this.subscribeWithContext(this.repo.getUser(), user => {
      this.user = user;
      if (user.role === 'CLIENT') {
        this.subscribeWithContext(this.repo.getBill(), it => {
          this.updateBill(it);
          if (it) {
            // todo uncomment to restart ws
            this.subscribeWithContext(this.repo.billFlow(it.id), bill => this.updateBill(bill));
          }
        });
      } else {
        this.subscribeWithContext(
          this.route.paramMap.pipe(
            map(m => m.get('idBill')),
            filter(bill => !!bill),
            map(bill => bill as string),
            mergeMap(bill => this.repo.waiterGetBill(bill))
          ),
          bill => {
            this.updateBill(bill);
            if (bill) {
              // todo uncomment to restart ws
              this.subscribeWithContext(this.repo.billFlow(bill.id), b => this.updateBill(b));
            }
          });
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
      const username: string = it.relatedClient.role !== 'CLIENT' ? 'Cameriere' : it.relatedClient.username;
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
      if (compareRole(this.user.role, 'CLIENT') >= 0) {
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

  getMenuUrl(): string | null {
    if (this.user) {
      if (this.user.role === 'CLIENT') {
        return '/menu';
      } else {
        return this.bill ? `/${this.bill.id}/waiterMenu` : null;
      }
    } else {
      return null;
    }

  }

  forceToggleCourse(course: Course, event: Event): void {
    event.stopImmediatePropagation();
    this.subscribeWithContext(
      this.repo.forceSetReady(course.id), action => {
        if (action) {
          course.isSent = true;
        }
      }
    );

  }
}
