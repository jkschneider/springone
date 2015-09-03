# Membership

To create a user:

`curl -XPOST http://localhost:8080/api/member -H 'Content-Type: application/json' -d '{"user":"jschneider"}'`

To login:

`curl http://localhost:8080/api/member/jschneider`


/api/edge/recommendation/{jschneider} => [{"movie": "bambi"},{"movie": "lion king"}]
/api/edge/recommendation/{twicksell} =>  [{"movie": "shawshank"},{"movie": "spring"}]

/api/member/{jschneider} => {"customerId": "jschneider", "age": "10"}
/api/member/{jschneider} => {"customerId": "jschneider", "age": "30"}