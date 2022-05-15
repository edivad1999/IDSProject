import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {RepositoryService} from '../../../../../data/repository/repository.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Bill} from '../../../../../domain/model/data';
import {SubscriberContextComponent} from '../../../../../utils/subscriber-context.component';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-close-bill',
  templateUrl: './close-bill.component.html',
  styleUrls: ['./close-bill.component.css']
})
export class CloseBillComponent extends SubscriberContextComponent implements OnInit {

  errorCourses = false;
  errorDishes = false;

  constructor(
    private snackBar: MatSnackBar,
    private fb: FormBuilder,
    private repo: RepositoryService,
    public dialogRef: MatDialogRef<CloseBillComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Bill,
  ) {
    super();
  }

  ngOnInit(): void {

  }

  getCoursesString(): string {
    const sent = this.data.courses.filter(it => it.isSent).length;
    if (sent === this.data.courses.length) {
      this.errorCourses = false;
      return 'Tutte le portate create sono state inviate';
    } else {
      this.errorCourses = true;
      return `Ci sono ${this.data.courses.length - sent} portate ancora da inviare`;
    }
  }

  getDishesString(): string {
    let total = 0;
    let delivered = 0;
    this.data.courses.forEach(it => {
      if (it.isSent) {
        delivered += it.dishes.filter(dish => dish.state === 'DELIVERED' || dish.state === 'PROBLEM').length;
        total += it.dishes.length;
      }
    });
    if (delivered !== total) {
      this.errorDishes = true;

      return 'Ci sono ancora dei piatti che non sono stati consegnati';
    } else {
      this.errorDishes = false;
      return 'Tutti i piatti sono stati consegnati';
    }
  }

  closeBill(): void {
    this.subscribeWithContext(this.repo.closeBill(this.data.id), action => {
      if (action) {
        this.snackBar.open('Conto chiuso correttamente', 'chiudi');
        this.dialogRef.close();
      } else {
        this.snackBar.open('Problemi nella chiusura del conto riprova', 'chiudi');
      }
    });

  }
}
