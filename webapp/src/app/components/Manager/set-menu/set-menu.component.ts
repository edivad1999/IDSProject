import {Component, OnInit} from '@angular/core';
import {RepositoryService} from '../../../data/repository/repository.service';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {MenuElement} from '../../../domain/model/data';
import {AbstractControl, FormBuilder, FormGroup, Validators} from '@angular/forms';


@Component({
  selector: 'app-set-menu',
  templateUrl: './set-menu.component.html',
  styleUrls: ['./set-menu.component.css']
})
export class SetMenuComponent extends SubscriberContextComponent implements OnInit {

  currentMenuArrayList = this.fb.array([]);

  formGroupContainer = this.fb.group({
    list: this.currentMenuArrayList
  });

  creatingElement = this.fb.group({
    name: this.fb.control(null, [Validators.required, Validators.minLength(1)]),
    ingredients: this.fb.control(null, [Validators.required, Validators.minLength(1)]),
    description: this.fb.control(null, [Validators.required, Validators.minLength(1)]),
    price: this.fb.control(null, [Validators.required, Validators.min(0)]),
  });

  constructor(private repo: RepositoryService,
              private fb: FormBuilder) {
    super();
  }

  ngOnInit(): void {
    this.subscribeWithContext(this.repo.getMenu(), it => it.forEach(value => this.addFormElement(value, false)));
  }

  addFormElement(view: MenuElement, editing: boolean): void {
    const group = this.fb.group({
      element: this.fb.group({
        name: this.fb.control({value: view.name, disabled: !editing}, [Validators.required, Validators.minLength(1)]),
        ingredients: this.fb.control({value: view.ingredients, disabled: !editing}, [Validators.required, Validators.minLength(1)]),
        description: this.fb.control({value: view.description, disabled: !editing}, [Validators.required, Validators.minLength(1)]),
        price: this.fb.control({value: view.price, disabled: !editing}, [Validators.required, Validators.min(0)]),
      }),
    });
    this.currentMenuArrayList.push(group);
    console.log(this.formGroupContainer);

  }

  castToFormGroup(abstract: AbstractControl): FormGroup {
    console.log(abstract as FormGroup);
    return abstract as FormGroup;

  }

  addCreating(): void {

    this.addFormElement(this.creatingElement.value, false);
    this.creatingElement.reset();
  }
}
