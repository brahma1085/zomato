import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AiService } from './ai.service';
import { LocationService } from './location.service';
import { MapComponent } from './map.component';

@Component({
  selector: 'app-ai-curated-results',
  standalone: true,
  imports: [CommonModule, RouterLink, MapComponent],
  templateUrl: './ai-curated-results.component.html',
  styles: [`
    :host {
      display: block;
      width: 100%;
      height: 100%;
    }
  `]
})
export class AiCuratedResultsComponent implements OnInit {
  query = signal('');
  isThinking = signal(true);
  chatMessage = signal('');
  restaurants = signal<any[]>([]);
  mapMarkers = signal<any[]>([]);
  selectedForCompare = signal<Set<string>>(new Set());

  constructor(private route: ActivatedRoute, private aiService: AiService, public locationService: LocationService) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['q']) {
        this.query.set(params['q']);
        this.fetchResults(params['q']);
      }
    });
  }

  async fetchResults(q: string) {
    this.isThinking.set(true);
    
    // Wait for the actual location to resolve instead of immediately using the default SF location
    const loc = await this.locationService.getLocationAsync();
    const lat = loc.lat;
    const lng = loc.lng;

    this.aiService.sendMessage(q, 'user123', [], lat, lng).subscribe({
      next: (res) => {
        this.chatMessage.set(res.message);
        this.restaurants.set(res.restaurants || []);
        
        if (res.restaurants && res.restaurants.length > 0) {
          const center = { lat, lng };
          const newMarkers = res.restaurants.map((r: any, idx: number) => ({
             lat: (r.lat !== undefined ? r.lat : r.latitude) || (center.lat + (idx * 0.005) - 0.0025), 
             lng: (r.lng !== undefined ? r.lng : r.longitude) || (center.lng + (idx * 0.005) - 0.0025), 
             title: r.name || r.restaurantName 
          }));
          this.mapMarkers.set(newMarkers);
        } else {
          this.mapMarkers.set([]);
        }
        this.isThinking.set(false);
      },
      error: (err) => {
        console.error(err);
        this.isThinking.set(false);
        this.chatMessage.set("Sorry, I'm having trouble connecting to my brain right now.");
      }
    });
  }

  toggleCompare(id: string, event: Event) {
    event.stopPropagation();
    const current = new Set(this.selectedForCompare());
    if (current.has(id)) {
      current.delete(id);
    } else {
      if (current.size >= 4) {
        alert("You can only compare up to 4 restaurants at a time.");
        return;
      }
      current.add(id);
    }
    this.selectedForCompare.set(current);
  }

  get compareIdsQuery(): string {
    return Array.from(this.selectedForCompare()).join(',');
  }
}
