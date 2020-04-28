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
  constructor(private vertxService: VertxService) {}

  ngOnInit(): void {}
  login() {
    console.log(this.username.value);

    // this.vertxService.login(this.username.value).subscribe(res=>{
    //   console.log(res);
    // });
  }
}
