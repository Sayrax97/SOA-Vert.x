import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private user_id = '';
  constructor() {}

  login(id: number) {
    this.user_id += id;
    this.putInLocalStorage();
  }
  putInLocalStorage() {
    localStorage.setItem('user_id', this.user_id);
  }
  clearLocalStorage() {
    localStorage.clear();
  }
  logout() {
    this.clearLocalStorage();
    this.user_id = '';
  }
  getUserId() {
    if (!this.user_id) {
      this.user_id = localStorage.getItem('user_id');
    }
    return this.user_id;
  }
}
