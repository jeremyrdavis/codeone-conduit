curl --location --request POST 'localhost:8080/api/users-secure' \
--header 'Content-Type: text/plain' \
--data-raw '{
"username": "Jacob",
"email": "jake@jake.jake",
"password": "jakejake"
}
'