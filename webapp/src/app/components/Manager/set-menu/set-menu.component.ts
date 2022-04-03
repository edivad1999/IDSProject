import {Component, OnInit} from '@angular/core';
import {RepositoryService} from '../../../data/repository/repository.service';
import {SubscriberContextComponent} from '../../../utils/subscriber-context.component';
import {MenuElement} from '../../../domain/model/data';
import {FormBuilder, Validators} from '@angular/forms';
import {v4 as uuid} from 'uuid';

@Component({
  selector: 'app-set-menu',
  templateUrl: './set-menu.component.html',
  styleUrls: ['./set-menu.component.css']
})
export class SetMenuComponent extends SubscriberContextComponent implements OnInit {

  currentMenu: MenuElement[] = [];

  toInsert = this.fb.group(
    {
      name: this.fb.control(null, [Validators.required]),
      ingredients: this.fb.control(null, [Validators.required]),
      description: this.fb.control(null, [Validators.required]),
      price: this.fb.control(null, [Validators.required])
    }
  );

  constructor(private repo: RepositoryService,
              private fb: FormBuilder
  ) {
    super();
  }

  ngOnInit(): void {
    this.subscribeWithContext(this.repo.getMenu(), it => this.currentMenu = it);
  }

  addElement(): void {
    const newElement = this.toInsert.value as MenuElement;
    newElement.uuid = uuid(); // setting this does nothing backend side it gets overrided
    this.currentMenu.push(newElement);
    console.log(newElement)
    this.toInsert.reset();
  }

  resetElement(): void {
    this.toInsert.reset();
  }
}
