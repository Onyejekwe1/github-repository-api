# GitHub Popular Repositories Service

This project is a Spring Boot application that allows users to discover popular repositories on GitHub. It provides functionality to list the most starred repositories, with options to filter by date and programming language.

### Features
* Retrieve a list of the most popular repositories on GitHub, sorted by the number of stars.
* Filter repositories by the date they were created and the programming language they use.
* Cache responses to enhance performance and reduce the load on the GitHub API.
* Implement resilience patterns like circuit breakers to handle potential API downtimes.

## Prerequisites

Before you begin, ensure you have met the following requirements:

* Java JDK 17 or later is installed on your machine.
* Maven is installed for managing project dependencies and running the application.
* Docker is installed.

# Installation and Running the Application
To install and run the GitHub Popular Repositories Service, follow these steps:

* Clone the repository:
``` shell
git clone https://github.com/Onyejekwe1/github-repository-api.git
```
* Navigate to the project directory:
``` shell
cd github-repository-api
```

* Build the project using Maven:
``` shell
mvn clean install
```

* Run docker to set up the redis instance:
```shell
docker-compose up --build
```

* Run the application:

```shell
mvn spring-boot:run
```
## Usage
Once the application is running, you can access the following endpoints:

* GET /api/github/repositories: Fetch the top repositories. Optional query parameters:
   * `count`: The number of repositories to retrieve (e.g., 10, 50, 100).
   * `language`: The programming language of the repositories (e.g., Java, Python).
   * `since`: The date from which to find repositories (e.g., 2021-01-01).

Example request using curl:
```shell
curl -X GET 'http://localhost:8080/api/github/repositories?count=10&language=Java&since=2021-01-01'
```

## Testing
To run the tests, execute the following command:
```shell
mvn test
```