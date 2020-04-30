import { Component, OnInit } from '@angular/core';
import { Time } from '@angular/common';
import { User } from 'src/app/models/user.model';
import { VertxService } from 'src/app/services/vertx.service';
import { AuthService } from 'src/app/services/auth.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Sensor } from 'src/app/models/sensor.model';
import { SensorData } from 'src/app/models/sensor.data.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  user: User;
  sensors: Sensor[];
  sensorSelect: FormControl;
  selectedSensor: Sensor;
  selectedData: SensorData;
  newSensorDataForm: FormGroup;
  constructor(private vertxService: VertxService, private auth: AuthService) {}

  ngOnInit(): void {
    this.newSensorDataForm = new FormGroup({
      speed: new FormControl('', Validators.required),
      incline: new FormControl(false),
      terrain_type: new FormControl('', Validators.required),
      hearth_rate: new FormControl('', Validators.required),
      distance: new FormControl('', Validators.required)
    });
    this.sensors = [];
    this.user = new User();
    this.sensorSelect = new FormControl('');
    this.vertxService.getUser(parseInt(this.auth.getUserId())).subscribe(
      res => {
        this.user.username = res.username;
        this.user.age = res.age;
        this.user.gender = res.gender;
        this.user.weight = res.weight;
        this.vertxService
          .getAllSenors(parseInt(this.auth.getUserId()))
          .subscribe(
            res => {
              console.log(res);
              this.sensors = res.result;
            },
            err => {
              console.log(err);
            }
          );
      },
      err => {
        console.log(err);
      }
    );
  }
  getSelectedSensorData() {
    this.vertxService.getAllSensorData(this.selectedSensor.id).subscribe(
      res => {
        console.log(res);
        this.selectedSensor.data = res.result;
      },
      err => {
        console.log(err);
      }
    );
  }
  sendNewData() {
    console.log(this.newSensorDataForm.value);
    this.vertxService.postSensorData(this.newSensorDataForm.value).subscribe(
      res => {
        console.log(res);
      },
      err => {
        console.log(err);
      }
    );
  }
}
