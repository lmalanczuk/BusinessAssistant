import { Component } from '@angular/core';
import {MOCK_TRANSCRIPTIONS} from "../../services/mock-data";
import {ExpandableListComponent} from "../expandable-list/expandable-list.component";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-transcriptions',
  standalone: true,
  imports: [
    ExpandableListComponent,
    NgForOf
  ],
  templateUrl: './transcriptions.component.html',
  styleUrl: './transcriptions.component.css'
})
export class TranscriptionsComponent {
  transcriptions = MOCK_TRANSCRIPTIONS;
}
