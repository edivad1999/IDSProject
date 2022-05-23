import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SetTablesComponent } from './set-tables.component';

describe('SetTablesComponent', () => {
  let component: SetTablesComponent;
  let fixture: ComponentFixture<SetTablesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SetTablesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SetTablesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
