import {Component, OnInit} from '@angular/core';
import {RepositoryService} from '../../../../data/repository/repository.service';
import {FormBuilder, Validators} from '@angular/forms';
import {SubscriberContextComponent} from '../../../../utils/subscriber-context.component';
import {MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-bill-association-dialog',
  templateUrl: './bill-association-dialog.component.html',
  styleUrls: ['./bill-association-dialog.component.css']
})
export class BillAssociationDialogComponent extends SubscriberContextComponent implements OnInit {

  number = this.fb.control(null, [Validators.required]);
  secretCode = this.fb.control(null, [Validators.required]);


  constructor(private repo: RepositoryService,
              private fb: FormBuilder,
              private dialogRef: MatDialogRef<BillAssociationDialogComponent>,
              private snackbar: MatSnackBar) {
    super();
  }


  ngOnInit(): void {

  }

  joinTable(): void {
    this.subscribeWithContext(
      this.repo.joinTable(this.number.value, this.secretCode.value), it => {
        if (it) {
          this.dialogRef.close(it);

        } else {
          this.snackbar.open('Non siamo riusciti ad aggiungerti al tavolo, Riprova!', 'Chiudi');
        }
      }
    );
  }

}
