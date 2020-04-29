import { Component, OnInit } from '@angular/core';
import { User } from 'src/app/models/user.model';
import { VertxService } from 'src/app/services/vertx.service';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  user: User;

  constructor(private vertxService: VertxService, private auth: AuthService) {}

  ngOnInit(): void {
    this.vertxService
      .getUser(parseInt(this.auth.getUserId()))
      .subscribe(res => {
        console.log(res);
      });
  }
}
