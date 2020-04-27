import { Time } from '@angular/common';

export class SensorData {
    id: number;
    speed:number;
    incline:boolean;
    terain_type:string;
    heart_rate:number;
    senzor_id:number;
    time_stemp:Time;
    distance_traveled:number;
  }