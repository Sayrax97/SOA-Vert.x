import { Component, OnInit } from '@angular/core';
import { Time } from '@angular/common';
import { User } from 'src/app/models/user.model';
import { VertxService } from 'src/app/services/vertx.service';
import { AuthService } from 'src/app/services/auth.service';
import { FormControl } from '@angular/forms';
import { Sensor } from 'src/app/models/sensor.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  user: User;
  sensors: Sensor[];
  sensorSelect: FormControl;
  selectedSensor: Sensor;
  constructor(private vertxService: VertxService, private auth: AuthService) {}

  ngOnInit(): void {
    this.sensors = [];
    this.user = new User();
    this.sensorSelect = new FormControl('');
    this.vertxService.getUser(parseInt(this.auth.getUserId())).subscribe(
      (res) => {
        this.user.username = res.username;
        this.user.age = res.age;
        this.user.gender = res.gender;
        this.user.weight = res.weight;
        this.vertxService
          .getAllSenors(parseInt(this.auth.getUserId()))
          .subscribe(
            (res) => {
              console.log(res);
              this.sensors = res.result;
            },
            (err) => {
              console.log(err);
            }
          );
      },
      (err) => {
        console.log(err);
      }
    );
  }
}
