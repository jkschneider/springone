# Simple Spring Boot App

* How does this stack up to a Go microservice?  (Spring is what is making Java a competitor)
* With hard-coded service links, multiple instances of a service need to run behind a load balancer (e.g. ELB) and
the load balancer needs to have a route registered with it.

## RESULT
* Recommendations contains a hard-coded link to a DNS name for membership
* Neither service registers with service discovery
* No metrics

---

# Why does the audience care?

* Make a Spring Boot app *cloud-ready* even if you are not yet *cloud-native*
* Spring Cloud is a misnomer.  Everything we will show you can be stood up in a private DC.
* TODO get McGarr's slide on Cloud as a **utility**
* Spring Cloud Netflix makes it easy to start up relatively complex pieces with little configuration

---

# Add Eureka

* Start Eureka with eureka module main method
* Add `@EnableEurekaClient`
* Start Membership and Recommendations
* Execute REST request to drop the Membership instance from discovery
* [Eureka REST Operations](https://github.com/Netflix/eureka/wiki/Eureka-REST-operations)

## RESULT
* Both services are registered in Eureka
* Red/Black push will drop an instance from discovery

---

# Respond to Application Event when No Longer In Discovery

* Demo executing REST request to drop an instance from discovery
* [Eureka REST Operations](https://github.com/Netflix/eureka/wiki/Eureka-REST-operations)

## RESULT
* Stop listening to queues, etc.

---

# Ribbon RestTemplate

* Replace Hard-coded Link with VIP addresses
* No longer tied to load balancer.  No need to register route with load balancer.
* Smarter load distribution?  TODO how?
* Discuss benefit of stacks -- segmenting traffic by responsibility

---

# What about Zuul

* Our premise has been that client-side load balancing is an architectural alternative to the typical load-balancer-per-server model.  Ribbon+Eureka folds load balancing into the service call itself.
* So why Zuul, a router distinct from the operation of any particular service?

---

# Spectator Metrics

* Tagged vs. Hierarchical structure.  Canonical example: how to get latency of all HTTP 200s in a hierarchical structure
consisting of `{uri}.200`.  Would have to first itemize all such `{uri}`.
* Add `@EnableSpectator`
* Show /metrics endpoint after executing a series of REST requests.

* What should not be a tag?  Example: customerId because of the combinatorial explosion of tags.
* Add a bad tag and see what happens in /metrics

* Walkthrough of underlying code

---

# Atlas

* Start atlas with ./startAtlas.sh
* Add `@EnableAtlas`
* Restart Membership
* Execute JMeter script
* List tags
* Retrieve a single png
* Demonstrate really basic stack language
* Demonstrate dashboard generation
* Requirements for standing up a production-ready Atlas

---

# Spectator Graphite via servo-graphite

* What does it do to tags?

---

# Hystrix

* Add `@EnableHystrix`
* Add a HystrixCommand to allow recommendations to fall back when the Membership service is down
* Add `@EnableHystrixDashboard`

---

# Turbine

* Start Turbine
* Remove `@EnableHystrixDashboard`
* Wire hystrix commands to Turbine
* Demo centralized circuit breaker in Turbine
