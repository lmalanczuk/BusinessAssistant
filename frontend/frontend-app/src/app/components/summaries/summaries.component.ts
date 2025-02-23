import { Component } from '@angular/core';
import {MOCK_SUMMARIES} from "../../services/mock-data";
import {ExpandableListComponent} from "../expandable-list/expandable-list.component";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-summaries',
  standalone: true,
  imports: [
    ExpandableListComponent,
    NgForOf
  ],
  templateUrl: './summaries.component.html',
  styleUrl: './summaries.component.css'
})
export class SummariesComponent {
  summaries = MOCK_SUMMARIES;
}
