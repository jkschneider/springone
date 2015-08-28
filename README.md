# Membership

To create a user:

`curl -XPOST http://localhost:8080/api/member -H 'Content-Type: application/json' -d '{"user":"jschneider"}'`

To login:

`curl http://localhost:8080/api/member/jschneider`