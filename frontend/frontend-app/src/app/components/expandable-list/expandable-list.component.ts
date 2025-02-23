import {Component, Input} from '@angular/core';
import {NgIf, SlicePipe} from "@angular/common";

@Component({
  selector: 'app-expandable-list',
  standalone: true,
  imports: [
    SlicePipe,
    NgIf
  ],
  templateUrl: './expandable-list.component.html',
  styleUrl: './expandable-list.component.css'
})
export class ExpandableListComponent {
  @Input() title: string = '';
  @Input() date: string = '';
  @Input() content: string = '';

  isExpanded = false;

  toggleExpand($event: MouseEvent) {
    this.isExpanded = !this.isExpanded;
  }
}
