import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuReadonlyComponent } from './menu-readonly.component';

describe('MenuReadonlyComponent', () => {
  let component: MenuReadonlyComponent;
  let fixture: ComponentFixture<MenuReadonlyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MenuReadonlyComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MenuReadonlyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
