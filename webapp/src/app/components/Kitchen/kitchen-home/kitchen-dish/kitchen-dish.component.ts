import {Component, Input, OnInit} from '@angular/core';
import {Dish, DishState, translateState} from '../../../../domain/model/data';
import {SubscriberContextComponent} from '../../../../utils/subscriber-context.component';
import {RepositoryService} from '../../../../data/repository/repository.service';

@Component({
  selector: 'app-kitchen-dish',
  templateUrl: './kitchen-dish.component.html',
  styleUrls: ['./kitchen-dish.component.css']
})
export class KitchenDishComponent extends SubscriberContextComponent implements OnInit {

  @Input() dish!: Dish;


  selectedNewState: DishState | null = null;

  nextState(state: DishState): DishState | null {
    if (state === 'PROBLEM' || state === 'DELIVERED') {
      return null;
    } else if (state === 'WAITING') {
      return 'PREPARING';
    } else {
      return 'DELIVERED';
    }
  }

  getAvailableStates(): DishState[] {
    const allStates: DishState[] = ['WAITING', 'PREPARING', 'PROBLEM', 'DELIVERED'];
    return allStates.filter(it => it !== this.dish.state);
  }

  constructor(private repo: RepositoryService) {
    super();
  }


  ngOnInit(): void {
  }

  translateState(dishState: DishState): string {
    return translateState(dishState);
  }

  setState(): void {
    if (this.selectedNewState !== null) {
      this.subscribeWithContext(this.repo.editDishState(this.dish.uuid, this.selectedNewState), value => {
        this.dish = value;
        this.selectedNewState = null;
      });
    }
  }
}
