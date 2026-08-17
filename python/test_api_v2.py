import requests
import json
import sys

print("Getting token...")
token_data = {
    "client_id": "zomato-frontend",
    "username": "test1",
    "password": "test1",
    "grant_type": "password"
}
try:
    r_token = requests.post("http://localhost:9090/realms/zomato-realm/protocol/openid-connect/token", data=token_data, timeout=10)
    r_token.raise_for_status()
    token = r_token.json()["access_token"]
    print("Token obtained.")
except Exception as e:
    print(f"Error getting token: {e}")
    sys.exit(1)

print("Calling ai-service...")
chat_data = {
    "userId": "test1",
    "query": "Recommend a good Italian restaurant in San Francisco",
    "location": {
        "latitude": 37.7749,
        "longitude": -122.4194
    }
}
headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json"
}
try:
    r_chat = requests.post("http://localhost:8080/api/ai/chat", json=chat_data, headers=headers, timeout=60)
    print(f"Status: {r_chat.status_code}")
    print(f"Response: {r_chat.text}")
except Exception as e:
    print(f"Error calling ai-service: {e}")
