import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatRequest {
  query: string;
  userId?: string;
  history?: string[];
  lat?: number;
  lng?: number;
}

export interface ChatResponse {
  message: string;
  restaurants: any[];
}

@Injectable({
  providedIn: 'root'
})
export class AiService {
  private apiUrl = '/api/ai/chat';

  constructor(private http: HttpClient) {}

  sendMessage(query: string, userId: string = 'test-user', history: string[] = [], lat?: number, lng?: number): Observable<ChatResponse> {
    const request: ChatRequest = { query, userId, history, lat, lng };
    return this.http.post<ChatResponse>(this.apiUrl, request);
  }
}
