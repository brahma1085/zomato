import requests
import json
import sys
import time

def get_token():
    print("\n--- Getting Keycloak Token ---")
    token_data = {
        "client_id": "zomato-frontend",
        "username": "test1",
        "password": "test1",
        "grant_type": "password"
    }
    try:
        r = requests.post("http://localhost:9090/realms/zomato-realm/protocol/openid-connect/token", data=token_data, timeout=10)
        r.raise_for_status()
        token = r.json()["access_token"]
        print("Token obtained successfully.")
        return token
    except Exception as e:
        print(f"Error getting token: {e}")
        sys.exit(1)

def run_test(name, token, payload, expected_status=200):
    print(f"\n--- Running Test: {name} ---")
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    try:
        start_time = time.time()
        r = requests.post("http://localhost:8080/api/ai/chat", json=payload, headers=headers, timeout=60)
        elapsed = time.time() - start_time
        
        print(f"Status Code: {r.status_code}")
        print(f"Response Time: {elapsed:.2f} seconds")
        
        if r.status_code != expected_status:
            print(f"FAILED: Expected {expected_status}, got {r.status_code}")
            print(f"Response: {r.text}")
            return False
            
        try:
            data = r.json()
            explanation = data.get("message", "")
            recommendations = data.get("restaurants", [])
            print(f"Explanation: {explanation[:100]}...")
            print(f"Recommendations count: {len(recommendations)}")
            if len(recommendations) > 0:
                print(f"Top recommendation: {recommendations[0].get('restaurantName')} - {recommendations[0].get('cuisine')} (Lat: {recommendations[0].get('lat')}, Lng: {recommendations[0].get('lng')})")
            print("PASSED")
            return True
        except ValueError:
            print(f"FAILED: Invalid JSON response")
            print(f"Response: {r.text}")
            return False
            
    except Exception as e:
        print(f"Error during test: {e}")
        return False

if __name__ == "__main__":
    token = get_token()
    
    tests = [
        {
            "name": "General Recommendation (Italian in SF)",
            "payload": {
                "userId": "test1",
                "query": "Recommend a good Italian restaurant in San Francisco",
                "location": {"latitude": 37.7749, "longitude": -122.4194}
            },
            "expected_status": 200
        },
        {
            "name": "Occasion-based (Date Night)",
            "payload": {
                "userId": "test1",
                "query": "Looking for a date night spot",
                "location": {"latitude": 37.7749, "longitude": -122.4194}
            },
            "expected_status": 200
        },
        {
            "name": "Dish/Craving specific",
            "payload": {
                "userId": "test1",
                "query": "I'm craving some spicy tacos",
                "location": {"latitude": 37.7749, "longitude": -122.4194}
            },
            "expected_status": 200
        },
        {
            "name": "Budget Constraints",
            "payload": {
                "userId": "test1",
                "query": "Cheap eats around here",
                "location": {"latitude": 37.7749, "longitude": -122.4194}
            },
            "expected_status": 200
        },
        {
            "name": "Zero Matches Fallback",
            "payload": {
                "userId": "test1",
                "query": "A restaurant on Mars",
                "location": {"latitude": 37.7749, "longitude": -122.4194}
            },
            "expected_status": 200
        },
        {
            "name": "Missing Location",
            "payload": {
                "userId": "test1",
                "query": "Find me a cafe"
            },
            "expected_status": 200
        },
        {
            "name": "Empty Query",
            "payload": {
                "userId": "test1",
                "query": "   ",
                "location": {"latitude": 37.7749, "longitude": -122.4194}
            },
            "expected_status": 200
        }
    ]
    
    results = []
    for test in tests:
        res = run_test(test["name"], token, test["payload"], test.get("expected_status", 200))
        results.append(res)
        
    print(f"\n=== TEST SUMMARY ===")
    print(f"Passed: {results.count(True)} / {len(tests)}")
    if all(results):
        print("ALL TESTS PASSED!")
    else:
        print("SOME TESTS FAILED.")
        sys.exit(1)
