import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OpenBillComponent } from './open-bill.component';

describe('OpenBillComponent', () => {
  let component: OpenBillComponent;
  let fixture: ComponentFixture<OpenBillComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OpenBillComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OpenBillComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
