import { TestBed } from '@angular/core/testing';

import { VertxService } from './vertx.service';

describe('VertxService', () => {
  let service: VertxService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VertxService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
