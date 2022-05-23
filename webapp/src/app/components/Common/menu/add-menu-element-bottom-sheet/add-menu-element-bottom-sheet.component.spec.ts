import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddMenuElementBottomSheetComponent } from './add-menu-element-bottom-sheet.component';

describe('AddMenuElementBottomSheetComponent', () => {
  let component: AddMenuElementBottomSheetComponent;
  let fixture: ComponentFixture<AddMenuElementBottomSheetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AddMenuElementBottomSheetComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AddMenuElementBottomSheetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
