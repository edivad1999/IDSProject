import {Component, OnInit} from '@angular/core';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {RepositoryService} from '../../../data/repository/repository.service';
import {DishState, KitchenCourse, translateState} from '../../../domain/model/data';
import {getDateStringFromInstant} from '../../../utils/utils';

@Component({
  selector: 'app-kitchen-home',
  templateUrl: './kitchen-home.component.html',
  styleUrls: ['./kitchen-home.component.css']
})
export class KitchenHomeComponent extends SubscriberContextComponent implements OnInit {

  openKitchenCourses: KitchenCourse[] = [];
  allKitchenCourses: KitchenCourse[] = [];

  constructor(private repo: RepositoryService) {
    super();
  }

  ngOnInit(): void {
    this.subscribeWithContext(this.repo.getOpenCourses(), it => this.openKitchenCourses = it);
    this.subscribeWithContext(this.repo.getCourses(), it => this.allKitchenCourses = it);
  }

  getDateStringFromInstant(instant: number): string {
    return getDateStringFromInstant(instant);
  }

  translateState(dishState: DishState): string {
    return translateState(dishState);
  }

}
