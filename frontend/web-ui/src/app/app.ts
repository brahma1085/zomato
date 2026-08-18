import { Component, signal, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AiService } from './ai.service';
import { LocationService } from './location.service';

import { MapComponent } from './map.component';

export interface ChatMessage {
  role: 'user' | 'ai';
  text: string;
}

import { OAuthService } from 'angular-oauth2-oidc';
import { authConfig } from './auth.config';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, FormsModule, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements AfterViewChecked {
  protected readonly title = signal('web-ui');
  
  mapMarkers = signal<{lat: number, lng: number, title: string}[]>([]);

  chatMessages = signal<ChatMessage[]>([
    {
      role: 'ai',
      text: "Hello! I'm your AI Restaurant Concierge. What kind of food are you in the mood for today?"
    }
  ]);
  
  recommendedRestaurants = signal<any[]>([]);
  
  currentInput = signal('');
  isThinking = signal(false);
  selectedForCompare = signal<Set<string>>(new Set());

  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  constructor(private aiService: AiService, private oauthService: OAuthService, public locationService: LocationService) {
    this.configureOAuth();
  }

  isAuthenticated = signal(false);

  private async configureOAuth() {
    console.info('App initialized, checking OAuth status...');
    this.oauthService.configure(authConfig);
    try {
      await this.oauthService.loadDiscoveryDocumentAndTryLogin();
      
      if (!this.oauthService.hasValidIdToken() || !this.oauthService.hasValidAccessToken()) {
        this.oauthService.initCodeFlow();
      } else {
        this.isAuthenticated.set(true);
      }
    } catch (e) {
      console.error('OAuth configuration failed', e);
    }
  }

  get currentLocation() {
    return this.locationService.currentLocation;
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      if (this.scrollContainer && this.isAuthenticated()) {
        this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
      }
    } catch(err) { }
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

  sendMessage(event?: Event) {
    if (event) event.preventDefault();
    if (!this.currentInput().trim()) return;

    const query = this.currentInput();
    
    // Extract history
    const history = this.chatMessages().map(msg => `${msg.role}: ${msg.text}`);
    
    this.chatMessages.update(msgs => [...msgs, { role: 'user', text: query }]);
    this.currentInput.set('');
    this.isThinking.set(true);

    console.info('Sending AI query:', query);
    const loc = this.currentLocation();
    this.aiService.sendMessage(query, 'user123', history, loc?.lat, loc?.lng).subscribe({
      next: (response) => {
        this.chatMessages.update(msgs => [...msgs, { role: 'ai', text: response.message }]);
        this.recommendedRestaurants.set(response.restaurants || []);
        
        if (response.restaurants && response.restaurants.length > 0) {
          const center = this.currentLocation() || { lat: 40.7128, lng: -74.0060 };
          const newMarkers = response.restaurants.map((r: any, idx: number) => ({
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
        console.error('AI Service Error:', err);
        this.chatMessages.update(msgs => [...msgs, { role: 'ai', text: "Sorry, I'm having trouble connecting to my brain right now." }]);
        this.isThinking.set(false);
      }
    });
  }

  sendPill(text: string) {
    this.currentInput.set(text);
    this.sendMessage();
  }

  isListening = signal(false);

  startVoiceRecognition() {
    if (this.isListening()) return; // Already listening

    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognition) {
      alert("Your browser does not support the Web Speech API.");
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.lang = 'en-US';
    recognition.interimResults = false;
    recognition.maxAlternatives = 1;

    recognition.onstart = () => {
      this.isListening.set(true);
      this.currentInput.set("Listening...");
    };

    recognition.onresult = (event: any) => {
      const transcript = event.results[0][0].transcript;
      this.currentInput.set(transcript);
      this.sendMessage();
    };

    recognition.onerror = (event: any) => {
      console.error('Speech recognition error', event.error);
      this.isListening.set(false);
      if (this.currentInput() === "Listening...") {
        this.currentInput.set("");
      }
    };

    recognition.onend = () => {
      this.isListening.set(false);
      if (this.currentInput() === "Listening...") {
        this.currentInput.set("");
      }
    };

    recognition.start();
  }

  markNotInterested(id: string, event: Event) {
    event.preventDefault();
    event.stopPropagation();
    
    // Simple mock logic for demonstration
    this.chatMessages.update(msgs => [...msgs, { 
      role: 'ai', 
      text: `Got it! I've noted that you're not interested in restaurant ${id} and will update your preferences.` 
    }]);
  }

  logout() {
    this.oauthService.logOut();
    this.isAuthenticated.set(false);
  }
}
