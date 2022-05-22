import {Component, OnInit} from '@angular/core';
import {RepositoryService} from '../../../data/repository/repository.service';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {Bill, CourseGrouped, Dish, DishState, SimpleUser, translateState} from '../../../domain/model/data';
import {filter, map, mergeMap} from 'rxjs/operators';
import {ActivatedRoute} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {getDateStringFromInstant} from '../../../utils/utils';

export interface DishesGrouped {
  [username: string]: Dish[];
}


@Component({
  selector: 'app-manage-bill',
  templateUrl: './manage-bill.component.html',
  styleUrls: ['./manage-bill.component.css'],

})
export class ManageBillComponent extends SubscriberContextComponent implements OnInit {

  bill: Bill | null = null;
  user: SimpleUser | null = null;

  expandedCourses: number[] = [];
  courseGrouped: CourseGrouped[] = [];


  constructor(
    private snackbar: MatSnackBar,
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
    if (this.bill) {
      this.courseGrouped = this.bill.courses.map(it => {
          const res: CourseGrouped = {
            id: it.id,
            dishes: this.getDishesGroupedByUsers(it.dishes),
            number: it.number,
            isSent: it.isSent,
            sentAt: it.sentAt,
            readyClients: it.readyClients
          };
          return res;
        }
      );
    } else {
      this.courseGrouped = [];
    }
  }

  getDateStringFromInstant(instant: number): string {
    return getDateStringFromInstant(instant);
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
    console.log(res);
    return res;
  }

  checkDishOwner(dish: Dish): boolean {
    if (this.user) {
      if (this.user.role !== 'CLIENT') {
        return true;
      } else {
        return this.user.username === dish.relatedClient.username;
      }
    } else {
      return false;
    }

  }

  deleteDish(dish: string, event: Event): void {
    event.stopImmediatePropagation();
    console.log(dish);
    this.subscribeWithContext(
      this.repo.removeDish(
        dish
      ), action => {
        if (!action) {
          this.snackbar.open('Errore nella eliminazione');
        }
      }
    );


  }

  toggleImReady(course: CourseGrouped, event: Event): void {
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


  amIReady(course: CourseGrouped): boolean {
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

  translateState(dishState: DishState): string {
    return translateState(dishState);
  }

  forceToggleCourse(course: CourseGrouped, event: Event): void {
    event.stopImmediatePropagation();
    this.subscribeWithContext(
      this.repo.forceSetReady(course.id), action => {
        if (action) {
          course.isSent = true;
        }
      }
    );

  }

  expandState(n: number): void {
    const index = this.expandedCourses.indexOf(n);
    if (index !== -1) {
      this.expandedCourses.splice(index);
    } else {
      this.expandedCourses.push(n);
    }
  }

  isExpanded(n: number): boolean {
    return this.expandedCourses.filter(it => it === n).length > 0;
  }

  dishRemovable(dish: Dish, course: CourseGrouped): boolean {
    if (this.user?.role !== 'CLIENT') {
      return true;
    }

    return !course.isSent && dish.state !== 'DELIVERED';
  }
}
