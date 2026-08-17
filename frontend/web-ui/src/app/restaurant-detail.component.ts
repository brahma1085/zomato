import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-restaurant-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './restaurant-detail.component.html'
})
export class RestaurantDetailComponent implements OnInit {
  restaurantId = '';
  matchPercentage = 94;
  
  restaurant: any = null;
  reviews: any[] = [];
  loading = true;

  constructor(private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit() {
    const state = window.history.state;
    if (state && state.restaurant) {
      this.restaurant = state.restaurant;
      this.restaurantId = this.restaurant.id || this.restaurant.restaurantId || '1';
      this.loading = false;
      this.fetchReviews();
    } else {
      this.route.paramMap.subscribe(params => {
        this.restaurantId = params.get('id') || '1';
        this.fetchRestaurantData();
      });
    }
  }

  fetchRestaurantData() {
    const token = localStorage.getItem('token');
    let headers: any = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    // Fetch restaurant details
    this.http.get<any>(`/api/restaurants/${this.restaurantId}`, { headers }).subscribe({
      next: (data) => {
        this.restaurant = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to fetch restaurant', err);
        this.loading = false;
      }
    });

    this.fetchReviews();
  }

  fetchReviews() {
    const token = localStorage.getItem('token');
    let headers: any = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    // Fetch restaurant reviews
    this.http.get<any[]>(`/api/reviews/restaurant/${this.restaurantId}`, { headers }).subscribe({
      next: (data) => {
        this.reviews = data;
      },
      error: (err) => {
        console.error('Failed to fetch reviews', err);
      }
    });
  }
}
