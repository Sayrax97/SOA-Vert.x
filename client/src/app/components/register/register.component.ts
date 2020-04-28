import { Component, OnInit } from '@angular/core';
import { VertxService } from 'src/app/vertx.service';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { User } from 'src/app/models/user.model';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
})
export class RegisterComponent implements OnInit {
  newUser: User = new User();
  registerForm: FormGroup;
  constructor(private vertxService: VertxService) {}

  ngOnInit(): void {
    this.registerForm = new FormGroup({
      username: new FormControl(this.newUser.username, Validators.required),
      gender: new FormControl(this.newUser.gender, Validators.required),
      weight: new FormControl(this.newUser.weight, [
        Validators.required,
        Validators.min(20),
      ]),
      age: new FormControl(this.newUser.age, [
        Validators.required,
        Validators.min(12),
      ]),
    });
  }
  register() {
    this.newUser = this.registerForm.value;
    // this.vertxService.postUser(this.newUser).subscribe(res=>{
    // });
  }
}
