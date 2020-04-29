import { User } from './../../models/user.model';
import { Component, OnInit } from '@angular/core';
import { VertxService } from 'src/app/services/vertx.service';
import { FormControl, Validators } from '@angular/forms';
import { AuthService } from 'src/app/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  username = new FormControl('', Validators.required);
  user: User = new User();
  constructor(
    private vertxService: VertxService,
    private auth: AuthService,
    private route: Router
  ) {}

  ngOnInit(): void {}
  login() {
    this.vertxService.login(this.username.value).subscribe(res => {
      console.log(res.id);
      this.auth.login(res.id);
      this.route.navigateByUrl('/home');
    });
  }
}
