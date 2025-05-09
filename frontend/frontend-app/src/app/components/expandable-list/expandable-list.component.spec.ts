import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExpandableListComponent } from './expandable-list.component';

describe('ExpandableListComponent', () => {
  let component: ExpandableListComponent;
  let fixture: ComponentFixture<ExpandableListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExpandableListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExpandableListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
