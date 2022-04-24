import {Component, Inject, OnInit} from '@angular/core';
import {MAT_BOTTOM_SHEET_DATA, MatBottomSheetRef} from '@angular/material/bottom-sheet';
import {AddToBillWrappedData} from '../menu.component';
import {Bill, MenuElement, Role} from '../../../../domain/model/data';
import {FormBuilder, Validators} from '@angular/forms';

@Component({
  selector: 'app-add-menu-element-bottom-sheet',
  templateUrl: './add-menu-element-bottom-sheet.component.html',
  styleUrls: ['./add-menu-element-bottom-sheet.component.css']
})
export class AddMenuElementBottomSheetComponent implements OnInit {

  menuElement: MenuElement;
  role: Role;
  bill: Bill;
  step: AddingStep = 'INSERTING';
  courseSelection: CourseSelection[] = [];

  courseNumber = this.fb.control(null, [Validators.required, Validators.min(1)]);
  notes = this.fb.control('');

  constructor(private bottomSheetRef: MatBottomSheetRef<AddMenuElementBottomSheetComponent>,
              @Inject(MAT_BOTTOM_SHEET_DATA) private data: AddToBillWrappedData,
              private fb: FormBuilder
  ) {
    this.menuElement = data.menuElement;
    this.bill = data.bill;
    this.role = data.role;
  }

  ngOnInit(): void {
    this.courseSelection = this.getCourseSelection();
  }


  selectIconLeft(): string {
    if (this.step === 'INSERTING') {
      return 'close';
    } else if (this.step === 'CONFIRMING') {
      return 'arrow_back';
    } else {
      return 'save';
    }
  }

  selectIconRight(): string {
    if (this.step === 'INSERTING') {
      return (this.formValid()) ? 'navigate_next' : 'do_not_disturb_on';
    } else if (this.step === 'CONFIRMING') {
      return 'done';
    } else {
      return 'save';
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
    }
  }

  getCourseSelection(): CourseSelection[] {
    const max = this.bill.courses.length < 5 ? 5 : this.bill.courses.length + 1;
    const res: CourseSelection[] = [];
    for (let i = 1; i < max + 1; i++) { // so poor
      const displayValue = this.bill.courses.map(it => it.number).indexOf(i) === -1 ? `Portata ${i}  ancora nessun piatto ` : `Portata ${i} contiene ${this.bill.courses[i].dishes.length} piatti`;
      const clickable = this.bill.courses.map(it => it.number).indexOf(i) === -1 ? true : !this.bill.courses[i].isSent;
      res.push({
          course: i,
          displayValue,
          clickable
        }
      );
    }
    return res;
  }

  rightAction(): void {
    if (this.step === 'INSERTING') {
      this.step = 'CONFIRMING';
    }

  }
}

export interface CourseSelection {
  course: number;
  displayValue: string;
  clickable: boolean;
}

export type AddingStep = 'INSERTING' | 'CONFIRMING' | 'DONE';
