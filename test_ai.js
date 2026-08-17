const http = require('http');

async function test() {
    try {
        const tokenRes = await fetch('http://localhost:9090/realms/zomato-realm/protocol/openid-connect/token', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({
                'client_id': 'zomato-frontend',
                'username': 'test1',
                'password': 'test1',
                'grant_type': 'password'
            })
        });
        const tokenData = await tokenRes.json();
        
        console.log("--- TEST 1: Normal Search ---");
        const res1 = await fetch('http://localhost:8086/api/ai/chat', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${tokenData.access_token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: 'test1',
                query: 'Recommend a good Italian restaurant in San Francisco',
                location: { latitude: 37.7749, longitude: -122.4194 }
            })
        });
        console.log('Status:', res1.status);
        console.log('Data:', await res1.text());
        
        console.log("\n--- TEST 2: Occasion Pill (Date Night) ---");
        const res2 = await fetch('http://localhost:8086/api/ai/chat', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${tokenData.access_token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: 'test1',
                query: 'Date Night',
                location: { latitude: 37.7749, longitude: -122.4194 }
            })
        });
        console.log('Status:', res2.status);
        console.log('Data:', await res2.text());

        console.log("\n--- TEST 3: Zero Matches Fallback ---");
        const res3 = await fetch('http://localhost:8086/api/ai/chat', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${tokenData.access_token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: 'test1',
                query: 'A restaurant on Mars',
                location: { latitude: 37.7749, longitude: -122.4194 }
            })
        });
        console.log('Status:', res3.status);
        console.log('Data:', await res3.text());

    } catch (e) {
        console.error('Error:', e);
    }
}
test();
