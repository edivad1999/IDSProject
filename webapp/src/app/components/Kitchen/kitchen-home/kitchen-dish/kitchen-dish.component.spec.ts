import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KitchenDishComponent } from './kitchen-dish.component';

describe('KitchenDishComponent', () => {
  let component: KitchenDishComponent;
  let fixture: ComponentFixture<KitchenDishComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KitchenDishComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KitchenDishComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
