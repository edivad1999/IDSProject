import {Component, OnInit} from '@angular/core';
import {RepositoryService} from "../data/repository/repository.service";
import {SubscriberContextComponent} from "../utils/subscriber-context.component";
import {Router} from "@angular/router";

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.css']
})
export class LogoutComponent extends SubscriberContextComponent implements OnInit {

  constructor(
    private router: Router,
    private repo: RepositoryService) {
    super()
  }

  ngOnInit(): void {
    this.subscribeWithContext(
      this.repo.logOut(),
      response => {
        response ? this.router.navigate(['']) :
          this.router.navigate(['home']);

      }
    )

  }

}
