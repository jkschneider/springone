# Membership

To create a user:

`curl -XPOST http://localhost:8080/api/member -H 'Content-Type: application/json' -d '{"user":"jschneider"}'`

To login:

`curl http://localhost:8080/api/member/jschneider`


/api/edge/recommendation/{jschneider} => [{"movie": "bambi"},{"movie": "lion king"}]
/api/edge/recommendation/{twicksell} =>  [{"movie": "shawshank"},{"movie": "spring"}]

/api/member/{jschneider} => {"customerId": "jschneider", "age": "10"}
/api/member/{jschneider} => {"customerId": "jschneider", "age": "30"}

# Spring Boot Metric Writer

Wrap the registry and intercept to add common metrics so we can swap out metrics backends

https://github.com/spinnaker/kork/blob/master/kork-core/src/main/java/com/netflix/spinnaker/kork/metrics/SpectatorConfiguration.java


meter -> counter
histogram -> distribution summary
timer -> timer
everything else -> gauge