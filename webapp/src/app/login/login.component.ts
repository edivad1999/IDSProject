import {Component, EventEmitter, OnInit, Output, SimpleChanges} from '@angular/core';
import {AuthState} from "../domain/model/data";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {SubscriberContextComponent} from "../utils/subscriber-context.component";
import {RepositoryService} from "../data/repository/repository.service";
import {ActivatedRoute, Router} from "@angular/router";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent extends SubscriberContextComponent implements OnInit {
  authState: AuthState = 'UNAUTHENTICATED';


  username = '';
  password = '';


  @Output()
  usernameChange = new EventEmitter<string>();
  @Output()
  passwordChange = new EventEmitter<string>();

  usernameControl = new FormControl('', [Validators.required]);
  passwordControl = new FormControl('', [
    Validators.required,
    Validators.minLength(6)
  ]);

  loginFormGroup = new FormGroup({
    email: this.usernameControl,
    password: this.passwordControl
  });

  constructor(
    private router: Router,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private api: RepositoryService
  ) {
    super();
  }

  ngOnInit(): void {
    this.subscribeWithContext(this.api.authenticationStateFlow, value => this.authState = value);

  }

  ngOnChanges(changes: SimpleChanges): void {
    if ('username' in changes) {
      this.usernameControl.setValue(changes.username.currentValue);
    }
  }

  login(): void {
    this.subscribeWithContext(this.api.loginWithEmailAndPassword(this.usernameControl.value, this.passwordControl.value),
      response => {
        if (!response) {
          this.snackBar.open('Errore nel login', 'chiudi')
        } else {
          this.router.navigate(['home'])
        }
      })


  }
}
