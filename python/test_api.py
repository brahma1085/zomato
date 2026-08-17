import requests
import sys

token_url = "http://localhost:9090/realms/zomato-realm/protocol/openid-connect/token"
token_data = {
    "client_id": "zomato-frontend",
    "username": "test1",
    "password": "test1",
    "grant_type": "password"
}

print("Fetching token...")
token_res = requests.post(token_url, data=token_data)
if token_res.status_code != 200:
    print(f"Failed to get token: {token_res.status_code} {token_res.text}")
    sys.exit(1)

token = token_res.json()["access_token"]
print("Got token, testing API...")

chat_url = "http://localhost:8080/api/ai/chat"
headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json"
}
chat_data = {
    "query": "pizza"
}

chat_res = requests.post(chat_url, headers=headers, json=chat_data)
print(f"Status: {chat_res.status_code}")
print(f"Body: {chat_res.text}")
