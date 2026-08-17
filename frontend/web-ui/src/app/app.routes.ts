import { Routes } from '@angular/router';
import { App } from './app';
import { RestaurantDetailComponent } from './restaurant-detail.component';

import { CompareComponent } from './compare.component';
import { IntelligentDiscoveryComponent } from './intelligent-discovery.component';
import { AiCuratedResultsComponent } from './ai-curated-results.component';
export const routes: Routes = [
    { path: '', component: IntelligentDiscoveryComponent },
    { path: 'results', component: AiCuratedResultsComponent },
    { path: 'restaurant/:id', component: RestaurantDetailComponent },
    { path: 'compare', component: CompareComponent }
];
