import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CloseBillComponent } from './close-bill.component';

describe('CloseBillComponent', () => {
  let component: CloseBillComponent;
  let fixture: ComponentFixture<CloseBillComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CloseBillComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CloseBillComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
