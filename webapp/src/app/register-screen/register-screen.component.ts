import {Component, OnInit} from '@angular/core';
import {SubscriberContextComponent} from '../utils/subscriber-context.component';
import {FormBuilder, Validators} from '@angular/forms';
import {RepositoryService} from '../data/repository/repository.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-register-screen',
  templateUrl: './register-screen.component.html',
  styleUrls: ['./register-screen.component.css']
})
export class RegisterScreenComponent extends SubscriberContextComponent implements OnInit {

  form = this.fb.group(
    {
      username: this.fb.control(null, [Validators.required]),
      mail: this.fb.control(null, [Validators.required]),
      password: this.fb.control(null, [Validators.required, Validators.minLength(6)])
    }
  );


  constructor(private fb: FormBuilder, private repo: RepositoryService, private snackbar: MatSnackBar) {
    super();
  }

  ngOnInit(): void {

  }

  register(): void {
    this.subscribeWithContext(this.repo.registerAccount(this.form.value.username, this.form.value.password, this.form.value.mail), it => {
      this.snackbar.open(it ? 'Registrazione andata a buon fine' : 'Registrazione fallita', 'chiudi');
    });


  }
}
