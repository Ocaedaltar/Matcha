import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-confirmation',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirmation.html',
  styleUrls: ['./confirmation.scss'],
})
export class ConfirmationComponent {
  private route = inject(ActivatedRoute);
  token$ = this.route.paramMap.pipe(map((p) => p.get('token')));
}
