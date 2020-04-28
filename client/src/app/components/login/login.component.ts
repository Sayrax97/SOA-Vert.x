import { User } from './../../models/user.model';
import { Component, OnInit } from '@angular/core';
import { VertxService } from 'src/app/vertx.service';
import { FormControl, Validators } from '@angular/forms';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  username = new FormControl('', Validators.required);
  user: User = new User();
  constructor(private vertxService: VertxService) {}

  ngOnInit(): void {}
  login() {
    console.log(this.username.value);

    this.vertxService.login(this.username.value).subscribe((res) => {
      if (res.statusCode == 200) {
        this.user.age = res.age;
        this.user.gender = res.gender;
        this.user.username = res.username;
        this.user.weight = res.weight;
        console.log(this.user.age);
      } else {
        console.log(res.statusCode);
      }
    });
  }
}
