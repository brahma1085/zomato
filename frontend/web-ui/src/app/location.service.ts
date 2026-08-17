import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LocationService {
  currentLocation = signal<{lat: number, lng: number} | null>(null);

  constructor() {
    this.initLocation();
  }

  private initLocation() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.currentLocation.set({ lat: position.coords.latitude, lng: position.coords.longitude });
        },
        (error) => {
          console.error("Geolocation error:", error);
          this.currentLocation.set({ lat: 37.7749, lng: -122.4194 }); // Fallback to SF by default
        }
      );
    } else {
      this.currentLocation.set({ lat: 37.7749, lng: -122.4194 }); // SF
    }
  }

  async getLocationAsync(): Promise<{lat: number, lng: number}> {
    if (this.currentLocation()) {
      return this.currentLocation()!;
    }
    
    return new Promise((resolve) => {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const loc = { lat: position.coords.latitude, lng: position.coords.longitude };
            this.currentLocation.set(loc);
            resolve(loc);
          },
          (error) => {
            console.error("Geolocation error:", error);
            const fallback = { lat: 37.7749, lng: -122.4194 };
            this.currentLocation.set(fallback);
            resolve(fallback);
          },
          { timeout: 5000 }
        );
      } else {
        const fallback = { lat: 37.7749, lng: -122.4194 };
        this.currentLocation.set(fallback);
        resolve(fallback);
      }
    });
  }
}
