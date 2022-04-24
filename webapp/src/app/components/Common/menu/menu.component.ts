import {Component, OnInit} from '@angular/core';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {RepositoryService} from '../../../data/repository/repository.service';
import {Bill, MenuElement, Role} from '../../../domain/model/data';
import {ActivatedRoute, Router} from '@angular/router';
import {filter, map, mergeMap} from 'rxjs/operators';
import {MatBottomSheet} from '@angular/material/bottom-sheet';
import {AddMenuElementBottomSheetComponent} from './add-menu-element-bottom-sheet/add-menu-element-bottom-sheet.component';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent extends SubscriberContextComponent implements OnInit {

  role: Role | null = null;
  menu?: MenuElement[];
  bill: Bill | null = null;

  constructor(
    private bottomSheet: MatBottomSheet,
    private route: ActivatedRoute,
    private router: Router,
    private repo: RepositoryService
  ) {
    super();
  }

  ngOnInit(): void {
    this.subscribeWithContext(this.repo.role.asObservable(), role => {
      this.role = role;
      if (this.role === Role.CLIENT) {
        this.subscribeWithContext(this.repo.getBill(), bill => this.bill = bill);
      } else {
        // Getting id by parameters
        this.subscribeWithContext(
          this.route.paramMap.pipe(
            map(m => m.get('bill')),
            filter(bill => !!bill),
            map(bill => bill as string),
            mergeMap(bill => this.repo.waiterGetBill(bill))
          ),
          bill => {
            this.bill = bill;
          });
      }
    });
    this.subscribeWithContext(this.repo.getMenu(), menu => {
      this.menu = menu;
    });

  }

  addToBill(menuElement: MenuElement): void {

    if (this.role != null && this.bill) {
      const data: AddToBillWrappedData = {
        role: this.role,
        bill: this.bill,
        menuElement
      };
      const bottomSheetRef = this.bottomSheet.open(AddMenuElementBottomSheetComponent, {data,});

    }
  }
}

export interface AddToBillWrappedData {
  menuElement: MenuElement;
  role: Role;
  bill: Bill;
}
