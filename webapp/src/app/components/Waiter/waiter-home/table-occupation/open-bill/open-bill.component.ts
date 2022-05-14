import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Table} from '../../../../../domain/model/data';
import {RepositoryService} from '../../../../../data/repository/repository.service';
import {SubscriberContextComponent} from '../../../../../utils/subscriber-context.component';
import {FormBuilder, Validators} from '@angular/forms';

@Component({
  selector: 'app-open-bill',
  templateUrl: './open-bill.component.html',
  styleUrls: ['./open-bill.component.css']
})
export class OpenBillComponent extends SubscriberContextComponent implements OnInit {


  coveredNumbers = this.fb.control(null, [Validators.required]);

  secretCode: string | null = null;

  constructor(
    private fb: FormBuilder,
    private repo: RepositoryService,
    public dialogRef: MatDialogRef<OpenBillComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Table,
  ) {
    super();
  }

  ngOnInit(): void {

  }

  openBill(): void {
    this.subscribeWithContext(this.repo.openBill(this.data.number, this.coveredNumbers.value), action => {
      this.secretCode = action;
    });

  }
}
