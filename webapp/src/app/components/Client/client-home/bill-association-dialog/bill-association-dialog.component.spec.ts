import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BillAssociationDialogComponent } from './bill-association-dialog.component';

describe('BillAssociationDialogComponent', () => {
  let component: BillAssociationDialogComponent;
  let fixture: ComponentFixture<BillAssociationDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BillAssociationDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BillAssociationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
