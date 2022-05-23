import {Component, OnInit} from '@angular/core';
import {RepositoryService} from '../../../data/repository/repository.service';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {Bill, MenuElement} from '../../../domain/model/data';
import {Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {BillAssociationDialogComponent} from './bill-association-dialog/bill-association-dialog.component';

@Component({
  selector: 'app-client-home',
  templateUrl: './client-home.component.html',
  styleUrls: ['./client-home.component.css']
})
export class ClientHomeComponent extends SubscriberContextComponent implements OnInit {

  currentBill: Bill | null = null;
  menu?: MenuElement[];

  constructor(
    private dialog: MatDialog,
    private router: Router,
    private repo: RepositoryService
  ) {
    super();
  }

  ngOnInit(): void {
    this.subscribeWithContext(this.repo.getBill(), bill => {
      this.currentBill = bill;
      if (this.currentBill != null) {
        this.router.navigate(['/bill']);
      } else {
        this.subscribeWithContext(this.repo.getMenu(), menu => {
          this.menu = menu;
        });
      }
    });
  }

  startAssociation(): void {
    if (this.currentBill == null) {
      const dialogRef = this.dialog.open(BillAssociationDialogComponent, {
          panelClass: 'app-full-bleed-dialog',
        }
      );
      this.subscribeWithContext(
        dialogRef.afterClosed(), value => {
          if (value) {
            this.subscribeWithContext(this.repo.getBill(), bill => {
              this.currentBill = bill;
              if (bill != null) {
                this.router.navigate(['/bill']);
              }
            });
          }
        }
      );
    }
    // this.subscribeWithContext(this.repo.getBill())
  }
}
