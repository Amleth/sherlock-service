TOKEN=$(curl -s -X POST http://localhost:5555/login -H 'Accept: application/json' -H 'Content-Type: application/json' -d '{"username":"sherlock","password":"password"}' | jq -r '.access_token')

curl -X GET "http://localhost:5555" -H "Authorization: Bearer ${TOKEN}"
echo ""
curl -X GET "http://localhost:5555/e13" -H "Authorization: Bearer ${TOKEN}"
echo ""
