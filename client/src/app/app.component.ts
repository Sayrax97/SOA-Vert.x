import { Component } from '@angular/core';
import { AuthService } from './services/auth.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Cyclo Meter';
  constructor(
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}
  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
  isLoggedIn() {
    return this.auth.isLoggedIn();
  }
}
