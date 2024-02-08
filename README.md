# CashHub
A "banking" portal web application written in Java.
---

## Features
- Web Interface
- NoSQL Document Database (CSV files)
- Third-party JavaScript Extension API (or as haters will call it XSS)
- Custom Session Management (cookies)
- Bespoke Build Pipeline (Makefile)
- All written in plain Java!

## How to run
### Domain
As the project uses cookies, it is advised to create a dummy domain in your `/etc/hosts` file e.g.
```
127.0.0.1	cashhub.com
```

### Building
To build you need `make` `javac` and `jar` if you want to package the app into a .jar file.
Run `make` or `make all` to compile the project or `make run` to compile and start the app.
There's also a `make jar` target to compile and package the app into a .jar archive.

### First run
On the first start of the application you will see errors notifying you of the absence of `users.csv` and `transactions.csv`.
These will be created once the first user is registered and the first transaction is performed respectively.

### Docker
The repository contains a Dockerfile that will compile the project and create an image with the .jar file.
To use it simply run
```sh
docker build -t [YOUR_TAG] .
docker run --name [CONTAINER_NAME] -p 8080:8080 [YOUR_TAG]
```

## Usage
The app listens for HTTP requests on port 8080. There is no HTTPS support.

### Registration
As of now there isn't a web page for registering a user. To register a POST request needs to be sent to
`/user/register` containing the required fields: `firstname`, `lastname`, `email`, `password`.
An example `curl` command using the `cashhub.com` domain:
```sh
curl -v --data "firstname=John&lastname=Smith&email=email@example.com&password=password1234" http://cashhub.com:8080/user/register 
```

### Logging in
To log in, connect to the web portal e.g. `http://cashhub.com:8080` and click on the `Log in` button. Then fill out the form and click the `Log in` button.
In case wrong credentials were supplied you will be redirected back to the login form (no wrong password feedback at the moment).

### Depositing
The deposit function is more of a mock for depositing/withdrawing funds. Just type a decimal number and click deposit.
If a negative number is provided the value will be subtracted instead of added from the user's balance. With this it
is possible to overdraw the account.

### Transactions
To perform a transaction provide the UUID of the recipient (visible for each user under their name) and provide an amount (a positive decimal number) and click
on `Transfer`. In a case the transaction fails you will be redirected back to the dashboard (no failed transaction feedback at the moment).
The transaction may fail if: the recipient UUID was invalid, the transferred amount was negative or the transferred amount was greater than the user's balance.

Transaction history is visible below the user's balance.
