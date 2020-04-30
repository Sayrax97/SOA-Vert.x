import { SensorData } from './../../models/sensor.data.model';
import { Component, OnInit } from '@angular/core';
import { Time } from '@angular/common';
import { User } from 'src/app/models/user.model';
import { VertxService } from 'src/app/services/vertx.service';
import { AuthService } from 'src/app/services/auth.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
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
  selectedData: SensorData;
  newSensorDataForm: FormGroup;
  constructor(private vertxService: VertxService, private auth: AuthService) {}

  ngOnInit(): void {
    this.newSensorDataForm = new FormGroup({
      speed: new FormControl('', Validators.required),
      incline: new FormControl(false),
      terrain_type: new FormControl('', Validators.required),
      heart_rate: new FormControl('', Validators.required),
      distance_traveled: new FormControl('', Validators.required),
    });
    this.sensors = [];
    this.user = new User();
    this.sensorSelect = new FormControl('');
    this.init();
  }
  init() {
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

  getSelectedSensorData() {
    this.vertxService.getAllSensorData(this.selectedSensor.id).subscribe(
      (res) => {
        console.log(res);
        this.selectedSensor.data = res.result;
      },
      (err) => {
        console.log(err);
      }
    );
  }

  viewSenzorData(index: number) {
    this.selectedData = this.selectedSensor.data[index];
    console.log(this.selectedData);
  }
  sendNewData() {
    let sensorData: SensorData = new SensorData();
    sensorData = this.newSensorDataForm.value;
    sensorData.senzor_id = this.selectedSensor.id;
    //console.log(sensorData);
    this.vertxService.postSensorData(sensorData).subscribe(
      (res) => {
        console.log(res);
        this.getSelectedSensorData();
      },
      (err) => {
        console.log(err);
      }
    );
  }

  startNewSensor() {
    this.vertxService.postSensor(parseInt(this.auth.getUserId())).subscribe(
      (res) => {
        console.log(res);
        this.ngOnInit();
      },
      (err) => {
        console.log(err);
      }
    );
  }

  stopSensor() {
    this.vertxService.putSensor(this.selectedSensor.id).subscribe(
      (res) => {
        console.log(res);
        this.init();
      },
      (err) => {
        console.log(err);
      }
    );
  }
}
