import { SensorData } from './sensor.data.model';

export class Sensor {
  id: number;
  total_distance_traveled: number;
  started_at: string;
  User_id: number;
  status_voznje: boolean;
  data: SensorData[];
}
