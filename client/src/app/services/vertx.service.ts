import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { User } from '../models/user.model';
import { Sensor } from '../models/sensor.model';
import { SensorData } from '../models/sensor.data.model';

@Injectable({ providedIn: 'root' })
export class VertxService {
  private url: string = 'http://localhost:1998/';

  constructor(private httpClient: HttpClient) {}

  getUser(id: Number) {
    return this.httpClient.get<any>(this.url + 'user/' + id);
  }
  postUser(newUser: User) {
    return this.httpClient.post(this.url + 'user/', newUser);
  }
  getSensor(id: Number) {
    return this.httpClient.get<any>(this.url + 'sensor/' + id);
  }
  postSensor(user_id: number) {
    return this.httpClient.post(this.url + 'sensor', { user_id });
  }
  postSensorData(newSensorData: SensorData) {
    return this.httpClient.post(this.url + 'sensor/data/', newSensorData);
  }
  getAllSensorData(id: Number) {
    return this.httpClient.get<any>(this.url + 'sensor/data/all/' + id);
  }
  login(username: string) {
    return this.httpClient.get<any>(this.url + 'user/login/' + username);
  }
  getAllSenors(user_id: number) {
    return this.httpClient.get<any>(this.url + '/sensor/all/' + user_id);
  }
  putSensor(senzor_id: number) {
    return this.httpClient.put<any>(this.url + '/sensor', { senzor_id });
  }

  getMET(speed: number) {
    return this.httpClient.get<any>(this.url + 'MET/' + speed);
  }
}
