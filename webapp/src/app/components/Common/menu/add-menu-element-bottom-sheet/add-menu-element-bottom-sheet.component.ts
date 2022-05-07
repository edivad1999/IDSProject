import {Component, Inject, OnInit} from '@angular/core';
import {MAT_BOTTOM_SHEET_DATA, MatBottomSheetRef} from '@angular/material/bottom-sheet';
import {AddToBillWrappedData} from '../menu.component';
import {Bill, Dish, MenuElement, Role, SimpleUser} from '../../../../domain/model/data';
import {FormBuilder, Validators} from '@angular/forms';
import * as uuid from 'uuid';
import {RepositoryService} from '../../../../data/repository/repository.service';
import {SubscriberContextComponent} from '../../../../utils/subscriber-context.component';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';

@Component({
  selector: 'app-add-menu-element-bottom-sheet',
  templateUrl: './add-menu-element-bottom-sheet.component.html',
  styleUrls: ['./add-menu-element-bottom-sheet.component.css']
})
export class AddMenuElementBottomSheetComponent extends SubscriberContextComponent implements OnInit {

  menuElement: MenuElement;
  role: Role;
  bill: Bill;
  step: AddingStep = 'INSERTING';
  courseSelection: CourseSelection[] = [];

  courseNumber = this.fb.control(null, [Validators.required, Validators.min(1)]);
  notes = this.fb.control('');
  user: SimpleUser | null = null;

  constructor(private bottomSheetRef: MatBottomSheetRef<AddMenuElementBottomSheetComponent>,
              private repo: RepositoryService,
              @Inject(MAT_BOTTOM_SHEET_DATA) private data: AddToBillWrappedData,
              private fb: FormBuilder,
              private snackbar: MatSnackBar,
              private router: Router
  ) {
    super();
    this.menuElement = data.menuElement;
    this.bill = data.bill;
    this.role = data.role;
  }

  ngOnInit(): void {
    this.subscribeWithContext(this.repo.getUser(), it => this.user = it);
    this.courseSelection = this.getCourseSelection();
  }


  selectIconLeft(): string {
    if (this.step === 'INSERTING') {
      return 'close';
    } else if (this.step === 'CONFIRMING') {
      return 'arrow_back';
    } else {
      return 'close';
    }
  }

  selectIconRight(): string {
    if (this.step === 'INSERTING') {
      return (this.formValid()) ? 'navigate_next' : 'do_not_disturb_on';
    } else {
      return 'done';
    }
  }

  formValid(): boolean {
    return this.notes.valid && this.courseNumber.valid;
  }

  leftAction(): void {
    if (this.step === 'INSERTING') {
      this.bottomSheetRef.dismiss();
    } else if (this.step === 'CONFIRMING') {
      this.step = 'INSERTING';
    } else {
      this.bottomSheetRef.dismiss();
    }
  }

  rightAction(): void {
    if (this.step === 'INSERTING') {
      this.step = 'CONFIRMING';
    } else if (this.step === 'CONFIRMING') {
      const dish = this.getDish();
      if (dish) {
        this.subscribeWithContext(this.repo.addToCourse(dish, this.courseNumber.value), response => {
          if (response) {
            this.step = 'DONE';
          } else {
            this.snackbar.open('Qualcosa è andato storto durante l\'associazione, riprova!', 'chiudi');
          }
        });
      }
    }

  }

  getCourseSelection(): CourseSelection[] {
    const max = this.bill.courses.length < 5 ? 5 : this.bill.courses.length + 1;
    const res: CourseSelection[] = [];
    for (let i = 1; i < max + 1; i++) { // so poor
      const indexExisting = this.bill.courses.map(it => it.number).indexOf(i);
      const displayValue = indexExisting === -1 ? `Portata ${i}  ancora nessun piatto ` : `Portata ${i} contiene ${this.bill.courses[indexExisting].dishes.length} piatti`;
      const clickable = indexExisting === -1 ? true : !this.bill.courses[indexExisting].isSent;
      res.push({
          course: i,
          displayValue,
          clickable
        }
      );
    }
    return res;
  }

  getDish(): Dish | null {
    if (this.user) {
      return {
        menuElement: this.menuElement,
        notes: this.notes.value,
        uuid: uuid.v4(),
        state: 'WAITING',
        relatedClient: this.user
      };
    } else {
      return null;
    }
  }

  goToBill(): void {
    this.bottomSheetRef.dismiss();

    this.router.navigate(['/bill']);
  }

  goToMenu(): void {
    this.bottomSheetRef.dismiss();
  }


}

export interface CourseSelection {
  course: number;
  displayValue: string;
  clickable: boolean;
}

export type AddingStep = 'INSERTING' | 'CONFIRMING' | 'DONE';
