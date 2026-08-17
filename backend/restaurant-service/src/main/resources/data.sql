INSERT INTO restaurants (id, name, description, address, city, latitude, longitude, average_rating, total_reviews, cost_for_two, has_parking, is_family_friendly, offers_delivery, image_url) VALUES
(1, 'Bella Trattoria', 'Authentic Italian cuisine with homemade pasta', '123 Columbus Ave', 'San Francisco', 37.7981, -122.4072, 4.7, 850, 80, true, true, true, 'https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=800'),
(2, 'El Farolito', 'Famous for giant burritos and late-night Mexican eats', '2779 Mission St', 'San Francisco', 37.7527, -122.4184, 4.5, 3200, 25, false, true, true, 'https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=800'),
(3, 'Gary Danko', 'Fine dining featuring French-inspired American cuisine', '800 North Point St', 'San Francisco', 37.8058, -122.4206, 4.8, 1500, 250, true, false, false, 'https://images.unsplash.com/photo-1544025162-81792bc49109?w=800'),
(4, 'House of Nanking', 'Bustling spot for Chinese comfort food', '919 Kearny St', 'San Francisco', 37.7965, -122.4052, 4.2, 1100, 45, false, true, true, 'https://images.unsplash.com/photo-1525648199074-cee30ba79a4a?w=800'),
(5, 'Tartine Bakery', 'Renowned bakery serving pastries, bread, and cafe fare', '600 Guerrero St', 'San Francisco', 37.7614, -122.4241, 4.6, 2800, 30, false, true, true, 'https://images.unsplash.com/photo-1509315703195-529879416c7e?w=800'),
(6, 'Tony''s Pizza Napoletana', 'World-famous authentic Neapolitan pizza and Italian dishes', '1570 Stockton St', 'San Francisco', 37.8003, -122.4090, 4.8, 4500, 50, false, true, true, 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800'),
(7, 'Joe''s Pizza', 'Classic New York style thin crust pizza slices', '7 Carmine St', 'New York', 40.7306, -74.0021, 4.7, 3900, 20, false, true, true, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800');

INSERT INTO restaurant_cuisines (restaurant_id, cuisine) VALUES
(1, 'Italian'),
(2, 'Mexican'), (2, 'Tacos'),
(3, 'American'), (3, 'French'),
(4, 'Chinese'),
(5, 'Bakery'), (5, 'Cafe'),
(6, 'Pizza'), (6, 'Italian'),
(7, 'Pizza'), (7, 'American');

INSERT INTO restaurant_ambience (restaurant_id, ambience) VALUES
(1, 'Romantic'), (1, 'Cozy'),
(2, 'Casual'), (2, 'Lively'),
(3, 'Romantic'), (3, 'Upscale'),
(4, 'Casual'),
(5, 'Casual'), (5, 'Cozy'),
(6, 'Casual'), (6, 'Lively'),
(7, 'Casual');

