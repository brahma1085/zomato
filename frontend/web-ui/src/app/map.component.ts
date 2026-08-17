import { Component, AfterViewInit, ElementRef, ViewChild, input, effect } from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-map',
  standalone: true,
  template: `<div #mapContainer class="w-full h-full bg-gray-200"></div>`,
  styles: [`
    :host {
      display: block;
      width: 100%;
      height: 100%;
    }
  `]
})
export class MapComponent implements AfterViewInit {
  @ViewChild('mapContainer') mapContainer!: ElementRef;
  
  // Accept markers as input [lat, lng, title]
  markers = input<Array<{lat: number, lng: number, title: string}>>([]);
  centerLocation = input<{lat: number, lng: number} | null>(null);
  
  private map: L.Map | undefined;
  private markerLayer = L.layerGroup();
  private userMarkerLayer = L.layerGroup();

  constructor() {
    effect(() => {
      this.updateMarkers(this.markers());
    });
    effect(() => {
      this.updateUserLocation(this.centerLocation());
    });
  }

  ngAfterViewInit() {
    this.initMap();
    
    // Fix Leaflet rendering issue when container dimensions aren't initially known
    setTimeout(() => {
      this.map?.invalidateSize();
    }, 100);
    setTimeout(() => {
      this.map?.invalidateSize();
    }, 500);
  }

  private initMap() {
    // Default to New York City if center not available
    const center = this.centerLocation();
    const startLat = center ? center.lat : 40.7128;
    const startLng = center ? center.lng : -74.0060;
    this.map = L.map(this.mapContainer.nativeElement).setView([startLat, startLng], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);

    this.markerLayer.addTo(this.map);
    this.userMarkerLayer.addTo(this.map);
    
    // Trigger initial updates once map is ready
    this.updateUserLocation(this.centerLocation());
    this.updateMarkers(this.markers());
  }

  private updateUserLocation(center: {lat: number, lng: number} | null) {
    if (!this.map || !center) return;

    this.userMarkerLayer.clearLayers();

    const userIcon = L.divIcon({
      className: 'user-location-icon',
      html: `<div style="background-color: #3b82f6; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 0 8px rgba(59,130,246,0.8); position: relative;">
               <div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 8px; height: 8px; background-color: white; border-radius: 50%;"></div>
             </div>`,
      iconSize: [24, 24],
      iconAnchor: [12, 12]
    });

    const marker = L.marker([center.lat, center.lng], { icon: userIcon, title: 'You are here', zIndexOffset: 1000 });
    marker.bindTooltip('You are here', { permanent: false, direction: 'top' });
    this.userMarkerLayer.addLayer(marker);

    // If there are no other markers, center on user
    if (this.markers().length === 0) {
      this.map.setView([center.lat, center.lng], 13);
    }
  }

  private updateMarkers(newMarkers: Array<{lat: number, lng: number, title: string}>) {
    if (!this.map) return;
    
    this.markerLayer.clearLayers();
    
    if (newMarkers.length === 0) {
      const center = this.centerLocation();
      if (center) {
        this.map.setView([center.lat, center.lng], 13);
      }
      return;
    }

    const bounds = L.latLngBounds([]);

    // Custom red icon similar to original UI
    const customIcon = L.divIcon({
      className: 'custom-div-icon',
      html: `<div style="background-color: #ef4444; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
      iconSize: [20, 20],
      iconAnchor: [10, 10]
    });

    newMarkers.forEach(m => {
      const marker = L.marker([m.lat, m.lng], { icon: customIcon, title: m.title });
      marker.bindTooltip(m.title);
      this.markerLayer.addLayer(marker);
      bounds.extend([m.lat, m.lng]);
    });

    const center = this.centerLocation();
    if (center) {
      bounds.extend([center.lat, center.lng]);
    }

    if (newMarkers.length > 0) {
      this.map.fitBounds(bounds, { padding: [50, 50] });
    }
    
    setTimeout(() => {
      this.map?.invalidateSize();
    }, 100);
  }
}
