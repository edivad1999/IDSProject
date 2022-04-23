import {Component, OnInit} from '@angular/core';
import {RepositoryService} from '../../../data/repository/repository.service';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {Bill, MenuElement} from '../../../domain/model/data';
import {Router} from '@angular/router';

@Component({
  selector: 'app-client-home',
  templateUrl: './client-home.component.html',
  styleUrls: ['./client-home.component.css']
})
export class ClientHomeComponent extends SubscriberContextComponent implements OnInit {

  currentBill: Bill | null = null;
  menu?: MenuElement[];

  constructor(
    private router: Router,
    private repo: RepositoryService
  ) {
    super();
  }

  ngOnInit(): void {
    this.subscribeWithContext(this.repo.getBill(), bill => {
      this.currentBill = bill;
      if (bill != null) {
        this.router.navigate(['/bill']);
      } else {
        this.subscribeWithContext(this.repo.getMenu(), menu => {
          this.menu = menu;
        });
      }
    });
  }

  startAssociation(): void {
    // this.subscribeWithContext(this.repo.getBill())
  }
}
