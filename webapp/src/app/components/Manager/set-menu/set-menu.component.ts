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
  editingElementId: string | null = null;
  toInsert = this.fb.group(
    {
      name: this.fb.control(null, [Validators.required]),
      ingredients: this.fb.control(null, [Validators.required]),
      description: this.fb.control(null, [Validators.required]),
      price: this.fb.control(null, [Validators.required])
    }
  );
  editing = this.fb.group(
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
    this.toInsert.reset();
  }

  resetElement(): void {
    this.toInsert.reset();
  }

  remove(menuElement: MenuElement): void {
    this.currentMenu = this.currentMenu.filter(it => it.uuid !== menuElement.uuid);
  }

  startEdit(menuElement: MenuElement): void {
    this.editingElementId = menuElement.uuid;
    this.editing.setValue(menuElement);
  }

  endEdit(menuElement: MenuElement): void {
    this.editingElementId = null;
    this.currentMenu.filter(it => it.uuid === menuElement.uuid)[0] = this.editing.value;// aggiungere setting ogggetto da editing form
    this.editing.reset();
  }

  cancelEdit(menuElement: MenuElement): void {
    this.editingElementId = null;
    this.editing.reset();

  }
}
