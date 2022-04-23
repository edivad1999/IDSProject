import {Component, Input, OnInit} from '@angular/core';
import {MenuElement} from '../../../domain/model/data';

@Component({
  selector: 'app-menu-readonly',
  templateUrl: './menu-readonly.component.html',
  styleUrls: ['./menu-readonly.component.css']
})
export class MenuReadonlyComponent implements OnInit {

  @Input() menuElement!: MenuElement;

  constructor() {
  }

  ngOnInit(): void {
  }

}
