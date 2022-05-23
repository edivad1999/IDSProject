import {Component, OnInit} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {RepositoryService} from '../../../data/repository/repository.service';
import {FormBuilder, Validators} from '@angular/forms';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {Table} from '../../../domain/model/data';

@Component({
  selector: 'app-set-tables',
  templateUrl: './set-tables.component.html',
  styleUrls: ['./set-tables.component.css']
})
export class SetTablesComponent extends SubscriberContextComponent implements OnInit {

  numberOfTables?: number;
  tables: Table[] = [];

  input = this.fb.control(null, [Validators.min(1)]);

  constructor(
    private snackBar: MatSnackBar,
    private repo: RepositoryService,
    private fb: FormBuilder
  ) {
    super();
  }

  ngOnInit(): void {
    this.subscribeWithContext(
      this.repo.getAllTables(), action => {
        this.numberOfTables = action.length;
        this.tables = action;
        this.input.setValue(this.numberOfTables);
      }
    );

  }

  save(): void {
    if (this.input.valid) {
      this.subscribeWithContext(this.repo.setTables(this.input.value), action => {
        if (action) {
          this.snackBar.open('Andato a buon fine!', 'Chiudi');
          this.subscribeWithContext(
            this.repo.getAllTables(), res => {
              this.numberOfTables = res.length;
              this.tables = res;
              this.input.setValue(this.numberOfTables);
            }
          );
        } else {
          this.snackBar.open('Qualcosa è andaro storto', 'Chiudi');

        }
      });

    }
  }
}
