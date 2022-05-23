import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TableOccupationComponent } from './table-occupation.component';

describe('TableOccupationComponent', () => {
  let component: TableOccupationComponent;
  let fixture: ComponentFixture<TableOccupationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TableOccupationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TableOccupationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
