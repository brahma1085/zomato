import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AiService } from './ai.service';

@Component({
  selector: 'app-intelligent-discovery',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './intelligent-discovery.component.html'
})
export class IntelligentDiscoveryComponent {
  currentInput = signal('');
  isListening = signal(false);

  constructor(private aiService: AiService, private router: Router) {}

  sendMessage(event?: Event) {
    if (event) event.preventDefault();
    if (!this.currentInput().trim()) return;
    // Navigate to curated results with the search query
    this.router.navigate(['/results'], { queryParams: { q: this.currentInput() } });
  }

  sendPill(text: string) {
    this.currentInput.set(text);
    this.sendMessage();
  }

  startVoiceRecognition() {
    if (this.isListening()) return;

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
}
